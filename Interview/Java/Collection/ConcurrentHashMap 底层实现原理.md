# ConcurrentHashMap 底层实现原理


关于这个问题呢，我从这三个方面来回答：

## 1.ConcurrentHashMap的整体架构

![img](https://pdai.tech/images/thread/java-thread-x-concurrent-hashmap-2.png)

我们来看这个图，这个呢是ConcurrentHashMap在jdk 1.8里面的存储结构，它是由数组+单向链表+红黑树来构成的，当我们去初始化一个ConcurrentHashMap实例的时候，默认会初始化一个长度为16的数组，由于ConcurrentHashMap它的核心仍然是Hash表，所以必然会存在Hash冲突的问题，所以讷，ConcurrentHashMap采用链式寻址的方式来解决Hash表的冲突，当Hash冲突比较多的时候，会造成链表长度较长的问题，所以这种情况下会使得ConcurrentHashMap中的一个数组元素的查询复杂度会增加，所以在jdk 1.8里面引入了红黑树这样一个机制，当数组长度大于64，并且链表的长度大于等于8的时候，单向链表就会转化成红黑素，另外呢，随着ConcurrentHashMap的一个动态扩容，一旦链表的长度小于6，红黑树会退化成单向链表。

## 2.ConcurrentHashMap的基本功能

ConcurrentHashMap本质上是一个HashMap，因此功能和HashMap是一样的，但是ConcurrentHashMap在HashMap的基础上提供了并发安全的一个实现。并发安全的实现，主要是通过对于node节点去加锁来保证数据更新的安全性

## 3.ConcurrentHashMap在性能方面做的一些优化

如何在并发性能和数据安全性之间去做好平衡，在很多地方都有类似的设计


eg：CPU的三级缓存，MySQL的buffer pool，Synchronized的锁升级等等

ConcurrentHashMap也做了类似的一个优化，主要体现方面：

1. 在jdk 1.8里面ConcurrentHashMap它的锁的力度是数组中的某一个节点，而在jdk 1.7里面，它锁定的是segment，锁的范围要更大，所以性能上它会更低。
2. 引入红黑树这样一个机制去降低数据查询的时间复杂度，红黑树的时间复杂度是O(logn)
3. 我们看这个图

![image-20230111113413134](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image-20230111113413134.png)

当数组的长度不够的时候，ConcurrentHashMap它需要对数组进行扩容，而在扩容的时间上，ConcurrentHashMap引入了多线程并发扩容的一个实现。简单来说，就是多个线程对原始数组进行分片，分片之后，每个线程去负责一个分片的数据迁移，从而去整体的提升了扩容过程中的数据迁移的一个效率，

第三 ConcurrentHashMap他有一个size()方法来获取总的元素个数，而在多线程并发场景中啊，在保证原子性的前提下去实现元素个数的累加性能是非常低的，所以ConcurrentHashMap在这个方面做了两个点的优化：

	1. 当现成竞争不激烈的时候，直接采用cas的方式来实现元素个数的一个原子递增
	1. 如果线程竞争比较激烈的情况下，使用一个数组来维护元素个数，如果要增加总的元素个数的时候，直接从数组中随机选择一个，再通过cas算法来实现原子递增，它的核心思想是引入了数组来实现对并发更新的一个负载