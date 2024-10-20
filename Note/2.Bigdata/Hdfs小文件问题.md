## 何为小文件？

小文件是指文件大小显著小于hdfs block块大小的的文件。

## 小文件问题

HDFS不适合大量小文件的存储，因namenode将文件系统的元数据存放在内存中，因此存储的文件数目受限于 namenode的内存大小。HDFS中每个文件、目录、数据块占用150Bytes。如果存放的文件数目过多的话会占用很大的内存甚至撑爆内存。HDFS适用于高吞吐量，而不适合低时间延迟的访问。如果同时存入大量的小文件会花费很长的时间。



## Spark

Spark2.4后合并小文件：Hint

```sql
SELECT /*+ COALESCE(3) */                  * FROM t
SELECT /*+ REPARTITION(3) */               * FROM t
SELECT /*+ REPARTITION(c) */               * FROM t
SELECT /*+ REPARTITION(3, c) */            * FROM t
SELECT /*+ REPARTITION_BY_RANGE(c) */      * FROM t
SELECT /*+ REPARTITION_BY_RANGE(3, c) */   * FROM t
-- 注：c为重新分区的分区键
-- 或者在 spark.sql(sql).repartition(1)/coalesce(1)
```

* COALESCE提示减少了分区数。它仅合并分区，因此最大程度地减少了数据移动；
* REPARTITION提示可以增加或减少分区数量。它执行数据的完全混洗，并确保数据平均分配；
* REPARTITION增加了一个新阶段，因此它不会影响现有阶段的并行性。相反，COALESCE确实会影响现有阶段的并行性，因为它不会添加新阶段；