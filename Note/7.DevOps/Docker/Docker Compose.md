# Docker Compose 核心知识点

## 1. YAML 高级语法：锚点与引用 (Anchors & Aliases)

这是 Docker Compose 配置中的重点特性。

* **锚点 (`&`)**：定义一个可复用的配置块。例如 `x-db-config: &db-config`。
* **引用 (`*`)**：在其他地方调用定义的配置块。
* **合并符号 (`<<:`)**：将锚点的内容"合并"到当前的层级中。

> **核心教训**：注意引用的层级。如果锚点里包含了 `environment:` 关键字，那么引用时必须放在服务的根层级；如果锚点里只是键值对，则引用时要放在 `environment:` 下方。

**示例**：
```yaml
x-db-config: &db-config
  image: postgres:latest
  environment:
    POSTGRES_DB: myapp
    POSTGRES_USER: admin
    POSTGRES_PASSWORD: secret

services:
  database:
    <<: *db-config
    ports:
      - "5432:5432"
    
  app:
    image: myapp:latest
    depends_on:
      - database
    environment:
      <<: *db-config
      APP_ENV: production
```

## 2. 环境变量管理

* **`.env` 文件**：Compose 默认读取同目录下的 `.env` 文件，将变量注入到 `${VARIABLE}` 占位符中。
* **`env_file` 关键字**：手动指定某个容器加载特定的环境变量文件。
* **优先级顺序**：Shell 环境变量 > `docker-compose.yaml` 里的 `environment` 块 > `.env` 文件。

**示例**：
```yaml
# .env 文件
DB_PASSWORD=mysecret
APP_PORT=8080

# docker-compose.yml
services:
  app:
    image: myapp:latest
    environment:
      - DB_PASSWORD=${DB_PASSWORD}
      - APP_PORT=${APP_PORT}
    env_file:
      - ./app.env
```

## 3. 服务依赖与生命周期控制 (`depends_on`)

复杂的系统（如 DataHub）非常依赖正确的启动顺序：

* **`condition: service_healthy`**：这比简单的 `depends_on` 更高级，它要求被依赖的服务不仅要"启动"，还得通过 `healthcheck`（健康检查）检测。
* **`condition: service_completed_successfully`**：适用于"一次性任务"服务（如 `mysql-setup` 或 `system-update`），要求该容器运行结束且退出码为 0，主服务才会启动。

**示例**：
```yaml
services:
  database:
    image: mysql:8.0
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  app-setup:
    image: app-setup:latest
    depends_on:
      database:
        condition: service_healthy

  main-app:
    image: main-app:latest
    depends_on:
      app-setup:
        condition: service_completed_successfully
```

## 4. 网络架构 (Networking)

* **内部通信**：容器之间通过"服务名"通信。例如 GMS 访问 `http://search:9200`，这里的 `search` 就是 Elasticsearch 的服务名。
* **端口映射 (`ports`)**：
  * **格式**：`宿主机端口:容器内端口`。
  * **冲突处理**：当你修改了外部端口（如 `18080:8080`），容器间通信依然用 `8080`，只有你从外面访问（如浏览器或 curl）才用 `18080`。

**网络配置示例**：
```yaml
networks:
  app-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16

services:
  web:
    image: nginx:latest
    ports:
      - "8080:80"
    networks:
      - app-network

  app:
    image: myapp:latest
    networks:
      - app-network
    # 内部使用服务名访问
    environment:
      - WEB_SERVICE_URL=http://web:80
```

## 5. 资源配额与性能调优 (`deploy.resources`)

对于 Java 这种吃内存的应用，必须限制资源：

* **`limits`**：容器能使用的最大资源。
* **`reservations`**：容器启动时预留的最小资源。
* **JVM 参数联动**：在 `environment` 中设置 `JAVA_OPTS` 或 `ES_JAVA_OPTS` 必须与 `limits` 相匹配，否则会导致容器被系统强制杀死（OOM Killed）。

**示例**：
```yaml
services:
  elasticsearch:
    image: elasticsearch:7.14.2
    environment:
      ES_JAVA_OPTS: "-Xms1g -Xmx1g"
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'
```

## 6. 数据持久化 (`volumes`)

* **绑定挂载 (Bind Mounts)**：将宿主机具体路径映射到容器，如 `${HOME}/.datahub/plugins`，方便修改配置文件。
* **命名卷 (Named Volumes)**：由 Docker 管理，通常用于数据库数据，如 `esdata:/usr/share/elasticsearch/data`，性能较好且更安全。
* **临时卷 (tmpfs)**：仅存储在内存中，适合临时数据。

**示例**：
```yaml
volumes:
  db-data:
  app-logs:

services:
  database:
    image: postgres:latest
    volumes:
      - db-data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  app:
    image: myapp:latest
    volumes:
      - ./config:/app/config
      - app-logs:/app/logs
      - /tmp:/app/temp:rw
```

---

## 7. Docker Compose 常用命令

### 基本操作
```bash
# 启动服务（后台运行）
docker-compose up -d

# 停止服务
docker-compose down

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f

# 重启服务
docker-compose restart
```

### 构建与清理
```bash
# 构建镜像
docker-compose build

# 强制重新构建
docker-compose build --no-cache

# 清理未使用的资源
docker-compose down --volumes --rmi all
```

### 调试与管理
```bash
# 进入容器
docker-compose exec service_name bash

# 查看容器资源使用
docker-compose stats

# 查看服务配置
docker-compose config
```

## 8. 最佳实践

1. **使用版本控制**：在 `docker-compose.yml` 中指定 `version` 字段
2. **环境分离**：使用不同的 compose 文件区分开发、测试、生产环境
3. **敏感信息管理**：使用 secrets 或外部配置文件管理密码等敏感信息
4. **健康检查**：为关键服务配置健康检查，确保依赖关系正确
5. **资源限制**：为生产环境服务设置合理的资源限制
6. **日志管理**：配置日志驱动和日志轮转策略
7. **网络隔离**：为不同的应用栈使用不同的网络

## 9. 常见问题与解决方案

### 端口冲突
```yaml
# 解决方案：使用不同的宿主机端口或动态端口分配
services:
  app1:
    ports:
      - "8080:80"
  app2:
    ports:
      - "8081:80"  # 或使用 - "80" 让 Docker 分配随机端口
```

### 文件权限问题
```bash
# 在容器启动脚本中设置正确的权限
chmod 644 /etc/mysql/conf.d/my.cnf
```

### 启动顺序问题
```yaml
# 使用健康检查和 depends_on condition
depends_on:
  database:
    condition: service_healthy
```

### 环境变量覆盖
```bash
# 使用环境变量文件或命令行覆盖
docker-compose --env-file .env.production up -d
```

---

> **提示**：本文档基于实际使用经验整理，特别是 DataHub 等复杂系统的部署实践。建议结合具体的项目需求进行调整和扩展。
