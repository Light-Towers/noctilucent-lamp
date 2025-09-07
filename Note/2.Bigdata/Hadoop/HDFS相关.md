# Gateway

# HttpFS

# NFS Gateway
Hadoop NFS Gateway 是一个允许客户端通过NFS (网络文件系统) 协议访问HDFS (Hadoop Distributed File System) 的服务。简单来说，NFS Gateway 使得用户可以将HDFS 挂载到本地文件系统，就像访问本地硬盘一样，从而方便地在HDFS 和其他系统之间传输数据。

HDFS NFS Gateway 提供了以下功能:
- 通过NFS 协议访问HDFS: 允许用户使用NFS 客户端(如Linux 或macOS) 挂载HDFS 目录，就像访问本地目录一样。
- 支持NFSv3 协议: NFS Gateway 目前支持NFSv3 协议，这意味着它可以与兼容NFSv3 的操作系统进行交互。
- 数据上传下载: 用户可以方便地将文件从本地文件系统上传到HDFS，或者从HDFS 下载文件到本地文件系统。
- 流式数据传输: 支持通过挂载点将数据直接流式传输到HDFS，例如，可以使用 dd 命令将数据流式写入到HDFS。
- 文件追加: 支持文件追加操作，但不支持随机写入。

总结一下，HDFS NFS Gateway 的主要作用是：
- 简化HDFS 访问: 通过NFS 协议，用户无需使用Hadoop 客户端，即可方便地访问HDFS 数据。
- 促进数据共享: NFS Gateway 使得HDFS 可以与其他系统进行数据共享，例如，可以将HDFS 挂载到数据分析服务器，方便进行数据分析。
- 提高易用性: 对于习惯于使用本地文件系统操作的用户来说，NFS Gateway 提供了更直观、更方便的HDFS 访问方式。


# JournalNode

# SecondaryNameNode

# NameNode

# Balancer
HDFS Balancer工具可以用来分析块的分布情况，并且可以重新分配DataNode中的数据。
## 背景信息
HDFS采用主从架构，其中NameNode管理文件系统的元数据（例如文件名、文件的块信息及其位置），而实际的数据块则存储在多个DataNode上。这种架构允许数据冗余存储，提高了系统的容错能力。

随着时间的推移，由于文件的添加、删除和修改操作，DataNodes之间的数据分布可能会变得不均衡，某些节点的存储空间可能接近饱和，而其他节点可能有大量的空闲空间。这种不均衡不仅影响了系统的存储效率，还可能增加数据丢失的风险，因为过度填充的节点更易受硬件故障的影响。

为了应对这一问题，HDFS提供了Balancer工具，这是一个命令行实用程序，旨在自动重新平衡DataNodes间的数据分布。Balancer通过移动数据块来减少各节点间的存储不均衡，确保整个集群的存储资源得到更有效的利用。

```bash
hdfs dfsadmin -report
```
## 启动HDFS Balancer
使用HDFS Balancer命令
```bash
hdfs balancer
[-threshold <threshold>]
[-policy <policy>]
[-exclude [-f <hosts-file> | <comma-separated list of hosts>]]
[-include [-f <hosts-file> | <comma-separated list of hosts>]]
[-source [-f <hosts-file> | <comma-separated list of hosts>]]
[-blockpools <comma-separated list of blockpool ids>]
[-idleiterations <idleiterations>]
```

Balancer主要参数如下表。

| **参数**           | **描述**                                                     |
| ------------------ | ------------------------------------------------------------ |
| **threshold**      | 磁盘容量的百分数。默认值为10%，表示上下浮动10%。当集群总使用率较高时，需要调小Threshold，避免阈值过高。当集群新增节点较多时，您可以适当增加Threshold，使数据从高使用率节点移向低使用率节点。 |
| **policy**         | 平衡策略。支持以下策略：datanode（默认）：当每一个DataNode是平衡的时候，集群就是平衡的。blockpool：当每一个DataNode中的blockpool是平衡的，集群就是平衡的。 |
| **exclude**        | Balancer排除特定的DataNode。                                 |
| **include**        | Balancer仅对特定的DataNode进行平衡操作。                     |
| **source**         | 仅选择特定的DataNode作为源节点。                             |
| **blockpools**     | Balancer仅在指定的blockpools中运行。                         |
| **idleiterations** | 最多允许的空闲循环次数。覆盖默认的5次。                      |

（可选）执行以下命令，修改Balancer的最大带宽。

```bash
hdfs dfsadmin -setBalancerBandwidth <bandwidth in bytes per second>
```

> `<bandwidth in bytes per second>`为设置的最大带宽。例如，如果需要设置带宽控制为200 MB/s，对应值为200 * 1024 * 1024B，即209715200字节，则完整代码示例为`hdfs dfsadmin -setBalancerBandwidth 209715200`。为优化网络资源利用并保障核心业务流畅，在集群高负载情形下，建议适度削减数据平衡带宽，可以改为20971520（20 MB/s）；在集群空闲时，为了加速数据均衡过程，建议将数据平衡带宽提高，可以改为1073741824（1 GB/s）。

查看日志，当提示信息包含`Successfully`字样时，表示执行成功。

```bash
tail -f /var/log/hadoop-hdfs/hadoop-hdfs-balancer-emr-header-1.cluster-xxx.log
```

## Balancer调优参数

执行Balancer会占用一定的系统资源，建议在业务空闲期执行。默认情况下，不需要对HDFS Balancer参数进行额外调整。当需要对Balancer参数进行额外调整时，可以配置  hdfs-site.xml，调整以下两类配置。

- 客户端配置

  | **参数**                           | **描述**                                                     |
  | ---------------------------------- | ------------------------------------------------------------ |
  | **dfs.balancer.dispatcherThreads** | Balancer在移动Block之前，每次迭代时查询出一个Block列表，分发给Mover线程使用。说明**dispatcherThreads**是该分发线程的个数，默认为200。 |
  | **dfs.balancer.rpc.per.sec**       | 默认值为20，即每秒发送的RPC数量为20。因为分发线程调用大量getBlocks的RPC查询，所以为了避免NameNode由于分发线程压力过大，需要控制分发线程RPC的发送速度。例如，您可以在负载高的集群调整参数值，减小10或者5，对整体移动进度不会产生特别大的影响。 |
  | **dfs.balancer.getBlocks.size**    | Balancer会在移动Block前，每次迭代时查询出一个Block列表，给Mover线程使用，默认Block列表中Block的大小为2 GB。因为getBlocks过程会对RPC进行加锁，所以您可以根据NameNode压力进行调整。 |
  | **dfs.balancer.moverThreads**      | 默认值为1000。Balancer处理移动Block的线程数，每个Block移动时会使用一个线程。 |

- DataNode配置

  | **参数**                                      | **描述**                                                     |
  | --------------------------------------------- | ------------------------------------------------------------ |
  | **dfs.datanode.balance.bandwidthPerSec**      | 指定DataNode用于Balancer的带宽，通常推荐设置为100 MB/s，您也可以通过**dfsadmin -setBalancerBandwidth** 参数进行适当调整，无需重启DataNode。例如，在负载低时，增加Balancer的带宽。在负载高时，减少Balancer的带宽。 |
  | **dfs.datanode.balance.max.concurrent.moves** | 默认值为5。指定DataNode节点并发移动的最大个数。通常考虑和磁盘数匹配，推荐在DataNode端设置为`4 * 磁盘数`作为上限，可以使用Balancer的值进行调节。例如：一个DataNode有28块盘，在Balancer端设置为28，DataNode端设置为`28 * 4`。具体使用时根据集群负载适当调整。在负载较低时，增加concurrent数；在负载较高时，减少concurrent数。 |

## 常见问题

Q：为什么Balancer的threshold设置为10（%），但是平衡以后看到差值为20%左右？

A：threshold的含义是控制每个DataNode的使用率不高于或者不低于集群平均的使用率，所以使用率最多和最少的DataNode在平衡后可能差值为20%。要减少这种差距，可以尝试把差值调节到5（%）。



> 参考： [阿里云EMR-HDFS Balancer](https://help.aliyun.com/zh/emr/emr-on-ecs/user-guide/hdfs-balancer)



# Failover Controller

HDFS 中的Failover Controller (FC)，也常被称为 [ZKFC](https://www.google.com/search?newwindow=1&sca_esv=e5c02846db75933a&cs=1&q=ZKFC&sa=X&ved=2ahUKEwjNl7LKpbGOAxXoVPUHHVeMJvMQxccNegQIAhAB&mstk=AUtExfD-YWv9qfOmXWW3mBMF3Wp4WoAczQTw5GTxgWOT_zHyQ_F_tOcckLxgXCj10Pm0ekY6tgxGYCf_JhJMLocLebvQ6T-iWuW2usIivH0TagHm73CvbNdetI8CAE1qMx-nyQw3ABTQHQUP-RQlfxj2JVOzDl8wfXrH9FDKbiZw0IhkbfCSVeRhPj9ZCPjYZI200LL_&csui=3) (ZooKeeper Failover Controller)，是HDFS 高可用(HA) 机制中的核心组件之一。

它的主要作用是监控NameNode 的状态，并在主NameNode 出现故障时，自动将备用NameNode 切换为活动状态，从而实现HDFS 的高可用性。

Failover Controller 的工作原理如下：

1. **监控NameNode:**

   ZKFC 持续监控活动NameNode 的健康状态，包括NameNode 进程是否存活、是否能够响应请求等。

2. **与 ZooKeeper 交互:**

   ZKFC 通过ZooKeeper 选举来保证集群中只有一个活动NameNode。

   它会在ZooKeeper 中创建并维护一个表示活动NameNode 的锁。

3. **故障检测:**

   如果活动NameNode 出现故障，ZKFC 会检测到NameNode 停止响应，或者无法获取ZooKeeper 中的锁。

4. **故障转移:**

   发现故障后，ZKFC 会触发故障转移操作，将备用NameNode 切换为活动状态，并获取ZooKeeper 中的锁，接管NameNode 的工作。

5. **状态同步:**

   在故障转移过程中，ZKFC 还会负责同步NameNode 的状态信息，确保新的活动NameNode 能够接管之前的工作，避免数据丢失或不一致。﻿

## QJM

QJM([Quorum Journal Manager](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HDFSHighAvailabilityWithQJM.html)) 方案是Hadoop HDFS中实现高可用(HA)的一种重要机制，它利用JournalNode节点集群来共享EditLog，并基于Quorum机制保证数据一致性。

简单来说，QJM 方案通过以下方式实现HDFS 的高可用：

1. **JournalNode 集群:**

   QJM 使用多个JournalNode 节点(通常是奇数个，如3个或5个) 组成一个集群，用于存储和共享HDFS 的EditLog。

2. **EditLog 共享:**

   当Active NameNode 进行元数据变更时，会将EditLog 写入到JournalNode 集群。

   QJM 保证只有当大多数JournalNode 成功写入后，才认为写入成功。

3. **Standby NameNode 同步:**

   Standby NameNode 会从JournalNode 集群读取EditLog，并将其应用到自身，从而保持与Active NameNode 的状态一致。

4. **主备切换:**

   当Active NameNode 出现故障时，Standby NameNode 可以快速接管，并从JournalNode 集群读取最新的EditLog，保证了HDFS 的高可用性。



QJM 的核心思想是Quorum机制，即“多数派”原则:

- 在写入EditLog 时，需要大多数JournalNode 节点成功返回，才能认为写入成功。
- 在读取EditLog 时，也需要大多数JournalNode 节点返回，才能认为读取成功。

QJM 相较于其他HDFS HA 方案(如NFS) 的优势:

- **更高的可靠性:**

  通过Quorum 机制，QJM 可以在一定数量的JournalNode 节点故障的情况下，仍然保证数据的一致性和可用性.

- **更好的性能:**

  QJM 通过优化EditLog 的写入和读取，可以提供比NFS 更好的性能.

- **更强的可扩展性:**

  QJM 允许添加更多的JournalNode 节点来扩展集群的容量和可靠性.

  

# DataNode



#
```bash
hdfs dfsadmin -report         # 显示集群状态
# 查看是否有节点被排除
hdfs dfsadmin -listExcludedNodes
# 进入安全模式
hdfs dfsadmin -safemode forceExit  # 退出安全模式
# 刷新节点
hdfs hdfs dfsadmin -refreshNodes

# 查看存储策略
hdfs storagepolicies -listPolicies

hdfs fsck / -files -blocks -locations     # 检查块报告一致性

hdfs fsck / -list-corruptfileblocks       # 查找孤儿块
# 删除孤儿块（谨慎操作！）
## hdfs fsck / -delete

hdfs debug -recoverLease -path <文件路径>  # 恢复文件租约（若卡住）
hdfs fsck / -list-corruptfileblocks      # 列出所有损坏块

# 检查文件
hdfs fsck /user/hive/warehouse/dw/app_exhibition_actor/dt=2025-04-22/part-00189-9b2039f4-c757-45b1-be64-85afa14390fb.c000 -files -blocks -locations

# 获取配置：查看排除的节点
hdfs getconf -confKey dfs.hosts.exclude
```