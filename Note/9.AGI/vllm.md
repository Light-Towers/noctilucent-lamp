# 轻松上手 vLLM：大语言模型推理加速全攻略

**vLLM** （Very Large Language Model）是一个专门为大语言模型（LLM）推理设计的高性能开源框架。它通过创新的 **PagedAttention** 技术大幅提升了推理速度和显存利用率。

### 核心优势

- **极高性能**：比 HuggingFace Transformers 快 24 倍，比 Text Generation Inference (TGI) 快 3.5 倍。
- **显存优化**：PagedAttention 技术实现 96% 以上的显存利用率。
- **无缝兼容**：原生支持 HuggingFace 模型，完美兼容 OpenAI API 接口。
- **易于部署**：支持多 GPU 分布式推理，大幅简化大模型生产环境部署。

---

## 1. 环境准备

### 1.1 基础安装
```bash
# 基础安装
pip install vllm

# 如果需要 GPU 支持（推荐）
pip install 'vllm[gpu]'

# 如果需要从源码安装最新开发版
# pip install git+https://github.com/vllm-project/vllm.git
```

### 1.2 环境变量与下载工具
```bash
# HuggingFace 国内镜像代理
export HF_ENDPOINT=https://hf-mirror.com

# 方法 A: 使用 HuggingFace CLI 下载
huggingface-cli download Qwen/Qwen2.5-7B-Instruct --local-dir ./model

# 方法 B: 使用 ModelScope 下载 (国内速度最快)
pip install modelscope
modelscope download --model="qwen/Qwen2.5-7B-Instruct" --local_dir ./model
```

### 1.3 编译器配置 (针对 T4/FlashInfer)
在 NVIDIA T4 等旧架构 GPU 上，若遇到 `FlashInfer` 编译错误：
```bash
# 1. 安装 GCC 12
sudo apt-get update && sudo apt-get install -y gcc-12 g++-12
# 2. 清理编译缓存
rm -rf ~/.cache/flashinfer/
# 3. 指定编译器
export CC=/usr/bin/gcc-12
export CXX=/usr/bin/g++-12
```

---

## 2. Python 快速上手

### 2.1 基础推理示例
```python
from vllm import LLM, SamplingParams

# 初始化模型
llm = LLM(model="facebook/opt-125m")

# 设置生成参数
sampling_params = SamplingParams(
    temperature=0.8,
    top_p=0.95,
    max_tokens=100
)

# 准备输入
prompts = ["人工智能的未来发展趋势是"]

outputs = llm.generate(prompts, sampling_params)

for output in outputs:
    print(f"输入: {output.prompt}")
    print(f"生成: {output.outputs[0].text}")
```

### 2.2 流式输出示例
```python
from vllm import LLM, SamplingParams

llm = LLM(model="facebook/opt-125m")
sampling_params = SamplingParams(temperature=0.8, max_tokens=150)

prompt = "请详细解释什么是自然语言处理技术"

# 注意：Python API 的流式输出通常需要配合 loop 或在 API Server 中通过 stream=True 使用
outputs = llm.generate([prompt], sampling_params)
for output in outputs:
    print(output.outputs[0].text)
```

---

## 3. Python 高级功能

### 3.1 多 GPU 分布式推理
```python
from vllm import LLM

# 配置多 GPU
llm = LLM(
    model="Qwen/Qwen2.5-7B-Instruct",
    tensor_parallel_size=2,      # 使用 2 个 GPU 进行张量并行
    gpu_memory_utilization=0.8   # 显存占用权重
)
```

### 3.2 自定义采样策略对比
```python
from vllm import LLM, SamplingParams

llm = LLM(model="facebook/opt-125m")

strategies = {
    "保守策略 (学术/事实)": SamplingParams(temperature=0.1, top_p=0.5, max_tokens=50),
    "平衡策略 (常规对话)": SamplingParams(temperature=0.7, top_p=0.9, max_tokens=100),
    "创意策略 (文学创作)": SamplingParams(temperature=1.2, top_p=0.95, max_tokens=150)
}

prompt = "写一首关于春天的诗"

for name, params in strategies.items():
    print(f"\n[{name}]:")
    outputs = llm.generate([prompt], params)
    print(outputs[0].outputs[0].text)
```

### 3.3 批量处理效率优化
```python
import time
from vllm import LLM, SamplingParams

llm = LLM(model="facebook/opt-125m")
prompts = [f"请解释第{i}个 AI 概念" for i in range(1, 21)]
sampling_params = SamplingParams(temperature=0.7, max_tokens=50)

start = time.time()
outputs = llm.generate(prompts, sampling_params)
duration = time.time() - start

print(f"处理 {len(prompts)} 个请求耗时: {duration:.2f}s")
print(f"平均吞吐量: {len(prompts)/duration:.2f} req/s")
```

---

## 4. 实际应用场景封装

### 4.1 智能客服 Bot 封装
```python
class ChatBot:
    def __init__(self, model_path):
        self.llm = LLM(model=model_path)
        self.params = SamplingParams(temperature=0.7, max_tokens=256, stop=["用户:", "助手:"])
    
    def ask(self, user_query):
        prompt = f"用户: {user_query}\n助手:"
        outputs = self.llm.generate([prompt], self.params)
        return outputs[0].outputs[0].text

# 使用示例
# bot = ChatBot("Qwen/Qwen2.5-7B-Instruct")
# print(bot.ask("你们的退换货政策是什么？"))
```

### 4.2 内容生成服务
```python
def generate_service(content_type, topic):
    llm = LLM(model="facebook/opt-125m")
    templates = {
        "文章": f"请围绕{topic}写一篇深度深度分析文章",
        "标题": f"请为关于{topic}的内容生成 5 个爆款标题"
    }
    params = SamplingParams(temperature=0.8, max_tokens=512)
    outputs = llm.generate([templates[content_type]], params)
    return outputs[0].outputs[0].text
```

---

## 5. CLI 服务部署 (`vllm serve`)

vLLM v0.13.0+ 推荐使用 `vllm serve` 命令。

### 5.1 启动 LLM 生成服务
```bash
vllm serve /path/to/model \
  --served-model-name qwen2.5 \
  --max-model-len 16384 \
  --gpu-memory-utilization 0.8
```

### 5.2 启动向量模型服务 (Embedding/Pooling)
针对 Jina-v4 等向量模型，必须指定 `pooling` 运行器。
```bash
vllm serve jinaai/jina-embeddings-v4-vllm-retrieval \
  --served-model-name jina-v4 \
  --runner pooling \
  --convert embed \
  --dtype half \
  --max-model-len 8192 \
  --enforce-eager \
  --trust-remote-code
```

---

## 6. API 交互指南

### 6.1 Curl 调用示例
```bash
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen2.5",
    "messages": [{"role": "user", "content": "你好"}],
    "stream": true
  }'
```

### 6.2 OpenAI SDK 调用
```python
from openai import OpenAI
client = OpenAI(api_key="EMPTY", base_url="http://localhost:8000/v1")

response = client.chat.completions.create(
    model="qwen2.5",
    messages=[{"role": "user", "content": "请解释什么是 AGI"}]
)
print(response.choices[0].message.content)
```

---

## 7. 核心参数详解

| 参数 | 说明 | 建议/备注 |
| :--- | :--- | :--- |
| `--tensor-parallel-size` | GPU 数量 | 多卡部署必设 |
| `--gpu-memory-utilization` | 显存利用率 | 默认 0.9，OOM 时调低至 0.7-0.8 |
| `--max-model-len` | 最大上下文长度 | 显存压力大时适当减小 |
| `--enforce-eager` | 强制使用 Eager 模式 | T4 GPU 或显存吃紧时，禁用 CUDA Graph 节省显存 |
| `--dtype` | 权重精度 | T4 不支持 bfloat16，建议强设为 `half` |
| `--quantization` | 量化方式 | 支持 awq, gptq, squeezellm 等 |

---

## 8. 生产实战避坑 (T4 GPU / Jina 案例)

1.  **GCC 版本冲突**：CUDA 12.2 不支持 GCC 13。务必降级到 GCC 12 并显式设置 `CC/CXX`。
2.  **向量模型任务不匹配**：部署 Embedding 模型若不加 `--runner pooling`，会报任务类型错误。
3.  **显存溢出 (OOM)**：
    *   降低 `--gpu-memory-utilization`。
    *   使用 `--enforce-eager` 牺牲微量性能换取显存稳定性。
4.  **Jina v4 精度优化**：在搜索场景下，`input` 建议携带 `retrieval.query: ` 前缀。

---

## 总结

vLLM 凭借 **PagedAttention** 成为大模型推理的性能标杆。无论是简单的 Python 调用，还是复杂的分布式 API 服务部署，它都提供了极高的灵活性。

**关键速记**：
- **快**：比 HF 快 24 倍。
- **省**：96% 显存利用率。
- **全**：支持 HF/AWQ/GGUF，兼容 OpenAI 接口.
- **稳**：分布式、量化、Eager 模式多重调优保障。
