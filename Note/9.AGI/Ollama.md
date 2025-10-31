# Ollama 使用指南

## 安装

### 官方下载

下载安装脚本，一键安装：

```bash
# 一键安装、升级（网络不稳定、多试几次）
curl -fsSL https://ollama.com/install.sh | sh

# 或者下载安装包，手动安装
curl -L https://ollama.com/download/ollama-linux-amd64.tgz -o ollama-linux-amd64.tgz
sudo tar -C /usr -xzf ollama-linux-amd64.tgz
```

### ModelScope 下载

(国内) 如果官方安装包下载太慢，可从 ModelScope 下载：

```bash
# 使用命令行前，请确保已经安装 ModelScope
pip install modelscope

# 下载 ollama 至 ollama-linux 目录，指定版本（如 v0.6.2）
modelscope download --model=modelscope/ollama-linux --local_dir ./ollama-linux --revision v0.6.2
```

ollama-linux 目录结构如下：

```bash
# 查看下载的内容
tree -L 2
.
├── README.md
├── ollama-linux-amd64.tgz         # 二进制安装包
└── ollama-modelscope-install.sh   # 安装脚本

# 有 root 权限，可 -C /usr 指定解压路径
tar -xzf ollama-linux-amd64.tgz    # 直接解压至当前目录

# 后台进程：启动 ollama 服务
nohup ./ollama-linux/bin/ollama serve > ./ollama-linux/logs/ollama.log 2>&1 &

# 前台进程：运行 qwq:32b 模型
./ollama-linux/bin/ollama run qwq:32b
```

## 命令使用

### 模型管理命令

```bash
# 拉取模型
ollama pull <model_name>

# 运行模型（如果未下载会自动下载）
ollama run <model_name>

# 列出已下载的模型
ollama list

# 查看模型信息
ollama show <model_name>

# 删除模型
ollama rm <model_name>

# 启动 Ollama 服务
ollama serve
```

### 模型交互

```bash
# 基本运行模型
ollama run llama3

# 运行模型并指定参数
ollama run llama3 --parameter num_predict=128

# 运行模型并传入提示
ollama run llama3 "你好，介绍一下人工智能"

# 多轮对话模式（在模型提示符下输入）
>>> 你好，介绍一下人工智能
>>> 能详细说说机器学习吗？
>>> 谢谢你的解答
>>> /bye  # 退出对话
```

### 模型创建和自定义

```bash
# 创建自定义模型
ollama create <new_model_name> -f <Modelfile_path>

# 推送模型到注册中心
ollama push <model_name>
```

## 高阶使用技巧

### 模型量化压缩

```bash
# 查看支持的量化格式
ollama show <model_name> --formats

# 创建量化模型（示例：q4_0格式）
ollama create quantized_model -f Modelfile --quantize q4_0
```

### API 交互

```bash
# 获取模型列表（API请求）
curl http://localhost:11434/api/tags

# 生成文本（POST请求示例）
curl http://localhost:11434/api/generate -d '{
  "model": "llama3",
  "prompt": "写一个Python脚本实现快速排序"
}'
```

### 多模型管理

```bash
# 并行下载多个模型
ollama pull llama3 & ollama pull mistral & wait

# 模型版本控制
ollama run llama3:70b
ollama run llama3:8b
```

### systemd 配置

编辑配置文件：`sudo vim /etc/systemd/system/ollama.service`

```bash
[Unit]
Description=Ollama Service
After=network-online.target

[Service]
ExecStart=/usr/local/bin/ollama serve
User=ollama
Group=ollama
Restart=always
RestartSec=3
Environment="PATH=/opt/miniconda3/bin:/opt/miniconda3/condabin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin"

[Install]
WantedBy=default.target
```

#### 服务优化

```bash
# 配置GPU加速（CUDA）
Environment="CUDA_VISIBLE_DEVICES=0,1"  # 指定GPU设备
Environment="OLLAMA_HOST=0.0.0.0"  # 对外暴露服务（默认只能本地访问）
Environment="OLLAMA_MAX_CONTEXT=4096"  # 最大上下文长度（默认2048）
Environment="OLLAMA_MODELS=/home/aistudio/ollama-linux/models"  # 自定义模型存储路径
```

临时生效
```bash
export OLLAMA_HOST=0.0.0.0
export Environment="OLLAMA_MODELS=/home/aistudio/ollama-linux/models"
```


#### 服务重启
修改完配置后，重新加载配置并重启服务：

```bash
sudo systemctl daemon-reload
sudo systemctl restart ollama
```

### 查看日志（实时）

使用 systemd 管理服务时，查看 Ollama 服务日志常用命令：

```bash
# 实时跟随日志输出（需要 sudo 或 root 权限）
sudo journalctl -u ollama.service -f

# 查看最近 200 行日志
sudo journalctl -u ollama.service -n 200

# 指定时间范围，例如查看自今天 08:00 以来的日志
sudo journalctl -u ollama.service --since "08:00"

# 按优先级过滤（err、warning、info 等）
sudo journalctl -u ollama.service -p err
```

说明：
- 使用 `-f` 实时跟随输出适合调试启动或模型加载问题。
- 若服务运行在非 systemd 环境（比如容器或直接用 nohup 启动），请查看相应的日志文件（例如 `./ollama-linux/logs/ollama.log`）。