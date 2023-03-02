# 

### 1. [Redis](https://cloud.tencent.com/product/crs?from=10680)的hash槽介绍

常见的Redis集群架构是三主三从的结构，为了保证数据分片，redis采用了Hash槽的概念，即:

>  将16383个solt映射到所有节点上 

常见的三主三从结构，将solt平均分到三个节点上

>  节点A覆盖0－5460; 节点B覆盖5461－10922; 节点C覆盖10923－16383. 

### 2. 获取数据

如果存入一个值，按照redis cluster哈希槽的[算法](https://links.jianshu.com/go?to=http%3A%2F%2Flib.csdn.net%2Fbase%2Fdatastructure)： CRC16('key')384 = 6782。 那么就会把这个key 的存储分配到 B 上了。同样，当我连接(A,B,C)任何一个节点想获取'key'这个key时，也会这样的算法，然后内部跳转到B节点上获取数据

### 3. 新增一个主节点

新增一个节点D，redis cluster的这种做法是从各个节点的前面各拿取一部分slot到D上，会变成这样：

>  节点A覆盖1365-5460 节点B覆盖6827-10922 节点C覆盖12288-16383 节点D覆盖0-1364,5461-6826,10923-12287 

同样删除一个节点也是类似，移动完成后就可以删除这个节点了。

### 4. 注意

Redis的Hash槽分配不是**一致性Hash**，一致性Hash是成一个hash环，当节点加入或者失效的时候，在环上顺时针找到对应节点。而Redis集群属于手动分配**线性Hash槽**，需要手动指定，并且尽量做到各个节点solt平均分配。 而至于为什么Redis没有采用一致性Hash，因为如果一个节点失效，把数据转移到下一个节点，容易造成缓存雪崩，而采用hash槽+副本节点失效的时候从节点自动接替，不易造成雪崩。

### 5. 参考

1. [Redis-Cluster集群](https://www.jianshu.com/p/813a79ddf932)
2. [redis集群扩容（添加新节点）](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Fyfacesclub%2Fp%2F11860927.html)
3. [Redis进阶实践之十二 Redis的Cluster集群动态扩容](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2FPatrickLiu%2Fp%2F8473135.html)
4. [Redis系列5：深入分析Cluster 集群模式 ](https://www.cnblogs.com/wzh2010/p/15886799.html)
5. [Redis Cluster通信原理](https://cloud.tencent.com/developer/article/1604782)
6. [为什么redis cluster至少需要三个主节点](https://www.zhihu.com/question/354518943)