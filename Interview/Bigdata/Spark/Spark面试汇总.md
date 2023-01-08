## Spark Core & SQL

### 介绍下Spark

Apache Spark是一个分布式、内存级计算框架。起初为加州大学伯克利分校`AMPLab`的实验性项目，后经过开源，在2014年成为`Apache`基金会顶级项目之一，现已更新至3.2.0版本。

### Spark的生态体系

Spark体系包含`Spark Core`、`Spark SQL`、`Spark Streaming`、`Spark MLlib`及 `Spark Graphx`。其中Spark Core为核心组件，提供RDD计算模型。在其基础上的众组件分别提供`查询分析`、`实时计算`、`机器学`、`图计算`等功能。

### Spark 有哪些组件

1. Master: 管理集群和节点，不参与计算。
2. Worker: 计算节点，进程本身不参与计算，和 Master 汇报。
3. Driver: 运行程序的 main 方法，创建 Spark Context 对象。
4. Spark Context: 控制整个 application 的生命周期，包括 DAG Scheduler 和 Task Scheduler 等组件。
5. Client: 用户提交程序的入口。

### Spark常用端口号

1. 4040 spark-shell任务端口

2. 7077 内部通讯端口。 类比Hadoop的8020/9000

3. 8080 查看任务执行情况端口。 类比Hadoop的8088

4. 18080 历史服务器。类比Hadoop的19888

👉注意: 由于Spark只负责计算，所有并没有Hadoop中存储数据的端口50070

### Spark为什么这么快

Spark是一个基于内存的，用于大规模数据处理的统一分析引擎，其运算速度可以达到Mapreduce的10-100倍。具有如下特点：

- 内存计算。Spark优先将数据加载到内存中，数据可以被快速处理，并可启用缓存。
- shuffle过程优化。和Mapreduce的shuffle过程中间文件频繁落盘不同，Spark对Shuffle机制进行了优化，降低中间文件的数量并保证内存优先。
- RDD计算模型。Spark具有高效的DAG调度算法，同时将RDD计算结果存储在内存中，避免重复计算。

### [Spark提交作业参数][2]

* master: local/local[K] 或 yarn-client/yarn-cluster
  * local/local[K]:  本地使用一个(或K个)worker线程运行spark程序
  * yarn-client/yarn-cluster
    * yarn-client: 以client方式连接到YARN集群，集群的定位由环境变量HADOOP_CONF_DIR定义，该方式driver在client运行
    * yarn-cluster: 以cluster方式连接到YARN集群，集群的定位由环境变量HADOOP_CONF_DIR定义，该方式driver也在集群中运行

* executor-cores: 每个executor使用的内核数，默认为1。官方建议2-5个，我们企业是4个

* num-executors: 启动executors的数量，默认为2

* executor-memory: executor内存大小，默认1G。一般6~10g 为宜，最大不超过20G，否则会导致GC代价过高，或资源浪费。

* driver-cores: driver使用内核数，默认为1

* driver-memory: driver内存大小，默认512M。不做任何计算和存储，只是下发任务与yarn资源管理器和task交互，除非你是 spark-shell，否则一般 1-2g

```shell
# 如果这里通过--queue 指定了队列，那么可以免去写--master
spark-submit \
  --master local[5]  \
  --driver-cores 2   \
  --driver-memory 8g \
  --executor-cores 4 \
  --num-executors 10 \
  --executor-memory 8g \
  --class PackageName.ClassName XXXX.jar \
  --name "Spark Job Name" \
  InputPath      \
  OutputPath
```



### Spark 的运行流程※💯

![Spark面试八股文（上万字面试必备宝典）_spark](https://s2.51cto.com/images/blog/202111/12175557_618e3a2deae7924783.png?x-oss-process=image/watermark,size_14,text_QDUxQ1RP5Y2a5a6i,color_FFFFFF,t_30,g_se,x_10,y_10,shadow_20,type_ZmFuZ3poZW5naGVpdGk=/format,webp/resize,m_fixed,w_1184)

[Spark 的运行流程][1]具体如下: 

1. 客户端提交任务，创建Driver进程并初始化 SparkContext
2. SparkContext 向资源管理器注册并申请运行 Executor
3. 资源管理器分配 Executor，然后资源管理器启动 Executor
4. Executor 发送心跳至资源管理器，<font color=red>同时向 SparkContext 申请 Task</font>
5. SparkContext 构建 DAG (有向无环图)
6. DAG Scheduler 将 DAG 分解成 Stage（TaskSet），划分 Stage 发送给 Task Scheduler
7. Task Scheduler 将 Task 发送给 Executor 运行 ，同时 SparkContext 将应用程序代码发放给 Executor
8. Task 在 Executor 上运行，运行完毕释放所有资源

### Spark运行模式有哪些？说说你最熟悉的一种

* `Local` 仅用于本地开发
* `Standalone` 是Spark自身的一个调度系统。对集群性能要求非常高时用。国内很少用
* `Yarn` 基于Hadoop Yarn集群调度，国内大量使用
* `Mesos` 国内几乎不用

Standalone模式是Spark内置的运行模式，常用于小型测试集群。这里我就拿Standalone模式来举例:

- Master为资源调度器，负责executors资源调度

- Worker负责Executor进程的启动和监控

- Driver在客户端启动，负责SparkContext初始化

  ![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/b92874c0-53f8-4f44-af6e-9292497e0daf.png)



### 谈谈Yarn Cluster和Yarn Client模式的区别

Yarn Cluster模式的driver进程托管给`Yarn`(AppMaster)管理，通过`yarn UI`或者`Yarn logs`命令查看日志。

Yarn Client模式的driver进程运行在`本地客户端`，因资源调度、任务分发会和Yarn集群产生大量网络通信，出现网络激增现象，适合`本地调试`，不建议生产上使用。

两者具体执行流程整理如下:

* Yarn Cluster模式

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/Spark%20Yarn%20Cluster.png)

* Yarn Client模式

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/Spark%20Yarn%20Client.png)

### RDD 机制是什么？

RDD(分布式弹性数据集) 是Spark的基础数据单元。本身不存储数据，仅作为数据访问的一种虚拟结构。Spark通过对RDD的相互转换操作完成整个计算过程。
* 分布式: RDD本质上可以看成是一组只读的、可分区的分布式数据集，支持跨节点多台机器上进行并行计算。
* 弹性: 数据优先内存存储，当计算节点内存不够时，可以把数据刷到磁盘等外部存储，且支持手动设定存储级别。
* 容错性: RDD的血脉机制保存RDD的依赖关系，同时支持Checkpoint容错机制，当RDD结构更新或数据丢失时可对RDD进行重建。

所有算子都是基于 RDD 来执行的，不同的场景会有不同的 rdd 实现类，但是都可以进行互相转换。rdd 执行过程中会形成 dag 图，然后形成 lineage 保证容错性等。从物理的角度来看 rdd 存储的是 block 和 node 之间的映射。


RDD 在逻辑上是一个 hdfs 文件，在抽象上是一种元素集合，包含了数据。它是被分区的，分为多个分区，每个分区分布在集群中的不同结点上，从而让 RDD 中的数据可以被并行操作（分布式数据集）

比如有个 RDD 有 90W 数据，3 个 partition，则每个分区上有 30W 数据。RDD 通常通过 Hadoop 上的文件，即 HDFS 或者 HIVE 表来创建，还可以通过应用程序中的集合来创建；RDD 最重要的特性就是容错性，可以自动从节点失败中恢复过来。即如果某个结点上的 RDD partition 因为节点故障，导致数据丢失，那么 RDD 可以通过自己的数据来源重新计算该 partition。这一切对使用者都是透明的。

RDD 的数据默认存放在内存中，但是当内存资源不足时，spark 会自动将 RDD 数据写入磁盘。比如某结点内存只能处理 20W 数据，那么这 20W 数据就会放入内存中计算，剩下 10W 放到磁盘中。RDD 的弹性体现在于 RDD 上自动进行内存和磁盘之间权衡和切换的机制。

### RDD属性有哪些※💯

```java
 *  - A list of partitions
 *  - A function for computing each split
 *  - A list of dependencies on other RDDs
 *  - Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)
 *  - Optionally, a list of preferred locations to compute each split on (e.g. block locations for an HDFS file)
```

[RDD概念、RDD五大属性、RDD创建方式](https://www.cnblogs.com/jimmy888/p/13551699.html)

[Spark源码解析：RDD](https://cloud.tencent.com/developer/article/1135965)

### RDD的宽依赖和窄依赖※💯

Spark中的RDD血脉机制，当RDD数据丢失时，可以根据记录的血脉依赖关系重新计算。而DAG调度中对计算过程划分stage，划分的依据也是RDD的依赖关系。针对不同的函数转换，RDD之间的依赖关系分为宽依赖和窄依赖。宽依赖会产生`shuffle`行为，经历map输出、中间文件落地和reduce聚合等过程。

- 宽依赖: 父RDD每个分区被多个子RDD分区使用
- 窄依赖: 父RDD每个分区被子RDD的一个分区使用

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/6c360b79-bd94-4bfd-a338-93de0bf39901.png)

下面我们结合示意图，分别列出宽依赖和窄依赖存在的四种情况：

| 依赖关系                                                  | 示意图                                                       |
| --------------------------------------------------------- | ------------------------------------------------------------ |
| 窄依赖(一个父RDD对应一个子RDD：map/filter、union算子)     | <img src="https://files.mdnice.com/user/23269/d0f598c7-4d1e-4a82-bac5-2d56d2a34a4c.png" alt="img" style="zoom:50%;" /> |
| 窄依赖(多个父RDD对应一个子RDD：co-partioned join算子)     | <img src="https://files.mdnice.com/user/23269/1ec3dc29-3225-4297-b1b0-81ebece5e751.png" alt="img" style="zoom:50%;" /> |
| 宽依赖(一个父RDD对应多个非全部子RDD: groupByKey算子等)    | <img src="https://files.mdnice.com/user/23269/ea320fa3-5ec9-4a37-8ea9-9a8a96ddf380.png" alt="img" style="zoom:50%;" /> |
| 宽依赖(一个父RDD对应全部子RDD: not co-partioned join算子) | <img src="https://files.mdnice.com/user/23269/40cc2c8e-90e8-48c3-b50d-1d8e96cbdb1e.png" alt="img" style="zoom:50%;" /> |

### Spark 算子※💯

Spark中的Transformation操作会生成一个新的RDD，且具有`Lazy特性`，不触发任务的实际执行。常见的算子有`map`、`filter`、`flatMap`、`groupByKey`、`join`等。一般聚合类算子多数会导致shuffle。Action操作是对RDD结果进行聚合或输出，此过程会触发Spark Job任务执行，从而执行之前所有的Transformation操作，结果可返回至Driver端。常见的算子有`foreach`、`reduce`、`count`、`saveAsTextFile`等。

1. Transformation
   1. 单Value
        （1）map
        （2）mapPartitions
        （3）mapPartitionsWithIndex
        （4）flatMap
        （5）glom
        （6）groupBy
        （7）filter
        （8）sample
        （9）distinct
        （10）coalesce
        （11）repartition
        （12）sortBy
        （13）pipe
   2. 双Value

        （1）intersection
        （2）union
   	（3）subtract
   	（4）zip

2. Action

  （1）reduce
  （2）collect
  （3）count
  （4）first
  （5）take
  （6）takeOrdered
  （7）aggregate
  （8）fold
  （9）countByKey
  （10）save
  （11）foreach

### Repartition和Coalesce区别

* **关系：**

两者都是用来改变RDD的partition数量的，repartition底层调用的就是coalesce方法：coalesce(numPartitions, shuffle = true)

* **区别：**

repartition一定会发生shuffle，coalesce根据传入的参数来判断是否发生shuffle。

一般情况下增大rdd的partition数量使用repartition，减少partition数量时使用coalesce

### Spark 任务阶段划分

1. Application：初始化一个SparkContext即生成一个Application
2. Job：一个Action算子就会生成一个Job
3. Stage：Stage等于宽依赖的个数加1
4. Task：一个Stage阶段中，最后一个RDD的分区个数就是Task的个数

Job、stage和task是spark任务执行流程中的三个基本单位。其中job是最大的单位，也是Spark Application任务执行的基本单元，由`action`算子划分触发生成。
stage隶属于单个job，根据shuffle算子(宽依赖)拆分。单个stage内部可根据数据分区数划分成多个task，由TaskScheduler分发到各个Executor上的task线程中执行。

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/a37603af-8a51-48b1-b90a-97fdbdcc478d.png)

### Spark 缓存和检查点区别

* DataFrame的cache默认采用 MEMORY_AND_DISK 
* RDD 的cache默认方式采用MEMORY_ONLY

区别：

1. Cache缓存只是将数据保存起来，不切断血缘依赖。Checkpoint检查点切断血缘依赖。
2. Cache缓存的数据通常存储在磁盘、内存等地方，可靠性低。Checkpoint的数据通常存储在HDFS等容错、高可用的文件系统，可靠性高。
3. 建议对checkpoint()的RDD使用Cache缓存，这样checkpoint的job只需从Cache缓存中读取数据即可，否则需要再从头计算一次RDD。







## Reference

[1]: https://andr-robot.github.io/Spark%E4%BD%9C%E4%B8%9A%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B/
[2]: https://blog.csdn.net/gamer_gyt/article/details/79135118 "spark 参数"
