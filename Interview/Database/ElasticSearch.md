1. elasticsearch 了解多少，说说你们公司 es 的集群架构，索引数据大小，分片有多少，以及一些调优手段 。
2. ElasticSearch对于大数据量（上亿量级）的聚合如何实现
3. 详细描述一下 Elasticsearch 索引文档的过程
4. elasticsearch 数据预热
5. 如何使用 Elasticsearch Tokenizer？
6. elasticsearch 数据的写入原理
7. 你是如何做 ElasticSearch 写入调优的？
8. Elasticsearch是如何实现Master选举的？
9. Master 节点和 候选 Master节点有什么区别？
11. ElasticSearch主分片数量可以在后期更改吗？为什么？
    1. [ES经典面试题：为什么主分片的数目不能修改？]()
11. 客户端在和集群连接时，是如何选择特定的节点执行请求的？
12. 对于 GC 方面，在使用 Elasticsearch 时要注意什么？
13. 拼写纠错是如何实现的？
14. 在Elasticsearch中 cat API的功能是什么？
16. [京东面试题：ElasticSearch深度分页解决方案](https://developer.aliyun.com/article/894729)







## 一、调优

### 1.1 设计阶段调优

1、根据业务增量需求，采取基于日期模板创建索引，通过 roll over API 滚动索引；

2、使用别名进行索引管理；

3、每天凌晨定时对索引做 force_merge 操作，以释放空间；

4、采取冷热分离机制，热数据存储到 SSD，提高检索效率；冷数据定期进行 shrink操作，以缩减存储；

5、采取 curator 进行索引的生命周期管理；

6、仅针对需要分词的字段，合理的设置分词器；

7、Mapping 阶段充分结合各个字段的属性，是否需要检索、是否需要存储等

### 1.2 写入调优

1、写入前副本数设置为 0；

2、写入前关闭 refresh_interval 设置为-1，禁用刷新机制；

3、写入过程中：采取 bulk 批量写入；

4、写入后恢复副本数和刷新间隔；

5、尽量使用自动生成的 id。

### 1.3 查询调优

1、禁用 wildcard；

2、禁用批量 terms（成百上千的场景）；

3、充分利用倒排索引机制，能 keyword 类型尽量 keyword；

4、数据量大时候，可以先基于时间敲定索引再检索；

5、设置合理的路由机制。

### 1.4 其它调优

部署调优，业务调优等。



> 参考：[24道进阶必备Elasticsearch 面试真题](https://zhuanlan.zhihu.com/p/102500311)
