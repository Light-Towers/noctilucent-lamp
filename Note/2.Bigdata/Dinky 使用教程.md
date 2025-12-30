>本文档使用 Dinky 1.2.3 、Flink 1.20

Dinky 是一个以 Apache Flink 为内核构建的开源实时计算平台，具备实时应用的作业开发、数据调试及运行监控能力，助力实时计算高效应用。

### **一、安装部署**

#### 1. **环境准备**
- **依赖**：JDK 8+、MySQL 5.7+、Flink 1.14+
- **下载地址**：[GitHub Release](https://github.com/DataLinkDC/dinky/releases)（选择 `dinky-release-{version}.tar.gz`）

#### 2. **初始化数据库**
Dinky 采用 mysql 作为后端的存储库，mysql 支持 5.7+。Dinky使用Flyway进行数据库版本管理，在第一次部署时，无需手动建表， Flyway 会自动完成。只需配置好数据库连接信息，其他配置文件默认即可。
```sql
-- Mysql-8.X
-- 创建数据库
CREATE DATABASE dinky;
-- 创建用户并允许远程登录
create user 'dinky'@'%' IDENTIFIED WITH mysql_native_password by 'dinky';
-- 授权
grant ALL PRIVILEGES ON dinky.* to 'dinky'@'%';
flush privileges;
```

#### 3. **修改配置文件**
- 编辑 `config/application.yml`，选择数据源为 mysql：
```yaml
  spring:
    application:
      name: Dinky
    profiles:
      active: mysql
```

- 编辑 `config/application-mysql.yml`：
```yaml
  spring:
    datasource:
      url: jdbc:mysql://<mysql_ip>:3306/dinky?useSSL=false
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 4. **加载依赖**
- 将 Flink 的 `lib/` 目录下所有 JAR 包复制到 `dinky/extends/flink<version>/`。
- 将 mysql-connector-java-xxx.jar 复制到 `dinky/extends/` 目录。
- **Hadoop 支持**：添加 `flink-shaded-hadoop-3-uber-*.jar` 到 `extends/` 目录（YARN 模式必需）。
- 由于 CDCSOURCE 是 Dinky 封装的新功能，Apache Flink 源码不包含，非 Application 模式提交需要在远程 Flink 集群所使用的依赖里添加一下依赖。将 Dinky 整库同步依赖包放置 $FLINK_HOME/lib下：
  ```bash
  lib/dinky-client-base-<dinky-version>.jar
  lib/dinky-common-<dinky-version>.jar
  extends/flink<version>/dinky/dinky-client-<dinky-version>.jar
  ```

> 🔥注意事项
> 1. Dinky 并没有内置的 mysql/postgres 数据库驱动，需要用户自己上传 mysql-connector-java-xxx.jar/postgresql-xxx.jar 等jdbc 驱动到 `lib 下`或者`extends 下`
> 2. Flink自带lib里的planner是带loader的,比如:flink-table-planner-loader-<version>.jar, 需要删除带loader的jar包，换一个不带loader的jar。
> 3. Dinky 当前版本的 yarn 的 per-job 与 application 执行模式依赖 flink-shaded-hadoop ，需要额外添加 flink-shaded-hadoop-uber 包，如果使用的是`flink-shaded-hadoop-uber-3`请手动删除该包内部的 javax.servlet 等冲突内容。 当然如果 Hadoop 为 3+ 也可以自行编译对于版本的 dinky-client-hadoop.jar 以替代 uber 包。


#### 5. **启动服务**
```bash
sh auto.sh start 1.20  # 指定 Flink 版本
```
- 访问 `http://<server_ip>:8888`，默认账号 `admin/dinky123!@#`。

---

### **二、核心功能配置**
#### 1. **集群管理**
- **Flink 集群注册**：
  - **Standalone/YARN**：在 `注册中心 > 集群 > Flink实例/集群配置` 添加 Flink REST 地址。

#### 2. **数据源配置**
在 `注册中心 > 数据源` 管理数据源配置，目前支持的数据源类型有：
- **OLTP**
  - MySQL
  - Oracle
  - PostgreSQLSQLServer
  - Phoenix
- **OLAP**
  - ClickHouse
  - Doris
  - StarRocks
  - Presto
  - Paimon
- **DataWarehouse/DataLake**
  - Hive

---

### **三、任务开发与提交**
#### 1. **Flink SQL 开发**
- **示例：整库同步（MySQL → Doris）**
  ```sql
  EXECUTE CDCSOURCE demo_doris WITH (
    'connector' = 'mysql-cdc',
<<<<<<< HEAD
    'hostname' = '192.168.100.xx',
=======
    'hostname' = '192.168.0.xx',
>>>>>>> 3c4181f7e07feb1892a8b680b63799f5f5dfde4c
    'port' = '3306',
    'username' = 'root',
    'password' = '123456',
    'checkpoint' = '10000',
    'scan.startup.mode' = 'initial',
    'parallelism' = '1',
    'table-name' = 'app_db\.orders',    -- 整库同步： app_db\..* ； 单表同步： app_db\.orders
    'sink.connector' = 'doris',
<<<<<<< HEAD
    'sink.jdbc-url' = 'jdbc:mysql://192.168.100.xx:9030',
    'sink.fenodes' = '192.168.100.xx:8030',
    'sink.benodes' = '192.168.100.xx:8040',
=======
    'sink.jdbc-url' = 'jdbc:mysql://192.168.0.xx:9030',
    'sink.fenodes' = '192.168.0.xx:8030',
    'sink.benodes' = '192.168.0.xx:8040',
>>>>>>> 3c4181f7e07feb1892a8b680b63799f5f5dfde4c
    'sink.username' = 'root',
    'sink.password' = '',
    'sink.doris.batch.size' = '1000',
    'sink.sink.max-retries' = '1',
    'sink.sink.buffer-flush.interval' = '60000',
    'sink.sink.db' = 'test',
    'sink.sink.properties.format' ='json',
    'sink.sink.properties.read_json_by_line' ='true',
    'sink.table.identifier' = '#{schemaName}.#{tableName}'  -- '#{schemaName}.#{tableName}'  |  'test\.orders'
  --    ,'sink.sink.label-prefix' = '#{schemaName}_#{tableName}_1'
  );
  ```
  **依赖处理**：需下载 `flink-sql-connector-mysql-cdc-3.4.0.jar`、`flink-doris-connector-1.20-25.1.0.jar` 包到 Dinky `dinky/extends/` 和 flink实例 `flink/lib/` 目录，并重启Dinky、Flink 服务。
  需要先在Doris中创建好库表，目前暂不支持自动创建库表

#### 2. **JAR 任务提交**
- 上传 JAR 到 HDFS 或 Dinky 服务器，任务配置中指定入口类及参数。
- **YARN Application 模式**：需在集群配置中启用 Application 集群。

---

### **四、错误处理**
- **问题一**
  
  ```bash
  Caused by: java.lang.ClassCastException: cannot assign instance of java.lang.invoke.SerializedLambda to field org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator.userFunction of type org.apache.flink.api.common.functions.Function in instance of org.apache.flink.streaming.api.operators.StreamFlatMap
  ```
  需要拷贝 dinky/jar/dinky-app-xxx.jar 到 `dinky/extends/flink<version>/dink/` 目录和 `flink/lib` 目录，并重启Dinky、Flink 服务。
  
- **问题二**
  启动Flink集群报错：
  ```bash
  Shutting StandaloneSessionClusterEntrypoint down with application status FAILED. Diagnostics java.lang.NoClassDefFoundError: Could not initialize class org.apache.hadoop.security.SecurityUtil
  ```
  下载 flink-shaded-hadoop-3-uber-3.1.1.7.2.9.0-173-9.0.jar 到 `flink/lib` 目录

---

### **五、高级特性**
- **多版本 Flink 支持**：通过 `extends/flink<version>` 目录切换版本。
- **自动恢复机制**：从最新 Checkpoint 恢复任务。
- **企业级功能**：多租户隔离、UDF 开发、血缘分析。



### 引用

[Dinky 和 Flink CDC 在实时整库同步的探索之路](https://mp.weixin.qq.com/s/K-yGG1lOlc3B1i8mHFS-_g?poc_token=HOrgbWijbxcv8-AiMSsRrelNecSiPaevHFQANJC4)
