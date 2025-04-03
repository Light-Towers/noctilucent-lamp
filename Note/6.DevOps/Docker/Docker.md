# 镜像
## 镜像是什么
镜像是一种轻量级、可执行的独立软件包，用来打包软件运行环境和基于运行环境开发的软件，它包含代码、运行时库、环境变量、配置文件等。
## Docker镜像加载原理
> UnionFS(联合文件系统)

文件系统结构：
1. 最底层：引导文件系统bootfs，容器启动后，容器本身转移到内存中，同时bootfs被卸载
2. 第二层：root文件系统rootfs，可以是一种或多种操作系统
3. Docker联合加载技术：一次同时加载多个文件系统，不过在宿主机只能看到一个文件系统
Docker镜像：联合加载将各层文件系统叠加一起后，形成的最终文件系统称为镜像

## restart 启动参数
Docker的restart参数用于指定自动重启Docker容器的策略，包含四个选项，分别是：

- no：默认值，表示容器退出时，Docker不自动重启容器。
- on-failure[:times]：若容器的退出状态非0，则Docker自动重启容器，还可以指定重启次数，若超过指定次数未能启动容器则放弃。
- always：容器退出时总是重启。
- unless-stopped：容器退出时总是重启，但不考虑Docker守护进程启动时就已经停止的容器。(**除非用户手动停止容器**)


## 修改容器的启动参数
```bash
docker container update --restart=always 容器名字
##修改时区 
-e TZ="Asia/Shanghai" 
```




# 安装/升级
## 停止 docker 
如果系统安装过docker
```bash
# 停止所有 docker 容器
docker stop $(docker ps -aq)
# 停止 docker 服务
sudo systemctl stop docker
```
备份docker文件（默认路径为/var/lib/docker）
```bash
sudo mv /var/lib/docker /var/lib/docker.bak
```

## 卸载 docker
```bash
sudo  yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```
                  
## 安装 yum-utils
```bash
sudo yum install -y yum-utils
```
## 添加 docker 源
```bash
# [官网源](https://download.docker.com/linux/centos/docker-ce.repo)国内无法访问，本次使用阿里源
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

## 安装 docker
在线安装 docker。（安装过程可能会遇到 GPG key 网络问题）
```bash
sudo yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
```
## 启动 docker 服务
```bash
# 启动 docker 服务
sudo systemctl start docker
# 设置 docker 开机自启
sudo systemctl enable docker
```

## 验证 docker
```bash
# 验证 docker 版本
docker version
# 查看 docker compose 版本
docker compose version
```