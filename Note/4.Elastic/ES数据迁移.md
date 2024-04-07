## 跨集群数据迁移

| 方案                            | 优点                                                         | 缺点                                                         | 适用场景                                                     |
| :------------------------------ | :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| **数据目录拷贝**                | 简单快速，无需额外工具<br/>适用于同版本迁移，且集群配置相同 | 不适用于不同版本或配置差异较大的情况<br/>风险较高，易受中断影响<br/>无法增量迁移 | 紧急迁移或测试环境迁移<br/>对数据一致性要求较低时 |
| **Snapshot & Restore**          | 支持跨版本迁移<br/>提供数据一致性保障<br/>可通过共享存储进行远程迁移<br/>支持增量迁移 | 需要预先配置快照仓库<br/>依赖于网络带宽和存储性能<br/>恢复过程中目标集群不可用（除非使用Shared FS或S3等可并发恢复的存储） | 生产环境迁移<br/>定期备份与恢复<br/>跨数据中心迁移 |
| **Reindex API**                 | 支持跨集群、跨版本迁移<br/>可进行数据转换和过滤<br/>迁移过程中源集群仍可接受写操作 | 消耗源集群资源（CPU、内存、网络）<br/>迁移速度受限于源集群性能及网络带宽<br/>不适合大规模数据迁移，可能导致性能下降 | 数据结构调整或迁移时需数据转换<br/>源集群需保持在线，且性能允许<br/>数据量适中，迁移窗口较长 |
| **Logstash**                    | 高度灵活，支持复杂的数据处理管道<br/>可利用批量写入提高效率<br/>支持多种输入/输出方式，适应不同源/目标环境 | 需配置和管理Logstash<br/>额外资源消耗（CPU、内存、网络）<br/>迁移速度取决于Logstash配置及源/目标性能 | 数据清洗、转换需求复杂<br/>源/目标环境差异较大，需定制化处理<br/>数据量适中，对实时性有一定要求 |
| **elasticsearch-dump (ESDUMP)** | 轻量级，易于使用<br/>支持多种格式（JSON、CSV）导出<br/>支持跨版本迁移 | 功能相对有限，不支持复杂数据处理或转换<br/>依赖于磁盘空间，大型数据集可能需要大量临时存储<br/>迁移速度可能低于原生API方法 | 小型至中型数据集迁移<br/>需要将数据导出为特定格式（如JSON或CSV）供其他系统使用<br/>对迁移工具复杂度要求较低的场景 |

### 1.elasticsearch-dump

#### 使用方式

elasticsearch-dump是一款开源的ES数据迁移工具，github地址: [https://github.com/taskrabbit/elasticsearch-dump](https://cloud.tencent.com/developer/tools/blog-entry?target=https%3A%2F%2Fgithub.com%2Ftaskrabbit%2Felasticsearch-dump&source=article&objectId=1145944)

1. 安装elasticsearch-dump

elasticsearch-dump使用node.js开发，可使用npm包管理工具直接安装：

```text
npm install elasticdump -g
```

2. 主要参数说明

```text
--input: 源地址，可为ES集群URL、文件或stdin,可指定索引，格式为：{protocol}://{host}:{port}/{index}
--input-index: 源ES集群中的索引
--output: 目标地址，可为ES集群地址URL、文件或stdout，可指定索引，格式为：{protocol}://{host}:{port}/{index}
--output-index: 目标ES集群的索引
--type: 迁移类型，默认为data,表明只迁移数据，可选settings, analyzer, data, mapping, alias
--limit：每次向目标ES集群写入数据的条数，不可设置的过大，以免bulk队列写满
```

3. 迁移单个索引

 以下操作通过elasticdump命令将集群172.16.0.39中的companydatabase索引迁移至集群172.16.0.20。注意第一条命令先将索引的settings先迁移，如果直接迁移mapping或者data将失去原有集群中索引的配置信息如分片数量和副本数量等，当然也可以直接在目标集群中将索引创建完毕后再同步mapping与data

```text
elasticdump --input=http://172.16.0.39:9200/companydatabase --output=http://172.16.0.20:9200/companydatabase --type=settings
elasticdump --input=http://172.16.0.39:9200/companydatabase --output=http://172.16.0.20:9200/companydatabase --type=mapping
elasticdump --input=http://172.16.0.39:9200/companydatabase --output=http://172.16.0.20:9200/companydatabase --type=data
```

4. 迁移所有索引：

   以下操作通过elasticdump命令将将集群172.16.0.39中的所有索引迁移至集群172.16.0.20。 注意此操作并不能迁移索引的配置如分片数量和副本数量，必须对每个索引单独进行配置的迁移，或者直接在目标集群中将索引创建完毕后再迁移数据

```text
elasticdump --input=http://172.16.0.39:9200 --output=http://172.16.0.20:9200
```

### 2.snapshot

#### 使用方式

snapshot api是[Elasticsearch](https://cloud.tencent.com/product/es?from_column=20065&from=20065)用于对数据进行备份和恢复的一组api接口，可以通过snapshot api进行跨集群的数据迁移，原理就是从源ES集群创建数据快照，然后在目标ES集群中进行恢复。需要注意ES的版本问题：

> 目标ES集群的主版本号(如5.6.4中的5为主版本号)要大于等于源ES集群的主版本号;
> 1.x版本的集群创建的快照不能在5.x版本中恢复;

1. 源ES集群中创建repository

创建快照前必须先创建repository仓库，一个repository仓库可以包含多份快照文件，repository主要有一下几种类型

```text
fs: 共享文件系统，将快照文件存放于文件系统中
url: 指定文件系统的URL路径，支持协议：http,https,ftp,file,jar
hdfs/s3/oss/cos: 快照存放于分布式文件系统中，以插件形式支持
```

在Elasticsearch配置文件elasticsearch.yml设置仓库路径：

```text
path.repo: ["/usr/local/services/test"]
```

之后调用snapshot api创建repository：

```text
curl -XPUT http://172.16.0.39:9200/_snapshot/my_backup -H 'Content-Type: application/json' -d '{
        "type": "fs",
        "settings": {
        "location": "/usr/local/services/test" 
        "compress": true
    }
}'
```

如果需要从其它云厂商的ES集群迁移至腾讯云ES集群，或者腾讯云内部的ES集群迁移，可以使用对应云厂商他提供的仓库类型，如AWS的S3, 阿里云的OSS，腾讯云的COS等

```text
curl -XPUT http://172.16.0.39:9200/_snapshot/my_s3_repository
    {
        "type": "s3",
        "settings": {
        "bucket": "my_bucket_name",
        "region": "us-west"
    }
}
```

2. 源ES集群中创建snapshot

调用snapshot api在创建好的仓库中创建快照

```text
curl -XPUT http://172.16.0.39:9200/_snapshot/my_backup/snapshot_1?wait_for_completion=true
```

创建快照可以指定索引，也可以指定快照中包含哪些内容，具体的api接口参数可以查阅官方文档

3. 目标ES集群中创建repository

目标ES集群中创建仓库和在源ES集群中创建仓库类似，用户可在腾讯云上创建COS对象bucket， 将仓库将在COS的某个bucket下。

4. 移动源ES集群snapshot至目标ES集群的仓库

把源ES集群创建好的snapshot上传至目标ES集群创建好的仓库中。(如果找不到快照，可重新注册下仓库)

5. 从快照恢复

```text
curl -XPUT http://172.16.0.20:9200/_snapshot/my_backup/snapshot_1/_restore
```

6. 查看快照恢复状态

```text
curl http://172.16.0.20:9200/_snapshot/_status
```

### 3.reindex

reindex是Elasticsearch提供的一个api接口，可以把数据从源ES集群导入到当前的ES集群，同样实现了数据的迁移，限于腾讯云ES的实现方式，当前版本不支持reindex操作。简单介绍一下reindex接口的使用方式。

1. 配置reindex.remote.whitelist参数

需要在目标ES集群中配置该参数，指明能够reindex的远程集群的白名单

2. 调用reindex api

以下操作表示从源ES集群中查询名为test1的索引，查询条件为title字段为elasticsearch，将结果写入当前集群的test2索引

```text
POST _reindex
{
      "source": {
        "remote": {
              "host": "http://172.16.0.39:9200"
        },
        "index": "test1",
        "query": {
              "match": {
                "title": "elasticsearch"
              }
        }
      },
      "dest": {
            "index": "test2"
      }
}
```

### 4.logstash

logstash支持从一个ES集群中读取数据然后写入到另一个ES集群，因此可以使用logstash进行数据迁移，具体的配置文件如下：

```text
input {
    elasticsearch {
        hosts => ["http://172.16.0.39:9200"]
        index => "*"
        docinfo => true
    }
}
output {
    elasticsearch {
        hosts => ["http://172.16.0.20:9200"]
        index => "%{[@metadata][_index]}"
    }
}
```

上述配置文件将源ES集群的所有索引同步到目标集群中，当然可以设置只同步指定的索引，logstash的更多功能可查阅logstash官方文档

### 总结

1. elasticsearch-dump和logstash做跨集群数据迁移时，都要求用于执行迁移任务的机器可以同时访问到两个集群，不然网络无法连通的情况下就无法实现迁移。而使用snapshot的方式没有这个限制，因为snapshot方式是完全离线的。因此elasticsearch-dump和logstash迁移方式更适合于源ES集群和目标ES集群处于同一网络的情况下进行迁移，而需要跨云厂商的迁移，比如从阿里云ES集群迁移至腾讯云ES集群，可以选择使用snapshot的方式进行迁移，当然也可以通过打通网络实现集群互通，但是成本较高。
2. elasticsearchdump工具和mysql数据库用于做[数据备份](https://cloud.tencent.com/solution/backup?from_column=20065&from=20065)的工具mysqldump工具类似，都是逻辑备份，需要将数据一条一条导出后再执行导入，所以适合数据量小的场景下进行迁移；
3. snapshot的方式适合数据量大的场景下进行迁移。