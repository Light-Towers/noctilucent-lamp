# Docker 网络详解

Docker 网络是容器通信和互联互通的关键基础，它提供了丰富的网络模型满足各种应用场景需求。下面我将详细介绍 Docker 网络架构和使用方法。

## 1. Docker 网络架构基础

Docker 使用 Linux 的网络命名空间（Network Namespaces）技术实现容器网络隔离。其核心组件包括：

- **网络驱动（Network Drivers）**：实现不同网络类型的组件
- **libnetwork**：Docker 的网络库，实现容器网络模型（CNM）
- **网络命名空间**：为每个容器提供独立的网络栈

## 2. Docker 网络驱动类型
### 2.1 bridge（桥接网络）

* 默认网络驱动
* 创建虚拟网桥（默认为 `docker0`）
* 容器获得私有 IP 地址
* 同一桥接网络的容器可以互相通信
* 通过 NAT 和端口映射实现与外部网络通信

```bash
# 创建自定义桥接网络
docker network create --driver bridge my_bridge_network
```

### 2.2 host（主机网络）
* 容器直接使用宿主机网络栈
* 没有网络隔离，可能出现端口冲突
* 性能更好（无 NAT 开销）
* 适用于需要高网络性能的应用
```bash
# 使用主机网络运行容器
docker run --network host nginx
```

### 2.3 none（空网络）
* 容器只有回环接口（localhost）
* 完全隔离外部网络
* 适用于不需要网络的应用或自定义网络方案
```bash
# 运行无网络容器
docker run --network none alpine
```

### 2.4 overlay（覆盖网络）
* 用于 Docker Swarm 集群中跨主机容器通信
* 基于 VXLAN 技术实现
* 提供容器间加密通信选项
* 支持服务发现和负载均衡

```bash
# 创建覆盖网络（需要 Swarm 模式）
docker network create --driver overlay my_overlay_network
```

### 2.5 macvlan
* 为容器分配真实的 MAC 地址和 IP 地址
* 使容器在物理网络中显示为独立设备
* 直接连接到物理网络，无需端口映射
* 需要网卡支持混杂模式
```bash
# 创建 macvlan 网络
docker network create --driver macvlan \
  --subnet=192.168.1.0/24 \
  --gateway=192.168.1.1 \
  -o parent=eth0 my_macvlan_network
```

### 2.6 ipvlan
与 macvlan 类似，但多容器共享同一 MAC 地址，有 L2 和 L3 两种模式。

## 3. 网络命令和操作
### 3.1 网络管理基础命令
```bash
# 列出所有网络
docker network ls
# 检查网络详情
docker network inspect bridge
# 删除网络
docker network rm my_network
# 清理未使用的网络
docker network prune
```

### 3.2 容器网络连接
```bash
# 创建容器并连接到指定网络
docker run --network my_network nginx

# 将运行中的容器连接到网络
docker network connect my_network container_name

# 断开网络连接
docker network disconnect my_network container_name
```

## 4. 容器间通信
### 4.1 同一网络内的容器通信
* 自定义网络中的容器可以通过容器名称互相解析（内置 DNS）
* 默认 bridge 网络需要使用 --link 选项（不推荐）或 IP 地址

### 4.2 跨网络通信
* 容器可以同时连接多个网络
* 使用网关或专门的路由容器实现跨网络通信

## 5. 端口映射
```bash
# 指定端口映射
docker run -p 8080:80 nginx  # 主机8080映射到容器80

# 随机端口映射
docker run -P nginx  # 随机映射所有暴露端口

# 指定绑定地址
docker run -p 127.0.0.1:8080:80 nginx  # 只允许本地访问
```
## 6. 高级网络配置
### 6.1 自定义网络配置
```bash
# 创建具有特定子网和网关的网络
docker network create --subnet=172.18.0.0/16 --gateway=172.18.0.1 custom_net

# 分配固定IP地址
docker run --network custom_net --ip 172.18.0.10 nginx
```
### 6.2 DNS配置
```bash
# 设置容器DNS服务器
docker run --dns 8.8.8.8 nginx

# 设置DNS搜索域
docker run --dns-search example.com nginx
```
## 7. 网络故障排查
### 7.1 常用调试命令
```bash
# 检查容器网络设置
docker inspect --format='{{json .NetworkSettings}}' container_name

# 进入容器测试网络
docker exec -it container_name ping google.com
docker exec -it container_name ip addr show

# 查看容器端口映射
docker port container_name
```
## 7.2 常见问题
* 网络连接超时：检查防火墙规则
* 容器间无法通信：检查网络模式和路由
* DNS解析失败：检查DNS配置
## 8. 多主机网络解决方案
* Docker Swarm 原生 overlay 网络
* Kubernetes 网络插件（Calico、Flannel、Weave等）
* 第三方网络方案（OpenVSwitch等）

Docker 网络提供了灵活而强大的容器通信能力，从简单的单主机场景到复杂的多云环境都有对应的网络解决方案。根据应用需求选择合适的网络驱动和配置，能够构建高效、安全的容器化应用架构。