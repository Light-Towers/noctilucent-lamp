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

## 5. 模型下载与处理

### 5.1 模型下载方法
```bash
# 使用 HuggingFace CLI 下载 GGUF 格式
huggingface-cli download Qwen/QwQ-32B-GGUF \
  --include "qwq-32b-q5_k_m-00006-of-00006.gguf" \
  --local-dir . \
  --local-dir-use-symlinks False

# 使用 ModelScope 下载 AWQ 格式
pip install modelscope
modelscope download --model="qwen/QwQ-32B-AWQ"
```

### 5.2 环境变量设置
```bash
# HuggingFace 国内镜像代理
export HF_ENDPOINT=https://hf-mirror.com
# 设置模型缓存目录
export HF_HOME="/home/aistudio/model"
```

---

## 6. CLI 服务部署 (`vllm serve`)

vLLM v0.13.0+ 推荐使用 `vllm serve` 命令。

### 6.1 基础部署命令
```bash
# 通用部署命令
vllm serve /path/to/model \
  --served-model-name model-name \
  --max-model-len 16384 \
  --gpu-memory-utilization 0.8

# 针对特定硬件的优化配置
vllm serve /path/to/model \
  --served-model-name model-name \
  --max-model-len 16384 \
  --max-num-seqs 2 \
  --gpu-memory-utilization 0.8 \
  --tensor-parallel-size 2 \
  --enforce-eager \
  --swap-space=1
```

### 6.2 具体模型部署示例
```bash
# 部署 DeepSeek-Qwen2.5-7B 模型
vllm serve /home/aistudio/script/model/deepseek-Qwen2.5-7B/ \
  --served-model-name Qwen2.5:0.5B \
  --max-model-len 16384 \
  --max-num-seqs 2 \
  --gpu-memory-utilization 0.8 \
  --tensor-parallel-size 2 \
  --enforce-eager \
  --swap-space=1

# 部署 QwQ-32B-AWQ 模型
vllm serve .cache/modelscope/hub/models/qwen/QwQ-32B-AWQ \
  --served-model-name QwQ:32B \
  --max-model-len 4096

# 使用 vLLM 部署 GGUF 模型
vllm serve ./qwq-32b-q5_k_m.gguf \
  --served-model-name QwQ:32B \
  --max-model-len 6384 \
  --max-num-seqs 2 \
  --gpu-memory-utilization 0.8
```

> **注意**：GGUF 文件合并和 llama.cpp 部署属于 llama.cpp 工具链，与 vLLM 无关。如需合并分割的 GGUF 文件，请使用 `llama-gguf-split` 工具。

### 6.3 向量模型服务 (Embedding/Pooling)
针对 Jina-v4 等向量模型，必须指定 `pooling` 运行器。

```bash
# Jina-Embeddings-v4 (T4 GPU 已验证配置)
vllm serve jinaai/jina-embeddings-v4-vllm-retrieval \
  --served-model-name jina-v4 \
  --runner pooling \
  --convert embed \
  --dtype half \
  --max-model-len 8192 \
  --enforce-eager \
  --trust-remote-code \
  --gpu-memory-utilization 0.85

# 同样适用于 text-matching 版本
vllm serve jinaai/jina-embeddings-v4-vllm-text-matching \
  --served-model-name jina-embeddings-v4 \
  --runner pooling \
  --convert embed \
  --dtype half \
  --max-model-len 32768 \
  --enforce-eager \
  --trust-remote-code \
  --disable-custom-all-reduce \
  --gpu-memory-utilization 0.9 \
  --port 8000
```

### 6.4 Embedding/Reranking 模型参数详解
| CLI 参数 | 说明 | 默认值 |
| :--- | :--- | :--- |
| `--runner` | 模型运行模式 | `default` (生成式) / `pooling` (向量模型) |
| `--convert` | 输出格式转换 | `auto` / `embed` (向量) / `reward` (rerank 打分) |
| `--trust-remote-code` | 允许模型自定义代码 | `False` (Jina/Chinese 模型必开) |
| `--enforce-eager` | 强制 Eager 模式 | `False` (T4 等旧卡建议开启) |
| `--disable-custom-all-reduce` | 禁用自定义 AllReduce | `False` (多卡通信优化) |

**重要提示**：
- **`--runner pooling`**：Jina、BGE、E5 等向量模型必须指定此模式。
- **`--convert embed`**：使 API 输出符合 OpenAI `/v1/embeddings` 标准。
- **`--convert reward`**：将输出转换为打分（适用于 Rerank 任务，如 `/v1/score`）。
- **`--enforce-eager`**：关闭 CUDA Graph，可减少显存波动，适合 T4、V100 等旧 GPU。

---

## 7. API 交互指南

### 7.1 Curl 调用示例
```bash
# 使用 Completions 接口
curl http://localhost:8000/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "QwQ:32B",
    "prompt": "2019第十届上海国际冷冻冷藏食品博览会，这个展会简称是什么？",
    "max_tokens": 2048,
    "temperature": 0.6,
    "top_p": 0.95,
    "top_k": 40,
    "repetition_penalty": 1.1
  }'

# 使用 Chat Completions 接口
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "Qwen2.5:0.5B",
    "messages": [
      {"role": "system", "content": "You are Qwen, created by Alibaba Cloud. You are a helpful assistant."},
      {"role": "user", "content": "深圳举办的展会有哪些？"}
    ],
    "temperature": 0,
    "top_p": 0.8,
    "repetition_penalty": 1.05,
    "max_tokens": 512
  }'

# 流式调用示例
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen2.5",
    "messages": [{"role": "user", "content": "你好"}],
    "stream": true
  }'
```

### 7.2 基于 OpenAI SDK 调用
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

## 8. 核心参数详解 (Python `LLM()` / CLI `vllm serve`)

> **参考文档**：更多详细参数请查看 [vLLM 官方文档](https://docs.vllm.ai/en/latest/)

### 8.1 并行与性能参数
| Python 参数 | CLI 参数 | 说明 | 建议/备注 |
| :--- | :--- | :--- | :--- |
| `tensor_parallel_size` | `-tp` | **张量并行**数 (GPU 数量) | 将模型权重切分到多块 GPU 上，增加显存并加速推理。通常设为卡数。 |
| `pipeline_parallel_size` | `-pp` | **流水线并行**数 | 将模型层切分到不同 GPU 组。常用于超大规模模型（如 405B）跨节点部署。 |
| `gpu_memory_utilization` | `--gpu-memory-utilization` | 显存利用率 | 默认 0.9。留出空间给激活值。OOM 或多卡负载不均时调至 0.7-0.8。 |
| `max_model_len` | `--max-model-len` | 最大上下文长度 | 显存压力大时适当减小该值，会直接影响 KV Cache 占用。 |
| `enforce_eager` | `--enforce-eager` | 强制使用 Eager 模式 | 禁用 CUDA Graph。在 T4/旧架构或变长输入频繁导致显存波动时非常有效。 |
| `block_size` | `--block-size` | PagedAttention 块大小 | 默认 16。影响内存碎片化率。 |

### 8.2 模型加载参数
| Python 参数 | CLI 参数 | 说明 | 建议/备注 |
| :--- | :--- | :--- | :--- |
| `dtype` | `--dtype` | 权重精度 | `auto`, `half` (FP16), `bfloat16`, `float`。T4 必须强制设为 `half`。 |
| `quantization` | `--quantization` | 量化方式 | 支持 `awq`, `gptq`, `squeezellm`, `fp8` 等。 |
| `trust_remote_code` | `--trust-remote-code` | 信任远程代码 | 对于包含自定义算子或层的模型（如 Jina, ChatGLM）必设。 |
| `download_dir` | `--download-dir` | 模型下载目录 | 默认使用 `~/.cache/huggingface`。 |
| `swap_space` | `--swap-space` | CPU 交换空间 (GiB) | 每个 GPU 的 CPU 交换空间大小，默认为 4 GiB。 |

### 8.3 运行时性能参数
| Python 参数 | CLI 参数 | 说明 | 建议/备注 |
| :--- | :--- | :--- | :--- |
| `max_num_batched_tokens` | `--max-num-batched-tokens` | 批处理最大 token 数 | 影响并发吞吐，内存富余时可适当增加。 |
| `max_num_seqs` | `--max-num-seqs` | 最大并发序列数 | 控制同时处理的请求数，与显存、`max_model_len` 协同调优。 |
| `cpu_offload_gb` | `--cpu-offload-gb` | CPU 卸载显存 (GB) | 显存不足时，将部分 KV Cache 换出到 CPU 内存。 |
| `distributed_executor_backend` | `--distributed-executor-backend` | 分布式后端 | `ray`（默认）或 `mp`（多进程）。在单机多卡时 `mp` 更轻量。 |

### 8.4 代码示例：并行配置
```python
from vllm import LLM

# 2 GPU 张量并行 + 流水线并行（若模型层数足够多）
llm = LLM(
    model="Qwen/Qwen2.5-72B-Instruct",
    tensor_parallel_size=2,          # 将模型权重切分到 2 块 GPU 上
    pipeline_parallel_size=1,        # 启用流水线并行（通常 1 表示禁用，>1 才激活）
    gpu_memory_utilization=0.85,     # 控制显存占用比例
    max_model_len=8192,              # 限制最大上下文长度以控制 KV Cache 大小
    enforce_eager=True,              # 为稳定性关闭 CUDA Graph
    dtype="half",                    # FP16 精度，T4 兼容
    trust_remote_code=True,          # 允许模型自定义代码
    max_num_batched_tokens=4096,     # 批处理 token 上限
    max_num_seqs=128                 # 最大并发请求数
)
```

**并行模式说明**：
- **张量并行 (Tensor Parallelism)**：将单个权重矩阵拆分到多块 GPU 上。适合单机多卡，能同时提升吞吐和显存容量。
- **流水线并行 (Pipeline Parallelism)**：将模型层序列拆分到不同 GPU 组。适合超大模型跨节点部署，降低单卡显存压力。
- **数据并行 (Data Parallelism)**：vLLM 天然支持，通过多个 `LLM` 实例在不同节点上同时服务不同请求。

### 8.5 批处理优化参数详解

**批处理规模与吞吐量关系**：
```python
# 吞吐优化配置示例
llm = LLM(
    model="Qwen/Qwen2.5-7B-Instruct",
    max_num_batched_tokens=4096,    # 批处理 token 数上限
    max_num_seqs=128,               # 并发序列数上限
    max_model_len=8192              # 每条序列最大长度
)
```

| 批处理参数 | 影响 | 建议值 |
| :--- | :--- | :--- |
| `max_num_batched_tokens` | 单批最大 token 数 | `2048` ~ `8192`，取决于 GPU 显存 |
| `max_num_seqs` | 并发序列数 | `64` ~ `256`，与请求队列深度匹配 |
| `max_model_len` | 每条序列最大长度 | 应根据业务需求（如 4K、8K、16K）设置 |

**批处理调优策略**：
1. **吞吐优先**：提高 `max_num_batched_tokens`，增加并发 token 数。
2. **延迟敏感**：降低 `max_num_seqs`，减少排队等待时间。
3. **长文本场景**：增大 `max_model_len`，但要相应降低 `max_num_seqs`。
4. **混合调度**：vLLM 自动根据输入长度动态调度批处理。

---

## 9. 生产实战避坑与性能调优

### 9.1 T4 GPU 特殊配置
- **GCC 版本冲突**：CUDA 12.2 不支持 GCC 13。务必降级到 GCC 12 并显式设置 `CC=/usr/bin/gcc-12`, `CXX=/usr/bin/g++-12`。
- **FlashInfer 编译缓存**：清理损坏的编译缓存：`rm -rf ~/.cache/flashinfer/`。
- **精度强制指定**：T4 不支持 bfloat16，需强制 `--dtype half` (FP16)。
- **Eager 模式强制**：`--enforce-eager` 关闭 CUDA Graph，减少显存波动，稳定压倒性能。

### 9.2 向量模型任务不匹配
- **Jina/BGE/E5 等向量模型**：启动命令**必须**包含 `--runner pooling`。
- **转换器参数**：`--convert embed`（向量）或 `--convert reward`（打分）。
- **避免旧版参数**：v0.13.0+ 弃用 `--task`，改用 `--runner` + `--convert`。

### 9.3 显存溢出 (OOM) 解决策略
- **降低显存占用率**：`--gpu-memory-utilization 0.7` ~ `0.8`。
- **减小上下文长度**：`--max-model-len 4096`（或更低）。
- **启用 CPU 卸载**：`--cpu-offload-gb 4`（将部分 KV Cache 换出至 CPU）。
- **牺牲性能换稳定**：`--enforce-eager`。

### 9.4 Jina v4 最佳实践
- **前缀优化**：查询时添加 `"retrieval.query: "` 前缀可提升检索精度。
- **版本区别**：
  - `jina-embeddings-v4-vllm-retrieval`：通用向量模型。
  - `jina-embeddings-v4-vllm-text-matching`：文本匹配专用。
- **多 GPU 推荐**：T4（16GB）单卡最多支持 `--max-model-len 8192`，建议 2 卡张量并行以服务更长序列。

### 9.5 部署后监控与日志
- **启动日志观察**：首次启动会编译算子（2-5 分钟），成功后生成 `~/.cache/flashinfer/` 二进制缓存。
- **吞吐监控**：通过 `prometheus` 或 `vLLM` 内置监控接口观察 `requests_per_second`, `tokens_per_second`。
- **错误日志关键词**：
  - `CUDA out of memory` → 降低 `--gpu-memory-utilization`。
  - `unsupported GNU version` → 设置 `CC/CXX`。
  - `The model does not support Embeddings API` → 检查 `--runner pooling`。

---

## 10. 关键参数总结

| 参数 | 说明 |
|------|------|
| `--tensor-parallel-size` | 张量并行度（多卡分割） |
| `--max-model-len` | 模型最大上下文长度 |
| `--gpu-memory-utilization` | GPU 显存利用率阈值 |
| `--enforce-eager` | 禁用 CUDA Graph 优化 |
| `--swap-space` | CPU 交换空间大小 (GB) |
| `top_k` + `top_p` | 采样策略控制输出多样性 |
| `repetition_penalty` | 重复惩罚系数 (>1.0 抑制重复) |

---

## 总结

vLLM 凭借 **PagedAttention** 成为大模型推理的性能标杆。无论是简单的 Python 调用，还是复杂的分布式 API 服务部署，它都提供了极高的灵活性。

**关键速记**：
- **快**：比 HF 快 24 倍。
- **省**：96% 显存利用率。
- **全**：支持 HF/AWQ/GGUF，兼容 OpenAI 接口。
- **稳**：分布式、量化、Eager 模式多重调优保障。

**官方资源**：
- [vLLM 官方文档](https://docs.vllm.ai/en/latest/)
- [GitHub 仓库](https://github.com/vllm-project/vllm)
- [API 参考](https://docs.vllm.ai/en/latest/serving/openai_compatible_server.html)

> **注意**：Qwen2.5 架构在 Unsloth 中的支持可能不完善，建议优先使用 Hugging Face 原生方法进行微调。AWQ/GGUF 格式适用于资源受限环境，但会损失部分精度。
