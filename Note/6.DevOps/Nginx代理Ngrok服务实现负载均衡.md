# Nginx 代理 Ngrok 服务实现负载均衡

## 一、背景：使用 Nginx 代理 Ngrok 服务实现负载均衡
先查查官方 ngrok 的nginx 使用介绍，额(⊙﹏⊙) 很简短的说明。[官方简短的原文](https://ngrok.com/docs/using-ngrok-with/nginx/)：
> It's a good idea to get a background on how NGINX processes a request. What that usually means is you need to ensure the --host-header flag is set when tunneling your service.
>
> 了解NGINX如何处理请求的背景是个好主意。这通常意味着您需要确保在通过隧道传输服务时设置--host标头标志。

**开始实操：**

### 1.1 安装flask: `pip install flask`， 用flask写一个简单的api服务:

```python
from flask import Flask, jsonify

# 创建 Flask 应用实例
app = Flask(__name__)

# 定义 /api/hello 接口
@app.route('/api/hello', methods=['GET'])
def hello():
    return jsonify({
        "message": "Hello from Flask API",
        "status": "success"
    })

if __name__ == '__main__':
    # 运行应用（0.0.0.0 允许外部访问）
    app.run(host='0.0.0.0', port=5000, debug=True)
```

### 1.2 ngrok命令：

```bash
# 安装 ngrok 的 python 客户端
pip install pyngrok
pyngrok		# 下载安装ngrok

ngrok config add-authtoken <token>	# 配置token
ngrok http 5000 --host-header="localhost"	# 映射端口
```

得到的公网域名地址：https://90c7-180-76-140-37.ngrok-free.app

## 二、问题探索过程与核心发现

### 测试与问题定位
- **现象**：使用 `curl -H "Host: localhost"` 请求失败, 去掉
  
  ```bash
  # 设置 -H "Host: localhost" 失败
  curl -H "Host: localhost" https://90c7-180-76-140-37.ngrok-free.app/api/hello
  Received a request for different Host than the current tunnel.
  Current Tunnel: 90c7-180-76-140-37.ngrok-free.app
  Requested Host: localhost
  
  # 去掉Host 成功
  curl https://90c7-180-76-140-37.ngrok-free.app/api/hello       # curl 默认会将请求的地址设为 Host 头信息
  # 或者设置 -H "Host: [隧道域名]" 成功
  curl -H "Host: 90c7-180-76-140-37.ngrok-free.app" https://90c7-180-76-140-37.ngrok-free.app/api/hello
  {
    "message": "Hello from Flask API",
    "status": "success"
  }
  ```
- **关键发现**：  
  Ngrok 对外服务严格校验 `Host` 头，必须与隧道域名完全匹配  
  （默认绑定生成的 `*.ngrok-free.app` 域名）

---

## 三、配置方案对比与优化

### 3.1 方案一：单层代理
#### nginx配置
```nginx
http {
    upstream ngrok_tunnel {  # 定义上游服务器组
        server 90c7-180-76-140-37.ngrok-free.app:443;
        keepalive 128;
    }

    server {
        listen 5000;
        server_name localhost;
        location / {
            proxy_pass https://ngrok_tunnel;  # 指向 upstream 集群
            proxy_http_version 1.1;      # 强制使用 HTTP/1.1 协议
			# proxy_set_header 设置
            proxy_set_header Connection "";     # 关闭逐跳头部，允许 keepalive 
            proxy_set_header Host 90c7-180-76-140-37.ngrok-free.app;        # 显示配置域名
            proxy_set_header X-Forwarded-Host 90c7-180-76-140-37.ngrok-free.app;    # 显示配置域名
            proxy_set_header X-Real-IP $remote_addr;      # 传递客户端真实IP
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;    # 记录转发链
            proxy_set_header X-Forwarded-Proto $scheme;   # 协议类型（http/https）
            # 超时配置
            proxy_connect_timeout 30s;
            proxy_read_timeout 180s;
            proxy_send_timeout 180s;
            # SSL 配置
            proxy_ssl_server_name on;   # 传递 SNI 信息
            proxy_ssl_name 90c7-180-76-140-37.ngrok-free.app;  # 明确 SSL 握手域名， 显示配置域名
            proxy_ssl_verify off;       # 测试环境可跳过证书验证（生产环境不建议）
        }
    }
}
```

但是方案一配置太死板，如果存在多个代理地址。`proxy_set_header Host`、`proxy_set_header X-Forwarded-Host`、`proxy_ssl_name` 无法动态配置域名。

问题：硬编码配置无法适应多域名扩展。

### 3.2 方案二：分层代理（推荐）

不如直接将每个ngrok服务单独配置一个nginx，然后使用upstream实现负载均衡。通过端口隔离实现动态路由。

#### nginx配置
```nginx
http {
    # 第一层：端口级代理（可水平扩展）
    server {
        listen 5001;
        location / {
            proxy_pass https://90c7-180-76-140-37.ngrok-free.app:443;
            proxy_http_version 1.1;
            proxy_set_header Connection "";

            # 若上游为 HTTPS 服务，需额外配置 SSL
            proxy_ssl_server_name on;  # 启用 SNI 支持
            proxy_ssl_verify off;      # 测试环境可跳过证书验证（生产环境不建议）
        }
    }
	## 扩展服务
    server {
        listen 5002;
        location / {
            proxy_pass http://192.168.10.23:5000;
        }
    }

    # 第二层：负载均衡
    upstream flask_serve {  # 定义上游服务器组
        server 127.0.0.1:5001 weight=2;
        server 127.0.0.1:5002 weight=1;
        keepalive 128;
    }

    # 对外访问统一端口
    server {
        listen 5000;
        server_name localhost;
        location / {
            proxy_pass http://flask_serve;  # 指向 upstream 集群
            proxy_http_version 1.1;      # 强制使用 HTTP/1.1 协议，必须项

            # 超时配置
            proxy_connect_timeout 30s;
            proxy_read_timeout 180s;
            proxy_send_timeout 180s;
        }
    }
}
```
#### 分层代理架构

实现： 外部请求 -> 5000端口 -> upstream集群（当前单节点）-> 5001端口服务 -> ngrok HTTPS隧道，形成完整的代理链路。

   ```mermaid
   graph TD
   A[外部请求] --> B[5000端口]
   B --> C{Upstream集群}
   C --> D[5001端口服务]
   C --> E[5002端口服务]
   D --> F[Ngrok HTTPS隧道]
   E --> G[其他后端服务]
   ```

#### 方案优势对比表

| 特性               | 方案一       | 方案二           |
|--------------------|-------------|------------------|
| 多域名支持         | ❌ 固定配置 | ✅ 动态扩展      |
| 配置维护复杂度     | 低          | 中              |
| 流量分配灵活性     | 有限         | ✅ 支持权重配置  |
| 后端服务异构支持   | ❌          | ✅ 混合HTTP/HTTPS|

---

## 四、关键技术原理解析

### 4.1 SNI 技术核心作用
```nginx
proxy_ssl_server_name on;  # 启用SNI
```
- **作用机制**：在 TLS 握手阶段传递域名信息
- **必要性**：  
  Ngrok 使用多域名共用IP，服务端依赖 SNI 选择正确证书

### 4.2 负载均衡策略选择
```nginx
upstream backend {
    ip_hash;               # 会话保持
    server backend1 weight=3;
    server backend2;
}
```
| 策略         | 配置指令      | 适用场景                     |
|--------------|-------------|----------------------------|
| 加权轮询     | 默认         | 通用场景                    |
| 最少连接数   | `least_conn`| 长连接服务（如WebSocket）   |
| IP哈希       | `ip_hash`   | 有状态服务                  |

### 4.3 健康检查机制
```nginx
upstream backend {
    server 192.168.0.1:80 max_fails=2 fail_timeout=30s;
    server 192.168.0.2:80 backup;  # 备用节点
}
```
- **主动检查**：需安装 `nginx_upstream_check_module`
- **被动检查**：  
  `max_fails`：最大连续失败次数  
  `fail_timeout`：节点不可用持续时间


---

> **思考总结**：  
> 通过两种配置方案的对比实践，深刻理解到动态化配置在代理架构中的重要性。分层方案虽增加初期配置复杂度，但为后续扩展保留了充足空间。未来可结合服务发现机制，实现全动态的代理集群管理。

🌟如你有好的方案，欢迎分享，一起交流。





## 五、内网穿透工具

内网穿透工具可以帮助用户通过公网访问内网资源（如NAS、服务器、摄像头等），以下是一些常用且高效的工具，适用于不同场景和需求：

---

### **一、免费 & 开源工具**
1. **Ngrok**  
   - **特点**：简单易用，支持多平台，无需公网IP，提供临时域名（付费可自定义）。  
   - **适用场景**：临时测试、开发调试（如微信本地开发）。  
   - **官网**：[ngrok.com](https://ngrok.com)

2. **FRP (Fast Reverse Proxy)**  
   - **特点**：开源、灵活，支持TCP/UDP/HTTP/HTTPS，需自备服务器部署服务端。  
   - **适用场景**：长期稳定的内网穿透，适合有一定技术基础的用户。  
   - **项目地址**：[GitHub - fatedier/frp](https://github.com/fatedier/frp)

3. **ZeroTier**  
   - **特点**：基于虚拟局域网（VPN），无需配置端口转发，支持P2P直连。  
   - **适用场景**：组网需求（如远程访问多台内网设备）。  
   - **官网**：[zerotier.com](https://www.zerotier.com)

4. **Serveo**  
   - **特点**：纯SSH实现，无需安装客户端，支持自定义域名。  
   - **适用场景**：快速临时穿透，适合SSH转发。  
   - **使用方式**：`ssh -R 80:localhost:3000 serveo.net`

---

### **二、国产工具（适合国内网络）**
1. **花生壳（Oray）**  
   - **特点**：国内老牌工具，提供免费版（带宽限制）和付费版，支持HTTP/TCP/UDP。  
   - **适用场景**：个人轻量级穿透需求，如远程桌面、摄像头访问。  
   - **官网**：[oray.com](https://hsk.oray.com)

2. **NPS (内网穿透代理)**  
   - **特点**：开源、高性能，支持多协议和多用户管理，需自建服务端。  
   - **适用场景**：企业级内网穿透，替代商业方案。  
   - **项目地址**：[GitHub - ehang-io/nps](https://github.com/ehang-io/nps)

---

### **三、商业工具（付费但稳定）**
1. **Tailscale**  
   - **特点**：基于WireGuard的VPN工具，自动组网，支持多平台，免费版可满足个人需求。  
   - **适用场景**：跨地区设备安全互联（如远程办公）。  
   - **官网**：[tailscale.com](https://tailscale.com)

2. **Cloudflare Tunnel**  
   - **特点**：集成Cloudflare的CDN和防火墙，无需暴露公网IP，支持HTTPS。  
   - **适用场景**：企业级Web服务穿透，安全性高。  
   - **官网**：[cloudflare.com](https://www.cloudflare.com/products/tunnel)

3. **Localtonet**  
   - **特点**：简单易用，支持HTTP/TCP/UDP，提供免费版和付费自定义域名。  
   - **适用场景**：开发测试、快速分享本地服务。  
   - **官网**：[localtonet.com](https://localtonet.com)

---

### **四、选择建议**
- **临时测试**：Ngrok、Serveo。  
- **长期稳定使用**：FRP（自建服务器）或 ZeroTier（P2P组网）。  
- **国内网络优化**：花生壳、NPS。  
- **企业级需求**：Cloudflare Tunnel、Tailscale。  
- **无需配置**：ZeroTier、Tailscale（自动组网）。

---

### **五、注意事项**
1. **安全性**：内网穿透可能暴露内网服务，建议开启身份验证、HTTPS、IP白名单等。  
2. **带宽限制**：免费工具通常限速，商业需求建议选择付费方案。  
3. **协议支持**：根据需求选择TCP/UDP/HTTP等协议支持的工具。