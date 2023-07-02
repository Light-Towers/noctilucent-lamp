# [MongoDB学习笔记之索引（一）](https://blog.csdn.net/yaya_jn/article/details/124396562)



[Python操作MongoDB看这一篇就够了](https://blog.csdn.net/m0_72829928/article/details/126860153)

[MongoDB 备份数据](https://www.qikegu.com/docs/3310)

[MongoDB常用命令](https://blog.csdn.net/weixin_49107940/article/details/125677592)

[Mongo 聚合查询](https://www.cnblogs.com/fatedeity/p/16920756.html)

[【详细教程】一文参透MongoDB聚合查询](https://segmentfault.com/a/1190000042114761)

[Java中Mongo语法使用，聚合函数（包含mongo多表关联查询）](https://blog.csdn.net/lexiaowu/article/details/130222960)

[MongoDB中regex操作符的介绍](https://blog.csdn.net/qq_15260769/article/details/80572161)

[SpringBoot 集成 Spring Data Mongodb 操作 MongoDB 详解](http://www.mydlq.club/article/85/#8mongodb-%E8%81%9A%E5%90%88%E6%93%8D%E4%BD%9C)



```javascript
# 批量修改字段类型
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

