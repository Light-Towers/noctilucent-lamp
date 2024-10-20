[toc]

## Spark Core

### 什么是Spark

Apache Spark是一个分布式、内存级计算框架。起初为加州大学伯克利分校`AMPLab`的实验性项目，后经过开源，在2014年成为`Apache`基金会顶级项目之一。Hadoop MapReduce的通用的[并行计算](https://www.zhihu.com/search?q=并行计算&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A25351353})框架，Spark基于[map reduce算法](https://www.zhihu.com/search?q=map reduce算法&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A25351353})实现的[分布式计算](https://www.zhihu.com/search?q=分布式计算&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A25351353})，现已更新至3.2.0版本。

### Spark的生态体系

Spark体系包含`Spark Core`、`Spark SQL`、`Spark Streaming`、`Spark MLlib`及 `Spark Graphx`。其中Spark Core为核心组件，提供RDD计算模型。在其基础上的众组件分别提供`查询分析`、`实时计算`、`机器学`、`图计算`等功能。

### Spark有哪些组件

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

### Spark运行模式有哪些？说说你最熟悉的一种

* `Local` 仅用于本地开发
* `Standalone` 是Spark自身的一个调度系统。对集群性能要求非常高时用。国内很少用
* `Yarn` 基于Hadoop Yarn集群调度，国内大量使用
* `Mesos` 国内几乎不用

Standalone：是Spark内部默认实现的一种集群管理模式，常用于小型测试集群。这种模式是通过集群中的**Master**来**统一管理资源**，而与Master进行资源请求协商的是Driver内部的StandaloneSchedulerBackend（实际上是其内部的StandaloneAppClient真正与Master通信）。

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

### Spark 的运行流程※💯

![深度剖析Spark分布式执行原理](https://pica.zhimg.com/v2-13366ce2e12e3b7d25579d4a574eff44_720w.jpg?source=172ae18b)

![Spark面试八股文（上万字面试必备宝典）_spark](https://s2.51cto.com/images/blog/202111/12175557_618e3a2deae7924783.png?x-oss-process=image/watermark,size_14,text_QDUxQ1RP5Y2a5a6i,color_FFFFFF,t_30,g_se,x_10,y_10,shadow_20,type_ZmFuZ3poZW5naGVpdGk=/format,webp/resize,m_fixed,w_1184)

[Spark 的运行流程][1]具体如下: 

1. 客户端提交任务，创建Driver进程并初始化 SparkContext
2. SparkContext 向资源管理器注册并申请运行 Executor
3. 资源管理器分配 Executor，然后资源管理器启动 Executor
4. Executor 发送心跳至资源管理器，<font color=red>同时向 SparkContext 注册并申请 Task</font>
5. SparkContext 构建 DAG (有向无环图)
6. DAG Scheduler 将 DAG 分解成 Stage（TaskSet），划分 Stage 发送给 Task Scheduler
7. Task Scheduler 将 Task 发送给 Executor 运行 ，同时 SparkContext 将应用程序代码发放给 Executor
8. Task 在 Executor 上运行，运行完毕释放所有资源

### 如何理解DAGScheduler的Stage划分算法※💯

首先放上官网的RDD执行流程图:

![img](https://files.mdnice.com/user/23269/aaed769a-4ea8-407f-a0e7-2b06f35b74eb.png)

针对一段应用代码(如上)，Driver会以Action算子为边界生成DAG调度图。DAGScheduler从DAG末端开始遍历`划分Stage`，封装成一系列的tasksets移交TaskScheduler，后者根据调度算法, 将`taskset`分发到相应worker上的Executor中执行。

**1. DAGSchduler的工作原理**

- DAGScheduler是一个`面向stage`调度机制的高级调度器，为每个job计算stage的`DAG`(有向无环图)，划分stage并提交taskset给TaskScheduler。
- 追踪每个RDD和stage的物化情况，处理因shuffle过程丢失的RDD，重新计算和提交。
- 查找rdd partition 是否cache/checkpoint。提供`优先位置`给TaskScheduler，等待后续TaskScheduler的最佳位置划分

**2. Stage划分算法**

- 从触发action操作的算子开始，从后往前遍历DAG。
- 为最后一个rdd创建`finalStage`。
- 遍历过程中如果发现该rdd是宽依赖，则为其生成一个新的stage，与旧stage分隔而开，此时该rdd是新stage的最后一个rdd。
- 如果该rdd是窄依赖，将该rdd划分为旧stage内，继续遍历，以此类推，直至DAG遍历完成。

![img](https://files.mdnice.com/user/23269/9b8641ce-55e9-498d-be60-54aebddf7e9d.png)

### 如何理解TaskScheduler的Task分配算法

TaskScheduler负责Spark中的task任务调度工作。TaskScheduler内部使用`TasksetPool`调度池机制存放task任务。TasksetPool分为`FIFO`(先进先出调度)和`FAIR`(公平调度)。

- FIFO调度: 基于队列思想，使用先进先出原则顺序调度taskset
- FAIR调度: 根据权重值调度，一般选取资源占用率作为标准，可人为设定

![img](https://files.mdnice.com/user/23269/72e429ea-983d-4c8c-8dc3-de8e80a3b464.png)

**1. TaskScheduler的工作原理**

- 负责Application在Cluster Manager上的注册
- 根据不同策略创建TasksetPool资源调度池，初始化pool大小
- 根据task分配算法发送Task到Executor上执行

1. Task分配算法

- 首先获取所有的executors，包含executors的ip和port等信息
- 将所有的executors根据shuffle算法进行打散
- 遍历executors。在程序中依次尝试`本地化级别`，最终选择每个task的`最优位置`(结合DAGScheduler优化位置策略)
- 序列化task分配结果，并发送RPC消息等待Executor响应

![img](https://files.mdnice.com/user/23269/95db9ca5-b05f-4f1c-b4b4-f8e7793a5f61.png)

### RDD 机制是什么？※💯

RDD(分布式弹性数据集) 是Spark的基础数据单元。本身不存储数据，仅作为数据访问的一种虚拟结构。Spark通过对RDD的相互转换操作完成整个计算过程。
* 分布式: RDD本质上可以看成是一组只读的、可分区的分布式数据集，支持跨节点多台机器上进行并行计算。
* 弹性: 数据优先内存存储，当计算节点内存不够时，可以把数据刷到磁盘等外部存储，且支持手动设定存储级别。
* 容错性: RDD的血脉机制保存RDD的依赖关系，同时支持Checkpoint容错机制，当RDD结构更新或数据丢失时可对RDD进行重建。

所有算子都是基于 RDD 来执行的，不同的场景会有不同的 RDD 实现类，但是都可以进行互相转换。rdd 执行过程中会形成 DAG 图，然后形成 lineage 保证容错性等。从物理的角度来看 rdd 存储的是 block 和 node 之间的映射。


RDD 在逻辑上是一个 hdfs 文件，在抽象上是一种元素集合，包含了数据。它是被分区的，分为多个分区，每个分区分布在集群中的不同结点上，从而让 RDD 中的数据可以被并行操作（分布式数据集）

eg: 有个 RDD 有 90W 数据，3 个 partition，则每个分区上有 30W 数据。RDD 通常通过 Hadoop 上的文件，即 HDFS 或者 HIVE 表来创建，还可以通过应用程序中的集合来创建；RDD 最重要的特性就是容错性，可以自动从节点失败中恢复过来。即如果某个结点上的 RDD partition 因为节点故障，导致数据丢失，那么 RDD 可以通过自己的数据来源重新计算该 partition。这一切对使用者都是透明的。

RDD 的数据默认存放在内存中，但是当内存资源不足时，spark 会自动将 RDD 数据写入磁盘。比如某结点内存只能处理 20W 数据，那么这 20W 数据就会放入内存中计算，剩下 10W 放到磁盘中。RDD 的弹性体现在于 RDD 上自动进行内存和磁盘之间权衡和切换的机制。

### Spark的本地化级别有哪几种？怎么调优

`移动计算` or `移动数据`？这是一个问题。在分布式计算的核心思想中，移动计算永远比移动数据要合算得多，如何合理利用本地化数据计算是值得思考的一个问题。

TaskScheduler在进行task任务分配时，需要根据本地化级别计算最优位置，一般是遵循`就近`原则，选择最近位置和缓存。Spark中的`本地化级别`在TaskManager中定义，分为五个级别。

**1. Spark本地化级别**

- PROCESS_LOCAL(进程本地化) partition和task在同一个executor中，task分配到本地Executor进程。

![img](https://files.mdnice.com/user/23269/5c7a195d-e8d0-476f-817e-1f768e986760.png)

- NODE_LOCAL(节点本地化) partition和task在同一个节点的不同Executor进程中，可能发生跨进程数据传输

![img](https://files.mdnice.com/user/23269/3403f0f1-d484-4deb-a022-1e4a20714178.png)

- NO_PREF(无位置) 没有最佳位置的要求，比如Spark读取JDBC的数据
- RACK_LOCAL(机架本地化) partition和task在同一个机架的不同worker节点上，可能需要跨机器数据传输

![img](https://files.mdnice.com/user/23269/ae30d013-dff0-4306-80d5-bb33991238ee.png)

- ANY(跨机架): 数据在不同机架上，速度最慢

**2. Spark本地化调优**

在task最佳位置的选择上，DAGScheduler先判断RDD是否有cache/checkpoint，即`缓存优先`；否则TaskScheduler进行本地级别选择等待发送task。

TaskScheduler首先会根据最高本地化级别发送task，如果在尝试`5次`并等待`3s`内还是无法执行，则认为当前资源不足，即降低本地化级别，按照PROCESS->NODE->RACK等顺序。

- 调优1：加大`spark.locality.wait` 全局等待时长
- 调优2：加大`spark.locality.wait.xx`等待时长(进程、节点、机架)
- 调优3：加大重试次数(根据实际情况微调)

![img](https://files.mdnice.com/user/23269/1dc2334e-7f27-4652-9807-252569652ea0.png)

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

<img src="https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/6c360b79-bd94-4bfd-a338-93de0bf39901.png" alt="img" style="zoom: 80%;" />

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

3. 会产生shuffle的算子

   **去重**

   distinct

   **聚合**

   reduceByKey
   groupBy
   groupByKey
   aggregateByKey
   combineByKey

   **排序**

   sortByKey
   sortBy

   **重分区**

   coalesce
   repartition

   **集合或者表操作**

   intersection
   subtract
   subtractByKey
   join
   leftOuterJoin

### groupbyKey和reduceBykey的区别

在Spark中存在很多聚合算子，根据Key进行分组聚合，常用于处理分类统计等计算场景。

- 分组：groupByKey算子
- 聚合：reduceByKey算子
- 本地聚合：CombineByKey算子     

**1. CombineByKey算子**

聚合算子内部调用的基础算子之一，程序调用CombineByKey算子时会在本地预先进行规约计算，类似于Mapreduce Shuffle中Map阶段的Combine阶段，先看一下执行原理:

- 为各分区内所有Key创建累加器对象并赋值

- 每次计算时分区内相同Key累加器值加一

- 合并各分区内相同Key的值

  <img src="https://files.mdnice.com/user/23269/79a98d7b-754f-4848-8581-4f2879084958.png" alt="img" style="zoom: 67%;" />

  ```scala
  val input = sc.parallelize(
   Array(1,1),(1,2),(2,3),(2,4),2)
  
  val result = input.combineByKey(
   # 初始化(k,v) 将v置换为c(1)
   (v) => (v, 1)
   #调用mergeKey结果 将v累加到聚合对象
   (arr: (Int, Int), v)
     => (arr._1 +v, arr._2+1),
   # 每个分区结果聚合
   (arr1:(Int,Int),arr2:(Int,Int))
     =>(arr1._1+arr2._1, arr1._2+arr2._2)
  ).map{
   case(k,v)=>(k, v._1/v._2)
  }
  ```

**2. ReduceByKey算子**

内部调用CombineByKey算子实现。即先在`本地预聚合`，随后在分布式节点聚合，最终返回(K, V) 数据类型的计算结果。通过第一步本地聚合，大幅度减少跨节点shuffle计算的数据量，提高聚合计算的效率。

<img src="https://files.mdnice.com/user/23269/3e694311-100f-4bae-8e04-6fcf2d68f624.png" alt="img" style="zoom:67%;" />

**3. GroupByKey算子**

GroupByKey内部禁用CombineByKey算子，将分区内相同Key元素进行组合，不参与聚合计算。此过程会和ReduceByKey一致均会产生`Shuffle`过程，但是ReduceByKey存在本地预聚合，效率高于GroupByKey。

- 在聚合计算场景下，计算效率低于ReduceBykey
- 可以搭配mapValues算子实现ReduceByKey的聚合计算

<img src="https://files.mdnice.com/user/23269/00f0ef60-e4fa-49b2-bf60-8227f14cf7f0.png" alt="img" style="zoom:67%;" />



### repartition和coalesce区别

两个算子都可以解决Spark的小文件过多和分区数据倾斜问题。eg: 在使用Spark进行数据处理的过程中，常常会调用filter方法进行数据预处理，频繁的过滤操作会导致分区数据产生大量小文件碎片，当shuffle过程读取分区文件时极容易产生数据倾斜现象。

Spark通过repartition和coalesce算子来控制分区数量，通过合并小分区的方式保持数据紧凑型，提高分区的利用率。

* **关系：**

两者都是用来改变RDD的partition数量的，repartition底层调用的就是coalesce方法：`coalesce(numPartitions, shuffle = true)`

![img](https://files.mdnice.com/user/23269/27bcbec3-8cd1-440a-b0b2-8c2047c887eb.png)

* **区别：**

repartition创建新的partition并且使用 full shuffle，所以一定会发生shuffle； coalesce根据传入的参数来判断是否发生shuffle，先通过生成随机数将partition重新组合（用已有的partition去尽量减少数据shuffle），随后生成CoalesceRDD进行后续的逻辑处理。

<img src="https://files.mdnice.com/user/23269/d0076ce9-cbca-482d-8283-49adc25fac10.png" alt="img" style="zoom:80%;" />

* **分区重分配原则**

  - 当分区数大于原分区时，类型为`宽依赖`(shuffle过程)，需要把coalesce的shuffle参数设为`true`，执行HashPartition重新`扩大`分区，这时调用repartition()

  - 当分区数两者相差不大时，类型为`窄依赖`，可以进行分区合并，这时调用coalesce()

  - 当分区数远远小于原分区时，需要综合考虑不同场景的使用

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

### 说说Spark和Mapreduce中Shuffle的区别※💯

Spark中的shuffle很多过程与MapReduce的shuffle类似，都有Map输出端、Reduce端，shuffle过程通过将Map端计算结果分区、排序并发送到Reducer端。

![img](https://files.mdnice.com/user/23269/60c7e390-be46-49b5-94ba-d29685ddcf21.png)

下面将对Spark和Mapreduce中shuffle过程分开叙述，Mapreduce的shuffle大家都不陌生了，主要重点突出Spark的Shuffle机制做了哪些优化工作。

**1. Hadoop Mapreduce Shuffle**

MapReduce的shuffle需要依赖大量磁盘操作，数据会频繁`落盘`产生大量`IO`，同时产生大量小文件冗余。虽然缓存buffer区中启用了缓存机制，但是阈值较低且内存空间小。

- 读取输入数据，并根据split大小切分为map任务
- map task在分布式节点中执行map()计算
- 每个map task维护一个环形的buffer缓存区，存储map输出结果，分区且排序
- 当buffer区域达到阈值时，开始溢写到临时文件中。map task结束时进行临时文件合并。此时，整个shuffle map端执行完成
- mapreduce根据partition数启动reduce任务，copy拉取数据
- merge合并拉取的文件
- reduce()函数聚合计算，整个过程完成

![img](https://files.mdnice.com/user/23269/d4d74c9f-6808-4d6b-a12e-426dd15261da.png)

**2. Spark的Shuffle机制**

默认并行度：`spark.sql.shuffle.partitions=200`

Spark1.2以前，默认的shuffle计算引擎是HashShuffleManager，此种Shuffle产生大量的中间磁盘文件，消耗磁盘IO性能。在Spark1.2后续版本中，默认的ShuffleManager改成了`SortShuffleManager`，通过索引机制和合并临时文件的优化操作，大幅提高shuffle性能。

![img](https://files.mdnice.com/user/23269/26cd854f-718c-4984-966e-efd968f84ea9.png)

- HashShuffleManager

HashShuffleManager的运行机制主要分成两种，一种是`普通运行机制`，另一种是`合并的运行机制`。合并机制主要是通过复用buffer来优化Shuffle过程中产生的小文件的数量，Hash shuffle本身不排序。开启合并机制后，同一个Executor共用一组core，文件个数为`cores * reduces`。

![img](https://files.mdnice.com/user/23269/a1d01952-4187-4ab5-ab72-67bd14b4ff5f.png)

- SortShuffleManager

SortShuffleManager的运行机制分成两种，普通运行机制和`bypass`运行机制。当shuffletask的数量小于等于`spark.shuffle.sort.bypassMergeThreshold`参数的值时(默认200)，会启用bypass机制。

SortShuffleManager机制采用了一个特殊的内存数据结构(Map)，数据优先写入此结构中，当达到阈值时溢写到磁盘中并清空内存数据结构。在过程中对数据进行排序并合并，减少最终的临时文件数量。ByPass机制下在其基础上加了一个`索引`机制，将数据存放位置记录hash索引值，相同hash的数据合并到同一个文件中。

![img](https://files.mdnice.com/user/23269/04cf0955-abca-4a33-860b-51acaed75562.png)

### 京东：调优之前与调优之后性能的详细对比（例如调整map个数，map个数之前多少、之后多少，有什么提升）

这里举个例子。比如我们有几百个文件，会有几百个map出现，读取之后进行join操作，会非常的慢。这个时候我们可以进行coalesce操作，比如240个map，我们合成60个map，也就是窄依赖。这样再shuffle，过程产生的文件数会大大减少。提高join的时间性能。

`spark.reducer.maxSizeInFilght`: reduce task能够拉取多少数据量（默认48M），当集群资源足够时，增大此参数可减少reduce拉取数据量的次数，从而达到优化shuffle的效果，一般调大为96M，资源够大可继续往上调

`spark.shuffle.file.buffer`: 每个shuffle文件输出流的内存缓冲区大小（默认32K），调大此参数可以减少在创建shuffle文件时进行**磁盘搜索**和**系统调用**的次数，一般调大为64k

### Spark的内存是怎么管理的

Spark内存分为堆内内存和堆外内存，其中堆内内存基于JVM实现，堆外内存则是通过调用JDK Unsafe API管理。在Spark1.6版本前后内存管理模式分为: 静态管理(Static Memory)和统一管理(Unified Memory)。

### Spark的广播变量和累加器的作用是什么

Executor接收到TaskScheduler的taskset分发命令，根据rdd分区数在ThreadPool中创建对应的Task线程，每个Task线程拉取并序列化代码，启动分布式计算。

<img src="https://files.mdnice.com/user/23269/4cf7ad53-a7ec-497e-ad25-3884fbf9cf86.png" alt="img" style="zoom:67%;" />

Spark在计算过程中的算子函数、变量都会由Driver分发到每台机器中，每个Task持有该变量的一个副本拷贝。可是这样会存在两个问题:

> 1. 是否可以只在Executor中存放一次变量，所有Task共享?
> 2. 分布式计算场景下怎么可以做到全局计数

**1. 广播变量(Broadcast)**

在Driver端使用broadcast()将一些`大变量`(List、Array)持久化，Executor根据broadcastid拉取本地缓存中的Broadcast对象，如果不存在，则尝试远程拉取Driver端持久化的那份Broadcast变量。

<img src="https://files.mdnice.com/user/23269/6d0fbbbb-f84f-4013-b084-d38b27de7e7d.png" alt="img" style="zoom:67%;" />

这样所有的Executor均存储了一份变量的备份，这个executor启动的task会共享这个变量，节省了通信的成本和服务器的资源。注意不能广播RDD，因为RDD不存储数据；同时广播变量只能在Driver端定义和修改，Executor端只能读取。

```scala
val sc = new SparkContext(conf)
val list = List('hello world')

//定义broadcast变量
val broadcastVal = sc.broadcast(list)
val dataRDD = sc.textFile('./test.txt')

//broadcast变量读取
dataRDD.filter{x => broadcastVal.value
     .contains(x)}.foreach{println}
```

**2. 累加器(Accumulator)**

Spark累加器支持在Driver端进行全局汇总的计算需求，实现分布式计数的功能。累加器在Driver端定义赋初始值，在Excutor端更新，最终在Driver端读取最后的汇总值。

<img src="https://files.mdnice.com/user/23269/cf2ca20b-13b8-46a8-8835-57535fc13fb0.png" alt="img" style="zoom:67%;" />

```scala
val sc = new SparkContext(conf)
// 定义累加器
val accumulator = sc.accumulator(0)
// 累加器计算
sc.textFile('./test.txt').foreach{x =>
      {accumulator.add(1)}}
// 累加器读数
println(accumulator.value)
```

### Spark SQL和Hive SQL的区别

Hive SQL是Hive提供的SQL查询引擎，底层由MapReduce实现。Hive根据输入的SQL语句执行词法分析、语法树构建、编译、逻辑计划、优化逻辑计划以及物理计划等过程，转化为Map Task和Reduce Task最终交由`Mapreduce`引擎执行。

- 执行引擎。具有mapreduce的一切特性，适合大批量数据离线处理，相较于Spark而言，速度较慢且IO操作频繁
- 有完整的`hql`语法，支持基本sql语法、函数和udf
- 对表数据存储格式有要求，不同存储、压缩格式性能不同

![img](https://files.mdnice.com/user/23269/a868f39b-ff86-4a17-ab39-a96f8678edaf.png)

Spark SQL底层基于`Spark`引擎，使用`Antlr`解析语法，编译生成逻辑计划和物理计划，过程和Hive SQL执行过程类似，只不过Spark SQL产生的物理计划为Spark程序。

- 执行引擎。背靠Spark计算模型，基于内存计算快速高效。
- 可支持SQL和DataFrame等形式，底层转化为Spark算子参与计算。
- 集成了HiveContext接口，基本实现Hive功能

### 说下Spark SQL的执行流程

可以参考Hive SQL的执行流程展开叙述，大致过程一致，具体执行流程如下:

- 输入编写的Spark SQL
- `SqlParser`分析器。进行语法检查、词义分析，生成未绑定的Logical Plan逻辑计划(未绑定查询数据的元数据信息，比如查询什么文件，查询那些列等)
- `Analyzer`解析器。查询元数据信息并绑定，生成完整的逻辑计划。此时可以知道具体的数据位置和对象，Logical Plan 形如from table -> filter column -> select 形式的树结构
- `Optimizer`优化器。选择最好的一个Logical Plan，并优化其中的不合理的地方。常见的例如谓词下推、剪枝、合并等优化操作
- `Planner`使用Planing Strategies将逻辑计划转化为物理计划，并根据最佳策略选择出的物理计划作为最终的执行计划
- 调用Spark Plan `Execution`执行引擎执行Spark RDD任务

![img](https://files.mdnice.com/user/23269/6dc81b0a-ef7e-43d5-84f5-57796334e2b5.png)

### RDD、DataFrame和DataSet的区别

**1. RDD和DataFrame、Dataset的共性**

三者均为Spark分布式弹性数据集，Spark 2.x 的DataFrame被Dataset合并，现在只有DataSet和RDD。三者有许多相同的算子如filter、map等，且均具有惰性执行机制。

<img src="https://files.mdnice.com/user/23269/695d0fcd-33cf-4e90-9641-a546506903f5.png" alt="img" style="zoom: 67%;" />

**2. DataFrame和DataSet的区别**

DataFrame是分布式Row对象的集合，所有record类型均为Row。Dataset可以认为是DataFrame的特例，每个record存储的是强类型值而不是Row，同理Dataframe可以看作Dataset[Row]。

**3. RDD、DataFrame和Dataset转换**

- DataFrame/DataSet转换为RDD

```scala
val rdd1 = myDF.rdd
```

- RDD转换为DataFrame/Dataset (spark低版)

~~~scala
import spark.implicits._
val myDF = rdd.map {
 line=> (line._1,line._2)}
 .toDF("col1","col2")
```****
- RDD转换为Dataset
```scala
import spark.implicits._

case class ColSet(col1:String,col2:Int) extends Serializable 
val myDS = rdd.map {row=>
  ColSet(row._1,row._2)
}.toDS
~~~

**4. Spark SQL中的RDD和Dataset**

RDD无法支持Spark sql操作，而dataframe和dataset均支持。

### TopN※💯

**方法1：**

（1）按照key对数据进行聚合（groupByKey）

（2）将value转换为数组，利用scala的sortBy或者sortWith进行排序（mapValues）数据量太大，会OOM。

**方法2：**

（1）取出所有的key

（2）对key进行迭代，每次取出一个key利用spark的排序算子进行排序

**方法3：**

（1）自定义分区器，按照key进行分区，使不同的key进到不同的分区

（2）对每个分区运用spark的排序算子进行排序

### 连续登录N天的用户※💯

**思路**

- 将用户分组并按照时间排序，并记录rank排名
- 计算dt-rank的差值，差值与用户共同分组
- 统计count并找出 count > N 的用户

https://blog.csdn.net/lovetechlovelife/article/details/115013020

## Spark Streaming

## Spark 优化

### 数据倾斜优化

参考[美团一篇文章][4]

## Reference

[1]: https://andr-robot.github.io/Spark%E4%BD%9C%E4%B8%9A%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B/
[2]: https://blog.csdn.net/gamer_gyt/article/details/79135118 "spark 参数"
[3]: https://bbs.huaweicloud.com/blogs/326863	"2022最全Spark面试体系"
[4]: https://tech.meituan.com/2016/05/12/spark-tuning-pro.html	"Spark性能优化指南——高级篇"
[5]: https://cloud.tencent.com/developer/article/1004904	"Spark Scheduler 内部原理剖析"



***
下面的这个需要时间去研究
[[SPARK][CORE] 面试问题之什么是 external shuffle service？] (https://cloud.tencent.com/developer/article/2020877?pos=comment)