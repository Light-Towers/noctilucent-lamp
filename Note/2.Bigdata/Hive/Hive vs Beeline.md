# Hive CLI和Beeline的区别
  Beeline主要是开发来与新服务器进行交互。Hive CLI是基于 Apache Thrift的客户端，而Beeline是基于SQLLine CLI的JDBC客户端。在本文中，我们将详细阐述Hive CLI和Beeline客户端之间的区别.
  以下是Hive CLI和Beeline客户端之间的一些区别。如果您从旧的Hive CLI迁移到新的Beeline客户端，了解它们之间的区别将会对您有所帮助

## 1.Server Connection
* Hive CLI
  Hive CLI使用HiveServer1。Hive CLI使用Thrift协议连接到远程Hiveserver1实例。要连接到服务器，必须指定主机名。端口号是可选的。

```bash
$ hive -h <host_name> -p <port>
```

* Beeline
  Beeline使用JDBC连接到远程HiveServer2实例。连接参数包括JDBC URL。

```bash
$ beeline -u <url> -n <username> -p <password>
```

## 2.交互模式
* Hive CLI
  在Hive CLI交互模式中，可以执行任何HiveQL查询语句：

```hive
hive> show databases;
OK
Default
test_db
```


也可以执行任何shell命令

```hive
hive> !more myfile.txt;
This is a test file.hive>
```



* Beeline
  在Beeline中也可以像Hive CLI中一样执行HiveQL

```hive
$ beeline -u jdbc:hive2://
Connecting to jdbc:hive2://
18/01/03 10:47:45 [main]: WARN service.CompositeService: Unable to create operation log root directory: /tmp/hive/operation_logs
Connected to: Apache Hive (version 1.2.1000.2.5.0.0-1245)
Driver: Hive JDBC (version 1.2.1000.2.5.0.0-1245)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 1.2.1000.2.5.0.0-1245 by Apache Hive
0: jdbc:hive2://> 
0: jdbc:hive2://> show tables;
OK
+----------------------------------------+--+
| database_name |
+----------------------------------------+--+
|Default |
| test_db |
+----------------------------------------+--+
```



也可以使用"!"来执行Beeline命令而不是shell命令：

```hive
beeline> !connect jdbc:hive2://
Connecting to jdbc:hive2://
Enter username for jdbc:hive2://: username
Enter password for jdbc:hive2://: *******
18/01/03 10:51:03 [main]: WARN service.CompositeService: Unable to create operation log root directory: /tmp/hive/operation_logs
Connected to: Apache Hive (version 1.2.1000.2.5.0.0-1245)
Driver: Hive JDBC (version 1.2.1000.2.5.0.0-1245)
Transaction isolation: TRANSACTION_REPEATABLE_READ
0: jdbc:hive2://>
```


“！quit”是退出Beeline客户端的命令

```hive
0: jdbc:hive2://> !q
Closing: 0: jdbc:hive2://
```


## 3.嵌入模式
嵌入模式是测试代码最最佳和最便捷的方式之一。HiveCLI和Beeline均支持嵌入模式。

* Hive CLI
  只需要输入hive命令而不添加任何参数就可以以嵌入模式启动Hive CLI：

```bash
$ hive
```



* Beeline
  为了在嵌入模式中启动Beeline客户端，需要指定一个连接URLjdbc:hive2://

```bash
$ beeline -u jdbc:hive2://
```


## 4.HiveQL 查询执行
查询语句的执行在Hive CLI和Beeline中是一样的。

* Hive CLI

  ```bash
  $ hive -e <query in quotes>$ hive -f <query file name>
  ```

* Beeline

  ```bash
  $ beeline -e <query in quotes>$ beeline -f <query file name>
  ```


在上述两种情况中，如果不提供-e和-f选项，客户端将进入交互模式。



## 5.变量替代
Hive CLI和Beeline均支持变量替代

* Hive CLI
  你可以使用set命令设置变量并将其作为参数传递给脚本：

```bash
$ hive --hiveconf var=value -e 'set var; set hiveconf:var; select * from table where col = ${hiveconf:var}'
```

* Beeline
  可以-hivevar变量传递值给脚本文件

```bash
$ beeline -u jdbc:hive2://hive_server:10000/test_db -n username -p password --hivevar var1=value -f file.sql
```

