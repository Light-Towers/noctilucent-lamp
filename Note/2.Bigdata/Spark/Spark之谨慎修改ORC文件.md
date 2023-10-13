## 一、背景

- 由于历史原因， 有些表并未使用 ORC 格式，而是直接才用了 TextFile 格式， 随着业务的迭代， 表越变越大，导致存储资源十分紧张。为了节省存储空间，经过调研，我们决定采用 ORC 格式，大概可以节省 75% 的存储空间， 对于 PB 级别的数据， 是十分可观的。
- 在做完 ORC 格式后，我们又对这些表进行拉链操作。拉链的过程，遇到一个十分怪异的错误。不管使用 Hive 还是 Spark 都会抛出异常，导致 SQL 失败。

**Spark 报错：**

```bash
Most recent failure:
Lost task 1.1 in stage 0.0 (TID 2, master02, executor 4): java.lang.ClassCastException: org.apache.spark.sql.catalyst.expressions.MutableAny cannot be cast to org.apache.spark.sql.catalyst.expressions.MutableLong
	at org.apache.spark.sql.catalyst.expressions.SpecificInternalRow.setLong(SpecificInternalRow.scala:283)
	at org.apache.spark.sql.hive.HiveInspectors$$anonfun$unwrapperFor$46.apply(HiveInspectors.scala:718)
	at org.apache.spark.sql.hive.HiveInspectors$$anonfun$unwrapperFor$46.apply(HiveInspectors.scala:718)
	at org.apache.spark.sql.hive.orc.OrcFileFormat$$anonfun$org$apache$spark$sql$hive$orc$OrcFileFormat$$unwrap$1$1.apply(OrcFileFormat.scala:339)
	at org.apache.spark.sql.hive.orc.OrcFileFormat$$anonfun$org$apache$spark$sql$hive$orc$OrcFileFormat$$unwrap$1$1.apply(OrcFileFormat.scala:329)
	at scala.collection.Iterator$$anon$11.next(Iterator.scala:410)
	at scala.collection.Iterator$$anon$11.next(Iterator.scala:410)
```

**1.1 问题表现：**

- 该问题出错的表为分区表，当你 `select * from xxxx where dt='yyyy-mm-dd' ` **查询最近一天的分区** 能够成功，但是**查询前一天**或者**不限制分区**查询，却会抛出上述的错误。

## 二、问题定位

#### 2.1 通过更加具体的报错提示，确定大体问题原因。

- 通过结合 Hive 的报错提示和 Spark 的警告信息，我们可以大概知道问题的原因是因为， **元数据记录的字段类型和真实的字段类型不一致，导致强制类型转换抛出异常。** 但是具体的原因，依然我们进一步排查。
- 在 Hive 开发的 Jira 上可以找到以下几个 issue.

> [ORC Schema Evolution Issues](https://ld246.com/forward?goto=https%3A%2F%2Fissues.apache.org%2Fjira%2Fplugins%2Fservlet%2Fmobile%23issue%2FHIVE-11981)
>  [https://issues.apache.org/jira/browse/HIVE-10591](https://ld246.com/forward?goto=https%3A%2F%2Fissues.apache.org%2Fjira%2Fbrowse%2FHIVE-10591)
>  [https://issues.apache.org/jira/browse/HIVE-11981](https://ld246.com/forward?goto=https%3A%2F%2Fissues.apache.org%2Fjira%2Fbrowse%2FHIVE-11981)

通过上述三个 issue 的描述，可以知道两个 ORC 相关的重要信息

- 从 **Hive2.1 之前** 是**不支持修改字段类型**.
- 从 **Hive2.1 开始** 才支持**字段类型**的更改。
- 从 **Hive2.2 开始**,才支持 **字段顺序**的更改。

所以问题大体原因： 应该是由于**该表的字段类型做过变更， 由于该表又是 ORC 格式，导致无法兼容旧的分区，但是对于新的分区则按更改后的字段类型，生成对应格式的 ORC 文件。**

## 三、 问题模拟

#### 3.1 问题复现

```sql
-- 创建 test_a 表
drop table IF EXISTS test.test_a;
CREATE EXTERNAL TABLE IF NOT EXISTS test.test_a
(
    `exh_id` bigint comment '展会ID',
    `exh_name` string comment '展会名称'
)   comment '历届展会表'
    PARTITIONED BY (dt string)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' STORED AS ORCFILE
    LOCATION '/user/hive/warehouse/test/test_a';
-- 创建 test_b 表
drop table IF EXISTS test.test_b;
CREATE EXTERNAL TABLE IF NOT EXISTS test.test_b
(
    `exh_id` bigint comment '展会ID',
    `exh_name` string comment '展会名称'
)   comment '历届展会表'
    PARTITIONED BY (dt string)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' STORED AS ORCFILE
    LOCATION '/user/hive/warehouse/test/test_b';

-- 模拟插入分区数据
-- 2023-07-30
insert overwrite table test.test_b partition (dt='2023-07-30')
values (1101, '新展会1'),
       (2202, '新展会2');
-- 修改字段类型
ALTER TABLE test.test_b CHANGE exh_id exh_id string;
-- 2023-07-31
insert overwrite table test.test_b partition (dt='2023-07-31')
values ('0fc66a4b4d1d4dbba020bd883906b740', '亚运会'),
       ('154af8960d724933bc509c178f121af6', '世博会');
       
-- Spark SQL 执行
insert overwrite table test.test_a
select `exh_id`,`exh_name`,dt from test.test_b
```

#### 3.2 查看Hive元数据 和 ORC文件Schema

##### 3.2.1 Hive元数据

![image-20230801114447520](C:\Users\osmondy\AppData\Roaming\Typora\typora-user-images\image-20230801114447520.png)

##### 3.2.1 ORC文件Schema

`hive --orcfiledump /user/hive/warehouse/test/test_b/dt=2023-07-30/000000_0`

![image-20230801114648394](C:\Users\osmondy\AppData\Roaming\Typora\typora-user-images\image-20230801114648394.png)

## 四、 解决方案

1. 通过外表挂载回写数据

2. hive-3.1.3 后支持。[Jira:HIVE-20126](https://issues.apache.org/jira/browse/HIVE-20126)

   ```sql
   set orc.force.positional.evolution=true;
   ```

