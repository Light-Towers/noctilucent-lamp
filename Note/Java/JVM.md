## 面试题：

* juc 和 jvm以及 同步锁机制
* 说下juc，说下aqs的大致流程

* cas自旋锁，是获取不到锁就一直自旋吗？ cas和synchronized区别在哪里，为什么cas好，具体优势在哪里? 自旋的这个线程能保证一直占用cpu吗？假如cpu放弃这个线程，不是还要带来线程再次抢占cpu的开销？

* synchronized底层如何实现的，实现同步的时候用到cas了吗？具体哪里用到了

* 对象头存储哪些信息，长度是多少位存储

* 【Java集合类】

  1、从集合开始吧，介绍一下常用的集合类，哪些是有序的，哪些是无序的

  2、 hashmap是如何寻址的，哈希碰撞后是如何存储数据的， 1.8后什么时候变成红黑树、说下红黑树的原理，红黑树有什么好处

  3、 concurrrenthashmap怎么实现线程安全，一个里面会有几个段segment， jdk1.8后有优化concurrenthashmap吗？分段锁有什么坏处

  【多线程JUC】

  4、reentrantlock实现原理,简单说下aqs

  5、 synchronized实现原理， monitor对象什么时候生成的？知道monitor的monitorenter和monitorexit这两个是怎么保证同步的吗，或者说，这两个操作计算机底层是如何执行的

  6、刚刚你提到了synchronized的优化过程，详细说一下吧。偏向锁和轻量级锁有什么区别？

  7、线程池几个参数说下，你们项目中如何根据实际场景设置参数的，为什么cpu密集设置的线程数比io密集型少

  

**对象的内存布局**: 在HotSpot虚拟机里，对象在堆内存中的存储布局可以划分为三个部分：**对象头**（Header）、**实例数据**（Instance Data）和**对齐填充**（Padding: 保证8个字节的倍数）

对象头

* 对象标记（markOop）
  * 哈希码
  * GC标记
  * GC次数
  * 同步锁标记
  * 偏向锁持有者
* 类元信息（klassOop）: 指向该对象类元数据（klass）的首地址



底层源码：

hash：保存对象的哈希码

age：保存对象的分代年龄

biased＿lock：偏向锁标识位 

lock：锁状态标识位

JavaThread＊：保存持有偏向锁的线程ID 

epoch：保存偏向时间戳

**ObjectMonitor？**



## 内存泄漏

内存泄漏: 不再被使用的对象或变量占用的内存不能被回收

虽然弱引用，保证了key指向的ThreadLocal对象能被及时回收，但是v指向的value对象是需要ThreadLocalMap调用get、set时发现key为null时才会去回收整个entry、value，因此**弱引用不能100％保证内存不泄露**。我们要在**不使用某个ThreadLocal对象后，手动调用remoev方法来删除它**，尤其是在线程池中，不仅仅是内存泄露酌问题，因为线程池中的线程是重复使用的，意味着这个线程的ThreadLocalMap对象也是重复使用的，如果 我们不手动调用remove方法，那么后面的线程就有可能获取到上个线程遗留下来的value值，造成bug。

### 弱引用



### 虚引用

1. **虚引用必须和引用队列（ReferenceQueue）联合使用**

虚引用需要java.lang.ref.PhantomReference类来实现，顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。**如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收**。它不能单独使用也不能通过它访问对象，**虚引用必须和引用队列（ReferenceQueue）联合使用**。

2. **PhantomReference的get方法总是返回null**

虚引用的主要作用是跟踪对象被垃圾回收的状态。**仅仅是提供了一种确保对象被finalize以后，做某些事情的通知机制**。PhantomReference的get方法总是返回null，因此无法访问对应的引用对象。

3. **处理监控通知使用**

换句话说，设置虚引用关联对象的唯一目的，就是在这个对象被收集器回收的时候收到一个系统通知或者后续添加进一步的处理，用来实现比finalize机制更灵活的回收操作