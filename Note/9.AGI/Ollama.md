# linux环境上安装
下载安装脚本，一键安装
curl -fsSL https://ollama.com/install.sh | sh
或者下载安装包，手动安装
curl -L https://ollama.com/download/ollama-linux-amd64.tgz -o ollama-linux-amd64.tgz
sudo tar -C /usr -xzf ollama-linux-amd64.tgz

(国内)官方安装包下载太慢，可从modelscope下载
```bash
## 使用命令行前，请确保已经通过pip install modelscope 安装ModelScope。
modelscope download --model=modelscope/ollama-linux --local_dir ./ollama-linux --revision v0.5.13
```
