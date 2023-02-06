# List VS Set VS Map

# 前言

集合类是Java开发最常用的工具，也是面试经常会问到的问题。Java提供了高性能的集合框架，主要包括两种容器类型：一种是**集合（Collection）**，存储一个元素集合；另一种是**图（Map）**，存储键/值对映射。

![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/35c613c0991f448da3573e10dc35963b~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)

# Java集合框架

Collection 可以主要分为 Set、List 、Queue 三种接口类型，而Map 是不属于 Collection 的。Map 接口是一个独立的数据结构，同时依赖于Collection接口，Collection接口又依赖于迭代器Iterator接口，这样所有的集合类型都可以使用统一的方式从中取出元素。

# List接口类型

**List 类型**的集合是有序集合，特点是可以精确控制每个元素的位置，用户可以通过整数索引来访问元素。List集合中的元素是可以重复的。List 集合的常用子类包括：**ArrayList、LinkedList、Vector。**

**ArrayList** 继承自 AbstractList，实现了 List 接口。底层是基于数组的，实现容量大小动态变化。ArrayList 按照添加的先后顺序排列。如果要对 ArrayList 按照元素的值进行排序，可以调用 Collection.sort() 方法，并提供一个 Comparator 比较器。

**LinkedList** 底层基于双向链表的，每个节点维护了 prev 和 next 指针，用于遍历链表。同时链表还维护了 first 和 last指针，分别指向第一个元素和最后一个元素。LinkedList是线程不安全的。

**Vector** 和 ArrayList 非常类似，同样继承自 AbstractList。两者在并发安全上有区别，Vector 是线程安全的，而ArrayList是线程不安全的。当Vector 创建一个 Iterator 并使用时，另一个线程改变了 Vector 的状态，比如说添加或者删除了一些元素， 这时再调用 Iterator的方法会抛出异常。

# Set接口类型

**Set 类型集合**存储的是无序的、不重复的数据，而List 存储的是有序的、可以重复的元素。是否允许重复项，是Set和List的最大区别。

Set检索效率低下，删除和插入效率高，因为插入和删除不会引起Set中元素位置的改变。而List正好相反，查找元素效率高，但插入删除效率低，因为插入和删除会引起元素位置改变。

Set类型常用的实现类： **HashSet** 和 **TreeSet**。两者都不是线程安全的。

- **HashSet**以哈希表的形式存放元素，插入删除速度很快。HashSet 不能保证元素的排列顺序，顺序有可能发生变化。
- **TreeSet**底层是基于二叉树的，可以确保集合元素处于排序状态。**TreeSet**支持两种排序方式，自然排序 和定制排序，其中自然排序为默认的排序方式。TreeSet **自然排序**是根据集合元素的大小，以升序排列，如果要**定制排序**，应该使用Comparator接口。

# Map接口类型

与List、Set不同，**Map类型**不是Collection接口的继承。那么什么是Map类型呢？

在数组中，是通过数组下标来对其内容进行索引的，在Map中，是通过对象来对内容（也是个对象）进行索引的，用来做索引的对象叫做key，其对应的内容对象叫做value。也就是我们平时说的键值对。

Map的 entrySet() 方法返回一个实现Map.Entry 接口的对象集合。集合中每个对象都是Map中的一个键值对。

Map的常用实现类是HashMap 和 TreeMap，与HashSet 和 TreeSet类似。

- **HashMap** 适用于在Map中插入、删除和定位元素。
- **TreeMap** 适用于按自然顺序或自定义顺序遍历键。

HashMap通常比TreeMap快一点，树和哈希表的数据结构使然，建议一般场合多使用HashMap，在需要排序的场合才用TreeMap。HashMap和TreeMap都不是线程安全的，AbstractMap的子类HashTable才是线程安全的，不过不建议使用该类，建议在多线程环境下使用JUC包中的ConcurrentHashMap类。

# 总结

最后我们用下表将List、Set和Map的常见集合类型做一个总结。

![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/baa8f3a9ff844c269a2a672b01e99e77~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)

