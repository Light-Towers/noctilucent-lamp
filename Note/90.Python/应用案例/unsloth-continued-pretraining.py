import torch
import os
from datasets import load_dataset
from trl import SFTTrainer, SFTConfig
from transformers import TextIteratorStreamer
from threading import Thread
import textwrap
from unsloth import FastLanguageModel, is_bfloat16_supported, UnslothTrainer, UnslothTrainingArguments
from unsloth.chat_templates import get_chat_template

max_seq_length = 2048 # Choose any! We auto support RoPE Scaling internally!
dtype = None # None for auto detection. Float16 for Tesla T4, V100, Bfloat16 for Ampere+
load_in_4bit = True # Use 4bit quantization to reduce memory usage. Can be False.

# Can select any from the below:
# "unsloth/Qwen2.5-0.5B", "unsloth/Qwen2.5-1.5B", "unsloth/Qwen2.5-3B"
# "unsloth/Qwen2.5-14B",  "unsloth/Qwen2.5-32B",  "unsloth/Qwen2.5-72B",
# And also all Instruct versions and Math. Coding verisons!

model_name = "unsloth/Qwen2.5-0.5B"
def load_model():
    model, tokenizer = FastLanguageModel.from_pretrained(
        model_name=model_name,
        max_seq_length=max_seq_length,
        dtype=dtype,
        load_in_4bit=load_in_4bit
    )

    # 我们现在添加了LoRA适配器，所以我们只需要更新所有参数的1%到10%！
    model = FastLanguageModel.get_peft_model(
        model,
        r=16,   # Choose any number > 0 ! Suggested 8, 16, 32, 64, 128
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                        "gate_proj", "up_proj", "down_proj",
                        "embed_tokens", "lm_head",], # Add for continual pretraining
        lora_alpha=32,  # 16
        lora_dropout=0, # Supports any, but = 0 is optimized
        bias="none",    # Supports any, but = "none" is optimized
        # [NEW] "unsloth" uses 30% less VRAM, fits 2x larger batch sizes!
        use_gradient_checkpointing="unsloth", # True or "unsloth" for very long context
        random_state = 3407,
        use_rslora=False,   # We support rank stabilized LoRA
        loftq_config=None,  # And LoftQ
    )
    return model, tokenizer


# 数据准备
def prepare_dataset(tokenizer):
    # # 聊天模板
    # tokenizer = get_chat_template(
    #     tokenizer,
    #     chat_template = "chatml", # Supports zephyr, chatml, mistral, llama, alpaca, vicuna, vicuna_old, unsloth
    #     mapping = {"role" : "from", "content" : "value", "user" : "human", "assistant" : "gpt"}, # ShareGPT style
    #     map_eos_token = True, # Maps <|im_end|> to </s> instead
    # )

    tokenizer = get_chat_template(
        tokenizer,
        chat_template = "qwen-2.5",
    )

    EOS_TOKEN = tokenizer.eos_token
    # 格式化提示词
    def formatting_prompts_func(examples):
        texts = [example + EOS_TOKEN for example in examples["journal_name"]]
        return { "text" : texts, }
    
    # 加载数据集
    ## 本地数据集
    dataset = load_dataset("csv", data_files="/home/aistudio/datasets/zhanhui.csv", split = "train")
    
    dataset = dataset.map(formatting_prompts_func, batched = True,)
    
    
    # 打印前5个样本
    for row in dataset[:5]["text"]:
        print("=========================")
        print(row)
    
    return dataset

def train_model():
    trainer = UnslothTrainer(
        model = model,
        tokenizer = tokenizer,
        train_dataset = dataset,
        dataset_text_field = "text",
        max_seq_length = max_seq_length,
        dataset_num_proc = 8,

        args = UnslothTrainingArguments(
            per_device_train_batch_size = 2,
            gradient_accumulation_steps = 8,

            warmup_ratio = 0.1,
            num_train_epochs = 1,

            # Select a 2 to 10x smaller learning rate for the embedding matrices!
            learning_rate = 5e-5,
            embedding_learning_rate = 5e-6,

            fp16 = not is_bfloat16_supported(),
            bf16 = is_bfloat16_supported(),
            logging_steps = 1,
            optim = "adamw_8bit",
            weight_decay = 0.00,
            lr_scheduler_type = "cosine",
            seed = 3407,
            output_dir = "outputs",
            report_to = "none", # Use this for WandB etc

            # save_strategy = "steps",
            # save_steps = 50,
        ),
    )


    #@title Show current memory stats
    gpu_stats = torch.cuda.get_device_properties(0)
    start_gpu_memory = round(torch.cuda.max_memory_reserved() / 1024 / 1024 / 1024, 3)
    max_memory = round(gpu_stats.total_memory / 1024 / 1024 / 1024, 3)
    print(f"GPU = {gpu_stats.name}. Max memory = {max_memory} GB.")
    print(f"{start_gpu_memory} GB of memory reserved.")
    
    # 开始训练
    trainer_stats = trainer.train()
    # trainer_stats = trainer.train(resume_from_checkpoint = True)    # start from the latest checkpoint and continue training.
    
    #@title Show final memory and time stats
    used_memory = round(torch.cuda.max_memory_reserved() / 1024 / 1024 / 1024, 3)
    used_memory_for_lora = round(used_memory - start_gpu_memory, 3)
    used_percentage = round(used_memory         /max_memory*100, 3)
    lora_percentage = round(used_memory_for_lora/max_memory*100, 3)
    print(f"{trainer_stats.metrics['train_runtime']} seconds used for training.")
    print(f"{round(trainer_stats.metrics['train_runtime']/60, 2)} minutes used for training.")
    print(f"Peak reserved memory = {used_memory} GB.")
    print(f"Peak reserved memory for training = {used_memory_for_lora} GB.")
    print(f"Peak reserved memory % of max memory = {used_percentage} %.")
    print(f"Peak reserved memory for training % of max memory = {lora_percentage} %.")


def inference(tokenizer):
    # tokenizer = get_chat_template(
    #     tokenizer,
    #     chat_template = "qwen-2.5",
    # )
    FastLanguageModel.for_inference(model) # Enable native 2x faster inference


    # messages = [
    #     {"role": "user", "content": "你知道哪些展会信息？"},
    # ]

    # inputs = tokenizer.apply_chat_template(
    #     messages,
    #     tokenize = False,
    #     add_generation_prompt = True, # Must add for generation
    #     return_tensors = "pt",
    # ).to("cuda")
    
    # outputs = model.generate(input_ids = inputs, max_new_tokens = 64, use_cache = True,
    #                         temperature = 1.5, min_p = 0.1)


    messages = [ "你知道哪些展会信息？", ]
    inputs = tokenizer(messages, return_tensors = "pt",).to("cuda")
    outputs = model.generate(**inputs, max_new_tokens = 64, use_cache = True, temperature = 1.5, min_p = 0.1)
    
    print(tokenizer.batch_decode(outputs))



    # FastLanguageModel.for_inference(model)
    # text_streamer = TextIteratorStreamer(tokenizer)
    # max_print_width = 100

    # inputs = tokenizer(
    # [
    #     "Once upon a time, in a galaxy, far far away,"
    # ]*1, return_tensors = "pt").to("cuda")

    # generation_kwargs = dict(
    #     inputs,
    #     streamer = text_streamer,
    #     max_new_tokens = 256,
    #     use_cache = True,
    # )
    # thread = Thread(target = model.generate, kwargs = generation_kwargs)
    # thread.start()

    # length = 0
    # for j, new_text in enumerate(text_streamer):
    #     if j == 0:
    #         wrapped_text = textwrap.wrap(new_text, width = max_print_width)
    #         length = len(wrapped_text[-1])
    #         wrapped_text = "\n".join(wrapped_text)
    #         print(wrapped_text, end = "")
    #     else:
    #         length += len(new_text)
    #         if length >= max_print_width:
    #             length = 0
    #             print()
    #         print(new_text, end = "")
    #     pass
    # pass 


def save_and_load_model():
    model.save_pretrained("lora_model")
    tokenizer.save_pretrained("lora_model")


def convert_model():
    model, tokenizer = FastLanguageModel.from_pretrained(
        model_name="lora_model",
        max_seq_length=max_seq_length,
        dtype=dtype,
        load_in_4bit=load_in_4bit,
    )
    ## merged
    model.save_pretrained_merged("model/Qwen2.5-0.5B-16bit", tokenizer, save_method = "merged_16bit")
    ## gguf
    # model.save_pretrained_gguf("model/Qwen2.5-0.5B-q4_k_m", tokenizer, quantization_method = "q4_k_m")



if __name__ == "__main__":
    model, tokenizer = load_model()
    dataset = prepare_dataset(tokenizer)
    train_model()
    inference(tokenizer)
    save_and_load_model()
    convert_model()