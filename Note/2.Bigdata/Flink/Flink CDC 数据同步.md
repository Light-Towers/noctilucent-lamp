# 整库同步 Mysql to Doris
## 📌 前提条件
1. **Flink 集群**：Flink 1.20 + Flink CDC 3.4
   - 需开启 Checkpoint
2. **源数据库**：MySQL 8.0
   - 必须开启 binlog-row 模式
   - 检查 MySQL 配置
    ```sql
    SHOW VARIABLES LIKE 'binlog_format';  -- 必须是 ROW
    SHOW VARIABLES LIKE 'binlog_row_image';  -- 建议为 FULL
    ```
1. **目标数据库**：Doris 3.0.6

## 📁 Pipeline 配置文件
> 文件名：`mysql-to-doris.yaml`，参照官网文档创建pipeline文件
```yaml 
################################################################################
# Description: Sync MySQL all tables to Doris
# ################################################################################
source:
  type: mysql
  hostname: 192.168.100.41
  port: 23306
  username: root
  password: 123456
  tables: app_db.\.*                                    # 同步整个数据库
  tables.exclude: app_db.k_database_type,app_db.k_log   # 排除特定表
  server-id: 5400-5404
  server-time-zone: UTC+8
  scan.newly-added-table.enabled: true                  # 支持自动发现新增表
  # MySQL 8.0 认证插件适配  # (由于Mysql 8 的 caching_sha2_password)
  debezium.database.connection.adapter: "jdbc"
  debezium.database.connection.adapter.jdbc.url: "jdbc:mysql://<host>:<port>/<db>?allowPublicKeyRetrieval=true"

sink:
  type: doris
  fenodes: 192.168.100.39:28030
  benodes: 192.168.100.39:28040
  username: root
  password: ""
  # 表创建参数
  table.create.properties.light_schema_change: true   # 轻量级 Schema 变更
  table.create.properties.replication_num: 1          # 副本数量

pipeline:
  name: Sync MySQL Database to Doris
  parallelism: 1
```

## ⚠️ 同步特性说明
| 操作类型       | Doris 行为                                      | 处理方式                            |
|----------------|-------------------------------------|-------------------------------------|
| 新增表         | 自动同步                           | 配置 `scan.newly-added-table.enabled: true` |
| 表名修改       | 视为新增表                         | 需手动处理旧表数据                  |
| 字段变更       | 支持 Schema 演化                   | 自动处理新增/修改字段               |
| 删除表         | 不触发目标端删除                   | 需手动清理目标端数据                |

> 🔄 **动态表处理**：变更表结构时(新增表、表名变更)需先触发 Savepoint，再从保存点恢复任务以保证数据一致性。Doris 3.0 支持轻量级 Schema 变更，但仍建议避免频繁修改表结构

---

## Flink CDC - 增量同步，遇到的问题
> 更详细的问题总结：https://nightlies.apache.org/flink/flink-cdc-docs-release-3.4/zh/docs/faq/faq/

在 Flink CDC 中，**增量同步**（即通过数据库日志捕获数据变更）**通常要求表必须有主键**，但可以通过特定方式绕过这一限制。以下是详细说明：


### **1. 为什么需要主键？**
- **唯一标识行**：主键用于唯一标识数据库表中的每一行数据。在增量同步中，Flink CDC 依赖主键来判断数据的插入、更新或删除操作，确保变更事件的准确性。
- **避免重复或丢失数据**：如果没有主键，Flink CDC 无法区分不同行的变更，可能导致数据重复、遗漏或逻辑错误。
- **Chunk 读取算法**（Flink CDC 2.0）：  
  在 Flink CDC 2.0 的 **Chunk 读取算法** 中，主键范围划分是核心机制。每个 Chunk 负责主键区间内的数据，通过全量读取和增量合并确保一致性。如果没有主键，无法划分 Chunk，导致算法失效。


### **2. 无主键表的解决方案**
如果源表没有主键，可以通过以下方式实现增量同步：

#### 2.1 修改原表增加主键
直接修改源表，增加主键（如果可以）
```sql
ALTER TABLE app_db.source_table ADD PRIMARY KEY (`id`);
```

#### **2.2 手动指定主键字段**
- **使用 `primaryKeyFields` 参数**：  
  即使表没有定义主键，也可以通过 Flink CDC 的 `primaryKeyFields` 参数指定一个或多个字段作为逻辑主键。  
  **要求**：  
  - 指定的字段在表中必须存在。
  - 字段值在业务逻辑上具有唯一性（例如 `order_id`、`user_id` 等）。
  - 如果字段值不唯一，可能导致数据同步错误。

  **示例配置**：
  ```sql
  CREATE TABLE source_table (
      id INT,
      name STRING,
      ts TIMESTAMP
  ) WITH (
      'connector' = 'mysql-cdc',
      'hostname' = 'localhost',
      'port' = '3306',
      'username' = 'root',
      'password' = '123456',
      'database-name' = 'test_db',
      'table-name' = 'no_pk_table',
      'primaryKeyFields' = 'id'  -- 手动指定主键字段
  );
  ```

#### **2.3 添加伪主键（推荐）**
- **在源表中新增主键字段**：  
  如果源表长期无主键，建议在数据库中添加一个自增字段（如 `id`）作为主键。  
  **优点**：  
  - 符合数据库设计规范。
  - 保证 Flink CDC 的稳定性和数据一致性。

- **使用复合主键**：  
  如果表中没有单一字段能唯一标识行，可以组合多个字段作为复合主键（通过 `primaryKeyFields` 指定多个字段）。

```sql
ALTER TABLE test.users ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY;
```

#### **2.4 使用其他工具辅助**
- **结合 Kafka 或其他消息队列**：  
  如果无法修改源表结构，可以借助外部工具（如 Debezium）捕获全量和增量数据，并将数据发送到 Kafka。Flink 可以从 Kafka 读取数据流，此时主键问题由上游工具处理。


### **3. 例外情况：无主键的全量同步**
- **仅需全量同步**：  
  如果只需要一次性全量读取表数据（无需增量变更），Flink CDC 可以在无主键的表上执行全量同步。此时无需主键，但无法捕获后续的更新或删除操作。


### **4. 注意事项**
- **主键字段的选择**：  
  - 优先选择数据库定义的主键。
  - 如果手动指定，确保字段值在业务逻辑上是唯一的且稳定的（避免频繁更新）。
- **性能影响**：  
  主键字段的索引效率会影响 Flink CDC 的读取性能，建议选择高基数、低更新频率的字段。
- **版本兼容性**：  
  - **Flink CDC 1.x**：强制要求主键，否则无法启动任务。
  - **Flink CDC 2.x**：支持通过 `primaryKeyFields` 指定主键，但底层仍依赖主键逻辑。


### **5. 总结**
| **场景**                | **是否需要主键** | **解决方案**                                                                 |
|-------------------------|------------------|------------------------------------------------------------------------------|
| 增量同步（CDC）         | **必须**         | 手动指定 `primaryKeyFields` 或在源表中添加主键。                              |
| 仅全量同步              | **不需要**       | 直接读取表数据，无需主键。                                                  |
| 无主键且无法修改表结构  | **需要变通**     | 使用 Kafka 等中间件捕获变更，或通过业务逻辑构造唯一标识。                   |

