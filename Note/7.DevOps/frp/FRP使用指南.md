# FRP 使用指南

## 一、FRP 简介

### 1.1 什么是 FRP

FRP（Fast Reverse Proxy）是一个高性能的反向代理应用，可以帮助将内网服务暴露到公网。它支持多种协议，包括 TCP、UDP、HTTP、HTTPS 等，非常适合内网穿透场景。

**项目地址**: [GitHub - fatedier/frp](https://github.com/fatedier/frp)

### 1.2 核心特点

- **开源免费**: 完全开源，免费使用
- **跨平台**: 支持 Linux、Windows、macOS 等多种操作系统
- **多协议支持**: TCP、UDP、HTTP、HTTPS、STCP、XTCP 等
- **高性能**: 使用 Go 语言开发，性能优异
- **配置简单**: TOML 格式配置文件，易于理解和维护
- **安全可靠**: 支持 Token 认证、TLS 加密等安全特性

### 1.3 适用场景

- **内网穿透**: 将内网服务暴露到公网
- **远程开发**: 本地开发环境远程访问
- **临时演示**: 快速展示本地项目
- **设备互联**: IoT 设备、智能家居等
- **游戏联机**: 本地游戏服务器对外提供服务

---

## 二、FRP 架构

### 2.1 基本架构

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   客户端     │  ──────> │  FRP 服务端  │ <──────  │   访问者     │
│  (内网)     │         │  (公网)     │         │  (公网)     │
└─────────────┘         └─────────────┘         └─────────────┘
     frpc                    frps                  用户
```

**工作原理**:
1. frpc（客户端）连接到 frps（服务端）
2. frps 监听公网端口
3. 外部用户访问 frps 的公网端口
4. frps 将请求转发给 frpc
5. frpc 将请求转发给本地服务

### 2.2 组件说明

- **frps**: 服务端程序，运行在有公网 IP 的服务器上
- **frpc**: 客户端程序，运行在内网机器上
- **配置文件**: TOML 格式，分别配置服务端和客户端

---

## 三、安装部署

### 3.1 下载安装

**Linux**:
```bash
# 下载最新版本
wget https://github.com/fatedier/frp/releases/download/v0.61.1/frp_0.61.1_linux_amd64.tar.gz

# 解压
tar -xzf frp_0.61.1_linux_amd64.tar.gz
cd frp_0.61.1_linux_amd64

# 赋予执行权限
chmod +x frps frpc
```

**Windows**:
```powershell
# 下载并解压
# 从 GitHub Releases 页面下载 Windows 版本
# 解压后得到 frps.exe 和 frpc.exe
```

**macOS**:
```bash
# 使用 Homebrew
brew install frpc
```

### 3.2 目录结构

```
frp/
├── frps          # 服务端程序
├── frps.toml     # 服务端配置文件
├── frpc          # 客户端程序
├── frpc.toml     # 客户端配置文件
└── logs/         # 日志目录
    ├── frps.log
    └── frpc.log
```

---

## 四、服务端配置

### 4.1 基础配置

**frps.toml**:
```toml
# 基础配置
bindAddr = "0.0.0.0"
bindPort = 7000

# KCP 协议配置（可选，提升性能）
# kcpBindPort = 7000

# 仪表板配置
webServer.addr = "0.0.0.0"
webServer.port = 7500
webServer.user = "admin"
webServer.password = "admin123"

# 日志配置
log.to = "./logs/frps.log"
log.level = "info"
log.maxDays = 3

# 认证配置
auth.method = "token"
auth.token = "123456"

# TLS 配置（可选）
# transport.tls.forceCert = true
# transport.tls.certFile = "server.crt"
# transport.tls.keyFile = "server.key"
```

### 4.2 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| bindAddr | 绑定地址 | "0.0.0.0" |
| bindPort | 绑定端口 | 7000 |
| webServer.addr | 仪表板地址 | "127.0.0.1" |
| webServer.port | 仪表板端口 | 7500 |
| auth.method | 认证方式 | "token" |
| auth.token | 认证令牌 | - |

### 4.3 启动服务端

```bash
# 前台启动
./frps -c frps.toml

# 后台启动
nohup ./frps -c frps.toml > /dev/null 2>&1 &

# 使用 systemd 管理
sudo systemctl start frps
sudo systemctl enable frps
```

---

## 五、客户端配置

### 5.1 基础配置示例

**frpc.toml**:
```toml
# 服务端地址配置
serverAddr = "your-server-ip"
serverPort = 7000

# TLS 配置（如果服务端启用了 TLS）
transport.tls.enable = true

# 日志配置
log.to = "./logs/frpc.log"
log.level = "info"
log.maxDays = 3

# 认证配置（需与服务端一致）
auth.method = "token"
auth.token = "123456"

# 代理配置
[[proxies]]
name = "ssh"
type = "tcp"
localIP = "127.0.0.1"
localPort = 22
remotePort = 6000
```

### 5.2 实际配置示例（CloudStudio）

```toml
# 服务端地址配置
serverAddr = "a5cc73ca4aac4337815359fb526179e7--7000.ap-shanghai2.cloudstudio.club"
serverPort = 443

# TLS 配置
transport.tls.enable = true

# 日志配置
log.to = "./logs/frpc.log"
log.level = "info"
log.maxDays = 3

# 认证配置
auth.method = "token"
auth.token = "123456"

# Ollama 服务代理
[[proxies]]
name = "ollama-tcp_1"
type = "tcp"
localIP = "127.0.0.1"
localPort = 11434
remotePort = 6001
```

### 5.3 配置说明

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| serverAddr | 服务端地址 | "your-server-ip" |
| serverPort | 服务端端口 | 7000 |
| transport.tls.enable | 启用 TLS | true |
| auth.method | 认证方式 | "token" |
| auth.token | 认证令牌 | "123456" |
| proxies.name | 代理名称 | "ollama-tcp_1" |
| proxies.type | 代理类型 | "tcp" |
| proxies.localIP | 本地 IP | "127.0.0.1" |
| proxies.localPort | 本地端口 | 11434 |
| proxies.remotePort | 远程端口 | 6001 |

### 5.4 启动客户端

```bash
# 前台启动
./frpc -c frpc.toml

# 后台启动
nohup ./frpc -c frpc.toml > /dev/null 2>&1 &

# Windows 后台启动
start /B frpc.exe -c frpc.toml
```

---

## 六、代理类型详解

### 6.1 TCP 代理

**适用场景**: SSH、数据库、自定义 TCP 服务

```toml
[[proxies]]
name = "ssh"
type = "tcp"
localIP = "127.0.0.1"
localPort = 22
remotePort = 6000
```

**访问方式**: `ssh user@server-ip -p 6000`

### 6.2 UDP 代理

**适用场景**: DNS、游戏服务器

```toml
[[proxies]]
name = "dns"
type = "udp"
localIP = "127.0.0.1"
localPort = 53
remotePort = 6000
```

### 6.3 HTTP 代理

**适用场景**: Web 服务、API 服务

```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 80
customDomains = ["www.yourdomain.com"]
```

**访问方式**: `http://www.yourdomain.com`

### 6.4 HTTPS 代理

**适用场景**: 安全 Web 服务

```toml
[[proxies]]
name = "web-https"
type = "https"
localIP = "127.0.0.1"
localPort = 443
customDomains = ["www.yourdomain.com"]
```

### 6.5 STCP 代理

**适用场景**: 安全 TCP 连接，需要访问者也运行 frpc

**服务端配置**:
```toml
[[proxies]]
name = "secret_ssh"
type = "stcp"
secretKey = "abcdefg"
localIP = "127.0.0.1"
localPort = 22
```

**访问者配置**:
```toml
[[visitors]]
name = "secret_ssh_visitor"
type = "stcp"
secretKey = "abcdefg"
serverAddr = "your-server-ip"
serverPort = 7000
bindAddr = "127.0.0.1"
bindPort = 6000
```

---

## 七、高级配置

### 7.1 负载均衡

```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 80
customDomains = ["www.yourdomain.com"]
loadBalancer.group = "web"
loadBalancer.groupKey = "web_key"
```

### 7.2 健康检查

```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 80
healthCheck.type = "http"
healthCheck.maxFails = 3
healthCheck.failTimeoutSec = 30
healthCheck.path = "/health"
```

### 7.3 带宽限制

```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 80
transport.bandwidthLimit = "10MB"
```

### 7.4 插件使用

**HTTP Basic Auth**:
```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 80
[proxies.plugin]
type = "http_basic_auth"
httpBasicAuth.user = "admin"
httpBasicAuth.password = "admin123"
```

**HTTPS2HTTP**:
```toml
[[proxies]]
name = "web"
type = "https"
localIP = "127.0.0.1"
localPort = 80
[proxies.plugin]
type = "https2http"
https2http.localAddr = "127.0.0.1:80"
https2http.crtPath = "./client.crt"
https2http.keyPath = "./client.key"
```

---

## 八、常见应用场景

### 8.1 SSH 远程访问

**服务端配置**:
```toml
bindAddr = "0.0.0.0"
bindPort = 7000
auth.method = "token"
auth.token = "123456"
```

**客户端配置**:
```toml
serverAddr = "your-server-ip"
serverPort = 7000
auth.method = "token"
auth.token = "123456"

[[proxies]]
name = "ssh"
type = "tcp"
localIP = "127.0.0.1"
localPort = 22
remotePort = 6000
```

**使用**: `ssh user@your-server-ip -p 6000`

### 8.2 Web 服务暴露

**客户端配置**:
```toml
[[proxies]]
name = "web"
type = "http"
localIP = "127.0.0.1"
localPort = 8080
customDomains = ["web.yourdomain.com"]
```

**使用**: `http://web.yourdomain.com`

### 8.3 数据库远程访问

**MySQL**:
```toml
[[proxies]]
name = "mysql"
type = "tcp"
localIP = "127.0.0.1"
localPort = 3306
remotePort = 3306
```

**Redis**:
```toml
[[proxies]]
name = "redis"
type = "tcp"
localIP = "127.0.0.1"
localPort = 6379
remotePort = 6379
```

### 8.4 AI 模型服务（Ollama）

**客户端配置**:
```toml
[[proxies]]
name = "ollama"
type = "tcp"
localIP = "127.0.0.1"
localPort = 11434
remotePort = 11434
```

**使用**: `http://your-server-ip:11434`

---

## 九、监控与运维

### 9.1 仪表板监控

访问 `http://your-server-ip:7500` 查看：
- 当前连接数
- 流量统计
- 代理状态
- 客户端信息

### 9.2 日志管理

**日志级别**:
- `trace`: 最详细
- `debug`: 调试信息
- `info`: 一般信息（推荐）
- `warn`: 警告信息
- `error`: 错误信息

**日志配置**:
```toml
log.to = "./logs/frps.log"
log.level = "info"
log.maxDays = 7
```

### 9.3 性能优化

**服务端优化**:
```toml
# 增加最大连接数
transport.maxPoolCount = 100

# 启用 TCP 多路复用
transport.tcpMux = true
transport.tcpMuxKeepaliveInterval = 60
```

**客户端优化**:
```toml
# 启用连接池
transport.poolCount = 10

# 心跳配置
transport.heartbeatInterval = 30
transport.heartbeatTimeout = 90
```

---

## 十、故障排查

### 10.1 常见问题

**问题1: 连接失败**
```
错误: dial tcp: connection refused
```
**解决**:
- 检查服务端是否启动
- 检查防火墙端口是否开放
- 检查 serverAddr 和 serverPort 配置

**问题2: 认证失败**
```
错误: authorization failed
```
**解决**:
- 检查 auth.token 是否一致
- 检查 auth.method 是否一致

**问题3: 端口被占用**
```
错误: bind: address already in use
```
**解决**:
- 检查端口是否被其他程序占用
- 更换 remotePort

### 10.2 调试技巧

**启用详细日志**:
```toml
log.level = "debug"
```

**测试连接**:
```bash
# 测试服务端端口
telnet your-server-ip 7000

# 测试代理端口
telnet your-server-ip 6000
```

**查看连接状态**:
```bash
# Linux
netstat -anp | grep frp

# Windows
netstat -ano | findstr frp
```

---

## 十一、安全建议

### 11.1 认证安全

- ✅ 使用强 Token（建议 16 位以上随机字符）
- ✅ 定期更换 Token
- ✅ 不同环境使用不同 Token

### 11.2 网络安全

- ✅ 启用 TLS 加密
- ✅ 限制访问 IP（防火墙规则）
- ✅ 使用 STCP 进行安全连接

### 11.3 端口安全

- ✅ 不要暴露敏感端口（如 22、3306）
- ✅ 使用非标准端口
- ✅ 定期检查开放的端口

### 11.4 日志安全

- ✅ 定期清理日志
- ✅ 不要在日志中记录敏感信息
- ✅ 限制日志文件访问权限

---

## 十二、最佳实践

### 12.1 生产环境建议

1. **使用 systemd 管理**:
```bash
# /etc/systemd/system/frps.service
[Unit]
Description=FRP Server
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/frps -c /etc/frp/frps.toml
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
```

2. **配置防火墙**:
```bash
# 开放 FRP 端口
firewall-cmd --permanent --add-port=7000/tcp
firewall-cmd --permanent --add-port=7500/tcp
firewall-cmd --reload
```

3. **监控告警**:
- 监控 FRP 进程状态
- 监控连接数和流量
- 设置异常告警

### 12.2 开发环境建议

1. **使用配置文件管理**
2. **启用详细日志**
3. **定期备份配置**
4. **使用版本控制**

---

## 十三、参考资料

- [FRP 官方文档](https://github.com/fatedier/frp)
- [FRP 中文文档](https://gofrp.org/zh-cn/)
- [FRP 配置示例](https://github.com/fatedier/frp/tree/dev/conf)
- [内网穿透工具对比](https://github.com/anderspitman/awesome-tunneling)

---

**文档版本**: V1.0
**最后更新**: 2026-03-18
**维护单位**: DevOps 团队
