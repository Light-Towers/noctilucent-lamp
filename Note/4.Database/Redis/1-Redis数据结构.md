# String

String类型，也就是字符串类型，是Redis中最简单的存储类型。其value是字符串，不过根据字符串的格式不同，又可以分为3类：

* string：普通字符串
* int：整数类型，可以做自增、自减操作
* float：浮点类型，可以做自增、自减操作

不管是哪种格式，底层都是字节数组形式存储，只不过是编码方式不同。字符串类型的最大空间不能超过512m

**常见命令：**

* SET：添加或者修改已经存在的一个String类型的键值对

* GET：根据key获取String类型的value
* MSET：批量添加多个String类型的键值对
* MGET：根据多个key获取多个String类型的value
* INCR：让一个整型的key自增1
* INCRBY:让一个整型的key自增并指定步长，例如：incrby num 2 让num值自增2
* INCRBYFLOAT：让一个浮点类型的数字自增并指定步长
* SETNX：添加一个String类型的键值对，前提是这个key不存在，否则不执行
* SETEX：添加一个String类型的键值对，并且指定有效期

# Hash

**常见命令：**

* HSET key field value：添加或者修改hash类型key的field的值
* HGET key field：获取一个hash类型key的field的值
* HMSET：批量添加多个hash类型key的field的值
* HMGET：批量获取多个hash类型key的field的值
* HGETALL：获取一个hash类型的key中的所有的field和value
* HKEYS：获取一个hash类型的key中的所有的field
* HVALS：获取一个hash类型的key中的所有的value
* HINCRBY:让一个hash类型key的字段值自增并指定步长
* HSETNX：添加一个hash类型的key的field值，前提是这个field不存在，否则不执行

# List

**常见命令：**

* LPUSH key  element ... ：向列表左侧插入一个或多个元素
* LPOP key：移除并返回列表左侧的第一个元素，没有则返回nil
* RPUSH key  element ... ：向列表右侧插入一个或多个元素
* RPOP key：移除并返回列表右侧的第一个元素
* LRANGE key star end：返回一段角标范围内的所有元素
* BLPOP和BRPOP：与LPOP和RPOP类似，只不过在没有元素时等待指定时间，而不是直接返回nil
* LREM key count value: LREM mylist 0 "value"  //从mylist中删除全部等值value的元素   0为全部，正值从头部开始，负值为从尾部开始。 
* LTRIM: LTRIM mylist 1 -1      //保留mylist中 1到末尾的值，即删除第一个值。
* LINDEX key index : 获取某个位置的值

# Set

**常见命令：**

* SADD key member ... ：向set中添加一个或多个元素
* SREM key member ... : 移除set中的指定元素
* SCARD key： 返回set中元素的个数
* SISMEMBER key member：判断一个元素是否存在于set中
* SMEMBERS：获取set中的所有元素
* SINTER key1 key2 ... ：求key1与key2的交集
* SDIFF key1 key2 ... ：求key1与key2的差集
* SUNION key1 key2 ..：求key1和key2的并集

# SortedSet

⚠️ `倒序：Z后加REV`

**常见命令：**

* ZADD key score member：添加一个或多个元素到sorted set ，如果已经存在则更新其score值
* ZREM key member：删除sorted set中的一个指定元素
* ZREMRANGEBYRANK key start stop: 删除指定下标范围内的元素
* ZSCORE key member : 获取sorted set中的指定元素的score值
* ZRANK key member：获取sorted set 中的指定元素的排名
* ZCARD key：获取sorted set中的元素个数
* ZCOUNT key min max：统计score值在给定范围内的所有元素的个数
* ZINCRBY key increment member：让sorted set中的指定元素自增，步长为指定的increment值
* ZRANGE key start stop：正序，获取指定下标范围内的元素
* **ZREVRANGE** key start stop：倒序，获取指定下标范围内的元素
* **ZRANGEBYSCORE** key min max：正序，获取指定score范围内的元素
* ZINTER、ZDIFF、ZUNION：求交集、差集、并集



## 批量删除

1.扫描到指定前缀的key 2.基于linux管道命令删除

> 参考：[Redis删除特定前缀key的优雅实现](https://juejin.cn/post/6844903869412016142)

```shell
# 扫描到指定前缀的key，然后进行unlink非阻塞删除
redis-cli --scan --pattern users:* | xargs redis-cli unlink
# del命令删除： 删除指定前缀的key (keys 和 del 都会阻塞主线程：慎用)
redis-cli --raw keys users* | xargs redis-cli del 
# 等同于 (redis) 
del users*
```

