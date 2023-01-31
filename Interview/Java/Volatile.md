> 爱问volatile关键字的面试官，大多数情况下都是有一定功底的，因为volatile作为切入点，往底层走可以切入Java内存模型（JMM），往并发方向走又可接切入Java并发编程，当然，再深入追究，JVM的底层操作、字节码的操作、单例都可以牵扯出来。

## JMM三大特性：可见性、有序性、原子性

* 可见性：当一个线程修改了某个共享变量的值，其它线程能够立即看到修改后的值
* 有序性：程序执行代码指令的顺序应当保证按照程序指定的顺序执行，即便是编译优化，也应当保证程序源语一致
* 原子性：一个或多个程序指令，要么全部正确执行完毕不能被打断，或者全部不执行



volatile满足特性：可见性、有序性

## volatile 无法保证原子性

使用volatile 和synchronized 锁都可以保证共享变量的可见性。相比synchronized 而言， volatile 在某些场景下可以看作是一个轻量级锁，所以使用volatile 的成本更低， 因为它不会引起线程上下文的切换和调度。

> **不要将volatile用在getAndOperate场合（这种场合不原子，需要再加锁），仅仅set或者get的场景是适合volatile的**。

但volatile 无法像synchronized 一样保证重操作的原子性

> 下面我们来聊聊volatile 的原子问题：
> volatile 无法保证原子性。所谓的原子性是指在一次操作或者多次操作中， 要么所有的操作全部都得到了执行并且不会受到任何因素的干扰而中断， 要么所有的操作都不执行。在多线程环境下，volatile 关键字可以保证共享数据的可见性， 但是并不能保证对数据操作的原子性。也就是说， 多线程环境下， 使用volatile 修饰的变量是**线程不安全的**。要解决这个问题，我们可以使用锁机制，或者使用原子类（ 如Atomiclnteger) 。这里特别说一下， 对任意单个使用volatile 修饰的变量的读/写是具有原子性，但类似于 flag = !flag 这种复合操作不具有原子性。简单地说就是，**单纯的值操作是原子性的**。

#### volatile没有原子性举例：AtomicInteger自增

例如你让一个volatile的integer自增（i++），其实要分成3步：1）读取volatile变量值到local； 2）增加变量的值；3）把local的值写回，让其它的线程可见。这3步的jvm指令为：

```assembly
mov    0xc(%r10),%r8d ; Load
inc    %r8d           ; Increment
mov    %r8d,0xc(%r10) ; Store
lock addl $0x0,(%rsp) ; StoreLoad Barrier
```

## 指令重排序

为了提高性能，编译器和处理器会对既定的代码执行顺序进行指令重排序

<img src="https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/%E6%8C%87%E4%BB%A4%E9%87%8D%E6%8E%92%E5%BA%8F.jpg" alt="img" style="zoom: 50%;" />

一般重排序可以分为如下三种：

* 编译器优化的重排序。编译器在不改变**单线程程序语义**的前提下,可以重新安排语句的执行顺序。

* 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性,处理器可以改变语句对应机器指令的执行顺序。

* 内存系统的重排序。由于处理器使用缓存和读/写缓冲区,这使得加载和存诸操作看上去可能是在乱序执行。

> 1. as-if-serial语义: 所有的动作(Action)都可以为了优化而被重排序，但是必须保证它们重排序后的结果和程序代码本身的应有结果是一致的。Java编译器、运行时和处理器都会保证**单线程**下的as-if-serial语义。
>
> 2. 存在数据依赖关系，禁止重排序
>

JMM规定happens-before规则: happens-before的前后两个操作不会被重排序且后者对前者的内存可见。

- 程序次序法则：线程中的每个动作A都happens-before于该线程中的每一个动作B，其中，在程序中，所有的动作B都能出现在A之后。
- 监视器锁法则：对一个监视器锁的解锁 happens-before于每一个后续对同一监视器锁的加锁。
- volatile变量法则：对volatile域的写入操作happens-before于每一个后续对同一个域的读写操作。
- 线程启动法则：在一个线程里，对Thread.start的调用会happens-before于每个启动线程的动作。
- 线程终结法则：线程中的任何动作都happens-before于其他线程检测到这个线程已经终结、或者从Thread.join调用中成功返回，或Thread.isAlive返回false。
- 中断法则：一个线程调用另一个线程的interrupt happens-before于被中断的线程发现中断。
- 终结法则：一个对象的构造函数的结束happens-before于这个对象finalizer的开始。
- 传递性：如果A happens-before于B，且B happens-before于C，则A happens-before于C

## 内存屏障

内存屏障（Memory Barrier，或有时叫做内存栅栏，Memory Fence）是一种CPU指令，用于控制特定条件下的重排序和内存可见性问题。Java编译器也会根据内存屏障的规则禁止重排序。

作用：

- 保证特定操作的执行顺序
- 强制更新一次不同CPU的缓存到主内存，保证某些变量的内存可见性。（利用该特性实现volatile的内存可见性）

### 四种屏障指令

- LoadLoad屏障：对于这样的语句Load1; LoadLoad; Load2，在Load2及后续读取操作要读取的数据被访问前，保证Load1要读取的数据被读取完毕。
- StoreStore屏障：对于这样的语句Store1; StoreStore; Store2，在Store2及后续写入操作执行前，保证Store1的写入操作对其它处理器可见。
- LoadStore屏障：对于这样的语句Load1; LoadStore; Store2，在Store2及后续写入操作被刷出前，保证Load1要读取的数据被读取完毕。
- StoreLoad屏障：对于这样的语句Store1; StoreLoad; Load2，在Load2及后续所有读取操作执行前，保证Store1的写入对所有处理器可见。

> StoreLoad 的开销是四种屏障中最大的。在大多数处理器的实现中，这个屏障是个万能屏障，兼具其它三种内存屏障的功能。

| 屏障类型   | 指令示例                 | 说明                                                         |
| ---------- | ------------------------ | ------------------------------------------------------------ |
| LoadLoad   | Load1;LoadLoad; Load2    | 保证load1的读取操作在load2及后续读取操作之前执行             |
| StoreStore | Store1;StoreStore;Store2 | 在store2及其后的写操作执行前，保证store1的写操作已刷新到主内存 |
| LoadStore  | Load1;LoadStore;Store2   | 在stroe2及其后的写操作执行前，保证load1的读操作已读取结束    |
| StoreLoad  | Store1;StoreLoad;Load2   | 保证store1的写操作已刷新到主内存之后，load2及其后的读操作才能执行 |

## volatile 读写过程

**read(读取)→load(加载)→use(使用)→assign(赋值)→store(存储)→write(写入)→**<font color=red>lock(锁定)→unlock(解锁)</font>

![image-20230130184030973](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image-20230130184030973.png)



### Atomic 之底层 Striped64 中一些变量或者方法的定义：

* base：类似于AtomicLong中全局的value值。在没有竞争情况下数据直接累加到base上，或者cells扩容时，也需要将数据写入到base上

* collide：表示扩容意向，false 一定不会扩容，true可能会扩容。

* cellsBusy：初始化cells或者扩容cells需要获取锁，0：表示无锁状态 1：表示其他线程已经持有了锁

* casCellsBusy（）： 通过CAS操作修改cellsBusy的值，CAS成功代表获取锁，返回 true

* NCPU：当前计算机CPU数量，Cell数组扩容时会使用到

* getProbe（）：获取当前线程的hash值

* advanceProbe（）：重置当前线程的hash值



CAS并发原语体现在JAVA语言中就是sun.misc.Unsafe类中的各个方法。调用UnSafe类中的CAS方法，JVM会帮我们实现出 CAS汇编指令。这是一种完全依赖于硬件的功能，通过它实现了原子操作。

> 再次强调，由于CAS是一种系统原语，原语属于操作系统用语范畴，是由若干条指令组成的，用于完成某个功能的一个过程，并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说CAS是一条CPU的原子指令，不会造成所谓的数据不一致问题。

## 总结

| 特性   | volatile关键字 | synchronized关键字 | Lock接口 | Atomic变量 |
| ------ | -------------- | ------------------ | -------- | ---------- |
| 原子性 | 无法保障       | 可以保障           | 可以保障 | 可以保障   |
| 可见性 | 可以保障       | 可以保障           | 可以保障 | 可以保障   |
| 有序性 | 一定程度保障   | 可以保障           | 可以保障 | 无法保障   |





## 引用

[1]: https://www.cnblogs.com/mainz/p/3556430.html	"为什么volatile不能保证原子性而Atomic可以？"
[2]: https://www.cnblogs.com/54chensongxia/p/12120117.html	"Java内存模型之有序性问题"
[3]: https://tech.meituan.com/2014/09/23/java-memory-reordering.html	"Java内存访问重排序的研究"
[4]: https://zhuanlan.zhihu.com/p/133851347 "volatile底层原理详解"



