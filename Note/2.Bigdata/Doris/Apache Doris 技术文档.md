# Apache Doris 技术文档

> **📄 文档说明**：本技术文档内容结合了官方文档与内部实践经验。由于 Apache Doris 版本更新较快，部分细节（如具体工具类名、实验性参数等）可能随版本变动。**建议在执行关键操作前，务必以对应版本的[官方文档](https://doris.apache.org/docs/dev/get-starting/)为准。**

## 一、Doris 简介

Apache Doris 是一款基于 **MPP（大规模并行处理）架构** 的实时分析型数据库，以其**极速查询**和**易于使用**而著称。它能够在海量数据下实现亚秒级的查询响应，同时支持**高并发点查询**与**高吞吐复杂分析**。Doris 适用于报表分析、即席查询、统一数仓构建、数据湖联邦查询加速等场景，可支撑用户行为分析、AB 实验平台、日志检索、用户画像、订单分析等应用。

## 二、核心特点与适用场景

### 1. 主要特点
- **MPP 架构**：分布式并行计算，支持线性扩展。
- **高性能**：列式存储、向量化执行引擎、多种索引。
- **高兼容性**：兼容 MySQL 协议与语法，支持标准 SQL。
- **高可用易运维**：架构简洁（仅 FE、BE 两类进程），通过一致性协议保障服务与数据可靠性。

### 2. 典型应用场景
- **报表分析**：实时看板、企业内部报表、面向客户的高并发报表（如电商广告报表）。
- **即席查询**：分析师自助分析，查询模式灵活，要求高吞吐。
- **统一数仓构建**：简化大数据架构，替代多组件组合。
- **数据湖联邦查询**：通过外表直接查询 Hive、Iceberg、Hudi 中的数据，无需数据迁移。

## 三、技术架构概述

### 1. 系统架构
Doris 采用存算一体架构，仅包含两类进程：
- **Frontend（FE）**：负责元数据管理、查询解析、调度、节点管理。
- **Backend（BE）**：负责数据存储与查询执行。

FE 和 BE 均可横向扩展，支持数百节点、数十 PB 数据量，通过一致性协议实现高可用。

![存算一体架构](https://doris.apache.org/zh-CN/assets/images/apache-doris-technical-overview-b8c5cb11b57d2f6559fa397d9fd0a8a0.png)

### 2. 存储引擎
- **列式存储**：高压缩比，减少 I/O。
- **多种数据模型**：
  - **Aggregate Key**：预聚合，提升查询性能。
  - **Unique Key**：主键唯一，支持行级更新。
  - **Duplicate Key**：冗余模型，保留原始数据，无聚合。

### 3. 索引详解
Doris 提供了丰富的索引结构，以加速数据检索，减少数据扫描量，是支撑其高性能查询的关键。

- **智能排序索引**
  - **复合排序键 (Sorted Compound Key Index)**：在建表时指定一个或多个列作为排序键。数据将按照这些列的顺序在磁盘上物理排序和存储。此索引能高效处理**范围查询**和**前缀匹配查询**，通过快速定位数据块来大幅减少扫描量。例如，以 `(日期, 城市)` 为排序键，查询 `WHERE 日期='2023-10-01' AND 城市='北京'` 会非常高效。

- **索引设计建议**
  - **多列过滤加速**：对于需要多列组合过滤加速的场景，建议通过合理排布**前缀索引**（Prefix Index）的列顺序，或者针对目标列建立**倒排索引**来实现。
  - **空间数据处理**：在处理地理信息系统（GEO）数据时，Doris 倾向于使用基于 Hilbert 曲线的 **S2 Geometry** 库（具有更好的空间连续性）。

- **快速过滤索引**
  - **Min/Max 索引**：自动为数据块（Tablet）内的列创建。它记录了每个数据块中该列的最小值和最大值。在查询时，通过对比查询条件与这些最值，可以快速跳过完全不包含目标数据的数据块，实现高效的数据裁剪。尤其适用于数值型和日期时间型列的范围查询。
  - **Bloom Filter 索引**：适用于对**高基数列**（如用户ID、订单号）进行**等值查询**。它为每个数据块生成一个概率性的数据结构（布隆过滤器）。当进行等值查询时，可以快速判断某个值"肯定不在"某个数据块中，从而跳过该块的扫描，有效减少I/O。

- **全文检索索引**
  - **倒排索引**：主要用于对文本类型的列（如文章内容、商品描述）进行**快速关键字搜索**和**短语查询**。它将文本内容分词，建立从词项到包含该词项的行位置的映射。这使得 `LIKE '%关键词%'` 这类模糊查询性能得到数量级的提升，满足了日志分析、内容搜索等场景的需求。

### 4. 查询引擎
- **高性能特性**：基于列式存储、向量化执行引擎和多种索引技术，提供卓越的查询性能。
- **向量化执行引擎**：所有内存结构按照列式布局，大幅减少虚函数调用、提升Cache命中率，高效利用SIMD(单指令多数据流)指令。在宽表聚合场景下性能是非向量化引擎的5-10倍。

![查询引擎](https://doris.apache.org/zh-CN/assets/images/apache-doris-query-engine-1-9e2beb07704b905a1c44dae1c5b3bd04.png)

- **自适应查询执行 (Adaptive Query Execution, AQE)**：根据运行时统计信息动态调整执行计划，通过Runtime Filter技术能够在运行时生成Filter推到Probe侧，并自动穿透到Probe侧最底层的Scan节点，大幅减少Probe数据量，加速Join性能。Doris的Runtime Filter支持In/Min/Max/Bloom Filter。

![查询引擎](https://doris.apache.org/zh-CN/assets/images/apache-doris-query-engine-2-92a7d1bd709c09e437e90dfedf559803.png)

- **组合优化策略**：采用CBO(基于代价的优化器)和RBO(基于规则的优化器)结合的优化策略，RBO支持常量折叠、子查询改写、谓词下推等，CBO支持Join Reorder。目前CBO还在持续优化中，主要集中在更加精准的统计信息收集和推导，更加精准的代价模型预估等方面。

## 四、部署方式

> **版本说明**：本指南采用 Docker (4.0.1) 进行快速功能测试（尝鲜最新特性），生产集群部署则基于 3.1.3 稳定版（长期支持版）。

### 1. 快速体验（仅开发测试）
使用 Docker Compose 快速启动单节点集群：

```yaml
services:
  fe:
    image: apache/doris:fe-4.0.2
    hostname: fe1       # 显式设置主机名
    ports:
      - "28030:8030"   # FE HTTP
      - "29030:9030"   # FE MySQL
    environment:
      - FE_SERVERS=fe1:172.20.0.10:9010  # 直接使用固定IP
      - FE_ID=1
    networks:
      doris-net:
        ipv4_address: 172.20.0.10  # 固定IP
    healthcheck:  # 添加健康检查确保BE在FE就绪后启动
      test: ["CMD", "curl", "-sf", "http://localhost:8030/api/bootstrap"]
      interval: 5s
      timeout: 10s
      retries: 20
    volumes:
      - /opt/docker_compose/doris/data/fe/doris-meta:/opt/apache-doris/fe/doris-meta  # FE元数据目录

  be:
    image: apache/doris:be-4.0.2
    hostname: be       # 显式设置主机名
    ports:
      - "28040:8040"   # BE HTTP
      - "29050:9050"   # BE 服务端口
    environment:
      - FE_SERVERS=fe1:172.20.0.10:9010  # 使用FE的固定IP
      - BE_ADDR=172.20.0.11:9050  # 使用BE的固定IP
    depends_on:
      fe:
        condition: service_healthy  # 等待FE健康后再启动
    networks:
      doris-net:
        ipv4_address: 172.20.0.11  # 固定IP
    volumes:
      - /opt/docker_compose/doris/data/be/storage:/opt/apache-doris/be/storage  # BE数据存储目录

networks:
  doris-net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/24
```

### 2. 生产集群部署（存算一体）

基于生产高性能部署方案，请参考官网：**[部署前准备](https://doris.apache.org/zh-CN/docs/4.x/install/preparation/env-checking)** 

#### 2.1 操作系统配置（生产环境必须）

在生产环境中部署 Doris 前，需对操作系统进行以下关键配置，以保障系统稳定性和性能：

1. **关闭透明大页（Transparent Huge Pages, THP）**  
   透明大页在某些场景下可能导致内存分配延迟和性能抖动，建议关闭或设置为 `madvise` 模式。

   编辑 `/etc/rc.d/rc.local` 文件，添加以下内容并赋予执行权限：

   ```bash
   cat >> /etc/rc.d/rc.local << EOF
   # 关闭系统透明大页
   echo madvise > /sys/kernel/mm/transparent_hugepage/enabled
   echo madvise > /sys/kernel/mm/transparent_hugepage/defrag
   EOF
   chmod +x /etc/rc.d/rc.local
   ```

   重启系统或手动执行上述 `echo` 命令使配置生效。

2. **优化 TCP 连接参数**  
   避免连接溢出导致性能下降，建议调整 TCP 参数：

   编辑 `/etc/sysctl.conf`，添加：

   ```bash
   cat >> /etc/sysctl.conf << EOF
   # 设置系统自动重置新连接
   net.ipv4.tcp_abort_on_overflow=1
   EOF
   ```

   执行 `sysctl -p` 使配置立即生效。

3. **关闭交换分区（swap）**  
   建议关闭交换分区以避免因内存换出导致的性能抖动：

   ```bash
   swapoff -a
   sed -i '/ swap / s/^/#/' /etc/fstab  # 永久禁用
   ```

4. **时钟同步**  
   所有节点时钟偏差应控制在 5 秒以内，建议使用 NTP 或 Chrony 同步。

   ```bash
   systemctl enable chronyd
   systemctl start chronyd
   ```

5. **文件描述符与进程数限制**  
   建议调整系统文件描述符和进程数限制：

   编辑 `/etc/security/limits.conf`：

   ```bash
   # 调整最大文件句柄数
   * soft nofile 1000000
   * hard nofile 1000000
   ```

#### 2.2 硬件要求

- **FE**：建议 16 核 64GB 以上。建议预留 100GB 以上的存储空间，使用 SSD 硬盘。。
- **BE**：建议 16 核 64GB 以上。存储空间按数据量 × 3（副本）计算，并额外预留 40% 用于压缩和中间数据。

#### 2.3 系统要求

- 支持 CentOS 7.1 及以上、Ubuntu 16.04 及以上。
- 关闭交换分区（swap）。
- 时钟同步（所有节点偏差 ≤ 5 秒）。

#### 2.4 端口规划

Doris 的各个实例通过网络进行通信，其正常运行需要网络环境提供以下端口。

| 实例名称 | 端口名称               | 默认端口 | 通信方向                 | 说明                                                  |
| -------- | ---------------------- | -------- | ------------------------ | ----------------------------------------------------- |
| BE       | be_port                | 9060     | FE -> BE                 | BE 上 Thrift Server 的端口，用于接收来自 FE 的请求    |
| BE       | webserver_port         | 8040     | BE <-> BE                | BE 上的 HTTP Server 端口                              |
| BE       | heartbeat_service_port | 9050     | FE -> BE                 | BE 上的心跳服务端口（Thrift），用于接收来自 FE 的心跳 |
| BE       | brpc_port              | 8060     | FE <-> BE，BE <-> BE     | BE 上的 BRPC 端口，用于 BE 之间的通信                 |
| FE       | http_port              | 8030     | FE <-> FE，Client <-> FE | FE 上的 HTTP Server 端口                              |
| FE       | rpc_port               | 9020     | BE -> FE，FE <-> FE      | FE 上的 Thrift Server 端口，每个 FE 的配置需保持一致  |
| FE       | query_port             | 9030     | Client <-> FE            | FE 上的 MySQL Server 端口                             |
| FE       | edit_log_port          | 9010     | FE <-> FE                | FE 上的 bdbje 通信端口                                |

#### 2.5 部署步骤

1. **下载并解压**：
   
   ```bash
   tar zxvf apache-doris-3.1.3-bin-x64.tar.gz
   cd apache-doris-3.1.3-bin-x64
   ```
   
2. **配置并启动 FE**：
   - 使用软链方式创建元数据目录：
     > **⚠️ 安全提醒**：如果是存量节点迁移，执行 `rm -rf` 前请务必先备份原有的 `doris-meta` 目录。新环境部署可直接删除默认目录并创建软链。
     ```bash
     # 删除原有的目录（请确保已备份或为新环境）
     rm -rf fe/doris-meta
     # 创建软链，数据存放在 /data0/dorisv3/doris-meta
     ln -s /data0/dorisv3/doris-meta fe/doris-meta
     ```
   - 修改 `fe/conf/fe.conf`：
     ```properties
     JAVA_HOME = /opt/java/jdk-17
     priority_networks = 192.168.100.0/24
     
     # 高级配置选项
     # 启用proxy_protocol（用于负载均衡器场景，如Nginx、HAProxy）
     enable_proxy_protocol = true
     
     # 大小写不敏感（兼容MySQL行为）
     lower_case_table_names = 1
     ```
   - 启动 FE：
     ```bash
     ./fe/bin/start_fe.sh --daemon
     ```
   - 验证 FE 状态：
     ```bash
     curl http://127.0.0.1:8030/api/bootstrap
     # 或通过 Web UI 访问 http://fe_ip:8030
     ```
   - 通过 MySQL 客户端连接 FE 后执行 `SHOW FRONTENDS\G;` 查看运行状态，关注以下关键指标：
     - **Alive** 为 `true` 表示节点存活；
     - **Join** 为 `true` 表示节点已加入到集群中，但不代表当前还在集群内（可能已失联）；
     - **IsMaster** 为 `true` 表示当前节点为 Master 节点。

3. **配置并启动 BE**：
   - 使用软链方式创建数据目录：
     > **⚠️ 安全提醒**：存量 BE 节点迁移涉及大量用户数据，操作前必须确认数据已在其他节点有副本或已完成备份。
     ```bash
     # 删除原有的 storage 目录（请确保已备份或为新环境）
     rm -rf be/storage
     # 创建软链，数据存放在 /data0/dorisv3/storage
     ln -s /data0/dorisv3/storage be/storage
     ```
   - 修改 `be/conf/be.conf`：
     ```properties
     JAVA_HOME = /opt/java/jdk-17
     priority_networks = 192.168.100.0/24
     ```
   - **在启动 BE 节点前，需要先在 FE 集群中注册 BE 节点**（通过 MySQL 客户端连接 FE）：
     ```sql
     ALTER SYSTEM ADD BACKEND "be_ip:9050";
     ```
   - 启动 BE：
     ```bash
     ./be/bin/start_be.sh --daemon
     ```
   - 验证 BE 状态，通过 MySQL 客户端执行 `SHOW BACKENDS\G;` 查看状态，关注以下指标：
     - **Alive** 为 true 表示节点存活
     - **TabletNum** 表示该节点上的分片数量，新加入的节点会进行数据均衡，TabletNum 逐渐趋于平均。

4. **集群可用性配置**：
   - **FE 高可用**：部署至少 3 个 Follower（1 Leader + 2 Follower）以实现高可用。添加 Follower：
     ```sql
     ALTER SYSTEM ADD FOLLOWER "follower_ip:9010";
     ```
   - **FE Observer 节点**：注册新的 FE Observer 节点：
     ```sql
     ALTER SYSTEM ADD OBSERVER "observer_ip:9010";
     ```
   - **启动新的 FE 节点**：启动新的 FE Follower 或 Observer 节点时，需要指定 `--helper` 参数指向集群中已有的 FE 节点：
     ```bash
     # helper_fe_ip 是 FE 集群中任何存活节点的 IP 地址
     # --helper 参数仅在第一次启动 FE 时需要，之后重启无需指定。
     bin/start_fe.sh --helper helper_fe_ip:9010 --daemon
     ```
   - **BE 扩展**：添加更多 BE 节点以扩展存储和计算能力，使用相同的 `ALTER SYSTEM ADD BACKEND` 命令。

**重要注意事项**：  
1. 生产环境建议 FE 和 BE 分开部署，避免资源竞争。
2. 建议至少 10 台机器以充分发挥性能（3 台 FE，其余 BE）。
3. 在混合部署时，确保元数据目录和数据目录位于不同磁盘。
4. 每台机器只能部署一个 BE 实例，但可部署一个 FE 实例（Follower 或 Observer）。
5. FE 的磁盘空间主要用于存储元数据，包括日志和图像。通常范围为几百 MB 到几 GB。
6. BE 的磁盘空间主要用于存储用户数据。总磁盘空间需求是用户数据总量（含3副本）的3倍，并额外保留 40% 的空间用于后台压缩和中间数据存储。
7. FE 节点的时钟必须一致（允许最大时钟偏差为5秒）。

## 五、基本操作与 SQL 实践

### 1. 创建数据库与表
```sql
CREATE DATABASE demo;
USE demo;

-- Aggregate Key 模型（预聚合）
CREATE TABLE example_tbl (
    user_id LARGEINT NOT NULL,
    date DATE NOT NULL,
    city VARCHAR(20),
    age SMALLINT,
    sex TINYINT,
    last_visit_datetime DATETIME REPLACE DEFAULT '1970-01-01 00:00:00',
    cost BIGINT SUM DEFAULT '0',
    max_dwell_time INT MAX DEFAULT '0',
    min_dwell_time INT MIN DEFAULT '99999'
)
AGGREGATE KEY(user_id, date, city, age, sex)
DISTRIBUTED BY HASH(user_id) BUCKETS 10 -- 生产环境建议根据数据量和集群规模调整分桶数
PROPERTIES ("replication_allocation" = "tag.location.default: 3"); -- 生产环境强制建议 3 副本
```

### 2. 数据导入（Stream Load）
将数据文件 `test.csv` 导入：
```bash
curl --location-trusted -u root: -T test.csv -H "column_separator:," \
     http://127.0.0.1:8030/api/demo/example_tbl/_stream_load
```

### 3. 其他表模型示例
- **Unique Key 模型**（数据覆盖）：
  ```sql
  CREATE TABLE user (
      user_id LARGEINT NOT NULL,
      username VARCHAR(50) NOT NULL,
      ...
  ) UNIQUE KEY(user_id, username)
  DISTRIBUTED BY HASH(user_id) BUCKETS 10;
  ```
- **Duplicate Key 模型**（保留全部数据）：
  ```sql
  CREATE TABLE example_log (
      timestamp DATETIME NOT NULL,
      type INT NOT NULL,
      ...
  ) DUPLICATE KEY(timestamp, type)
  DISTRIBUTED BY HASH(timestamp) BUCKETS 10;
  ```

### 4. 系统变量配置

Doris 支持通过系统变量来配置特定功能，例如启用 Unicode 字符支持：

```sql
-- 允许在对象名字里使用 Unicode 字符（多语言字符）
SET GLOBAL enable_unicode_name_support = true;
```

该配置允许在数据库、表、列等对象的命名中使用 Unicode 字符，包括中文及其他语言字符。

## 六、数据导入机制

Apache Doris 提供了多种[数据导入](https://doris.apache.org/zh-CN/docs/4.x/data-operate/import/load-manual)方式，以适应不同的数据源（本地文件、HDFS/S3、Kafka、MySQL 等）和同步需求（离线批量、实时流式）。

### 1. 导入方式选型指南

| 导入方式 | 核心场景 | 同步/异步 | 适用数据量 | 数据源 |
| --- | --- | --- | --- | --- |
| **Stream Load** | 实时微批导入、文本日志推送 | 同步 | MB ~ GB 级别 | 本地文件、内存流 |
| **Broker Load** | 离线超大数据量迁移、数仓历史数据初始化 | 异步 | GB ~ PB 级别 | HDFS, S3, OSS |
| **Routine Load** | 自动订阅消费流式数据 | 异步(自动) | 持续流式 | Kafka |
| **Insert Into** | 内部 ETL、清洗表数据 | 同步 | 视 SQL 而定 | Doris 内部表、JDBC 外表 |
| **Flink Connector** | **整库同步**、CDC 实时入仓、流计算结果写入 | 异步 (Checkpoint) | 持续流式 | Flink, CDC |

### 2. Stream Load（本地/实时同步导入）

Stream Load 是一种基于 HTTP 协议的同步导入方式。用户通过 HTTP 协议发送请求将本地文件或数据流导入到 Doris 中，主要用于**微批导入**或**流式框架写入**。

**基本语法 (Curl 示例)：**

```bash
# 将本地文件 data.csv 导入到 test_db 的 test_table 中
curl --location-trusted -u user:password \
    -H "Expect:100-continue" \
    -H "column_separator:," \
    -H "columns: user_id, user_name, age" \
    -T data.csv \
    http://<fe_ip>:8030/api/test_db/test_table/_stream_load
```

**关键特性：**

* **原子性**：单次导入事务要么全部成功，要么全部失败。
* **同步返回**：请求返回即代表导入结果（Success 或 Fail）。
* **Label 机制**：建议在 Header 中指定 `-H "label:unique_label_id"`，Doris 保证相同 Label 的请求只会被执行一次，用于实现**幂等性**（防止重复消费）。

### 3. Broker Load（远端批量异步导入）

Broker Load 用于从远端存储系统（如 HDFS、S3、BOS 等）通过 Broker 进程读取数据并导入 Doris。适用于**历史数据的大规模迁移**。

> **注**：在较新版本中，Doris 支持不部署 Broker 进程直接读取 S3/HDFS，但 SQL 语法仍沿用 `WITH BROKER` 关键字或使用 `WITH S3`。

**使用示例：**

```sql
-- 从 HDFS 导入数据，前提先创建表
LOAD LABEL hive_physicalization.dw_dws_person
(
    DATA INFILE("hdfs://master01:8020/user/hive/warehouse/dw/person/dt=2025-05-22/*")
    INTO TABLE dw_dws_person
    COLUMNS TERMINATED BY "\t"
    FORMAT AS "orc"
    (user_id, date, city, age, sex)
)
with HDFS (
    "fs.defaultFS"="hdfs://master01:8020",
    "hadoop.username"="hdfs"
)
PROPERTIES
(
    "timeout"="1200",
    "max_filter_ratio"="0.1"
);
```

**查看进度：**
由于是异步导入，提交 SQL 后会立即返回。需使用命令查看状态：

```sql
SHOW LOAD WHERE LABEL = 'label_20231001'\G;
```

### 4. Routine Load（Kafka 自动订阅）

Routine Load 能够持续消费 Kafka 中的消息并导入 Doris。用户提交一个作业后，Doris 会在后台不间断地运行导入任务。这是实现 **"Log -> Kafka -> Doris"** 实时数仓链路的核心功能。

**创建任务示例：**

```sql
CREATE ROUTINE LOAD test_db.kafka_job_1 ON example_tbl
COLUMNS(user_id, date, city, age, sex),
WHERE age > 18 -- 支持简单的过滤
PROPERTIES (
    "desired_concurrent_number" = "3",  -- 并发度
    "max_batch_interval" = "20",        -- 批次间隔时间(秒)
    "max_batch_rows" = "300000",        -- 批次最大行数
    "format" = "json",                  -- 源数据格式
    "jsonpaths" = "[\"$.user_id\",\"$.date\",\"$.city\",\"$.age\",\"$.sex\"]" -- JSON 映射
)
FROM KAFKA (
    "kafka_broker_list" = "192.168.1.10:9092",
    "kafka_topic" = "user_behavior_topic",
    "property.group.id" = "doris_consumer_group"
);
```

**运维命令：**

* **查看任务状态**：`SHOW ROUTINE LOAD;`
* **暂停任务**：`PAUSE ROUTINE LOAD FOR kafka_job_1;`
* **恢复任务**：`RESUME ROUTINE LOAD FOR kafka_job_1;`

### 5. Insert Into（内部 ETL）

类似于标准 SQL 的 `INSERT INTO`，Doris 支持从一张表查询数据并写入另一张表，常用于**数据清洗**、**宽表构建**或**从外表（MySQL/Hive/Iceberg）同步数据**。

```sql
-- 1. 从 ODS 层表清洗数据写入 DWD 层表
INSERT INTO dwd_user_log
SELECT user_id, date, city 
FROM ods_raw_log 
WHERE date = '2023-10-01';

-- 2. 配合 TVF (Table Value Function) 直接读取 HDFS 文件写入
INSERT INTO test_table 
SELECT * FROM HDFS(
    "uri" = "hdfs://namenode:9000/user/data/file.parquet",
    "format" = "parquet"
);

-- 3. 通过 ctas(create table as select) 导入
CREATE TABLE `internal`.`hz_venue_data_governance`.`vb_belong_record`
PROPERTIES('replication_num' = '1')
AS
SELECT * FROM `mysql_catalog`.`db_mingyang_venue_booking2.0`.`vb_belong_record`;
```

### 6. Flink Doris Connector (整库同步与 CDC)

这是目前实时数仓最推荐的方案。其内置集成了 Flink CDC 特性，通过 [Flink Doris Connector](https://doris.apache.org/zh-CN/docs/ecosystem/flink-doris-connector) 可以实现极简的入仓链路。

> **🔔 实践说明**：以下展示的 Jar 包版本、类名及命令行参数均基于 **Flink 官方 CDC 文档**与 **Doris Flink Connector 文档**的组合实践示例，建议在使用时根据具体版本环境进行微调。

#### (1) 支持范围

目前整库同步方案支持的数据源包括：**MySQL、Oracle、PostgreSQL、SQLServer、MongoDB、DB2**。

> **注意**：前提是相关数据源必须开启 CDC（Change Data Capture）特性。

#### (2) 环境准备与依赖配置

在使用前，需将相关数据库的驱动和 CDC 连接器放置在 `$FLINK_HOME/lib` 下：

* **核心包**：`flink-doris-connector-1.20-25.1.0.jar`
* **MySQL**：`mysql-connector-j-8.4.0.jar`、`flink-sql-connector-mysql-cdc-3.4.0.jar`
* **Postgres**：`postgresql-42.7.8.jar` (推荐 > 42.5.x)、`flink-sql-connector-postgres-cdc-3.4.0.jar`
* **SQLServer**：`mssql-jdbc-13.2.1.jre8.jar`、`flink-sql-connector-sqlserver-cdc-3.4.0.jar`

#### (3) 数据源开启 CDC 特性

* **MySQL 要求**：
在 `my.cnf` 配置文件中开启 Binlog，格式必须为 `ROW`：
```ini
[mysqld]
server-id=1
log_bin=mysql-bin
binlog_format=ROW
binlog_row_image=FULL
expire_logs_days=7
```


*配置完成后需重启服务。*
* **PostgreSQL 要求**：
1. 开启逻辑复制（Logical Decoding）：
```sql
ALTER SYSTEM SET wal_level = 'logical';
```

*配置完成后需重启服务。*
2. **类型限制**：目前支持 `boolean/int/decimal/json/array` 等类型，**不支持** `point/geography/vector` 等类型。


#### (4) 运行整库同步命令（以 MySQL 为例）

通过 `CdcTools` 可以无需编码，一键同步整个数据库到 Doris：

```bash
/opt/flink/current/bin/flink run -d \
    -Dexecution.checkpointing.interval=10s \
    -Dparallelism.default=1 \
    -c org.apache.doris.flink.tools.cdc.CdcTools \
    /opt/flink/current/lib/flink-doris-connector-1.20-25.1.0.jar \
    mysql-sync-database \
    --database test_doris \
    --table-prefix pay_ \
    --mysql-conf hostname=mysql_host \
    --mysql-conf port=3306 \
    --mysql-conf username=root \
    --mysql-conf password=123456 \
    --mysql-conf database-name=test_pay \
    --including-tables ".*" \
    --sink-conf fenodes=doris_fe_nodes \
    --sink-conf username=root \
    --sink-conf password="123456" \
    --sink-conf jdbc-url=jdbc:mysql://doris_fe_nodes:9030 \
    --sink-conf sink.label-prefix=label \
    --table-conf replication_num=1
```

**关键技术细节：**

* **Checkpoint**：整库同步必须开启 Flink Checkpoint（如上文 `-D` 参数设置），否则无法利用 Doris 的两阶段提交保障数据一致性。
* **Label 机制**：通过 `sink.label-prefix` 指定前缀，防止任务重启导致的 Label 冲突。
* **多源支持**：若数据源为 SQLServer 或 Oracle，只需将命令中的 `mysql-sync-database` 替换为相应的同步工具类（如 `sqlserver-sync-database`），并更新 `--conf` 对应的连接参数即可。

---

## 七、系统监控与任务管理

Apache Doris 提供了多维度的监控体系，包括**实时任务查看**（SQL 级别）、**指标监控**（Prometheus + Grafana）以及**审计日志**（Audit Log）。

### 1. 实时任务与作业管理

通过 SQL 接口，运维人员可以实时查看集群中正在运行的查询、导入或表结构变更任务。

#### 1.1 查询监控 (Query Monitor)

* **查看当前正在执行的 SQL**：
```sql
SELECT * FROM information_schema.active_queries;

```


*返回字段包含：QueryId, StartTime, TimeMs (耗时), Sql (部分截断), Database, User 等。*
* **查看 BE 节点资源负载**：
```sql
SELECT * FROM information_schema.backend_active_tasks;

```


*可查看具体某个 BE 节点上正在消耗 CPU/内存的任务详情。*

#### 1.2 导入任务监控 (Load Monitor)

* **批量导入 (Broker/Spark Load)**：
```sql
-- 查看正在进行中的导入
SHOW LOAD WHERE STATE = "LOADING";
-- 查看指定 Label 的导入结果
SHOW LOAD WHERE LABEL = "label_20231001";

```


* **例行导入 (Routine Load)**：
```sql
-- 查看作业整体状态 (Running/Paused) 及消费进度
SHOW ROUTINE LOAD;
-- 查看当前正在执行的子任务详情 (排查卡顿用)
SHOW ROUTINE LOAD TASK WHERE JobName = "your_job_name";

```



#### 1.3 表结构变更监控 (Schema Change)

查看 `ALTER TABLE`（如加减列）或 `ROLLUP` 创建进度：

```sql
SHOW ALTER TABLE COLUMN;  -- 查看列变更
SHOW ALTER TABLE ROLLUP;  -- 查看 Rollup 进度

```

### 2. Prometheus + Grafana 监控平台

Doris 的 FE 和 BE 进程均内置了 HTTP Server，直接以 Prometheus 标准格式暴露监控指标（Metrics），无需安装额外的 Exporter。

#### 2.1 监控架构

* **监控数据源**：
* **FE 指标**：`http://<fe_ip>:8030/metrics` (默认 http_port)
* **BE 指标**：`http://<be_ip>:8040/metrics` (默认 webserver_port)


* **数据流向**：Doris (Metrics) -> Prometheus (Pull) -> Grafana (Dashboard)

#### 2.2 Prometheus 配置 (`prometheus.yml`)

在 Prometheus 配置文件中添加 Doris 的抓取任务（Job）：

```yaml
scrape_configs:
  # 抓取 FE 指标
  - job_name: 'doris_fe'
    metrics_path: '/metrics'
    static_configs:
      - targets: ['fe_host1:8030', 'fe_host2:8030', 'fe_host3:8030']
        labels:
          group: 'fe'

  # 抓取 BE 指标
  - job_name: 'doris_be'
    metrics_path: '/metrics'
    static_configs:
      - targets: ['be_host1:8040', 'be_host2:8040', 'be_host3:8040']
        labels:
          group: 'be'

```

*配置完成后重启 Prometheus 服务。*

#### 2.3 Grafana 面板配置

Apache Doris 官方提供了适配的 Grafana Dashboard 模板，涵盖了集群概览、查询性能、导入吞吐、Compaction 状态等关键指标。

1. **下载模板**：访问 [Doris 监控和报警](https://doris.apache.org/zh-CN/docs/4.x/admin-manual/maint-monitor/monitor-alert) 下载 JSON 文件。
2. **导入 Grafana**：
* Log in Grafana -> Dashboards -> New -> Import。
* 上传 JSON 文件或粘贴 JSON 内容。
* 选择对应的 Prometheus 数据源。



**关键监控指标推荐：**

* **Query Rate (QPS)**：集群每秒查询数。
* **Query 99th Latency**：查询延迟的 P99 分位值。
* **Import Rows/s**：实时数据导入速率。
* **Compaction Score**：如果该分数持续过高（>100），说明数据版本积压，可能会影响查询性能。
* **BE Memory**：重点关注 Memtable 内存使用情况，防止 OOM。

### 3. 查询审计与慢查询分析 (Audit Loader)

Doris 默认将审计日志（包含所有 SQL 的执行详情、耗时、扫描行数等）输出到 `fe/log/fe.audit.log` 文件中。为了便于分析，推荐使用 **Audit Loader** 插件将这些日志实时入库到 Doris 内部表中。

#### 3.1 方案优势

将日志入库后，可以通过 SQL 直接进行多维分析，例如：

* 找出过去 1 小时最慢的 10 个查询。
* 统计访问最频繁的表。
* 分析某个用户的查询模式。

#### 3.2 部署简述

1. **创建审计表**：在 Doris 中创建用于存储日志的表（`doris_audit_db__` 库）。
2. **配置插件**：下载并配置 `audit_loader` 插件（通常集成在 FE 中）。
3. **开启功能**：在 `fe.conf` 中配置 `enable_audit_plugin = true`。

#### 3.3 慢查询分析示例

一旦日志入库，即可使用如下 SQL 分析慢查询：

```sql
-- 查询最近 1 小时耗时超过 5 秒的 SQL
SELECT 
    query_id, 
    user, 
    time AS exec_time_ms, 
    scan_rows, 
    scan_bytes, 
    stmt 
FROM doris_audit_db__.doris_audit_log_tbl 
WHERE time > 5000 
  AND timestamp > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY time DESC 
LIMIT 10;

```

---

## 八、数据联邦查询 (Multi-Catalog)

Apache Doris 的 Multi-Catalog 功能是实现**湖仓一体（Lakehouse）**的核心。它允许 Doris 直接挂载外部数据源的元数据，无需搬运数据即可实现对异构数据源（Hive, MySQL, Iceberg 等）的跨库联邦分析。

### 1. Catalog 类型与驱动要求

根据数据源的不同，Catalog 主要分为两类：

| Catalog 类型 | 适用数据源 | 驱动要求 | 核心参数 |
| --- | --- | --- | --- |
| **JDBC 类** | MySQL, PG, SQL Server, ClickHouse, Oracle, OceanBase 等 | **必须**提供对应 JDBC 驱动 Jar 包 | `jdbc_url`, `driver_class` |
| **HMS/Lakehouse 类** | Hive, Hudi, Iceberg, Paimon 等 | **不需要**额外提供驱动包 | `hive.metastore.uris` |

#### (1) JDBC Catalog 配置要点

JDBC 类 Catalog 必须通过 `driver_url` 指定驱动。

* **驱动存放位置**：
* 预放在 FE/BE 的 `jdbc_drivers_dir` 目录下（配置后仅需写文件名）。
* 使用 `file:///` 绝对路径或 `http://` 远程下载地址。


**创建示例：**

```sql
-- MySQL
CREATE CATALOG mysql_catalog PROPERTIES (
    'type' = 'jdbc',
    'user' = 'username',
    'password' = 'pwd',
    'jdbc_url' = 'jdbc:mysql://host:3306',
    'driver_url' = 'mysql-connector-j-8.4.0.jar',
    'driver_class' = 'com.mysql.cj.jdbc.Driver'
);

-- SQL Server
CREATE CATALOG sqlserver_catalog PROPERTIES (
    'type' = 'jdbc',
    'user' = 'sa',
    'password' = 'pwd',
    'jdbc_url' = 'jdbc:sqlserver://host:1433;databaseName=test;encrypt=false',
    -- 官方推荐驱动 >= 11.2.x
    'driver_url' = 'mssql-jdbc-11.2.3.jre17.jar',
    'driver_class' = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
);
```

#### (2) HMS / Lakehouse Catalog 配置要点

此类通过 Hive Metastore 访问，只需填好元数据服务和 HDFS 存储配置，无需 JDBC 驱动。

**创建示例：**

```sql
CREATE CATALOG hive_catalog PROPERTIES (
    "type" = "hms",
    "ipc.client.fallback-to-simple-auth-allowed" = "true",
    "hive.metastore.uris" = "thrift://master02:9083",
    "hadoop.username" = "doris",
    -- HDFS 高可用配置 (可选)
    "dfs.nameservices" = "nameservice1",
    "dfs.namenode.rpc-address.nameservice1.master03" = "master03:8020",
    "dfs.namenode.rpc-address.nameservice1.master01" = "master01:8020",
    "dfs.ha.namenodes.nameservice1" = "master01,master03",
    "dfs.client.failover.proxy.provider.nameservice1" = "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider"
);
```

---

### 2. 联邦查询实战

#### (1) 基本查询操作

使用 `SWITCH` 关键字或三段式路径（`catalog.db.table`）进行查询：

```sql
-- 方式 1：切换 Catalog
SWITCH hive_catalog;
USE db_name;
SELECT * FROM table_name LIMIT 10;

-- 方式 2：三段式全路径直接查询
SELECT * FROM mysql_catalog.db_name.table_name LIMIT 10;
```

#### (2) 跨数据源跨 Catalog JOIN (核心场景)

无需数据迁移，直接在一条 SQL 中关联 Hive 中的历史事实表与 MySQL 中的实时维度表：

```sql
-- 跨 Catalog 关联查询示例
SELECT 
    h.user_id, 
    m.user_name, 
    SUM(h.order_amount) 
FROM hive_catalog.dw.fact_orders h
JOIN mysql_catalog.crm.dim_users m ON h.user_id = m.id
WHERE h.dt = '2023-10-01'
GROUP BY h.user_id, m.user_name;
```

---

### 3. 进阶应用：联邦查询加速与定时入湖

#### (1) 异步物化视图加速

针对联邦查询延迟较高的情况，可以基于 Catalog 创建**异步物化视图**，将查询结果缓存到 Doris 内部以获得亚秒级响应。

```sql
CREATE MATERIALIZED VIEW mv_federated_summary
BUILD DEFERRED
REFRESH AUTO ON SCHEDULE EVERY 1 HOUR
AS 
SELECT h.id, m.name FROM hive_catalog.db.table1 h JOIN mysql_catalog.db.table2 m ON h.id = m.id;
```

#### (2) 配合 Job Scheduler 定时入湖

利用 Doris 内置的任务调度器，可以定期将联邦查询结果写入湖仓（如 Iceberg）：

```sql
CREATE JOB schedule_sync_to_iceberg
ON SCHEDULE EVERY 1 HOUR DO
INSERT INTO iceberg_catalog.target_db.ice_table
SELECT h.id, m.name
FROM hive_catalog.source_db.hive_table h
JOIN mysql_catalog.source_db.mysql_table m ON h.id = m.id;
```

---

### 总结

* **JDBC Catalog**：访问数据库类（MySQL/Oracle等），**需要驱动**。
* **HMS Catalog**：访问湖仓类（Hive/Iceberg等），**不需要驱动**，需元数据配置。
* **多 Catalog + 一条 SQL**：即可实现零成本的跨异构数据源联邦分析。

---


## 九、生产部署注意事项

1. **节点规划**：
   - FE：至少 1 个 Follower（生产建议 3 Follower + 1~3 Observer 保障 HA）。
   - BE：至少 3 个实例（保障数据 3 副本）。每台机器建议只部署一个 BE。

2. **磁盘与内存**：
   - FE：存储元数据，需几百 MB 到几 GB。
   - BE：存储用户数据，预留总数据量 × 3（副本）× 1.4（压缩与中间数据）的空间。

3. **网络与时钟**：
   - 所有节点需在同一网络域，时钟偏差不超过 5 秒。

4. **高可用**：
   - 多个 FE 时需配置选举与元数据同步。
   - BE 数据自动多副本分布。

5. **BE 节点下线操作（重要安全提示）**：

   - **✅ 推荐做法（生产环境首选）**：使用 `DECOMMISSION BACKEND` 命令
     ```sql
     ALTER SYSTEM DECOMMISSION BACKEND "127.0.0.1:9050";
     ```
     该命令会自动执行以下安全流程：
     1. **数据迁移**：自动将该 BE 上的数据分片（Tablets）安全迁移到其他存活的 BE 节点。
     2. **自动下线**：待数据迁移进度达到 100% 后，系统会自动将该节点从集群中移除。
     3. **零丢失保障**：确保在缩容过程中数据副本数不降低，业务不受影响。
     
     > **监控进度**：执行后可通过 `SHOW BACKENDS;` 或 `SHOW PROC '/backends'\G` 查看 `SystemDecommissioned` 状态。

   - **⚠️ 危险操作警告：关于 DROP/DROPP**
     当需要强制移除损坏且无法恢复的节点时，需谨慎对待以下命令：
     - **错误做法**：`ALTER SYSTEM DROP BACKEND`。Doris 为了防止误操作，会拒绝执行该命令并提示使用 `DROPP`。
     - **强制删除（仅限紧急情况）**：
       ```sql
       ALTER SYSTEM DROPP BACKEND "127.0.0.1:9050";
       ```
       **注意**：`DROPP` 会立即切断元数据联系并丢弃该节点数据，**不会进行数据迁移**。仅在节点物理损坏无法启动且 `DECOMMISSION` 无法进行时使用。



## 十、集群升级指南

本章介绍从旧版本（以 3.0.8 为例）升级到新版本（以 3.1.3 为例）的标准流程。Doris 采用**滚动升级**的方式，确保业务在升级过程中不中断。

> **⚠️ 升级路径原则（经验做法）**：
> 1. **严禁跨大版本跳级**：例如不建议直接从 2.x 升级到 3.2.x。
> 2. **逐级平滑迁移**：建议遵循 `2.x -> 3.0 -> 3.1 -> 3.2 -> ...` 的平滑执行路径。*注意：具体升级路径请参考各版本的 Release Notes。*
> 3. **顺序要求**：始终遵循 **"先升级所有 BE，再升级所有 FE"** 的严格顺序。

Doris 官方[集群升级文档](https://doris.apache.org/zh-CN/docs/4.x/admin-manual/cluster-management/upgrade)提供了更多细节。

### 1. 环境变量准备

在开始之前，建议在操作终端设置好环境变量，以便后续命令复用（请根据实际目录修改路径）：

```bash
export DORIS_OLD_HOME=/opt/doris/apache-doris-3.0.8-bin-x64
export DORIS_NEW_HOME=/opt/doris/apache-doris-3.1.3-bin-x64
```


### 2. 预升级检查：元数据兼容性测试

> **⚠️ 重要**：在正式升级生产环境前，强烈建议先验证新版本对旧版本元数据的兼容性。

1. **复制元数据**：通过 `SHOW FRONTENDS` 找到 Master FE 节点，将旧集群的元数据目录复制到新版本的目录下。

   ```bash
   cp -r ${DORIS_OLD_HOME}/fe/doris-meta ${DORIS_NEW_HOME}/fe/doris-meta
   ```

2. **修改测试端口**：为了避免与运行中的服务冲突，修改新版本的 `fe.conf` 端口配置：

   ```properties
   JAVA_HOME=/opt/java/jdk-17/
   
   ## 修改端口避免冲突
   http_port = 18030
   rpc_port = 19020
   query_port = 19030
   arrow_flight_sql_port = 19040
   edit_log_port = 19010
   ```

3. **启动测试 FE**：使用元数据故障恢复模式启动：
> **🛑 安全提示**：官方文档明确说明 **"metadata recovery mode" (--metadata_failure_recovery)** 的使用存在风险，误用可能导致不可逆的数据损坏。非必要情况下，**不建议普通用户自行操作此模式**。请优先参考官方的“集群升级”流程进行验证。

   ```bash
   sh ${DORIS_NEW_HOME}/bin/start_fe.sh --daemon --metadata_failure_recovery
   ```

1. **验证状态**：

   ```bash
   mysql -uroot -P19030 -h127.0.0.1
   ```

   如果能成功连接并查询到表结构信息，说明元数据兼容性测试通过。测试完成后请清理测试进程。

### 3. 正式升级准备

在开始滚动升级前，需关闭集群的自动均衡策略，以避免升级过程中不必要的数据迁移。

通过 MySQL 客户端连接 FE 节点执行：

```sql
admin set frontend config("disable_balance" = "true");
admin set frontend config("disable_colocate_balance" = "true");
admin set frontend config("disable_tablet_scheduler" = "true");
```

### 4. 升级 BE 节点

**原则**：先升级 BE，再升级 FE。BE 节点可逐台进行升级（滚动升级）。

对于每一台 BE 节点，执行以下步骤：

1. **停止 BE 服务**

   ```bash
   sh ${DORIS_OLD_HOME}/be/bin/stop_be.sh
   ```

2. **备份数据目录** (建议操作)

   ```bash
   # 假设数据存储在 /data0/doris/storage
   cp -r /data0/doris/storage /data0/doris/storage_bak_v3.0.8
   ```

3. **替换程序文件** (保留配置，替换 bin 和 lib)

   ```bash
   # 备份旧版本的 bin 和 lib
   mv ${DORIS_OLD_HOME}/be/bin ${DORIS_OLD_HOME}/be/bin_bak_v3.0.8
   mv ${DORIS_OLD_HOME}/be/lib ${DORIS_OLD_HOME}/be/lib_bak_v3.0.8
   
   # 复制新版本的 bin 和 lib 到运行目录
   cp -r ${DORIS_NEW_HOME}/be/bin ${DORIS_OLD_HOME}/be/bin
   cp -r ${DORIS_NEW_HOME}/be/lib ${DORIS_OLD_HOME}/be/lib
   ```

4. **启动 BE 节点**

   ```bash
   sh ${DORIS_OLD_HOME}/be/bin/start_be.sh --daemon
   ```

5. **验证**：通过 `SHOW BACKENDS` 确认该节点 `Alive` 状态为 `true` 且版本号已更新，再进行下一台 BE 的升级。

### 5. 升级 FE 节点

**原则**：先升级非 Master 节点 (Follower/Observer)，最后升级 Master 节点。

对于每一台 FE 节点，执行以下步骤：

1. **停止 FE 服务**

   ```bash
   sh ${DORIS_OLD_HOME}/fe/bin/stop_fe.sh
   ```

2. **备份元数据** (关键步骤)

   ```bash
   cp -r ${DORIS_OLD_HOME}/fe/doris-meta ${DORIS_OLD_HOME}/fe/doris-meta_bak_v3.0.8
   ```

3. **替换程序文件**

   ```bash
   # 备份旧目录
   mv ${DORIS_OLD_HOME}/fe/bin ${DORIS_OLD_HOME}/fe/bin_bak_v3.0.8
   mv ${DORIS_OLD_HOME}/fe/lib ${DORIS_OLD_HOME}/fe/lib_bak_v3.0.8
   # 如果有配置 SSL，也需处理证书目录
   mv ${DORIS_OLD_HOME}/fe/mysql_ssl_default_certificate ${DORIS_OLD_HOME}/fe/mysql_ssl_default_certificate_bak_v3.0.8
   
   # 复制新目录
   cp -r ${DORIS_NEW_HOME}/fe/bin ${DORIS_OLD_HOME}/fe/bin
   cp -r ${DORIS_NEW_HOME}/fe/lib ${DORIS_OLD_HOME}/fe/lib
   cp -r ${DORIS_NEW_HOME}/fe/mysql_ssl_default_certificate ${DORIS_OLD_HOME}/fe/mysql_ssl_default_certificate
   ```

4. **启动 FE 节点**

   ```bash
   sh ${DORIS_OLD_HOME}/fe/bin/start_fe.sh --daemon
   ```

5. **验证**：连接集群执行 `SHOW FRONTENDS\G`。

   - 确保 `Alive` 为 `true`。
   - 确保 `Version` 为新版本。
   - 确认无误后，继续升级下一台 FE。

### 6. 升级后收尾

当所有 BE 和 FE 节点都升级完成，并且状态均为 `Alive` 后，打开集群副本修复和均衡功能。

通过 MySQL 客户端连接 FE 节点执行：

```sql
admin set frontend config("disable_balance" = "false");
admin set frontend config("disable_colocate_balance" = "false");
admin set frontend config("disable_tablet_scheduler" = "false");
```

至此，集群升级完成。




[Doris MCP + Dify](https://mp.weixin.qq.com/s/O8bTYJYeBs2-UayyGrIl_w)
