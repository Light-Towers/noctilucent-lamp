# SeaTunnel Web 部署笔记

## 一、环境架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    SeaTunnel Web (端口: 8801)                    │
│                  Web管理界面 + REST API                          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  通过 Hazelcast Client 连接 Engine                          │ │
│  │  提交任务 → 序列化 → 发送到 Engine 执行                      │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬───────────────────────────────────────┘
                          │
            ┌─────────────┴─────────────┐
            │ Hazelcast 协议 (端口 5801) │
            ▼                           ▼
┌───────────────────────┐     ┌───────────────────────┐
│   SeaTunnel Engine    │     │        MySQL          │
│   (Zeta Engine)       │     │   (元数据存储)         │
│   端口: 5801          │     │   端口: 3306          │
│   任务执行引擎         │     │   数据库: seatunnel_web│
│   基于 Hazelcast 构建  │     └───────────────────────┘
└───────────────────────┘
```

## 二、版本兼容性

| SeaTunnel Web | SeaTunnel Engine | 说明 |
|---------------|------------------|------|
| 1.0.3-SNAPSHOT | **2.3.11** | 当前版本，API 必须一致 |
| 1.0.2 | 2.3.8 | |
| 1.0.1 | 2.3.3 | |
| 1.0.0 | 2.3.3 | |

**重要**: SeaTunnel Web 和 Engine 有严格的版本依赖，API 版本必须一致，不可混用版本。

## 三、核心组件说明

### 3.1 SeaTunnel Engine 是什么？

SeaTunnel Engine 是 SeaTunnel 的**分布式任务执行引擎**，类似于 Flink 或 Spark 的运行时环境。

```
SeaTunnel Web    →  任务编排、管理界面 (不执行任务)
SeaTunnel Engine →  真正执行数据同步任务
```

### 3.2 Hazelcast 是什么？

Hazelcast 是 SeaTunnel Engine 的底层基础设施，提供：

| 能力 | 说明 |
|------|------|
| 集群管理 | 节点发现、成员管理、心跳检测 |
| 分布式通信 | 任务分发、状态同步 |
| 分布式计算 | 任务调度、负载均衡 |
| 容错机制 | 自动故障转移、数据复制 |

```
SeaTunnel Engine 架构层次:

┌─────────────────────────────────────┐
│      SeaTunnel 业务层               │
│  任务管理 | 资源管理 | 调度策略      │
├─────────────────────────────────────┤
│      Hazelcast 基础层               │
│  集群通信 | 分布式计算 | 数据同步    │
└─────────────────────────────────────┘
```

**GitHub**: https://github.com/hazelcast/hazelcast

### 3.3 Web 如何连接 Engine？

通过 `hazelcast-client.yaml` 配置文件：

```yaml
hazelcast-client:
  cluster-name: seatunnel-default  # 必须与 Engine 端一致
  network:
    cluster-members:
      - localhost:5801             # Engine 地址
```

连接流程：
```
Web → 读取 hazelcast-client.yaml → 创建 SeaTunnelClient → 连接 Engine
```

## 四、部署模式

### 4.1 本地模式 (推荐)

```
┌─────────────────────────────────┐
│  单机部署                        │
│  ┌─────────────┐ ┌───────────┐  │
│  │SeaTunnel Web│ │  Engine   │  │
│  │  (8801)     │ │  (5801)   │  │
│  └─────────────┘ └───────────┘  │
└─────────────────────────────────┘
✅ 无需额外配置，直接可用
```

### 4.2 异机部署 (有严格要求)

```
┌──────────────────┐    ┌──────────────────┐
│  Server A        │    │  Server B        │
│ ┌──────────────┐ │    │ ┌──────────────┐ │
│  │SeaTunnel Web│ │───▶│ │   Engine     │ │
│  │  (8801)     │ │    │ │   (5801)     │ │
│  └──────────────┘ │    │ └──────────────┘ │
│ SEATUNNEL_HOME=  │    │ SEATUNNEL_HOME=  │
│ /opt/seatunnel   │    │ /opt/seatunnel   │ ← 路径必须相同!
└──────────────────┘    └──────────────────┘
```

**异机部署要求**:
| 要求 | 说明 |
|------|------|
| 操作系统 | 必须相同 (Linux/Linux) |
| 安装路径 | 必须完全一致 |
| SEATUNNEL_HOME | 两边环境变量值相同 |

**原因**: 任务序列化时会包含文件绝对路径，Engine 需要用相同路径读取配置和插件。

## 五、部署步骤

### 5.1 安装 SeaTunnel Engine

```bash
# 解压安装包 (bin.tar.gz 是预编译包，无需再编译)
tar -xzf apache-seatunnel-2.3.11-bin.tar.gz -C /opt/seatunnel/

# 设置环境变量
export SEATUNNEL_HOME=/opt/seatunnel/apache-seatunnel-2.3.11

# 安装 Connector 插件 (首次使用需要)
cd $SEATUNNEL_HOME
sh bin/install-plugin.sh

# 启动 Engine (后台模式)
./bin/seatunnel-cluster.sh -d

# 验证启动成功
jps | grep SeaTunnel
netstat -tlnp | grep 5801
```

### 5.2 构建并安装 SeaTunnel Web

```bash
# SeaTunnel Web 是源码项目，需要编译
cd /workspace/seatunnel-web
mvn clean package -DskipTests

# 或使用项目脚本
sh build.sh code

# 解压安装
mkdir -p /opt/seatunnel-web
tar -xzf seatunnel-web-dist/target/apache-seatunnel-web-1.0.3-SNAPSHOT-bin.tar.gz -C /opt/seatunnel-web/
```

### 5.3 配置修改

**文件: `conf/application.yml`**

```yaml
server:
  port: 8801

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seatunnel_web?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secretKey: your_jwt_secret_key  # 必须添加，不能太短
```

**文件: `script/seatunnel_server_env.sh`**

```bash
HOST="localhost"
PORT="3306"
USERNAME="root"
PASSWORD="your_mysql_password"
DATABASE="seatunnel_web"
```

**复制配置文件**:

```bash
# 复制 hazelcast-client.yaml (如果异机部署，需修改地址)
cp $SEATUNNEL_HOME/config/hazelcast-client.yaml \
   /opt/seatunnel-web/apache-seatunnel-web-*/conf/

# 复制 plugin-mapping.properties
cp $SEATUNNEL_HOME/connectors/plugin-mapping.properties \
   /opt/seatunnel-web/apache-seatunnel-web-*/conf/
```

### 5.4 数据库初始化

```bash
# 创建数据库
mysql -h localhost -u root -pyour_mysql_password -e "CREATE DATABASE IF NOT EXISTS seatunnel_web;"

# 执行初始化脚本
mysql -h localhost -u root -pyour_mysql_password seatunnel_web < script/seatunnel_server_mysql.sql
```

### 5.5 启动服务

```bash
export ST_WEB_BASEDIR_PATH=/opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT
export SEATUNNEL_HOME=/opt/seatunnel/apache-seatunnel-2.3.11
cd $ST_WEB_BASEDIR_PATH
./bin/seatunnel-backend-daemon.sh start
```

## 六、遇到的问题及解决方案

### 问题分析

官方 SQL 脚本 (`seatunnel_server_mysql.sql`) 的内容：

| 数据 | 是否有 | 行号 |
|------|--------|------|
| 角色数据 (role) | ✅ 有 | 第40-41行 |
| 用户数据 (user) | ✅ 有 | 第270行 |
| 工作空间 (workspace) | ✅ 有 | 第286行 |
| 用户角色关联 (role_user_relation) | ❌ **缺失** | - |

### 问题1: SQL脚本数据库名称不匹配

**现象**: 初始化SQL脚本使用的数据库是 `seatunnel`，但配置文件中是 `seatunnel_web`

**原因**: 脚本与配置文件不一致

**解决方案**: 
```bash
# 修改SQL脚本中的数据库名
sed -i 's/seatunnel`/seatunnel_web`/g' seatunnel_server_mysql.sql
sed -i 's/seatunnel\./seatunnel_web./g' seatunnel_server_mysql.sql
```

### 问题2: role_user_relation 缺少初始数据 (官方Bug)

**现象**: 用户登录成功但无权限

**原因**: 官方 SQL 脚本缺少用户角色关联的 INSERT 语句

**解决方案**:
```sql
-- 查询用户ID
SELECT id FROM user WHERE username = 'admin';

-- 关联用户到管理员角色 (假设 user_id = 2)
INSERT INTO role_user_relation(role_id, user_id) VALUES(1, 2);
```

### 问题3: 数据库初始化后表为空

**现象**: workspace、user、role 等表为空

**原因**: 数据库名配置不一致，SQL 插入到了错误的库

**解决方案**: 确保数据库名一致后重新初始化

## 七、完整初始化SQL汇总

如果初始化后数据缺失，可手动补充：

```sql
-- 1. 确保 workspace 存在
INSERT INTO workspace(workspace_name, description) 
VALUES('default', 'default workspace');

-- 2. 确保用户存在 (密码: admin 的 MD5)
INSERT INTO user(username, password, status, type, auth_provider)
VALUES('admin', '7f97da8846fed829bb8d1fd9f8030f3b', 0, 0, 'DB');

-- 3. 确保角色存在
INSERT INTO role(type, role_name, description) 
VALUES(0, 'ADMIN_ROLE', 'Admin User'), 
      (1, 'NORMAL_ROLE', 'Normal User');

-- 4. 关联用户角色 (根据实际 user_id 调整)
INSERT INTO role_user_relation(role_id, user_id) VALUES(1, 2);
```

## 八、核心概念

### Workspace (工作空间)

Workspace 是 SeaTunnel Web 的资源隔离机制，用于实现多租户管理：

- 每个工作空间有独立的资源（数据源、任务、脚本等）
- 用户登录时必须选择工作空间
- 不同工作空间之间的数据相互隔离

### 用户认证

- 密码采用 MD5 加密存储
- `auth_provider` 字段标识认证方式：
  - `DB`: 数据库认证
  - `LDAP`: LDAP 认证（需配置）

## 九、常用命令

```bash
# 查看 SeaTunnel Engine 状态
jps | grep SeaTunnel

# 查看 Engine 端口
netstat -tlnp | grep 5801

# 查看 SeaTunnel Web 日志
tail -f /opt/seatunnel-web/apache-seatunnel-web-*/logs/seatunnel-web-backend.log

# 停止 Web 服务
./bin/seatunnel-backend-daemon.sh stop

# 重启 Web 服务
./bin/seatunnel-backend-daemon.sh restart

# 停止 Engine
$SEATUNNEL_HOME/bin/seatunnel-cluster.sh stop
```

## 十、访问信息

- **访问地址**: http://localhost:8801
- **默认用户名**: admin
- **默认密码**: admin
- **默认工作空间**: default

## 十一、文档缺失说明

官方 README 存在以下问题：

| 问题 | 说明 |
|------|------|
| 数据库名一致性 | 未强调配置文件与 SQL 脚本必须一致 |
| role_user_relation | SQL 脚本缺少该表的初始数据 |
| 默认密码说明 | 只写了 admin/admin，未说明 MD5 值 |
| 异机部署要求 | 未详细说明路径必须一致的原因 |
