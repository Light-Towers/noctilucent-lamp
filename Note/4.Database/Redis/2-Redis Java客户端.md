# SpringDataRedis

|             API             |   返回值类型    |           说明            |
| :-------------------------: | :-------------: | :-----------------------: |
| redisTemplate.opsForValue() | ValueOperations |  操作**String**类型数据   |
| redisTemplate.opsForHash()  | HashOperations  |   操作**Hash**类型数据    |
| redisTemplate.opsForList()  | ListOperations  |   操作**List**类型数据    |
|  redisTemplate.opsForSet()  |  SetOperations  |    操作**Set**类型数据    |
| redisTemplate.opsForZSet()  | ZSetOperations  | 操作**SortedSet**类型数据 |
|        redisTemplate        |                 |         通用命令          |



RedisTemplate可以接收任意Object作为值写入Redis，只不过写入前会把Object序列化为字节形式，默认是采用JDK序列化。缺点：1.可读性差 2.内存占用较大

