## 性能优化

系统性能调优要结合业务，软硬件一起分析，调整。

需要研究用大硬盘(16T)还是小硬盘(6T)，用大硬盘节省盘位，有利于扩展，但IO性能肯定不如多个小硬盘同时工作。也与raid卡的性能有关，raid卡性能带宽和cache大小，能算出来最优方案
常见的raid卡带宽是12g bit每秒，cache是2g
硬盘缓存是256m或者512m
机械硬盘写就是80M Byte，相当于480M bit

**是否可混用？**

可以混用，创建不同的raid卷，用不同磁盘大小，不同的cache模式

**cdh对磁盘的管理。是不是可以自动管理数据存储到不同分区?**



raid其实是服务器领域最便宜的存储高可用方案，最低端服务器用法。小型机和大型机都用SAN存储网络或者其他

存储有几个方面的诉求，一是存储量要大，二是要稳定，三是性能高，同时要考虑成本。从这几个维度，按照成本从低到高是 LVM、Raid、ip网络存储（NAS和iSCSI这些）、SAN，中间再结合磁带做离线备份。

欧拉操作系统已经和apache基金会合作: https://gitee.com/src-openeuler/ambari







1. ## 生产事故报告

   ### 一、事故概述

   2023年11月1日上午8点左右，我们发现大数据平台Hive服务无法使用。经过仔细排查，我们发现问题源于39节点和41节点的磁盘故障。这些磁盘采用了RAID0阵列，但在尝试更换其中故障磁盘时未能成功，导致39节点和41节点的数据全部丢失（副本数据也丢失），仅40节点保留了一份数据。

   ### 二、事故详细信息

   1. 事故时间：2023年11月1日上午8点。
   2. 事故地点：大数据平台Hive服务所在的39节点和41节点。
   3. 事故原因：磁盘故障导致的数据丢失。
   4. 故障节点：39节点和41节点。
   5. 故障磁盘阵列：RAID0。
   6. 数据恢复情况：39节点和41节点的数据完全丢失，仅40节点保留了一份数据。

   ### 三、事故解决措施

   我们采取了以下步骤来解决这个问题：

   1. 元数据备份：Mysql数据备份（Hive、udf、DS）， 2NN的Checkpoint数据。
   2.  节点恢复：我们首先将39节点和41节点恢复到正常状态，并确保磁盘空间恢复至16T。
      1. 39节点重装系统，修改hostname，SSH免密、时间同步服务ntp。
   3. CDH平台恢复：
      1. 39重新离线安装CM所需软件([安装步骤](http://wiki.mingyang100.com:8190/pages/viewpage.action?pageId=17007040)：Mysql、JDK、Cloudera-scm-agent等)
      2. 将39节点加入guoran_dw1集群失败。**39节点剔除集群后，无法再加入集群，后来重启cloudera-scm-server后自动识别加入成功。**
      3. 在39节点上创建NN实例，从SNN中拷贝恢复的数据。并创建DN等其它实例。
      4. 遇到DN下线问题：删除DN实例(解除授权、删除)，重新创建DN实例，DN正常，数据开始正常同步。
   4. 其他服务恢复：
      1. 39添加其余的角色。其中ZK、Hive、Tez、Spark做过升级，从40节点将包拷贝至39节点。
      2. Hive Matastore服务恢复：关闭Mysql的SSL认证。/etc/my.cnf 增加一行 skip-ssl
      3. DS: 先恢复ds数据库，重新下载3.1.7官方tar包，将40上/opt/apache-dolphinscheduler-3.1.7/bin/env 目录下dolphinscheduler_env.sh和 install_env.sh 复制到39的env目录下， Mysql 8.0-jar驱动加入 master-server/libs、alert-server/libs/、api-server/libs/、standalone-server/libs/、worker-server/libs/ ，执行一键安装部署脚本${dolphinscheduler_home}/bin/install.sh
   5. 解决datanode数据写入到39和41慢的问题：将write though(默认模式：透写模式) 改成write back(回写模式) 

   ​     **原理**：当选用write through方式时，系统的写磁盘操作并不利用阵列卡的Cache，而是直接与磁盘进行数据的交互。而write Back方式则利用阵列Cache作为系统与磁盘间的二传手，系统先将数据交给Cache，然后再由Cache将数据传给磁盘。

   ​          write through 适合读，性能(IOPS和吞吐量)是write back的两倍。write back适合写，性能(IOPS和吞吐量)是write through的7倍。

   ​     **场景**：从40上读数据，写到39和41磁盘上，write through 预估两天时间48h ,改成write back之后，11h 完成，效率提高很多

   ### 四、预防措施和建议

   为了避免类似问题再次发生，我们建议采取以下预防措施：

   1. 服务高可用：对于单点的服务(Mysql、Hdfs、DS)
   2. 定期检查和维护磁盘阵列，包括RAID0和RAID1等配置。
   3. 定期备份数据，确保副本数据可用。
   4. 对于关键节点，建议采用更高可靠性的磁盘阵列配置，如RAID10。
   5. 加强系统监控和告警机制，及时发现并处理潜在问题。
   6. 对所有服务进行定期测试和验证，确保其可用性和可靠性。