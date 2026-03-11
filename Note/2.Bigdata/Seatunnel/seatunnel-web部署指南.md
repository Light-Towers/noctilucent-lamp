# SeaTunnel Web 部署笔记

## 一、环境架构

```
┌─────────────────────────────────────────────────────────┐
│                    SeaTunnel Web                         │
│                  (端口: 8801)                             │
│              Web管理界面 + REST API                       │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌───────────────┐           ┌───────────────┐
│ SeaTunnel     │           │    MySQL      │
│ Zeta Engine   │           │  (your-db-name)│
│ (端口: 5801)  │           │  端口: 3306    │
│ 任务执行引擎   │           │  元数据存储    │
└───────────────┘           └───────────────┘
```

## 二、版本要求

| 组件 | 版本 |
|------|------|
| SeaTunnel Web | 1.0.3-SNAPSHOT |
| SeaTunnel Engine | 2.3.11 |
| MySQL | 5.7+ |
| JDK | 8 或 11 |

## 三、部署步骤

### 3.1 安装 SeaTunnel Engine

```bash
# 解压安装包
tar -xzf apache-seatunnel-2.3.11-bin.tar.gz -C /opt/seatunnel/

# 设置环境变量
export SEATUNNEL_HOME=/opt/seatunnel/apache-seatunnel-2.3.11

# 启动 Engine (本地模式)
cd $SEATUNNEL_HOME
./bin/seatunnel-cluster.sh -d
```

### 3.2 构建并安装 SeaTunnel Web

```bash
# 构建项目
cd /workspace/seatunnel-web
mvn clean package -DskipTests

# 解压安装
mkdir -p /opt/seatunnel-web
tar -xzf seatunnel-web-dist/target/apache-seatunnel-web-1.0.3-SNAPSHOT-bin.tar.gz -C /opt/seatunnel-web/
```

### 3.3 配置修改

**文件: `/opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT/conf/application.yml`**

```yaml
server:
  port: 8801

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seatunnel_web?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver

seatunnel:
  engine:
    host: localhost
    port: 5801

jwt:
  secretKey: your_jwt_secret_key
```

**文件: `/opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT/script/seatunnel_server_env.sh`**

```bash
HOST="localhost"
PORT="3306"
USERNAME="root"
PASSWORD="your_mysql_password"
DATABASE="seatunnel_web"
```

### 3.4 数据库初始化

```bash
# 创建数据库
mysql -h localhost -u root -pyour_mysql_password -e "CREATE DATABASE IF NOT EXISTS seatunnel_web;"

# 执行初始化脚本
mysql -h localhost -u root -pyour_mysql_password seatunnel_web < /opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT/script/seatunnel_server_mysql.sql
```

### 3.5 启动服务

```bash
export ST_WEB_BASEDIR_PATH=/opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT
cd $ST_WEB_BASEDIR_PATH
./bin/seatunnel-backend-daemon.sh start
```

## 四、遇到的问题及解决方案

### 问题1: SQL脚本数据库名称不匹配

**现象**: 初始化SQL脚本使用的数据库是 `seatunnel`，但配置文件中是 `seatunnel_web`

**原因**: 脚本与配置文件不一致

**解决方案**: 
```bash
# 修改SQL脚本中的数据库名
sed -i 's/seatunnel`/seatunnel_web`/g' seatunnel_server_mysql.sql
sed -i 's/seatunnel\./seatunnel_web./g' seatunnel_server_mysql.sql
```

### 问题2: workspace 表为空

**现象**: 登录界面要求选择 workspace，但下拉列表为空

**原因**: 初始化脚本未插入默认 workspace 数据

**解决方案**:
```sql
INSERT INTO workspace(workspace_name, description) 
VALUES('default', 'default workspace');
```

### 问题3: user 表为空

**现象**: 登录失败，提示 "username and password not matched or user is disabled"

**原因**: 初始化脚本未创建默认用户

**解决方案**:
```sql
-- 密码 'admin' 的 MD5 值 (实际部署时请替换为真实的MD5值)
INSERT INTO user(username, password, status, type, auth_provider)
VALUES('admin', 'your_password_md5_hash', 0, 0, 'DB');
```

**用户表字段说明**:
- `status`: 0=启用, 1=禁用
- `type`: 用户类型
- `auth_provider`: 认证方式 ('DB' 表示数据库认证)

### 问题4: role 表为空

**现象**: 用户无角色权限

**原因**: 初始化脚本未创建角色数据

**解决方案**:
```sql
INSERT INTO role(type, role_name, description) 
VALUES(0, 'ADMIN_ROLE', 'Admin User'), 
      (1, 'NORMAL_ROLE', 'Normal User');
```

### 问题5: role_user_relation 表为空

**现象**: 用户未关联任何角色

**原因**: 初始化脚本未创建用户角色关联

**解决方案**:
```sql
-- 将 admin 用户关联到 ADMIN_ROLE
INSERT INTO role_user_relation(role_id, user_id) VALUES(1, 2);
```

**注意**: user_id 需要根据实际插入后的ID调整，可通过 `SELECT * FROM user;` 查询确认

## 五、核心概念

### Workspace (工作空间)

Workspace 是 SeaTunnel Web 的资源隔离机制，用于实现多租户管理：

- 每个工作空间有独立的资源（数据源、任务、脚本等）
- 用户登录时必须选择工作空间
- 不同工作空间之间的数据相互隔离

### 用户认证

- 密码采用 MD5 加密存储
- `auth_provider` 字段标识认证方式：
  - `DB`: 数据库认证
  - 其他值可能对接 LDAP/OAuth 等外部认证

## 六、完整初始化SQL汇总

```sql
-- 1. 创建 workspace
INSERT INTO workspace(workspace_name, description) 
VALUES('default', 'default workspace');

-- 2. 创建用户 (密码: 请自行设置, 需要MD5加密)
INSERT INTO user(username, password, status, type, auth_provider)
VALUES('admin', 'your_password_md5_hash', 0, 0, 'DB');

-- 3. 创建角色
INSERT INTO role(type, role_name, description) 
VALUES(0, 'ADMIN_ROLE', 'Admin User'), 
      (1, 'NORMAL_ROLE', 'Normal User');

-- 4. 关联用户角色 (注意: user_id 需要根据实际情况调整)
INSERT INTO role_user_relation(role_id, user_id) VALUES(1, 2);
```

## 七、常用命令

```bash
# 查看 SeaTunnel Engine 状态
jps | grep SeaTunnel

# 查看 SeaTunnel Web 日志
tail -f /opt/seatunnel-web/apache-seatunnel-web-1.0.3-SNAPSHOT/logs/seatunnel-web-backend.log

# 停止服务
./bin/seatunnel-backend-daemon.sh stop

# 重启服务
./bin/seatunnel-backend-daemon.sh restart
```

## 八、访问信息

- **访问地址**: http://localhost:8801
- **默认用户名**: admin
- **默认密码**: (请根据实际设置的密码)
