# Clash
## 下载
由于 Clash 作者删除了项目，所以还请使用下面的下载链接进行下载。解压后执行以下步骤以确保 clash 能够正常启动。
* 适用于 x86 / x64 架构的 Linux 系统
clash-linux-amd64-v1.18.0.gz
```bash
## 下载
#https://github.com/WindSpiritSR/clash/releases/download/v1.18.0/clash-linux-amd64-v1.18.0.gz
wget https://agentneo.tech/doc/installers/clash-linux-amd64-v1.18.0.gz

## 解压
gzip -dk clash-*-*-v1.18.0.gz # 解压压缩包
chmod +x clash-*-*-v1.18.0 # 赋与可执行权限
```

## 配置文件
主配置文件名为 config.yaml. 默认情况下, Clash会在 $HOME/.config/clash 目录读取配置文件. 如果该目录不存在, Clash会在该位置生成一个最小的配置文件.

如果您想将配置文件放在其他地方 (例如 /etc/clash) , 您可以使用命令行选项 -d 来指定配置目录:

```bash
clash -d . # current directory
clash -d /etc/clash
```

或者, 您可以使用选项 -f 来指定配置文件:

```bash
clash -f ./config.yaml
clash -f /etc/clash/config.yaml
```

## 配置环境变量
### 临时生效（当前终端会话）
```bash
# 直接在终端中执行
export http_proxy=http://127.0.0.1:7890
export https_proxy=http://127.0.0.1:7890
export all_proxy=socks5://127.0.0.1:7891  # 若使用 Socks5
```
### 永久生效（全局）
```bash
#--vim start-- 编辑用户配置文件（如 ~/.bashrc 或 ~/.zshrc）
vim ~/.bashrc
export http_proxy=http://127.0.0.1:7890
export https_proxy=http://127.0.0.1:7890
export all_proxy=socks5://127.0.0.1:7891  # 若使用 Socks5
#--vim end-- 保存并退出编辑

# 重新加载配置文件
source ~/.bashrc
```


## 参考
[什么是 Clash？](https://windspiritsr.github.io/clash/zh_CN/)