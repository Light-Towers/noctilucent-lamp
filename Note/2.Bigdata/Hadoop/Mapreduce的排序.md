> 见 https://blog.csdn.net/L13763338360/article/details/119730498



## MR 参数优化

1.配置map输入合并
--每个Map最大输入大小，决定合并后的文件数。
Set mapred.max.split.size=256000000;
--一个节点上split的至少的大小，决定了多个datanode上的文件是否需要合并
Set mapred.min.split.size.per.node=100000000;
--一个交换机下split的至少的大小，决定了多个交换机上的文件是否需要合并
Set mapred.min.split.size.per.rack=100000000;
--执行Map前进行小文件合并
Set hive.input.format=org.apache.hadoop.hive.ql.io.CombineHiveInputFormat;

2.配置hive结果合并。
hive.merge.mapfiles 在map-only job后合并文件，默认true
hive.merge.mapredfiles 在map-reduce job后合并文件，默认false
hive.merge.size.per.task 合并后每个文件的大小，默认256000000
hive.merge.smallfiles.avgsize 平均文件大小，是决定是否执行合并操作的阈值，默认16000000