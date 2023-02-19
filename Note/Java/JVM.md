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




## JVM 组成

### JVM 主要组成部分及其作用

JVM 主要由四大部分组成：

* ClassLoader（类加载器）: 加载类文件到内存。
* Runtime Data Area（运行时数据区）: 
  * 堆（Heap）:JVM中内存中最大的一块，虚拟机启动时创建，用于存放对象实例
  * 方法区（Method Area）：用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。

  * 程序计数器（Program Counter Register）：一块较小的内存空间，作用是当前线程所执行的字节码的行号指示器。

  * JVM栈（JVM Stacks）：生命周期与线程相同。虚拟机栈描述的是Java方法执行的内存模型：每个方法被执行的时候都会同时创建一个栈帧（Stack Frame）用于存储**局部变量表、操作栈、动态链接、方法出口**等信息。每一个方法被调用直至执行完成的过程，就对应着一个栈帧在JVM栈中从入栈到出栈的过程。

  * 本地方法栈（Native Method Stacks）: 与虚拟机栈所发挥的作用是非常相似，其区别不过是虚拟机栈为虚拟机执行Java方法（也就是字节码）服务，而本地方法栈则是为虚拟机使用到的Native方法服务。
* Execution Engine（执行引擎）: 负责解释命令，交由操作系统执行。
* Native Interface（本地库接口）: 融合不同的语言为 java 提供接口

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/JVM%E7%BB%84%E6%88%90.jpg)



### 类加载过程

- **加载**：根据查找路径找到相应的 class 文件然后导入；
- **检查**：检查加载的 class 文件的正确性；
- **准备**：给类中的静态变量分配内存空间；
- **解析**：虚拟机将常量池中的符号引用替换成直接引用的过程。
- **初始化**：对静态变量和静态代码块执行初始化工作。

### 怎么判断对象是否可以被回收

- 引用计数器：为每个对象创建一个引用计数器，有对象引用时计数器 +1，引用被释放时计数 -1，当计数器为 0 时就可以被回收。它有一个缺点不能解决循环引用的问题；
- 可达性分析：从 GC Roots 开始向下搜索，搜索所走过的路径称为引用链。当一个对象到 GC Roots 没有任何引用链相连时，则证明此对象是可以被回收的。



### 分代垃圾回收器是怎么工作的

分代回收器有两个分区：老年代和新生代，新生代默认的空间占比总空间的 1/3，老年代的默认占比是 2/3。

新生代使用的是复制算法，新生代里有 3 个分区：Eden、To Survivor、From Survivor，它们的默认占比是 8:1:1，它的执行流程如下：

- 把 Eden + From Survivor 存活的对象放入 To Survivor 区；
- 清空 Eden 和 From Survivor 分区；
- From Survivor 和 To Survivor 分区交换，From Survivor 变 To Survivor，To Survivor 变 From Survivor。

每次在 From Survivor 到 To Survivor 移动时都存活的对象，年龄就 +1，当年龄到达 15（默认配置是 15）时，升级为老年代。大对象也会直接进入老年代。

老年代当空间占用到达某个值之后就会触发全局垃圾收回，一般使用标记整理的执行算法。以上这些循环往复就构成了整个分代垃圾回收的整体执行流程。

###  JVM 调优的工具

JDK 自带了很多监控工具，都位于 JDK 的 bin 目录下。

- jps，JVM Process Status Tool,显示指定系统内所有的HotSpot虚拟机进程。
- jstat，JVM statistics Monitoring是用于监视虚拟机运行时状态信息的命令，它可以显示出虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。
- jmap，JVM Memory Map命令用于生成heap dump文件
- jhat，JVM Heap Analysis Tool命令是与jmap搭配使用，用来分析jmap生成的dump，jhat内置了一个微型的HTTP/HTML服务器，生成dump的分析结果后，可以在浏览器中查看
- jstack，用于生成java虚拟机当前时刻的线程快照。
- jinfo，JVM Configuration info 这个命令作用是实时查看和调整虚拟机运行参数。

两款视图监控工具

- jconsole：用于对 JVM 中的内存、线程和类等进行监控；
- jvisualvm：JDK 自带的全能分析工具，可以分析：内存快照、线程快照、程序死锁、监控内存的变化、gc 变化等。



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



## Refrence

[1]: https://cloud.tencent.com/developer/article/1588179 "Java中的对象都是在堆上分配的吗？"