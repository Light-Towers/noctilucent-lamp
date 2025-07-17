以下是根据 MySQL binlog 类型整理的清晰笔记，包含核心概念、格式对比和操作指南：

---

### **MySQL binlog 类型详解**
#### **一、binlog 核心作用**
1. **数据记录**  
   - 记录所有数据更改操作（`INSERT`/`UPDATE`/`DELETE`）  
   - 记录数据库结构变更（`CREATE`/`ALTER` 等 DDL 语句）  
2. **主要用途**  
   - ✅ 主从复制（Replication）  
   - ✅ 数据恢复（Point-in-Time Recovery）  

---

#### **二、binlog 的三种格式**
| **特性**         | **`STATEMENT` (SBR)**       | **`ROW` (RBR)**             | **`MIXED` (MBR)**               |
|------------------|-----------------------------|-----------------------------|---------------------------------|
| **记录内容**     | 原始 SQL 语句               | 每行数据的变化（前/后镜像） | 混合模式（自动选择 STATEMENT 或 ROW） |
| **日志大小**     | ⭐️ **小**（单语句记录）     | ⚠️ **大**（尤其批量操作）   | 中等（多数小，不安全语句大）    |
| **可读性**       | ⭐️ **高**（直接显示 SQL）   | ❌ **差**（需工具解析）      | 取决于实际记录模式              |
| **数据一致性**   | ❌ **低**（非确定函数风险）  | ⭐️ **高**（直接复制行变化） | ⭐️ **高**（不安全时切 ROW 保证） |
| **行锁（从库）** | 可能锁更多行                | 仅锁定实际修改行            | 取决于记录模式                  |
| **适用场景**     | 空间受限/低风险环境         | **生产环境首选**            | 平衡场景                       |

---

#### **三、格式详解**
1. **`STATEMENT` (SBR)**  
   - **优点**：日志体积小、可读性强。  
   - **缺点**：  
     - 主从不一致风险（如 `NOW()`, `RAND()`, `UUID()` 等非确定函数）。  
     - 从库可能锁更多行。  

2. **`ROW` (RBR)**  
   - **优点**：  
     - 数据一致性高（直接记录行变更）。  
     - 减少从库行锁范围。  
   - **缺点**：  
     - 日志体积大（尤其含 `BLOB`/`TEXT` 的批量操作）。  
     - 需用 `mysqlbinlog -vv` 解析日志。  

3. **`MIXED` (MBR)**  
   - **机制**：  
     - 默认用 `STATEMENT` 记录。  
     - 检测到风险语句（如非确定函数）时自动切为 `ROW` 模式。  
   - **定位**：平衡日志体积与安全性。  

> 💡 **生产建议**：**优先使用 `ROW` 格式**（MySQL 5.7.7+ 默认），确保数据一致性。

---

#### **四、操作命令**
```sql
-- 1. 检查
SHOW VARIABLES LIKE '%log_bin%';             -- 查看 binlog 配置是否开启
SHOW GLOBAL VARIABLES LIKE 'binlog_format';  -- 查看当前 binlog 格式
SHOW MASTER STATUS;                          -- 查看 binlog 状态信息， [mysql 8.4+](https://dev.mysql.com/doc/refman/8.4/en/show-master-status.html) 变为 `SHOW BINARY LOG STATUS;`  

-- 2. 动态修改格式（需 SUPER 权限，仅对新会话生效）
SET GLOBAL binlog_format = 'ROW';  -- 可选 'STATEMENT'/'MIXED'

-- 3. 永久生效配置（修改 my.cnf/my.ini）
[mysqld]
log-bin=mysql-bin                   -- 启用二进制日志（binlog），并指定日志文件的基本名称
binlog_format=ROW                   -- 设置格式
expire_logs_days=90                 -- 设置 binlog 文件过期天数
max_binlog_size=100M                -- 设置 binlog 文件大小
binlog-do-db=bin_log_test           -- 设置只监控某个或某些数据库
server-id=1                         -- 主从复制必填
```

---

#### **五、注意事项**
1. 主从复制时**主从库 binlog 格式必须一致**。  
2. 动态修改后，重启 MySQL 会失效，需写入配置文件。  
3. `ROW` 模式日志量大时，可定期清理 binlog：  
   ```sql
   PURGE BINARY LOGS BEFORE '2025-07-17 00:00:00';  -- 清理指定时间前的日志
   ```
