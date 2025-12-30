# 国内无法访问 GitHub 时的解决方案
针对 `git clone` 连接 GitHub 超时的问题，以下是综合多种场景的解决方案，按优先级排序：

---

### **1. 设置 Git 代理（需本地已开启代理工具）**
若你使用代理工具（如 Clash、V2Ray 等），需手动配置 Git 的代理端口（一般为 `7890` 或 `1080`）：
```bash
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy http://127.0.0.1:7890
```
**验证代理生效**：
```bash
curl -v https://github.com  # 若返回正常，说明代理成功
```
**取消代理**：
```bash
git config --global --unset http.proxy
git config --global --unset https.proxy
```
**原理**：Git 默认不跟随系统代理，需手动指定代理服务器 。

---

### **2. 使用国内镜像加速**
将 GitHub 链接替换为国内镜像站，绕过直接连接限制：
```bash
# 方法一：使用 CNPMJS 镜像
git clone https://github.com.cnpmjs.org/langgenius/dify.git --branch 1.1.3

# 方法二：使用 ghproxy 代理
git clone https://ghproxy.com/https://github.com/langgenius/dify.git --branch 1.1.3
```
**长期生效**：修改全局 Git 配置，自动替换镜像源：
```bash
git config --global url."https://ghproxy.com/https://github.com/".insteadOf "https://github.com/"
```
**优势**：无需代理工具，适合国内网络环境 。

---

### **3. 修改 Hosts 文件**
更新本地 DNS 解析，避免 GitHub 域名被污染：
1. **获取最新 GitHub IP**：访问 [IPAddress](https://github.com.ipaddress.com/) 查询 `github.com` 和 `github.global.ssl.fastly.net` 的最新 IP。
2. **编辑 Hosts 文件**（需管理员权限）：
   ```bash
   sudo vim /etc/hosts
   ```
   添加以下内容（示例 IP，需替换为最新值）：
   ```
   140.82.121.3 github.com
   199.232.69.194 github.global.ssl.fastly.net
   ```
3. **刷新 DNS 缓存**：
   ```bash
   ipconfig /flushdns  # Windows
   sudo systemd-resolve --flush-caches  # Linux
   ```
   **适用场景**：解决 DNS 解析失败或延迟问题 。

---

### **4. 改用 SSH 协议**
SSH 协议可能更稳定，尤其在公司网络限制 HTTPS 的情况下：
1. **生成 SSH 密钥并添加到 GitHub**：
   ```bash
   ssh-keygen -t ed25519 -C "your_email@example.com"
   cat ~/.ssh/id_ed25519.pub  # 将公钥粘贴到 GitHub 账户设置
   ```
2. **克隆仓库**：
   ```bash
   git clone git@github.com:langgenius/dify.git --branch 1.1.3
   ```
3. **若 SSH 超时**：修改 `~/.ssh/config`，强制使用端口 443：
   ```
   Host github.com
     Hostname ssh.github.com
     Port 443
     User git
   ```
   **优势**：绕过 HTTPS 端口限制，提升连接稳定性 。

---

### **5. 调整 Git 超时参数**
针对大仓库或慢速网络，延长超时时间或降低传输速度限制：
```bash
# 设置超时时间为 300 秒（默认 60 秒）
git config --global http.timeout 300

# 允许低速传输（1KB/s 持续 600 秒）
git config --global http.lowSpeedLimit 1000
git config --global http.lowSpeedTime 600

# 增大缓冲区（避免大文件传输失败）
git config --global http.postBuffer 524288000  # 500MB
```
**适用场景**：网络波动或大型仓库克隆 。

---

### **6. 其他临时方案**
- **浅层克隆**：仅拉取最新提交（减少数据量）：
  ```bash
  git clone --depth 1 https://github.com/langgenius/dify.git --branch 1.1.3
  ```
- **下载 ZIP 包**：直接从 GitHub 网页下载仓库压缩包，解压后初始化 Git。

---

### **总结建议**
1. **优先使用镜像或代理**：国内用户推荐镜像站（如 `ghproxy`），国际网络可尝试代理。
2. **排查网络环境**：检查防火墙、DNS 设置，或切换网络（如 4G 热点）。
3. **长期配置优化**：修改 Hosts 或 SSH 配置，避免重复调整参数。

若问题持续，可参考 GitHub 官方文档或通过 `GIT_CURL_VERBOSE=1 git clone` 查看详细错误日志 。

> 继续提问：
> * 国内 GitHub 仓库的克隆速度优化方法有哪些？
> * 设置 Git 代理后如何测试连接是否成功？