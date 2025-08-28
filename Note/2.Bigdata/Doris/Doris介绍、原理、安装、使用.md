参考文献地址：[https://blog.csdn.net/weixin_43114209/article/details/131395344](https://blog.csdn.net/weixin_43114209/article/details/131395344)   
Doris官网文档：[https://doris.apache.org/docs/get-starting/quick-start/](https://doris.apache.org/docs/get-starting/quick-start/)

## **一、Doris介绍**

Apache Doris 是一个基于 MPP 架构的高性能、实时的分析型数据库，以极速易用的特点被人们所熟知，仅需亚秒级响应时间即可返回海量数据下的查询结果，不仅可以支持高并发的点查询场景，也能支持高吞吐的复杂分析场景。基于此，Apache Doris 能够较好的满足报表分析、即席查询、统一数仓构建、数据湖联邦查询加速等使用场景，用户可以在此之上构建用户行为分析、AB 实验平台、日志检索分析、用户画像分析、订单分析等应用。

**MPP-分布式并行结构化数据库架构**

## **二、使用场景**

- 报表分析

1.  实时看板 （Dashboards） 
2.  面向企业内部分析师和管理者的报表
3.  面向用户或者客户的高并发报表分析（Customer Facing Analytics）。比如面向网站主的站点分析、面向广告主的广告报表，并发通常要求成千上万的 QPS  
    ，查询延时要求毫秒级响应。著名的电商公司京东在广告报表中使用 Apache Doris ，每天写入 100 亿行数据，查询并发QPS 上万，99 分位的查询延时 150ms。

- 即席查询（Ad-hoc Query）：面向分析师的自助分析，查询模式不固定，要求较高的吞吐。小米公司基于 Doris构建了增长分析平台（Growing Analytics，GA），利用用户行为数据对业务进行增长分析，平均查询延时 10s，95分位的查询延时 30s 以内，每天的 SQL 查询量为数万条。
- 统一数仓构建 ：一个平台满足统一的数据仓库建设需求，简化繁琐的大数据软件栈。海底捞基于 Doris 构建的统一数仓，替换了原来由  
    Spark、Hive、Kudu、Hbase、Phoenix 组成的旧架构，架构大大简化。
- 数据湖联邦查询：通过外表的方式联邦分析位于 Hive、Iceberg、Hudi 中的数据，在避免数据拷贝的前提下，查询性能大幅提升

## **三、技术概述**

Doris整体架构如下图所示，Doris 架构非常简单，只有两类进程

- Frontend（FE），主要负责用户请求的接入、查询解析规划、元数据的管理、节点管理相关工作。
- Backend（BE），主要负责数据存储、查询计划的执行。  
    这两类进程都是可以横向扩展的，单集群可以支持到数百台机器，数十 PB 的存储容量。并且这两类进程通过一致性协议来保证服务的高可用和数据的高可靠。这种高度集成的架构设计极大的降低了一款分布式系统的运维成本。  
    
    ![存算一体架构](https://doris.apache.org/zh-CN/assets/images/apache-doris-technical-overview-b8c5cb11b57d2f6559fa397d9fd0a8a0.png)

在使用接口方面，Doris 采用 MySQL 协议，高度兼容 MySQL 语法，支持标准 SQL，用户可以通过各类客户端工具来访问 Doris，并支持与 BI 工具的无缝对接。

在存储引擎方面，Doris 采用列式存储，按列进行数据的编码压缩和读取，能够实现极高的压缩比，同时减少大量非相关数据的扫描，从而更加有效利用 IO 和 CPU 资源。

Doris 也支持比较丰富的索引结构，来减少数据的扫描：

- Sorted Compound Key Index，可以最多指定三个列组成复合排序键，通过该索引，能够有效进行数据裁剪，从而能够更好支持高并发的报表场景
- Z-order Index ：使用 Z-order 索引，可以高效对数据模型中的任意字段组合进行范围查询
- Min/Max ：有效过滤数值类型的等值和范围查询
- Bloom Filter ：对高基数列的等值过滤裁剪非常有效
- Invert Index ：能够对任意字段实现快速检索

**在存储模型方面，Doris 支持多种存储模型，针对不同的场景做了针对性的优化：**

- Aggregate Key 模型：相同 Key 的 Value 列合并，通过提前聚合大幅提升性能
- Unique Key 模型：Key 唯一，相同 Key 的数据覆盖，实现行级别数据更新
- Duplicate Key 模型：冗余模型，数据完全按照导入文件中的数据进行存储，不会有任何聚合。即使两行数据完全相同，也都会保留。 而在建表语句中指定的 DUPLICATE KEY，只是用来指明底层数据按照那些列进行排序。

Doris 也支持强一致的物化视图，物化视图的更新和选择都在系统内自动进行，不需要用户手动选择，从而大幅减少了物化视图维护的代价。

在查询引擎方面，Doris 采用 MPP 的模型，节点间和节点内都并行执行，也支持多个大表的分布式 Shuffle Join，从而能够更好应对复杂查询。  

![查询引擎](https://doris.apache.org/zh-CN/assets/images/apache-doris-query-engine-1-9e2beb07704b905a1c44dae1c5b3bd04.png)

Doris 查询引擎是_**[向量化的查询引擎](https://cloud.tencent.com/developer/article/2355179)**_，所有的内存结构能够按照列式布局，能够达到大幅减少虚函数调用、提升 Cache 命中率，高效利用 SIMD(单指令多数据流) 指令的效果。在宽表聚合场景下性能是非向量化引擎的 5-10 倍。

![查询引擎](https://doris.apache.org/zh-CN/assets/images/apache-doris-query-engine-2-92a7d1bd709c09e437e90dfedf559803.png)

Doris 采用了 Adaptive Query Execution 技术(自适应执行优化引擎)， 可以根据 Runtime Statistics 来动态调整执行计划，比如通过 Runtime Filter 技术能够在运行时生成 Filter 推到 Probe 侧，并且能够将 Filter 自动穿透到 Probe 侧最底层的 Scan 节点，从而大幅减少 Probe 的数据量，加速 Join 性能。Doris 的 Runtime Filter 支持 In/Min/Max/Bloom Filter。

在优化器方面 Doris 使用 CBO(基于代价的优化器) 和 RBO(基于规则的优化器) 结合的优化策略，RBO 支持常量折叠、子查询改写、谓词下推等，CBO 支持 Join Reorder。目前 CBO 还在持续优化中，主要集中在更加精准的统计信息收集和推导，更加精准的代价模型预估等方面。  


## **四、快速使用**

1.下载：[https://doris.apache.org/download](https://doris.apache.org/download)  
2.解压：tar xf apache-doris-x.x.x.tar.xz  
3.配置：  
进入apache-doris-x.x.x/fe 目录：  
cd apache-doris-x.x.x/fe  
修改FE配置文件conf/fe.conf，这里我们主要修改两个参数：priority_networks和meta_dir，如果需要更优化的配置，请参考FE参数配置如何调整。

a.添加priority_networks参数  
priority_networks=192.168.100.0/24

b.添加元数据目录

meta_dir=/path/your/doris-meta

注：

这里你可以不配置，默认是Doris FE安装目录中的doris-meta。

单独配置元数据目录，需要提前创建您指定的目录

**启动：**

在FE安装目录下执行以下命令，完成FE启动。

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_11-57-52.png?version=2&modificationDate=1714112717490&api=v2)

#### 查看 FE 运营[状态](https://doris.apache.org/docs/get-starting/quick-start/#view-fe-operational-status "直接链接查看 FE 运行状态")

可以通过以下命令检查Doris是否启动成功

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_11-58-24.png?version=1&modificationDate=1705291104868&api=v2)

这里的IP和端口是FE的IP和http_port（默认8030），如果是在FE节点执行，直接运行上面的命令即可。

如果返回结果有“ ”字样`"msg": "success"`，则启动成功。

您还可以通过在浏览器中输入地址，通过 Doris FE 提供的 Web UI 进行检查

[http://fe_ip:8030](http://fe_ip:8030/)

可以看到如下界面，说明FE已经启动成功

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_11-58-54.png?version=1&modificationDate=1705291134588&api=v2)

> 笔记：
> 
> 1. 这里我们使用Doris内置的默认用户root，以空密码登录。
> 2. 这是Doris的管理界面，只有具有管理权限的用户才能登录。

#### 连接[FE](https://doris.apache.org/docs/get-starting/quick-start/#connect-fe "直接链接至 Connect FE")

下面我们通过MySQL客户端连接Doris FE，下载免安装的[MySQL客户端](https://doris-build-hk.oss-cn-hongkong.aliyuncs.com/mysql-client/mysql-5.7.22-linux-glibc2.12-x86_64.tar.gz)

解压刚刚下载的MySQL客户端，在目录中可以找到`mysql`命令行工具`bin/`。然后执行以下命令连接Doris。

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-13-7.png?version=1&modificationDate=1705295588412&api=v2)

> 笔记：
> 
> 1. 这里使用的root用户是doris内置的默认用户，也是超级管理员用户，参见[权限管理](https://doris.apache.org/docs/admin-manual/privilege-ldap/user-privilege)
> 2. -P：这里是我们连接Doris的查询端口，默认端口是9030，对应于`query_port`fe.conf中
> 3. -h：这里是我们要连接的FE的IP地址，如果你的客户端和FE安装在同一节点上你可以使用127.0.0.1

执行以下命令查看FE运行状态

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-13-43.png?version=1&modificationDate=1705295623515&api=v2)

然后您可以看到类似于以下内容的结果。

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-14-2.png?version=1&modificationDate=1705295642789&api=v2)

1. 如果 IsMaster、Join 和 Alive 列均为 true，则节点正常

### **进入BE配置**

**![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-16-32.png?version=1&modificationDate=1705295793269&api=v2)**

**![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-16-57.png?version=1&modificationDate=1705295817921&api=v2)**

  

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-17-40.png?version=1&modificationDate=1705295861328&api=v2)

1. Alive : true 表示节点正常运行

### 创建

1.创建数据库

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-21-19.png?version=1&modificationDate=1705296080261&api=v2)

2.创建表

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-22-30.png?version=2&modificationDate=1706594394419&api=v2)

3. 实例数据

10000,2017-10-01,beijing,20,0,2017-10-01 06:00:00,20,10,10  
10006,2017-10-01,beijing,20,0,2017-10-01 07:00:00,15,2,2  
10001,2017-10-01,beijing,30,1,2017-10-01 17:05:45,2,22,22  
10002,2017-10-02,shanghai,20,1,2017-10-02 12:59:12,200,5,5  
10003,2017-10-02,guangzhou,32,0,2017-10-02 11:20:00,30,11,11  
10004,2017-10-01,shenzhen,35,0,2017-10-01 10:00:15,100,3,3  
10004,2017-10-03,shenzhen,35,0,2017-10-03 10:20:22,11,6,6

将以上数据保存到`test.csv`文件中。

1. 导入数据

这里我们通过Stream load将上面文件中保存的数据导入到我们刚刚创建的表中。

curl  --location-trusted -u root: -T test.csv -H "column_separator:," [http://127.0.0.1:8030/api/demo/example_tbl/_stream_load](http://127.0.0.1:8030/api/demo/example_tbl/_stream_load)

- -T test.csv ：这是我们刚刚保存的数据文件，如果路径不同，请指定完整路径
- -u root：这里是用户名和密码，我们使用默认用户root，密码为空
- 127.0.0.1:8030 ：分别是fe的ip和http_port

执行成功后我们可以看到如下返回信息

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-29-4.png?version=1&modificationDate=1705296545225&api=v2)

1. `NumberLoadedRows`表示已导入的数据记录条数
   
2. `NumberTotalRows`表示要导入的数据总量
   
3. `Status`:Success表示导入成功
   

到这里我们就完成了数据的导入，现在我们可以根据自己的需求来查询和分析数据了。

## 查询[数据](https://doris.apache.org/docs/get-starting/quick-start/#query-data "直接链接到查询数据")

上面我们已经完成了建表和导入数据，接下来我们可以体验一下Doris快速查询和分析数据的能力。

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-15_13-29-52.png?version=1&modificationDate=1705296593154&api=v2)

  

**Unique Key模型：**

|   |
|---|
|`CREATE TABLE IF NOT EXISTS test_db.user`<br><br>`(`<br><br>    `` `user_id` LARGEINT NOT NULL COMMENT `` `"用户id"``,`<br><br>    `` `username` VARCHAR( ```50``) NOT NULL COMMENT` `"用户昵称"``,`<br><br>    `` `city` VARCHAR( ```20``) COMMENT` `"用户所在城市"``,`<br><br>    `` `age` SMALLINT COMMENT `` `"用户年龄"``,`<br><br>    `` `sex` TINYINT COMMENT `` `"用户性别"``,`<br><br>    `` `phone` LARGEINT COMMENT `` `"用户电话"``,`<br><br>    `` `address` VARCHAR( ```500``) COMMENT` `"用户地址"``,`<br><br>    `` `register_time` DATETIME COMMENT `` `"用户注册时间"`<br><br>`)`<br><br>``UNIQUE KEY(`user_id`, `username`)``<br><br>``DISTRIBUTED BY HASH(`user_id`) BUCKETS`` `10``;`<br><br>`insert into test_db.user values(``10000``,``'zhangsan'``,``'北京'``,``20``,``0``,``13112345312``,``'北京西城区'``,``'2020-10-01 07:00:00'``);`<br><br>`insert into test_db.user values(``10000``,``'zhangsan'``,``'北京'``,``20``,``0``,``13112345312``,``'北京海淀区'``,``'2020-11-15 06:10:20'``);`<br><br>`insert into test_db.user values(``10001``,``'lisi'``,``'上海'``,``20``,``0``,``13112345312``,``'上海黄浦区'``,``'2020-11-15 06:10:20'``);`|

**这里以`user_id`, `username`为键，当键值一样时，就自动覆盖原来的数据。**

**![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-30_13-31-17.png?version=1&modificationDate=1706592677166&api=v2)**

  

**Duplicate Key 模型：**

|   |
|---|
|`CREATE TABLE IF NOT EXISTS test_db.example_log`<br><br>`(`<br><br>    `` `timestamp` DATETIME NOT NULL COMMENT `` `"日志时间"``,`<br><br>    `` `type` INT NOT NULL COMMENT `` `"日志类型"``,`<br><br>    `` `error_code` INT COMMENT `` `"错误码"``,`<br><br>    `` `error_msg` VARCHAR( ```1024``) COMMENT` `"错误详细信息"``,`<br><br>    `` `op_id` BIGINT COMMENT `` `"负责人id"``,`<br><br>    `` `op_time` DATETIME COMMENT `` `"处理时间"`<br><br>`)`<br><br>    ``DUPLICATE KEY(`timestamp`, `type`)``<br><br>``DISTRIBUTED BY HASH(`timestamp`) BUCKETS`` `10``;`<br><br>`insert into test_db.example_log values(``'2020-10-01 08:00:05'``,``1``,``404``,``'not found page'``,` `101``,` `'2020-10-01 08:00:05'``);`<br><br>`insert into test_db.example_log values(``'2020-10-01 08:00:05'``,``1``,``500``,``'service error'``,` `201``,` `'2021-10-01 08:00:05'``);`<br><br>`insert into test_db.example_log values(``'2020-10-01 08:00:05'``,``2``,``404``,``'not found page'``,` `101``,` `'2020-10-01 08:00:06'``);`<br><br>`insert into test_db.example_log values(``'2020-10-01 08:00:06'``,``2``,``404``,``'not found page'``,` `101``,` `'2020-10-01 08:00:07'``);`|

哪怕键值`timestamp`, `type`都一样，也不会覆盖，同时会存在。这里的键值只是排序的作用而已。

![](http://wiki.mingyang100.com:8190/download/attachments/35850349/image2024-1-30_13-33-21.png?version=1&modificationDate=1706592801855&api=v2)

## **五、集成HIVE**

提前将 core-site.xml  hdfs-site.xml  hive-site.xml 配置文件放到be/conf  及 fe/conf 文件夹下

**1.创建Catalog**

Hive On HDFS HA

在40节点：执行命令-mysql -uroot -P9030 -h127.0.0.1  进入msyql

在mysql连接中执行

|   |
|---|
|`CREATE` `CATALOG hive PROPERTIES (`<br><br>    `'type'``=``'hms'``,`<br><br>    `'hive.metastore.uris'` `=` `'thrift://master02:9083'``,`<br><br>    `'hadoop.username'` `=` `'hive'``,`<br><br>    `'dfs.nameservices'``=``'nameservice1'``,`<br><br>    `'dfs.ha.namenodes.nameservice1'``=``'master01,master03'``,`<br><br>    `'dfs.namenode.rpc-address.nameservice1.master01'``=``'master01:8020'``,`<br><br>    `'dfs.namenode.rpc-address.nameservice1.master03'``=``'master03:8020'``,`<br><br>    `'dfs.client.failover.proxy.provider.nameservice1'``=``'org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider'`<br><br>`);`|

删除catalog–drop catalog hive;

REFRESH CATALOG hive PROPERTIES("invalid_cache" = "true");  **--手动刷新元数据**

**2.切换到hive库**

switch hive;

1.在40节点：执行命令-mysql -uroot -P9030 -h127.0.0.1  进入msyql

2. 在mysql中执行命令：switch hive;   --进入hive  
show databases;  
switch internal; --**重新回到doris;**  
show databases;  
然后就可以执行sql语句了。

**3.测试：**

|   |
|---|
|`a.`<br><br>`select` `ent_status,``count``(*) tt` `from`  `dw.app_enterprise_info`  `where` `dt=``'2024-01-04'` `group` `by` `ent_status;`<br><br>`TEZ: 59.244s`<br><br>`spark: 39s`<br><br>`doris: 3.44s`<br><br>`b.`<br><br>`select` `c.pid,`<br><br>        `collect_set(``case` `c.invse_currency_name`<br><br>                        `when` `'人民币'` `then` `1`<br><br>                        `when` `'美元'` `then` `2`<br><br>                        `when` `'欧元'` `then` `3`<br><br>                        `when` `'英镑'` `then` `4`<br><br>                        `when` `'港元'` `then` `5`<br><br>                        `when` `'卢比'` `then` `6`<br><br>                        `when` `'日元'` `then` `7`<br><br>                        `when` `'新台币'` `then` `8`<br><br>                        `when` `'其他'` `then` `9`<br><br>            `end``)` `as` `invse_currency_name_type,`<br><br>        `collect_set(c.invse_currency_name)` `as` `invse_currency_name,`<br><br>        `collect_set(c.comfundstatusname)`   `as` `invese_round_name,`<br><br>        `collect_set(``case` `when` `c.comfundstatusname=``'未融资'` `then` `1`<br><br>                         `when` `c.comfundstatusname` `in``(``'Pre种子轮'``,``'种子轮'``,``'天使轮'``,``'天使+轮'``)` `then` `2`<br><br>                         `when` `c.comfundstatusname` `in``(``'PreA轮'``,``'Pre-A+++轮'``,``'Pre-A++轮'``,``'Pre-A+轮'``,``'Pre-A轮'``,``'A轮'``,``'A+++轮'``,``'A++轮'``,``'A+轮'``)` `then` `3`<br><br>                         `when` `c.comfundstatusname` `in``(``'PreB轮'``,``'Pre-B+轮'``,``'Pre-B轮'``,``'B轮'``,``'B++轮'``,``'B+轮'``)` `then` `4`<br><br>                         `when` `c.comfundstatusname` `in``(``'PreC轮'``,``'Pre-C轮'``,``'C'``,``'C轮'``,``'C++++++轮'``,``'C++++轮'``,``'C++轮'``,``'C+轮'``,``'PreD轮'``,``'Pre-D轮'``,``'D轮'``,``'D++轮'``,``'D+轮'``,``'E轮'``,``'E+轮'``,``'E轮及以上'``,``'F轮'``,``'G轮'``)` `then` `5`<br><br>                         `when` `c.comfundstatusname` `in``(``'Pre-IPO'``,``'PreIPO'``,``'IPO'``,``'IPO(转板)'``)` `then` `6`<br><br>                         `when` `c.comfundstatusname` `in``(``'新三板'``)` `then` `7`<br><br>                         `when` `c.comfundstatusname =``'新四板'` `then` `8`<br><br>                         `when` `c.comfundstatusname =(``'并购'``)` `then` `9`<br><br>                         `when` `c.comfundstatusname` `in``(``'战略融资'``,``'战略投资'``)` `then` `10`<br><br>                         `when` `c.comfundstatusname =` `'定向增发'` `then` `11`<br><br>                         `when` `c.comfundstatusname` `in` `(``'私募'``,``'私有化'``)` `then` `12`<br><br>                         `when` `c.comfundstatusname` `in` `(``'IPO(退市)'``,``'退市'``,``'已退市'``,``'已退市(新三板)'``,``'已退市（新三板）'``)` `then` `13`<br><br>                         `when` `c.comfundstatusname` `in` `(``'其他'``,``'300668SZ'``)` `then` `14`<br><br>            `end``)` `as` `invese_round_type`<br><br> `from` `(`<br><br>          `select` `pid,comfundstatusname,``max``(invse_currency_name)` `as` `invse_currency_name`<br><br>          `from` `(`<br><br>                   `select` `pid,comfundstatusname,invse_similar_money_name,`<br><br>                          `case` `when` `invse_similar_money_name` `like` `'%人民币%'` `then` `'人民币'`<br><br>                               `when` `invse_similar_money_name` `like` `'%美元%'` `then` `'美元'` `end` `invse_currency_name`<br><br>                   `from` `dw.dwd_enterprise_invse_info`<br><br>                   `where` `dt =` `'2023-11-10'`<br><br>               `) t`<br><br>          `group` `by` `pid,comfundstatusname`<br><br>      `) c`<br><br> `group` `by` `c.pid limit 200;`<br><br>`执行时间：`<br><br>`TEZ: 16.99s`<br><br>`spark: 29s`<br><br>`doris: 0.51s`|

> 注1：
> 
> 1. FE的磁盘空间主要用于存储元数据，包括日志和图像。通常范围为几百 MB 到几 GB。
> 2. BE的磁盘空间主要用于存储用户数据。占用的总磁盘空间是总用户数据（3份）的3倍。然后额外保留 40% 的空间用于后台压缩和中间数据存储。
> 3. 在一台机器上，可以部署多个BE实例，但**只能部署一个FE实例**。如果您需要3份数据，则需要在3台机器上部署3个BE实例（每台机器1个实例），而不是在一台机器上部署3个BE实例）。**FE服务器的时钟必须一致（允许最大时钟偏差为5秒）。**
> 4. 测试环境也可以仅使用 1 个 BE 实例进行测试。在实际生产环境中，BE实例的数量直接决定了整体查询延迟。
> 5. 禁用所有部署节点的交换。

> 注2：FE节点数量 
> 
> 6. FE节点根据角色分为追随者和观察者。（Leader是Follower组中选举产生的角色，以下也简称为Follower。）
> 7. FE 节点的数量应至少为 1（1 个 Follower）。如果部署1个Follower和1个Observer，就可以实现高读可用性；如果部署3个Followers，则可以实现较高的读写可用性（HA）。
> 8. 虽然一台机器上可以部署多个BE，但建议只部署**一个实例**，同时只能部署**一个FE 。**如果需要3份数据，则至少需要3台机器来部署一个BE实例（而不是1台机器部署3个BE实例）。**多个FE所在服务器的时钟必须一致（时钟偏差最大允许5秒）**。
> 9. 根据以往的经验，对于集群可用性要求较高的业务（例如在线服务商），我们建议您部署3个Followers和1-3个Observers；对于线下业务，我们建议您部署1个Follower和1-3个Observers。

- **通常我们推荐10到100台机器，以充分发挥Doris的性能（其中3台（HA）部署FE，其余部署BE）。**
- **Doris的性能与节点数量及其配置呈正相关。最少四台机器（一台FE，三台BE；一台BE和一台Observer FE混合部署，提供元数据备份）和相对较低的配置，Doris仍然可以流畅运行。**
- **在FE和BE混合部署中，您可能需要注意资源竞争，并确保元数据目录和数据目录属于不同的磁盘。**


[Doris MCP + Dify](https://mp.weixin.qq.com/s/O8bTYJYeBs2-UayyGrIl_w)
