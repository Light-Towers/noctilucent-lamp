1. ConcurrentHashMap默认初始容量是多少？
   从下面ConcurrentHashMap类的静态变量可以看出它的初始容量为16

2. ConCurrentHashmap 的key，value是否可以为null。
   不行 如果key或者value为null会抛出空指针异常

3. ConCurrentHashmap 每次扩容是原来容量的几倍
   2倍 在transfer方法里面会创建一个原数组的俩倍的node数组来存放原数据。

4. ConCurrentHashmap的数据结构是怎么样的？(后面会具体分析它的put方法)
   在java1.8中，它是一个数组+链表+红黑树的数据结构。

5. 存储在ConCurrentHashmap中每个节点是什么样的，有哪些变量
   它是实现Map.Entry<K,V>接口。里面存放了hash，key，value，以及next节点。它的value和next节点是用volatile进行修饰，可以保证多线程之间的可见性。

6. ConCurrentHashmap的put过程是怎样的？
   整体流程跟HashMap比较类似，大致是以下几步：

（1）如果桶数组未初始化，则初始化；

（2）如果待插入的元素所在的桶为空，则尝试把此元素直接插入到桶的第一个位置；

（3）如果正在扩容，则当前线程一起加入到扩容的过程中；

（4）如果待插入的元素所在的桶不为空且不在迁移元素，则锁住这个桶（分段锁）；

（5）如果当前桶中元素以链表方式存储，则在链表中寻找该元素或者插入元素；

（6）如果当前桶中元素以红黑树方式存储，则在红黑树中寻找该元素或者插入元素；

（7）如果元素存在，则返回旧值；

（8）如果元素不存在，整个Map的元素个数加1，并检查是否需要扩容；

添加元素操作中使用的锁主要有（自旋锁 + CAS + synchronized + 分段锁）。

7. java1.8中ConCurrentHashmap节点是尾插还是头插？
   尾插法,见上述put方法。

8. java1.8中，ConCurrentHashmap什么情况下链表才会转换成红黑树进行存储？
   链表长度大于8。数组长度大于64。从put源码和以下源码可以看出：并非一开始就创建红黑树结构，如果当前Node数组长度小于阈值MIN_TREEIFY_CAPACITY，默认为64，先通过扩大数组容量为原来的两倍以缓解单个链表元素过大的性能问题。

9. java1.8中，ConCurrentHashmap的get过程是怎样的？
   1、计算 hash 值
   2、根据 hash 值找到数组对应位置: (n - 1) & h
   3、根据该位置处结点性质进行相应查找

如果该位置为 null，那么直接返回 null 就可以了
如果该位置处的节点刚好就是我们需要的，返回该节点的值即可

如果该位置节点的 hash 值小于 0，说明正在扩容，或者是红黑树，后面我们再介绍 find 方法如果以上 3 条都不满足，那就是链表，进行遍历比对即可

10. java1.8中，ConCurrentHashmap是如何计算它的size大小的？
    对于size的计算，在扩容和addCount()方法就已经有处理了，可以注意一下Put函数，里面就有addCount()函数。

11. ConcurrentHashMap有哪些构造函数？
    一共有五个，作用及代码如下：

```java
//无参构造函数
public ConcurrentHashMap() {
}
//可传初始容器大小的构造函数
public ConcurrentHashMap(int initialCapacity) {
  if (initialCapacity < 0)
    throw new IllegalArgumentException();
  int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
             MAXIMUM_CAPACITY :
             tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
  this.sizeCtl = cap;
}
//可传入map的构造函数
public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
  this.sizeCtl = DEFAULT_CAPACITY;
  putAll(m);
}
//可设置阈值和初始容量
public ConcurrentHashMap(int initialCapacity, float loadFactor) {
  this(initialCapacity, loadFactor, 1);
}

//可设置初始容量和阈值和并发级别
public ConcurrentHashMap(int initialCapacity,
                         float loadFactor, int concurrencyLevel) {
  if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
    throw new IllegalArgumentException();
  if (initialCapacity < concurrencyLevel)   // Use at least as many bins
    initialCapacity = concurrencyLevel;   // as estimated threads
  long size = (long)(1.0 + (long)initialCapacity / loadFactor);
  int cap = (size >= (long)MAXIMUM_CAPACITY) ?
    MAXIMUM_CAPACITY : tableSizeFor((int)size);
  this.sizeCtl = cap;
}
```

jdk1.8：放弃了Segment臃肿的设计，采用Node+CAS+Synchronized来保证线程安全；

13. ConcurrentHashMap的get方法是否要加锁，为什么？
    不需要，get方法采用了unsafe方法，来保证线程安全。

14. ConcurrentHashMap迭代器是强一致性还是弱一致性？HashMap呢？
    弱一致性，HashMap强一直性。

ConcurrentHashMap可以支持在迭代过程中，向map添加新元素，而HashMap则抛出了ConcurrentModificationException，因为HashMap包含一个修改计数器，当你调用他的next()方法来获取下一个元素时，迭代器将会用到这个计数器。

15. ConcurrentHashMap1.7和1.8的区别
    jdk1.8的实现降低锁的粒度，jdk1.7锁的粒度是基于Segment的，包含多个HashEntry，而jdk1.8锁的粒度就是Node

数据结构：jdk1.7 Segment+HashEntry；jdk1.8 数组+链表+红黑树+CAS+synchronized