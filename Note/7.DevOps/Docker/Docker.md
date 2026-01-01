# Docker 核心概念与使用指南

## 1. Docker 基础概念

### 1.1 镜像（Image）
镜像是一种轻量级、可执行的独立软件包，用来打包软件运行环境和基于运行环境开发的软件，它包含代码、运行时库、环境变量、配置文件等。

### 1.2 容器（Container）
容器是镜像的运行实例。容器可以被创建、启动、停止、删除、暂停等。每个容器都是相互隔离的，拥有自己的文件系统、网络配置和进程空间。

### 1.3 仓库（Registry）
仓库是集中存放镜像的地方。Docker Hub 是官方的公共仓库，也可以搭建私有仓库。

## 2. Docker 镜像详解

### 2.1 镜像加载原理：UnionFS（联合文件系统）

文件系统结构：
1. **最底层**：引导文件系统 bootfs，容器启动后，容器本身转移到内存中，同时 bootfs 被卸载
2. **第二层**：根文件系统 rootfs，可以是一种或多种操作系统
3. **Docker 联合加载技术**：一次同时加载多个文件系统，不过在宿主机只能看到一个文件系统

Docker 镜像：联合加载将各层文件系统叠加一起后，形成的最终文件系统称为镜像。

### 2.2 镜像分层结构
Docker 镜像采用分层存储架构：
- **只读层**：基础镜像和中间层，不可修改
- **读写层**：容器层，所有修改都在这一层
- **Copy-on-Write**：写时复制机制，提高效率

## 3. 容器生命周期管理

### 3.1 restart 启动参数
Docker 的 restart 参数用于指定自动重启容器的策略，包含四个选项：

- **no**：默认值，表示容器退出时，Docker 不自动重启容器。
- **on-failure[:times]**：若容器的退出状态非 0，则 Docker 自动重启容器，还可以指定重启次数，若超过指定次数未能启动容器则放弃。
- **always**：容器退出时总是重启。
- **unless-stopped**：容器退出时总是重启，但不考虑 Docker 守护进程启动时就已经停止的容器。（**除非用户手动停止容器**）

### 3.2 修改容器的启动参数
```bash
# 修改容器重启策略
docker container update --restart=always 容器名字

# 修改容器时区
docker run -e TZ="Asia/Shanghai" ...
```

### 3.3 容器状态管理
```bash
# 创建容器
docker create --name my_container nginx:latest

# 启动容器
docker start my_container

# 停止容器
docker stop my_container

# 重启容器
docker restart my_container

# 删除容器
docker rm my_container

# 查看容器状态
docker ps -a
```

## 4. Docker 网络配置

### 4.1 网络模式
Docker 支持多种网络模式：
- **bridge**：默认模式，容器通过虚拟网桥连接到宿主机网络
- **host**：容器直接使用宿主机的网络命名空间
- **none**：容器没有网络接口
- **container**：容器共享另一个容器的网络命名空间

### 4.2 网络代理配置

#### 4.2.1 为 dockerd 设置网络代理
"docker pull" 命令是由 dockerd 守护进程执行，而 dockerd 守护进程是由 systemd 管理。因此如果需要在执行 “docker pull” 命令时使用 HTTP/HTTPS 代理，需要通过 systemd 配置代理：

1. 为 dockerd 创建配置文件夹：
   ```bash
   sudo mkdir -p /etc/systemd/system/docker.service.d
   ```

2. 创建 HTTP/HTTPS 代理配置文件 `/etc/systemd/system/docker.service.d/http-proxy.conf`：
   ```ini
   [Service]
   Environment="HTTP_PROXY=http://proxy.example.com:7890/"
   Environment="HTTPS_PROXY=http://proxy.example.com:7890/"
   Environment="NO_PROXY=localhost,127.0.0.1,.example.com"
   ```

3. 刷新配置并重启 docker 服务：
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart docker
   ```

#### 4.2.2 为 docker 容器设置网络代理
在容器运行阶段设置代理：

1. **通过 docker 客户端配置**：创建或更改 `~/.docker/config.json`：
   ```json
   {
     "proxies": {
       "default": {
         "httpProxy": "http://proxy.example.com:7890/",
         "httpsProxy": "http://proxy.example.com:7890/",
         "noProxy": "localhost,127.0.0.1,.example.com"
       }
     }
   }
   ```

2. **通过环境变量**：
   ```bash
   docker run --env HTTP_PROXY="http://proxy.example.com:8080/" \
              --env HTTPS_PROXY="http://proxy.example.com:7890/" \
              --env NO_PROXY="localhost,127.0.0.1,.example.com" \
              nginx:latest
   ```

#### 4.2.3 为 docker build 过程设置网络代理
在容器构建阶段设置代理：

```bash
docker build \
    --build-arg "HTTP_PROXY=http://proxy.example.com:8080/" \
    --build-arg "HTTPS_PROXY=http://proxy.example.com:8080/" \
    --build-arg "NO_PROXY=localhost,127.0.0.1,.example.com" .
```

或在 Dockerfile 中设置：
```dockerfile
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV NO_PROXY=${NO_PROXY}
```

### 4.3 端口映射与网络连接
```bash
# 端口映射
docker run -p 8080:80 nginx:latest

# 指定网络
docker run --network=bridge nginx:latest

# 连接现有网络
docker network connect mynetwork container_name
```


## 5. Docker 核心组件架构

### 5.1 容器运行时核心组件

#### **`runc`**
- **作用**：符合 OCI 标准的底层容器运行时工具，直接调用 Linux 内核功能（namespaces/cgroups）创建容器进程。
- **特点**：由 Docker 贡献给 Open Container Initiative (OCI)，是容器化的基石。

#### **`containerd`**
- **作用**：核心容器运行时守护进程，负责管理容器生命周期（创建/启动/停止）、镜像管理、存储等。
- **特点**：作为后台服务运行，被 Docker 等上层工具调用，属于中间层。

#### **`containerd-shim-runc-v2`**
- **作用**：作为 `containerd` 和 `runc` 之间的适配层，确保容器进程与 `containerd` 解耦。
- **功能**：
  - 允许 `containerd` 重启不影响运行中的容器。
  - 收集容器退出状态并转发给 `containerd`。

### 5.2 Docker 生态系统工具

#### **`docker` (CLI)**
- **作用**：用户最常用的命令行客户端，用于向 `dockerd` 发送指令（如构建镜像、运行容器）。

#### **`dockerd`**
- **作用**：Docker 守护进程，接收来自 `docker` CLI 的请求，管理镜像、容器、网络和存储卷。
- **层级**：调用 `containerd` 处理容器操作，自身负责高层抽象。

#### **`docker-init`**
- **作用**：轻量级初始化系统（基于 tini），解决容器内 PID 1 进程信号处理问题。
- **典型场景**：避免僵尸进程，确保 `SIGTERM` 等信号正确传递。

#### **`docker-proxy`**
- **作用**：实现容器端口与宿主机的映射（通过 `-p` 参数）。
- **原理**：监听宿主机端口，将流量转发到容器（部分场景由 iptables 替代）。

### 5.3 辅助工具与接口

#### **`ctr`**
- **作用**：`containerd` 自带的调试工具，提供基础容器管理功能。
- **使用场景**：开发者直接操作 `containerd`（如检查容器状态），非日常使用。

#### **`docker-compose`**
- **作用**：通过 YAML 文件定义多容器应用，一键启动/停止复杂服务栈。
- **典型场景**：开发环境快速部署 Web 服务+数据库+缓存等组合。

> **注意**：关于 Docker Compose 的详细使用，请参考 [Docker Compose 文档](./Docker Compose.md)

### 5.4 组件协作流程示例

当执行 `docker run` 时：
1. `docker` CLI 发送请求到 `dockerd`
2. `dockerd` 解析请求，调用 `containerd` API
3. `containerd` 准备容器环境，通过 `containerd-shim-runc-v2` 启动 `runc`
4. `runc` 调用内核创建容器进程
5. `docker-proxy` 和网络组件处理端口映射和网络隔离

### 5.5 总结
- **用户常用工具**：`docker`, `docker-compose`（日常操作）
- **系统级守护进程**：`dockerd`, `containerd`（后台服务）
- **底层运行时**：`runc`, `containerd-shim`（由上层自动调用）
- **调试工具**：`ctr`（开发者/运维人员使用）

这些组件共同构成 Docker 容器技术的核心栈，理解其分层结构有助于排查问题或进行高级定制。

## 6. Docker 安装与配置

### 6.1 准备工作

#### 停止现有 Docker 服务
如果系统已安装过 Docker：
```bash
# 停止所有 Docker 容器
docker stop $(docker ps -aq)

# 停止 Docker 服务
sudo systemctl stop docker

# 备份 Docker 数据（可选）
sudo mv /var/lib/docker /var/lib/docker.bak
```

#### 卸载旧版本 Docker
```bash
sudo yum remove docker \
                docker-client \
                docker-client-latest \
                docker-common \
                docker-latest \
                docker-latest-logrotate \
                docker-logrotate \
                docker-engine
```

### 6.2 CentOS/RHEL 安装

#### 安装依赖工具
```bash
sudo yum install -y yum-utils
```

#### 添加 Docker 仓库
```bash
# 使用阿里云镜像源（国内用户推荐）
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 或者使用官方源（可能需要代理）
# sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
```

#### 安装 Docker 及相关组件
```bash
sudo yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
```

### 6.3 启动与验证

#### 启动 Docker 服务
```bash
# 启动 Docker 服务
sudo systemctl start docker

# 设置 Docker 开机自启
sudo systemctl enable docker

# 将当前用户加入 docker 组（避免每次使用 sudo）
sudo usermod -aG docker $USER
# 需要重新登录生效
```

#### 验证安装
```bash
# 验证 Docker 版本
docker version

# 验证 Docker Compose 版本
docker compose version

# 运行测试容器
docker run hello-world
```

### 6.4 自动化安装脚本

#### 使用官方安装脚本
```bash
# 下载并运行官方安装脚本
curl -fsSL get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 配置用户组
sudo groupadd docker 2>/dev/null || echo "Docker group already exists"
sudo usermod -aG docker $USER

# 重启 Docker 服务
sudo systemctl restart docker

# 清理脚本文件
rm -f get-docker.sh
```

#### 自定义安装脚本示例
创建 `install-docker.sh`：
```bash
#!/bin/bash
# Docker 自动化安装脚本

echo "开始安装 Docker..."

# 1. 安装依赖
sudo yum install -y yum-utils

# 2. 添加仓库
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 3. 安装 Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. 启动服务
sudo systemctl start docker
sudo systemctl enable docker

# 5. 配置用户组
sudo groupadd docker 2>/dev/null || true
sudo usermod -aG docker $USER

echo "Docker 安装完成！请重新登录以使组权限生效。"
echo "运行 'docker run hello-world' 测试安装。"
```

## 7. Docker 存储管理

### 7.1 数据卷（Volumes）
```bash
# 创建数据卷
docker volume create myvolume

# 查看数据卷
docker volume ls

# 使用数据卷
docker run -v myvolume:/data nginx:latest

# 删除数据卷
docker volume rm myvolume
```

### 7.2 绑定挂载（Bind Mounts）
```bash
# 绑定宿主机目录
docker run -v /host/path:/container/path nginx:latest

# 只读挂载
docker run -v /host/path:/container/path:ro nginx:latest
```

### 7.3 临时文件系统（tmpfs）
```bash
# 使用内存文件系统
docker run --tmpfs /app/tmp nginx:latest
```

## 8. Docker 日志管理

### 8.1 查看容器日志
```bash
# 查看最新日志
docker logs container_name

# 实时查看日志
docker logs -f container_name

# 查看指定时间段的日志
docker logs --since 1h container_name

# 查看最后 N 行日志
docker logs --tail 100 container_name
```

### 8.2 日志驱动配置
```bash
# 运行时指定日志驱动
docker run --log-driver=json-file --log-opt max-size=10m --log-opt max-file=3 nginx:latest

# 配置全局日志驱动（在 daemon.json 中）
# {
#   "log-driver": "json-file",
#   "log-opts": {
#     "max-size": "10m",
#     "max-file": "3"
#   }
# }
```

## 9. 最佳实践与常见问题

### 9.1 最佳实践
1. **使用特定版本标签**：避免使用 `latest` 标签，使用具体版本如 `nginx:1.21.0`
2. **限制资源使用**：为容器设置 CPU 和内存限制
3. **使用非 root 用户**：在容器内使用非 root 用户运行应用
4. **定期清理**：清理未使用的镜像、容器和卷
5. **备份重要数据**：定期备份容器内的关键数据

### 9.2 常用命令参考
更多详细命令请参考：[Docker常用命令](./Docker常用命令.md)

### 9.3 配置文件说明
Docker 守护进程配置请参考：[Docker之daemon.json配置文件](./Docker之daemon.json配置文件.md)

---

> **提示**：本文档提供了 Docker 的核心概念和使用指南。建议结合实际项目需求，参考相关文档进行实践。
