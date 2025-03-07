# 安装依赖

```bash
# 安装最新版本的PaddleNLP
pip install --pre --upgrade paddlenlp==3.0.0b0.post20240820 -f https://www.paddlepaddle.org.cn/whl/paddlenlp.html
```

### 验证是否安装成功

```python
import paddlenlp
print(paddlenlp.__version__)
```



# 数据预处理

## 准备工作
### 1.原始数据转换 jsonl 格式
```bash
python /paddle/PaddleNLP/llm/tools/preprocess/trans_to_json.py  \
 --input_path /paddle/data/mingpian/  \
 --output_path /paddle/data/mingpian/mingpian_sample
```



### 2.数据ID化
```bash
python -u  /paddle/PaddleNLP/llm/tools/preprocess/create_pretraining_data.py \
    --model_name "Qwen/Qwen2-0.5B-Instruct" \
    --tokenizer_name "Qwen2Tokenizer" \
    --input_path "/paddle/data/mingpian/mingpian_sample.jsonl" \
    --output_prefix "/paddle/data/mingpian/mingpian_sample_pretrain_0.5B"  \
    --data_format "JSON" \
    --json_key "text" \
    --split_sentences \
    --data_impl "mmap" \
    --log_interval 5 \
    --workers 1
```




### 预训练配置
```bash
vim /home/aistudio/configs/pretrain.json
{
  "model_name_or_path": "Qwen/Qwen2-0.5B",
  "tokenizer_name_or_path": "Qwen/Qwen2-0.5B",
  "input_dir": "/home/aistudio/data-pretrain/data/mingpian",
  "output_dir": "/home/aistudio/checkpoints/pretrain_ckpts",
  "per_device_train_batch_size": 1,
  "gradient_accumulation_steps": 1,
  "per_device_eval_batch_size": 1,
  "tensor_parallel_degree": 1,
  "pipeline_parallel_degree": 1,
  "sharding_parallel_degree": 1,
  "sharding": "stage2",
  "virtual_pp_degree": 1,
  "sequence_parallel": 0,   
  "use_flash_attention": false,
  "use_fused_rms_norm": false,
  "use_fused_rope": false,
  "max_seq_length": 4096,
  "learning_rate": 3e-05,
  "min_learning_rate": 3e-06,
  "warmup_steps": 30,
  "logging_steps": 1,
  "max_steps": 50,
  "save_steps": 100,
  "eval_steps": 1000,
  "weight_decay": 0.01,
  "fp16": true,
  "fp16_opt_level": "O2",
  "warmup_ratio": 0.01,
  "max_grad_norm": 1.0,
  "dataloader_num_workers": 1,
  "continue_training": 1,
  "do_train": true,
  "do_eval": true,
  "do_predict": true,
  "disable_tqdm": true,
  "recompute": true,
  "distributed_dataloader": 1,
  "recompute_granularity": "full",
  "unified_checkpoint": true,
  "save_total_limit": 2
}
```




## 预训练
```bash
python -u -m paddle.distributed.launch --gpus "0" \
 /home/aistudio/PaddleNLP/llm/run_pretrain.py \
 /home/aistudio/configs/pretrain.json
```




# 微调/精调
## 准备精调数据

## 修改配置
```bash
vim /home/aistudio/configs/sft.json
{
  # "model_name_or_path": "Qwen/Qwen2-0.5B",
  "model_name_or_path": "/home/aistudio/checkpoints/pretrain_ckpts",
  "dataset_name_or_path": "/home/aistudio/data-pretrain/data/sft",
  "output_dir": "/home/aistudio/checkpoints/sft_ckpts",
  "per_device_train_batch_size": 1,
  "gradient_accumulation_steps": 1,
  "per_device_eval_batch_size": 1,
  "eval_accumulation_steps":1,
  "num_train_epochs": 3,
  "max_step": 20,
  "learning_rate": 3e-05,
  "warmup_steps": 30,
  "logging_steps": 1,
  "evaluation_strategy": "no",
  "save_strategy": "epoch",
  "src_length": 1024,
  "max_length": 2048,
  "fp16": true,
  "fp16_opt_level": "O2",
  "do_train": true,
  "do_eval": false,
  "disable_tqdm": true,
  "load_best_model_at_end": false,
  "eval_with_do_generation": false,
  "metric_for_best_model": "accuracy",
  "recompute": true,
  "save_total_limit": 1,
  "tensor_parallel_degree": 1,
  "pipeline_parallel_degree": 1,
  "pipeline_parallel_config": "disable_p2p_cache_shape",
  "sharding": "stage2",
  "lora": false,
  "zero_padding": false,
  "unified_checkpoint": true,
  "use_flash_attention": false
}
```



## 精调
```bash
python -u -m paddle.distributed.launch --gpus "0" \
 /home/aistudio/PaddleNLP/llm/run_finetune.py \
 /home/aistudio/configs/sft.json
```



## 高效参数精调 （LoRA）
```bash
python -u -m paddle.distributed.launch --gpus "0" \
 /home/aistudio/PaddleNLP/llm/run_finetune.py \
 /home/aistudio/configs/sft.json \
 --lora true \
 --output_dir /home/aistudio/checkpoints/lora_ckpts
```



# 模型合并
```bash
python /home/aistudio/PaddleNLP/llm/tools/merge_lora_params.py \
    --model_name_or_path /home/aistudio/checkpoints/sft_ckpts \
    --lora_path /home/aistudio/checkpoints/lora_ckpts \
    --output_path /home/aistudio/checkpoints/merge \
    --device "gpu" \
    --safe_serialization True
```









# 按照教程预先下载模型
```bash
# 导出模型 (可设置paddle.set_device("cpu")，通过内存导出模型)
python -m paddle.distributed.launch --gpus "0,1,2,3,4,5,6,7" predict/export_model.py --model_name_or_path meta-llama/Meta-Llama-3.1-405B-Instruct --output_path /path/to/a8w8c8_tp8 --inference_model 1 --block_attn 1 --dtype bfloat16 --quant_type a8w8 --cachekv_int8_type static --use_fake_parameter 1
```



# 推理
```bash
python -m paddle.distributed.launch --gpus "0,1,2,3,4,5,6,7" predict/predictor.py \
 --model_name_or_path /path/to/a8w8c8_tp8 \
 --mode static --inference_model 1 \
 --block_attn 1 --dtype bfloat16 \
 --quant_type a8w8 --cachekv_int8_type static
```



# 部署

```bash
cd PaddleNLP/llm

#导入当前llm目录
export PYTHONPATH=./:$PYTHONPATH

python -m paddle.distributed.launch --gpus "0" ./predict/flask_server.py \
    --model_name_or_path Qwen/Qwen2-0.5B \
    --port 8010 \
    --flask_port 8011 \
    --dtype "float16"
```







# Python Api

使用 PaddleNLP 调用 llm 大模型api代码

```python
from paddlenlp.transformers import AutoTokenizer, AutoModelForCausalLM
tokenizer = AutoTokenizer.from_pretrained("Qwen/Qwen2-0.5B")
model = AutoModelForCausalLM.from_pretrained("Qwen/Qwen2-0.5B", dtype="float16")
input_features = tokenizer("你好！请自我介绍一下。", return_tensors="pd")
outputs = model.generate(**input_features, max_length=128)
print(tokenizer.batch_decode(outputs[0]))
```









| 实验ID                        | 学习率    | mAP@0.5   |
| ----------------------------- | --------- | --------- |
| RT-DETR-H_30-0.00001-85.01    | 0.00001   | 85.01     |
| RT-DETR-H_30-0.0001-92.11     | 0.0001    | 92.11     |
| RT-DETR-H_30-0.0005-92.07     | 0.0005    | 92.07     |
| **RT-DETR-H_50-0.0001-92.94** | **0.001** | **92.94** |



| 实验ID                             | 学习率   | mAP@0.5   |
| ---------------------------------- | -------- | --------- |
| PicoDet-L_layout_50-0.0001-33.47   | 0.00001  | 33.47     |
| PicoDet-L_layout_50-0.08-76.75     | 0.08     | 76.75     |
| **PicoDet-L_layout_60-0.08-77.43** | **0.08** | **77.43** |













PP-OCRv4_server_det-30_0.0001_93.02

PP-OCRv4_mobile_det-30_0.001
