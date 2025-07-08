# linux环境上安装
## 官方下载
下载安装脚本，一键安装
```bash
## 一键安装、升级（网络不稳定、多试几次）
curl -fsSL https://ollama.com/install.sh | sh
## 或者下载安装包，手动安装
curl -L https://ollama.com/download/ollama-linux-amd64.tgz -o ollama-linux-amd64.tgz
sudo tar -C /usr -xzf ollama-linux-amd64.tgz
```
## modelscope下载
(国内)如果官方安装包下载太慢，可从modelscope下载
```bash
## 使用命令行前，请确保已经安装modelscope 安装ModelScope。
pip install modelscope
## 下载ollama至ollama-linux目录，指定版本 v0.6.2
modelscope download --model=modelscope/ollama-linux --local_dir ./ollama-linux --revision v0.6.2
```
ollama-linux目录结构如下：
```bash
# 查看下载的内容
tree -L 2
.
├── README.md
├── ollama-linux-amd64.tgz         # 二进制安装包
└── ollama-modelscope-install.sh   # 安装脚本

# 有root 权限，可 -C /usr 指定解压路径
tar -xzf ollama-linux-amd64.tgz    # 直接解压至当前目录

# 后台进程：启动ollama服务
nohup ./ollama-linux/bin/ollama serve > ./ollama-linux/logs/ollama.log 2>&1 &

# 前台进程：运行qwq:32b模型
./ollama-linux/bin/ollama run qwq:32b
```

## 指定模型目录
ollama 模型默认目录为 ~/.ollama/models
```bash
# 临时配置
export OLLAMA_MODELS=/home/aistudio/ollama-linux/models
# 永久配置：将 export 命令添加到 .bashrc 或 .zshrc 中
```









# ollama 配置

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
Environment="OLLAMA_HOST=0.0.0.0"		# 新增 对外暴露（默认只有当前机器可以访问）

[Install]
WantedBy=default.target
```

