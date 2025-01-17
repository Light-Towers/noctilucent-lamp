# [MongoDB学习笔记之索引（一）](https://blog.csdn.net/yaya_jn/article/details/124396562)



[Python操作MongoDB看这一篇就够了](https://blog.csdn.net/m0_72829928/article/details/126860153)

[MongoDB 备份数据](https://www.qikegu.com/docs/3310)

[MongoDB常用命令](https://blog.csdn.net/weixin_49107940/article/details/125677592)

[Mongo 聚合查询](https://www.cnblogs.com/fatedeity/p/16920756.html)

[【详细教程】一文参透MongoDB聚合查询](https://segmentfault.com/a/1190000042114761)

[Java中Mongo语法使用，聚合函数（包含mongo多表关联查询）](https://blog.csdn.net/lexiaowu/article/details/130222960)

[MongoDB中regex操作符的介绍](https://blog.csdn.net/qq_15260769/article/details/80572161)

[SpringBoot 集成 Spring Data Mongodb 操作 MongoDB 详解](http://www.mydlq.club/article/85/#8mongodb-%E8%81%9A%E5%90%88%E6%93%8D%E4%BD%9C)



## 批量修改字段类型

```javascript
db.exhibition_participate.updateMany({}, [
  {
    $set: {
      use_status: {
        $toString: "$use_status"
      }
    }
  }
])
```

## 强制终止任务
```javascript
/* 查看数据库当前正在执行的操作，输出各参数的说明请参考如下：
    client：该请求是由哪个客户端发起的。
    opid：操作的唯一标识符。
    secs_running：表示该操作已经执行的时间，单位为秒。
    microsecs_running：表示该操作已经执行的时间，单位为微秒。
    ns：该操作目标集合。
    op：表示操作的类型。通常是查询、插入、更新、删除中的一种。
    locks：跟锁相关的信息。
*/ 
db.currentOp()
// 杀死一个操作
db.killOp([$OPID])
```

> https://www.mongodb.com/docs/manual/reference/operator/aggregation/toString/

## 设置时区

```bash
# TODO 待验证
mongo –eval "printjson(db.getSiblingDB('admin').runCommand({setParameter: 1, timezone: 'Asia/Shanghai'}))"
```





## Mongo 外联表 (left join)

```bash
db.collection.aggregate([
        {$match: { journal_id: { $nin: ["", null] }, pid: id }},
        {
            $lookup: {          // 与展会表关联
                from: "exhibition_journal",
                localField: "journal_id",
                foreignField: "journal_id",
                as: "journalInfo"
            }
        },
        {$unwind: "$journalInfo"},
        {
            $project: {
                _id: 0,
                pid: 1,
                journal_id: 1,
                participate_in_date: 1,
                booth_id: 1,
                participateExhDate: "$journalInfo.journal_year",
                exhName: "$journalInfo.journal_name",
                boothType: "标准展位/特装展位",
                participateExhNum: 1
            }
        },
        {$sort: {participateExhDate: -1}}, 
        {$skip: page}, 
        {$limit: limit}
    ])
```

> [**mongodb3.0性能测试 mongodb lookup性能**](https://blog.51cto.com/u_16099331/7094088)

## 统计字段所有值

```javascript
db.CollectionName.distinct( "field");
// 或者
db.CollectionName.aggregate([ {$match:{"age" : 18}}, {$project:{"name":true}}, {$group:{_id:"$name"}}, {$group:{_id:null,count:{$sum:1}}} ])
```

## 集合
```javascript
// 创建集合
db.createCollection("app_enterprise_info")
// 删除集合
db.app_enterprise_info.drop()
```

## 索引
```javascript
// 查询索引
db.app_enterprise_info.getIndexes()

// 创建索引
db.app_enterprise_info.createIndex({ pid: 1 }, { unique: true })
// 创建联合索引
db.app_enterprise_info.createIndex({
    "pid": NumberInt("1"),
    "name": NumberInt("1"),
}, {
    name: "pid_1_name_1"
});

// 删除索引
db.app_enterprise_info.dropIndex({ pid: 1 })
```

