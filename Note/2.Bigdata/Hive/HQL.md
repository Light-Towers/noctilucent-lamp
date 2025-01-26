# HQl

> SQL 语法： https://cloud.tencent.com/document/product/1342/61800


- [ ] (待消化好文) Hive 优化：https://cloud.tencent.com/developer/article/1453464
- [ ] 《Hive性能调优实战》-林志煌 : https://weread.qq.com/web/reader/a503221071a486c0a503e7akc81322c012c81e728d9d180
> 别人整理的大数据笔记： https://github.com/fancyChuan/bigdata-hub/tree/master?tab=readme-ov-file
- [ ] (待消化) Hive SQL的编译过程: https://tech.meituan.com/2014/02/12/hive-sql-to-mapreduce.html

## DDL

### 库

* 创建

  ```hive
  create database if not exists db_hive;
  ```

* 查询

  * 显示数据库

  ```hive
  --显示所有数据库
  show databases;
  --过滤数据库， 语法： SHOW (DATABASES | SCHEMAS) [LIKE 'identifier'];
  show databases like 'db_hive*';
  --查看当前使用的库
select current_database();
  ```

  * 显示数据库信息
  
  ```hive
  desc database db_hive;
  OK
  db_hive 
  hdfs://hadoop102:9820/user/hive/warehouse/db_hive.db
  atguiguUSER
  --显示详细信息 extended
  desc database extended db_hive;
  --格式化显示详细信息(建议)
desc formatted student;
  ```

  * 切换
  
  ```hive
  use db_hive;
  ```

  

* 修改

  用户可以使用 ALTER DATABASE 命令为某个数据库的 DBPROPERTIES 设置键-值对属性值，来描述这个数据库的属性信息。

  ```hive
  alter database db_hive set dbproperties('createtime'='20170830');
  # 修改表注释
  ALTER TABLE table_name SET TBLPROPERTIES('comment' = '这是表注释!');
  # 修改字段注释
  ALTER TABLE table_name CHANGE COLUMN muid muid_new STRING COMMENT '这里是列注释!'; 
  # 删除表属性
  ALTER TABLE my_table UNSET TBLPROPERTIES ('comment', 'created_by');
  ```

* 删除

  ```hive
  drop database if exists db_hive2;
  --强制删除
  drop database db_hive cascade;
  ```

### 表

* 查看表

  ```hive
  -- 查看某库下有哪些表
  -- 语法：SHOW TABLES [IN database_name] ['identifier'];
  show tables in dw 'load*';
  -- 查看表的创建信息
  show create table dw.test;
  -- 查看表的扩展信息，语法：SHOW TABLE EXTENDED [IN/FROM database_name] LIKE 'identifier' [PARTITION(partition_spec)];
  show table extended like 'test_partition';
  ```

  

* 创建

  ```hive
  create table student(id string, name string) row format delimited fields terminated by '\t';
  --创建分区表
  create table dept_partition(deptno int, dname string, loc string) partitioned by (day string) 
  row format delimited fields terminated by '\t';
  ```

* 修改

  ```hive
  -- 重命名表
  ALTER TABLE table_name RENAME TO new_table_name
  -- 修改表属性
  ALTER TABLE student set serdeproperties ("field.delim"=" ");
  
  -- 增加列: 新增列的位置位于末尾
  ALTER TABLE table_name ADD COLUMNS (col_name data_type [COMMENT col_comment], ...)
  -- 更新列: 修改指定列的列名、数据类型、注释信息以及在表中的位置
  ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]
  -- eg:
  alter table dw.load_huikan_test add columns (contact string comment '联系人'); -- 正确，添加在最后
  alter table dw.load_huikan_test change contact contact string after tel_phone ;  -- 正确，移动到指定位置,address字段的后面
  
  -- 替换列: 用新的列集替换表中原有的全部列
  ALTER TABLE table_name REPLACE COLUMNS (col_name data_type [COMMENT col_comment], ...)
  ```

* 截断/清空

  ```hive
  truncate table student;
  ```

* 删除表

  ```hive
  drop table if exists student;
  ```

* 删除分区

  ```hive
  alter table student drop partition (stat_year_month>='2018-01');
  ```

  

## DML

### 数据导入

* load

```hive
--从本地加载
load data local inpath '/opt/module/hive/datas/student.txt' into table default.student;
--从hdfs加载
load data inpath '/user/atguigu/hive/student.txt' into table default.student;
```

* insert

```hive
insert overwrite table student_par select id, name from student where month='201709';
```

<span style="color:red">注意：insert 不支持插入部分字段</span>

* import



### 查询

```hive
 select ename AS name, deptno dn from emp;
```

* 算术运算符

  运算符

  | 运算符 | 描述 |
  | :----: | :--: |
  |  A+B   |  A 加 B    |
  |  A-B   |  A 减 B    |
  |  A*B   |  A 乘 B    |
  |  A/B   |  A 除 B    |
  |  A%B   |  A 对 B 取余    |
  |  A&B   |  A 和 B 按位取与    |
  | A｜B 	|	 A 和 B 按位取或   |
  |  A^B  |  A 和 B 按位取异或    |
  |  ~A   |   A 按位取反   |
  
  
  
* 比较运算符

  下面表中描述了谓词操作符，这些操作符同样可以用于 JOIN…ON 和 HAVING 语句中。

  | 操作符 | 支持的数据类型 | 描述 |
  | :----: | :--: | :--: |
  | A = B | 基本数据类型 | 如果 A 等于 B 则返回 TRUE，反之返回 FALSE |
  | A <=> B | 基本数据类型 | 如果 A 和 B 都为 NULL，则返回 TRUE，如果一边为 NULL，返回 False |
  | A <> B, A!=B | 基本数据类型 | A 或者 B 为 NULL 则返回 NULL；如果 A 不等于 B，则返回TRUE，反之返回 FALSE |
  | A < B | 基本数据类型 | A 或者 B 为 NULL，则返回 NULL；如果 A 小于 B，则返回TRUE，反之返回 FALSE|
  | A <= B | 基本数据类型 | A 或者 B 为 NULL，则返回 NULL；如果 A 小于等于 B，则返回 TRUE，反之返回 FALSE |
  | A > B | 基本数据类型 | A 或者 B 为 NULL，则返回 NULL；如果 A 大于 B，则返回TRUE，反之返回 FALSE|
  | A >= B | 基本数据类型 | A 或者 B 为 NULL，则返回 NULL；如果 A 大于等于 B，则返回 TRUE，反之返回 FALSE |
  | A [NOT] BETWEEN B AND C | 基本数据类型 |如果 A，B 或者 C 任一为 NULL，则结果为 NULL。如果 A 的值大于等于 B 而且小于或等于 C，则结果为 TRUE，反之为 FALSE。如果使用 NOT 关键字则可达到相反的效果。|
  | A IS NULL | 所有数据类型 | 如果 A 等于 NULL，则返回 TRUE，反之返回 FALSE |
  | A IS NOT NULL | 所有数据类型 | 如果 A 不等于 NULL，则返回 TRUE，反之返回 FALSE |
  | IN(数值 1, 数值 2) | 所有数据类型 | 使用 IN 运算显示列表中的值 |
  | A [NOT] LIKE B | STRING 类型 | B 是一个 SQL 下的简单正则表达式，也叫通配符模式，如 果 A 与其匹配的话，则返回 TRUE；反之返回 FALSE。B 的表达式说明如下：‘x%’表示 A 必须以字母‘x’开头，‘%x’表示 A 必须以字母’x’结尾，而‘%x%’表示 A 包含有字母’x’,可以位于开头，结尾或者字符串中间。如果使用 NOT 关键字则可达到相反的效果。 |
  | A RLIKE B, A REGEXP B | STRING 类型 | B 是基于 java 的正则表达式，如果 A 与其匹配，则返回 TRUE；反之返回 FALSE。匹配使用的是 JDK 中的正则表达式接口实现的，因为正则也依据其中的规则。例如，正则表达式必须和整个字符串 A 相匹配，而不是只需与其字符串匹配。 |

  


* 常用函数

  count、max、min、sum、avg
  
* Like 和 RLike

  * Like
  
    % 代表零个或多个字符(任意个字符)。 _ 代表一个字符。
  
  * RLIKE
  
    RLIKE 子句是 Hive 中这个功能的一个扩展，其可以通过 Java 的正则表达式这个更强大的语言来指定匹配条件。
  
    ```hive
    --查找名字中带有 A 的员工信息
    select * from emp where ename RLIKE '[A]';
    ```
  
* 分组


  * Group By
  * Having

* Join

  优化：当对 3 个或者更多表进行 join 连接时，如果每个 on 子句都使用相同的连接键的话，那么只会产生一个 MapReduce job。 


  * 内连接

  * 左外连接

  * 右外连接

  * 满外连接

  * 多表连接

  * 笛卡尔积

    （1）省略连接条件

    （2）连接条件无效

    （3）所有表中的所有行互相连接

* 排序

  * 全局排序（Order by）

    只有一个 Reducer

  * 每个 Reduce 内部排序（Sort By）

    Sort by 为每个 reducer 产生一个排序文件

  * 分区（Distribute By）

    distribute by 的分区规则是根据分区字段的 hash 码与 reduce 的个数进行模除后，余数相同的分到一个区

  * Cluster By

    cluster by 除了具有 distribute by 的功能外还兼具 sort by 的功能。但是排序只能是升序排序，不能指定排序规则为 ASC 或者 DESC。

## 分区、分桶

### 分区

* 创建分区表

  ```hive
  create table dept_partition(deptno int, dname string, loc string) partitioned by (day string) 
  row format delimited fields terminated by '\t';
  --创建二级分区表
  create table dept_partition2(deptno int, dname string, loc string) partitioned by (day string, hour string)
  row format delimited fields terminated by '\t';
  ```

* 向分区表加载数据

  ```hive
  load data local inpath '/opt/module/hive/datas/dept_20200401.log' into table dept_partition partition(day='20200401');
  --向二级分区表加载数据
  load data local inpath '/opt/module/hive/datas/dept_20200401.log' into table dept_partition2 partition(day='20200401', hour='12');
  ```

* 分区查询

  ```hive
  select * from dept_partition2 where day='20200401';
  --二级分区查询
  select * from dept_partition2 where day='20200401' and hour='12';
  ```

* 增加分区

  ```hive
  alter table dept_partition add partition(day='20200404');
  --增加多分区
  alter table dept_partition add partition(day='20200405') partition(day='20200406');
  ```

* 删除分区

  ```hive
  alter table dept_partition drop partition (day='20200406');
  --删除多分区
  alter table dept_partition drop partition (day='20200404'), partition(day='20200405');
  ```

* 查看分区

  ```hive
  --查看有多少分区
  show partitions dept_partition;
  --查看分区表结构
  desc formatted dept_partition;
  ```

* 动态分区调整

  关系型数据库中，对分区表 Insert 数据时候，数据库自动会根据分区字段的值，将数据插入到相应的分区中，Hive 中也提供了类似的机制，即动态分区(Dynamic Partition)，只不过，使用 Hive 的动态分区，需要进行相应的配置。

  * 参数设置

    ```hive
    #开启动态分区功能（默认 true，开启）
    hive.exec.dynamic.partition=true
    #设置为非严格模式（动态分区的模式，默认 strict，表示必须指定至少一个分区为静态分区，nonstrict 模式表示允许所有的分区字段都可以使用动态分区。）
    hive.exec.dynamic.partition.mode=nonstrict
    #在所有执行 MR 的节点上，最大一共可以创建多少个动态分区。默认 1000
    hive.exec.max.dynamic.partitions=1000
    #在每个执行 MR 的节点上，最大可以创建多少个动态分区。
    hive.exec.max.dynamic.partitions.pernode=100
    #整个 MR Job 中，最大可以创建多少个 HDFS 文件。默认 100000
    hive.exec.max.created.files=100000
    #当有空分区生成时，是否抛出异常。一般不需要设置。默认 false
    hive.error.on.empty.partition=false
    ```

* 修复分区

    ```hive
    MSCK REPAIR TABLE dw.dws_exhibition;
    ```



### 分桶

分区针对的是数据的存储路径；分桶针对的是数据文件。

注意事项：

1. reduce 的个数设置为-1,让 Job 自行决定需要用多少个 reduce 或者将 reduce 的个数设置为大于等于分桶表的桶数

2. 从 hdfs 中 load 数据到分桶表中，避免本地文件找不到问题

3. 不要使用本地模式



* 创建分桶表

  ```hive
  create table stu_buck(id int, name string) clustered by(id) into 4 buckets row format delimited fields terminated by '\t';
  ```

* 向分桶表加载数据

  ```hive
  load data inpath '/student.txt' into table stu_buck;
  ```

### 抽样查询

对于非常大的数据集，有时用户需要使用的是一个具有代表性的查询结果而不是全部结果。

语法: TABLESAMPLE(BUCKET x OUT OF y) ， x <= y

```hive
select * from stu_buck tablesample(bucket 1 out of 4 on id);
```

## 函数

官方文档地址: https://cwiki.apache.org/confluence/display/Hive/HivePlugins

### 行转列

* CONCAT(string A/col, string B/col…)

* CONCAT_WS(separator, str1, str2,...)

​	入参必须是：string or array<string>

* COLLECT_SET(col)

### 列转行

* EXPLODE(col)

  LATERAL VIEW: 用于和 split, explode 等 UDTF 一起使用，它能够将一列数据拆成多行数据，在此基础上可以对拆分后的数据进行聚合。

### 开窗/窗口函数

* OVER()

  CURRENT ROW：当前行

  n PRECEDING：往前 n 行数据

  n FOLLOWING：往后 n 行数据

  UNBOUNDED：起点，

  ​	UNBOUNDED PRECEDING 表示从前面的起点，

  ​	UNBOUNDED FOLLOWING 表示到后面的终点

  LAG(col,n,default_val)：往前第 n 行数据

  LEAD(col,n, default_val)：往后第 n 行数据

  NTILE(n)：把有序窗口的行分发到指定数据的组中，各个组有编号，编号从 1 开始，对于每一行，NTILE 返回此行所属的组的编号。注意：n 必须为 int 类型。

### 排名/序

* Rank()
* DENSE_RANK()
* ROW_NUMBER()

### 自定义函数

函数分类：

* UDF(User-Defined Function)

​	一进一出

* UDAF(User-Defined Aggregation Function)

​	多进一出

* UDTF(User-Defined Table-Generating Function)

​	一进多出

函数操作：

* 查看函数

  ```hive
  show functions;
  ```

* 注册函数

  ```hive
  -- 注册临时函数
  CREATE [temporary] FUNCTION [dbname.]function_name AS class_name;
  -- 注册永久函数
  CREATE FUNCTION [db_name.]function_name AS class_name [USING JAR|FILE|ARCHIVE 'file_uri' [, JAR|FILE|ARCHIVE 'file_uri'] ];
  eg: create function db_hive.myStringLength as 'com.study.udf.MyStringLength' using jar 'hdfs:///hive-demo-1.0-SNAPSHOT.jar';
  ```
  
* 使用函数

  ```hive
  -- 函数全名是以 db_name.function_name表示，使用的时候可以直接写函数全名，如果函数在当前操作的库下面，使用函数的时候可以不写库名，直接写函数名即可。
  select db_hive.myStringLength("hello"); 
  ```

* 销毁函数

  ```hive
  Drop [temporary] function [if exists] [dbname.]function_name;
  ```

* 重新加载函数

  ```hive
  -- 集群上有多个hiveServer2实例的时候，在一个hiveServer2实例上注册的UDF在另外一个hiveServer2实例上并不能马上看到新注册的实例，需要重新加载以刷新本实例的函数信息。
  reload function;
  ```

  

## 查看ORC文件类容

```bash
hive --orcfiledump /user/hive/warehouse/test/test_b/dt=2023-07-30/000000_0
hive --service orcfiledump -d /user/hive/warehouse/test/test_b/dt=2023-07-30/000000_0 | tail
```





Hive Bug

https://blog.csdn.net/weixin_38070561/article/details/126895259
