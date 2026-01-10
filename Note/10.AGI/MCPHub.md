# MCPHub：将 MCP 服务器转换为 HTTP/SSE 服务

## 1. 核心概念

### 1.1 MCPHub 是什么
MCPHub 是一个**聚合网关服务器**，专门用于将多个基于标准输入/输出（stdio）的 MCP 服务器封装为统一的 HTTP 服务，支持以下两种接口：
- **SSE 端点**：提供 Server-Sent Events 流式接口
- **Web 界面**：提供可视化的 Web 操作界面

### 1.2 工作原理
```mermaid
flowchart TD
    A[客户端应用] -->|HTTP/SSE 请求| B[MCPHub 网关<br>端口:3000]
    B -->|stdio 调用| C[MCP Server 1<br>如 DataHub]
    B -->|stdio 调用| D[MCP Server 2<br>如文件系统]
    B -->|stdio 调用| E[MCP Server N]
```

## 2. 安装与部署

### 2.1 Docker 部署（推荐）

#### 基本运行命令
```bash
# 前台运行（测试推荐，可查看实时日志）
docker run -p 3000:3000 \
  -v ./mcp_settings.json:/app/mcp_settings.json \
  -v ./data:/app/data \
  samanhappy/mcphub

# 后台运行（生产推荐）
docker run -d \
  --name mcphub \
  --restart unless-stopped \
  -p 3000:3000 \
  -v ./mcp_settings.json:/app/mcp_settings.json \
  -v ./data:/app/data \
  samanhappy/mcphub
```

### 2.2 环境准备
确保在运行前创建必要的目录和文件：
```bash
# 创建配置文件
touch mcp_settings.json

# 创建数据目录（用于需要文件存储的 MCP 服务器）
mkdir -p data
```

## 3. 配置 MCP 服务器

### 3.1 配置文件结构（mcp_settings.json）

```json
{
  "mcpServers": {
    "server1": {
      "command": "执行命令",
      "args": ["参数1", "参数2"],
      "env": {
        "环境变量": "值"
      }
    },
    "server2": {
      // 另一个 MCP 服务器配置
    }
  }
}
```

### 3.2 常用 MCP 服务器配置示例

```json
{
  "mcpServers": {
    "datahub": {
      "command": "uvx",
      "args": ["mcp-server-datahub"],
      "env": {
        "DATAHUB_GMS_URL": "https://your-datahub-instance.acryl.io"
      },
      "_comment": "DataHub 服务器"
    },
    "files": {
      "command": "npx",
      "args": ["@modelcontextprotocol/server-filesystem", "/app/data"],
      "_comment": "文件系统服务器"
    },
    "postgres": {
      "command": "npx",
      "args": ["@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_URL": "postgresql://user:password@host:5432/db"
      }
    }
  }
}
```

## 4. 使用方式

### 4.1 通过 Web 界面访问
1. 启动 MCPHub 后，在浏览器中访问：`http://localhost:3000`
2. 界面会显示已配置的所有 MCP 服务器
3. 可以：
   - 查看服务器提供的工具（Tools）
   - 查看资源（Resources）模板
   - 直接调用工具并查看结果

### 4.2 通过 SSE 端点调用
SSE 端点是 MCPHub 的核心功能，允许客户端通过 HTTP 连接 MCP 服务器。

#### 基本连接
```javascript
// 客户端 JavaScript 示例
const eventSource = new EventSource('http://localhost:3000/sse');

eventSource.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};

eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
};
```

#### 带分组的连接
如果配置了多个服务器，可以通过分组进行连接：
```javascript
// 连接所有服务器
const allServers = new EventSource('http://localhost:3000/sse');

// 仅连接特定服务器组（需在 MCPHub 中配置分组）
const specificGroup = new EventSource('http://localhost:3000/sse/your-group-name');
```

### 4.3 通过 HTTP API 调用

#### 检查服务器状态
```bash
curl http://localhost:3000/health
```

#### 列出可用工具
```bash
curl http://localhost:3000/api/tools
```

## 5. 故障排查

### 5.1 常见问题与解决

#### 容器启动失败
```bash
# 1. 检查端口是否被占用
sudo lsof -i :3000

# 2. 检查配置文件语法
python3 -m json.tool mcp_settings.json

# 3. 查看详细错误日志
docker logs mcphub
```

#### MCP 服务器连接失败
1. **检查命令路径**：确保 `command` 中的命令在容器内可用
2. **检查环境变量**：确认敏感信息已正确设置
3. **检查权限**：确保容器有执行命令的权限

#### SSE 连接问题
1. **检查 CORS 设置**：如果从浏览器不同源访问，需要配置 CORS
2. **检查防火墙**：确保端口 3000 可访问
3. **查看连接状态**：检查浏览器开发者工具中的 Network 标签

### 5.2 日志级别调整
可以通过环境变量调整日志详细程度：
```bash
docker run -d \
  -p 3000:3000 \
  -e LOG_LEVEL=debug \  # 可选: error, warn, info, debug
  -v ./mcp_settings.json:/app/mcp_settings.json \
  samanhappy/mcphub
```

## 6. 进阶配置

### 6.1 自定义分组
在配置文件中创建服务器分组：
```json
{
  "mcpServers": {
    "datahub": { /* 配置 */ },
    "filesystem": { /* 配置 */ },
    "sql": { /* 配置 */ }
  },
  "groups": {
    "data-team": ["datahub", "sql"],
    "all-services": ["datahub", "filesystem", "sql"]
  }
}
```

### 6.2 持久化数据
对于需要保存状态的 MCP 服务器，使用 Docker 卷持久化：
```bash
# 创建命名卷（推荐）
docker volume create mcphub_data

docker run -d \
  -p 3000:3000 \
  -v ./mcp_settings.json:/app/mcp_settings.json \
  -v mcphub_data:/app/data \
  samanhappy/mcphub
```

### 6.3 网络配置
在 Docker Compose 中集成：
```yaml
version: '3.8'
services:
  mcphub:
    image: samanhappy/mcphub
    ports:
      - "3000:3000"
    volumes:
      - ./mcp_settings.json:/app/mcp_settings.json
      - mcphub_data:/app/data
    restart: unless-stopped
    networks:
      - app-network

volumes:
  mcphub_data:

networks:
  app-network:
    driver: bridge
```

## 7. 安全注意事项

1. **保护配置文件**：`mcp_settings.json` 可能包含敏感信息（API 密钥、令牌等）
2. **限制网络访问**：生产环境中，限制对 3000 端口的访问
3. **使用 HTTPS**：通过反向代理添加 SSL/TLS 加密
4. **定期更新**：确保使用最新版本的 MCPHub 镜像

---

## 实用提示

- **配置文件验证**：使用 [JSONLint](https://jsonlint.com/) 验证配置文件格式
- **增量配置**：先添加一个 MCP 服务器测试，确认正常后再添加更多
- **资源监控**：使用 `docker stats mcphub` 监控容器资源使用情况
- **备份配置**：定期备份 `mcp_settings.json` 文件
