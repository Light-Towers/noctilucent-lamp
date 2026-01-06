# 部署 MCPO 服务器与 UV 环境指南

### 一、 安装 UV（推荐方式）

**UV** 是由 Astral 团队开发的高性能 Python 包管理工具，旨在替代 `pip`、`poetry` 等。

#### 1. 为什么推荐使用 curl 安装？
*   **自包含性**：用 Rust 编写，不依赖系统 Python 环境。
*   **自更新**：支持 `uv self update` 直接升级。
*   **极速**：直接下载预编译二进制文件，速度比 `pip` 快 10-100 倍。

#### 2. 安装命令
```bash
# Linux / macOS
curl -LsSf https://astral.sh/uv/install.sh | sh

# Windows (PowerShell)
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"
```

#### 3. 配置环境变量
安装后需使命令生效（通常脚本会提示）：
```bash
source $HOME/.cargo/env  # 或者根据提示重启终端
uv --version             # 验证安装
```

---

### 二、 认识 UV 的核心优势
1.  **极速性能**：依赖解析速度极快，适用于大型项目和 CI/CD。
2.  **一体化**：整合了包管理、虚拟环境、Python 版本控制。
3.  **零激活运行**：通过 `uv run` 直接运行，无需手动 `source bin/activate`。
4.  **别名工具**：`uvx` (即 `uv tool run`) 可在隔离的临时环境中运行工具（类似 `pipx`）。

---

### 三、 使用 mcpo 部署 MCP 服务

`mcpo` 是一个用于管理和代理多个 MCP (Model Context Protocol) 服务器的工具。

#### 1. 安装依赖
建议使用 `uv` 进行安装以保证环境隔离：
```bash
# 使用 uv 全局安装工具
uv tool install mcpo
# 或者通过 pip 安装
pip install uv mcpo --no-cache-dir
```

#### 2. 创建 `config.json`
配置需要聚合的 MCP 服务器，支持 SSE 等传输协议。

**示例 `config.json`**
```json
{
  "mcpServers": {
    "baidu-maps": {
      "timeout": 60,
      "url": "https://aaxxx.api-inference.modelscope.cn/sse",
      "transportType": "sse"
    },
    "fetch": {
      "timeout": 60,
      "url": "https://aaxxx.api-inference.modelscope.cn/sse",
      "transportType": "sse"
    }
  }
}
```

#### 3. 启动服务
```bash
mcpo --host 0.0.0.0 --port 8000 --config config.json
```
启动后，`mcpo` 会自动连接并代理配置文件中定义的所有 MCP 实例。

---

### 四、 UV 常用指令手册

| 功能 | 命令 | 说明 |
| :--- | :--- | :--- |
| **安装包** | `uv pip install <package>` | 兼容 pip 语法但速度更快 |
| **创建虚拟环境** | `uv venv` | 在当前目录创建 `.venv` |
| **安装 Python 版本** | `uv python install 3.12` | 快速切换/安装不同版本 |
| **运行脚本** | `uv run main.py` | 自动在虚拟环境中运行 |
| **运行临时工具** | `uvx mcp-server-time` | 隔离环境中运行 MCP 工具 |
| **同步依赖** | `uv sync` | 根据 lock 文件同步环境 |
| **清理缓存** | `uv cache clean` | 清除所有全局缓存 |

---

### 五、 进阶配置与常见问题

#### 1. 配置国内镜像源
在项目根目录创建 `uv.toml` 或在 `pyproject.toml` 中设置：
```toml
[tool.uv]
index-url = "https://pypi.tuna.tsinghua.edu.cn/simple"
```

#### 2. 缓存管理
*   `uv cache prune`：删除未使用的缓存项，节省磁盘空间。
*   环境变量 `UV_CACHE_DIR`：可自定义缓存路径。

#### 3. 常见问题排查
*   **依赖冲突**：优先使用 `uv.lock` 文件锁定版本，通过 `uv sync` 同步。
*   **权限问题**：在容器环境中使用 `uv` 时，确保当前用户对 `.venv` 有读写权限。
*   **多版本共存**：使用 `uv venv --python 3.12` 明确指定环境版本。
