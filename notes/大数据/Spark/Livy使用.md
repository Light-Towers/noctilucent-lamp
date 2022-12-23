# Rest操作

### 获取当前session

```bash
# from与size为翻页参数
curl "http://ha-node2:8998/sessions?from=0&size=10"
```

### 创建一个session

```bash
# 创建的是一个sql类型的Session
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"kind" : "sql","driverMemory" : "512m","driverCores" : 1,"executorMemory" : "512m","executorCores": 1,"numExecutors" : 1,"name" : "Submit Demo Session 1","heartbeatTimeoutInSecond" : 500}' "http://ha-node2:8998/sessions"
```

解释下参数：

| 参数名称                 | 说明                                               |
| :----------------------- | :------------------------------------------------- |
| kind                     | session类型，可以是spark、pyspark、sparkr、**sql** |
| proxyUser                | 代理用户，以哪个用户提交作业                       |
| driverMemory             | driver所需内存                                     |
| driverCores              | driver所需cpu core                                 |
| executorMemory           | executor所需内存                                   |
| executorCores            | executor所需要的core                               |
| numExecutors             | 总共的executor数量                                 |
| name                     | session的名称                                      |
| heartbeatTimeoutInSecond | session会话的超时时间，单位为秒                    |

### 提交sql

```bash
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"code": "show databases", "kind":"sql"}' "http://ha-node2:8998/sessions/0/statements"

# 提交spark
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"code": "sc.textFile(\"hdfs://ns1/input.txt\").flatMap(_.split(\" \")).map((_,1)).reduceByKey(_+_).saveAsTextFile(\"hdfs://ns1/output.txt\")", "kind":"spark"}' "http://ha-node2:8998/sessions/0/statements"
```

![在Livy webui上可以展示运行结果](/Users/yangjinhua/Research/noctilucent-lamp/notes/大数据/Spark/Livy使用.assets/modb_20210908_6e1cf3ee-105d-11ec-b6f0-00163e068ecd.png)

<center><p>在Livy webui上可以展示运行结果</p></center>



### 获取执行结果

```bash
curl "http://ha-node2:8998/sessions/0/statements/0"
```

```json
{
    "id": 3,
    "code": "show databases",
    "state": "available",
    "output": {
        "status": "ok",
        "execution_count": 3,
        "data": {
            "application/json": {
                "schema": {
                    "type": "struct",
                    "fields": [
                        {
                            "name": "databaseName",
                            "type": "string",
                            "nullable": false,
                            "metadata": {}
                        }
                    ]
                },
                "data": [
                    [
                        "default"
                    ],
                    [
                        "hudi_datalake"
                    ],
                    [
                        "kylin_test"
                    ],
                    [
                        "test"
                    ]
                ]
            }
        }
    },
    "progress": 1,
    "started": 1616691026653,
    "completed": 1616691028536
}

```

### 删除session

```bash
curl -XDELETE "http://ha-node2:8998/sessions/5"
```







🐤小记：

1. livy-session 等同于 spark-shell (处理交互式请求)
   1. session的创建
   2. session的查看

livy-batches 等同于 spark-submit (处理非交互式请求)
