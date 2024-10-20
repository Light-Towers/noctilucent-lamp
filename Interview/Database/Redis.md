## 什么是Redis

Redis 是一种基于内存实现的Key-Value数据结构的NoSql数据库，**读写速度非常快**，常用于**缓存，消息队列、分布式锁等场景**。

## 说说Redis的基本数据结构类型

- String（字符串）

  - 特点：二进制安全，获取字符串长度时间复杂度O(1)，值最大为512M

  - 应用场景：共享session、分布式锁，计数器、限流。

  - 内部编码有3种，`int（8字节长整型）/embstr（小于等于39字节字符串）/raw（大于39个字节字符串）`

  - **SDS（simple dynamic string）**

    - ```c
      struct sdshdr{ 
      	unsigned int len; // 标记buf的长度 
      	unsigned int free; //标记buf中未使用的元素个数 
      	char buf[]; // 存放元素的坑 
      }
      ```

      

- Hash（哈希）

- List（列表）

- Set（集合）

- zset（有序集合）

它还有其它特殊的数据结构类型

- Geospatial（地理信息）
- Hyperloglog (基数统计)
- Bitmap (位图)
- Stream（流）

## 首先Redis为什么这么快？

1.基于内存，不会受到硬盘IO速度的限制

2.单线程，避免了[多线程](https://so.csdn.net/so/search?q=多线程&spm=1001.2101.3001.7020)切换导致的CPU消耗,也不用考虑锁的问题,不存在加锁释放锁的操作，也不存在因死锁而导致的性能消耗

3.使用多路I/O复用模型，非阻塞IO

## Redis客户端执行一条命令分为如下四个过程：

1）发送命令

2）命令排队

3）命令执行

4）返回结果

其中 1 + 4 称为Round Trip Time（RTT，往返时间）。



## Refrence

[1]: https://xiaolincoding.com/redis/base/redis_interview.html#%E8%AE%A4%E8%AF%86-redis	"Redis 常见面试题"
[2]: http://kaito-kidd.com/2021/03/14/redis-trap/	"颠覆认知——Redis会遇到的15个「坑」，你踩过几个？"
