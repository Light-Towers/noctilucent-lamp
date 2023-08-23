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

## 统计字段所有值

```javascript
db.CollectionName.distinct( "field");
```

