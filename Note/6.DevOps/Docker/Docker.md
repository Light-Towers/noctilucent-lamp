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

# 代理
## 1.为dockerd设置网络代理

“docker pull” 命令是由 dockerd 守护进程执行。而 dockerd 守护进程是由 systemd 管理。因此，如果需要在执行 “docker pull” 命令时使用 HTTP/HTTPS 代理，需要通过 systemd 配置。

- 为 dockerd 创建配置文件夹。

  ```bash
  sudo mkdir -p /etc/systemd/system/docker.service.d
  ```

- 为 dockerd 创建 HTTP/HTTPS 网络代理的配置文件，文件路径是 /etc/systemd/system/docker.service.d/http-proxy.conf 。并在该文件中添加相关环境变量。

  ```bash
  [Service]
  Environment="HTTP_PROXY=http://proxy.example.com:7890/"
  Environment="HTTPS_PROXY=http://proxy.example.com:7890/"
  Environment="NO_PROXY=localhost,127.0.0.1,.example.com"
  ```

- 刷新配置并重启 docker 服务。

  ```bash
  sudo systemctl daemon-reload
  sudo systemctl restart docker
  ```
  
## 2.为 docker 容器设置网络代理

  在容器运行阶段，如果需要使用 HTTP/HTTPS 代理，可以通过更改 docker 客户端配置，或者指定环境变量的方式。

  - 更改 docker 客户端配置：创建或更改 ~/.docker/config.json，并在该文件中添加相关配置。
  
    ```yaml
    {
     "proxies":
     {
       "default":
       {
         "httpProxy": "http://proxy.example.com:7890/",
         "httpsProxy": "http://proxy.example.com:7890/",
         "noProxy": "localhost,127.0.0.1,.example.com"
       }
     }
    }
    ```
  
  - 指定环境变量：运行 “docker run” 命令时，指定相关环境变量。
  
    ```bash
    docker run --env HTTP_PROXY="http://proxy.example.com:8080/ --env HTTPS_PROXY="http://proxy.example.com:7890/" --env NO_PROXY="localhost,127.0.0.1,.example.com"
    ```

  # 3.为 docker build 过程设置网络代理

  在容器构建阶段，如果需要使用 HTTP/HTTPS 代理，可以通过指定 “docker build” 的环境变量，或者在 Dockerfile 中指定环境变量的方式。

  - 使用 “–build-arg” 指定 “docker build” 的相关环境变量
  
    ```bash
    docker build \
        --build-arg "HTTP_PROXY=http://proxy.example.com:8080/" \
        --build-arg "HTTPS_PROXY=http://proxy.example.com:8080/" \
        --build-arg "NO_PROXY=localhost,127.0.0.1,.example.com" .
    ```


---

# Docker容器技术核心组件详解--执行文件介绍
以下是这些容器相关组件及其作用的详细解释，按照功能分层归类：

### **1. 容器运行时核心组件**
#### **`runc`**
- **作用**: 符合OCI标准的底层容器运行时工具，直接调用Linux内核功能（namespaces/cgroups）创建容器进程。
- **特点**: 由Docker贡献给Open Container Initiative (OCI)，是容器化的基石。

#### **`containerd`**
- **作用**: 核心容器运行时守护进程，负责管理容器生命周期（创建/启动/停止）、镜像管理、存储等。
- **特点**: 作为后台服务运行，被Docker等上层工具调用，属于中间层。

#### **`containerd-shim-runc-v2`**
- **作用**: 作为`containerd`和`runc`之间的适配层，确保容器进程与`containerd`解耦。
- **功能**: 
  - 允许`containerd`重启不影响运行中的容器。
  - 收集容器退出状态并转发给`containerd`。

### **2. Docker生态系统工具**
#### **`docker` (CLI)**
- **作用**: 用户最常用的命令行客户端，用于向`dockerd`发送指令（如构建镜像、运行容器）。

#### **`dockerd`**
- **作用**: Docker守护进程，接收来自`docker` CLI的请求，管理镜像、容器、网络和存储卷。
- **层级**: 调用`containerd`处理容器操作，自身负责高层抽象。

#### **`docker-init`**
- **作用**: 轻量级初始化系统（基于tini），解决容器内PID 1进程信号处理问题。
- **典型场景**: 避免僵尸进程，确保`SIGTERM`等信号正确传递。

#### **`docker-proxy`**
- **作用**: 实现容器端口与宿主机的映射（通过`-p`参数）。
- **原理**: 监听宿主机端口，将流量转发到容器（部分场景由iptables替代）。

### **3. 辅助工具与接口**
#### **`ctr`**
- **作用**: `containerd`自带的调试工具，提供基础容器管理功能。
- **使用场景**: 开发者直接操作`containerd`（如检查容器状态），非日常使用。

#### **`docker-compose`**
- **作用**: 通过YAML文件定义多容器应用，一键启动/停止复杂服务栈。
- **典型场景**: 开发环境快速部署Web服务+数据库+缓存等组合。


### **4. 组件协作流程示例**
当执行 `docker run` 时：
1. `docker` CLI 发送请求到 `dockerd`。
2. `dockerd` 解析请求，调用 `containerd` API。
3. `containerd` 准备容器环境，通过 `containerd-shim-runc-v2` 启动 `runc`。
4. `runc` 调用内核创建容器进程。
5. `docker-proxy` 和网络组件处理端口映射和网络隔离。

### **总结**
- **用户常用工具**: `docker`, `docker-compose`（日常操作）
- **系统级守护进程**: `dockerd`, `containerd`（后台服务）
- **底层运行时**: `runc`, `containerd-shim`（由上层自动调用）
- **调试工具**: `ctr`（开发者/运维人员使用）

这些组件共同构成Docker容器技术的核心栈，理解其分层结构有助于排查问题或进行高级定制。普通用户通常只需关注`docker`和`docker-compose`即可。

---


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