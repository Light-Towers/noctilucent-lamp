## ThreadLocal工具类

Thread类中有一行：

```java
/* ThreadLocal values pertaining to this thread. This map is maintained by the ThreadLocal class. */
ThreadLocal.ThreadLocalMap threadLocals = null;
```

其中ThreadLocalMap类的定义是在ThreadLocal类中，真正的引用却是在Thread类中。同时，ThreadLocalMap中用于存储数据的entry定义：**Map的key是ThreadLocal类的实例对象，value为用户的值**。

因此ThreadLocal其实只是个符号意义，本身不存储变量，仅仅是用来索引各个线程中的变量副本。值得注意的是，Entry的Key即ThreadLocal对象是采用**弱引用**引入的。

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
    super(k);
    value = v;
    }
}
```



- 每个Thread线程内部都有一个ThreadLocalMap。
- Map里面存储线程本地对象ThreadLocal（key）和线程的变量副本（value）。
- Thread内部的Map是由ThreadLocal维护，ThreadLocal负责向map获取和设置线程的变量值。
- 一个Thread可以有多个ThreadLocal。

### 问题：ThreadLocal中的弱引用

1.为什么ThreadLocalMap使用弱引用存储ThreadLocal？

假如使用强引用，当ThreadLocal不再使用需要回收时，发现某个线程中ThreadLocalMap存在该ThreadLocal的强引用，无法回收，造成内存泄漏。

2.那通常说的ThreadLocal内存泄漏是如何引起的呢？

Entry对象中，虽然Key(ThreadLocal)是通过弱引用引入的，但是value还是通过强引用引入。这就导致，假如不作任何处理，由于ThreadLocalMap和线程的生命周期是一致的，当线程资源长期不释放，即使ThreadLocal本身由于弱引用机制已经回收掉了，但value还是驻留在线程的ThreadLocalMap的Entry中。即存在key为null，但value却有值的无效Entry。导致内存泄漏。

## InheritableThreadLocal子线程同步父线程数据

1. 在创建 `InheritableThreadLocal` 对象的时候赋值给线程的 t.inheritableThreadLocals 变量
2. 在创建新线程的时候会 check 父线程中 t.inheritableThreadLocals 变量是否为 null，如果不为 null 则 copy 一份数据到子线程的 t.inheritableThreadLocals 成员变量中去
3. `InheritableThreadLocal` 重写了 getMap(Thread) 方法，所以 get 的时候，就会从 t.inheritableThreadLocals 中拿到 ThreadLocalMap 对象，从而实现了可以拿到父线程 ThreadLocal 中的值

## Refrence

[ThreadLocal弱引用与内存泄漏分析](https://zhuanlan.zhihu.com/p/91579723)

[ThreadLocal 父子线程之间该如何传递数据？](https://cloud.tencent.com/developer/article/2209970?areaSource=&traceId=)