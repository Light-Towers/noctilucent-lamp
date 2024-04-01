### Elasticsearch 快照和恢复



## 简介
快照是正在运行的Elasticsearch集群的备份。可以使用快照来：

* 定期备份群集，无需停机
* 删除或硬件故障后恢复数据
* 在群集之间传输数据等

> 默认情况下，集群的快照包含集群状态、所有常规数据流和所有常规索引
>
> 快照必须存储在存储库中，存储库的内容不能修改，否则会造成快照损坏或导致数据不一致等一系列问题。所以在创建快照之前，需要先创建存储库

## 快照存储库
### 说明

- 存储库需要集群中的节点可以共享访问（共享文件系统、云存储等）
- 多个集群之间不要使用同一个储存库。避免多个集群修改同样的存储库，造成不必要的问题。
- 存储库的内容不要修改，避免操作数据损坏等问题

### 存储库

```http
PUT /_snapshot/my_repository
{
  "type": "url",
  "settings": {
    "url": "https://pan.baidu.com/disk/main#/transfer/send?surl=ABkAAAAddddAABEHbw"
  }
}
// 或者
POST /_snapshot/es_bak_20240329
{
  "type": "fs",
  "settings": {
    "location": "/usr/share/elasticsearch/backup",
    "max_restore_bytes_per_sec": "50mb",
    "max_snapshot_bytes_per_sec": "50mb",
    "compress": true
  }
}
//查看存储库
GET /_cat/repositories
```

### 快照

```http
//删除
DELETE /_snapshot/es_bak_20240329
//验证
POST /_snapshot/es_bak_20240329/_verify
//查看所有
GET /_snapshot/_all
//指定库名
GET /_snapshot/es_bak_20240329
//创建快照
PUT /_snapshot/es_bak_20240329/test_snapshot?wait_for_completion=true
{
    "indices":"my-index",
    "include_global_state": false,
    "metadata": {
        "message": "测试快照"
    }
}
//创建动态名称的快照   //需要对特殊字符转义 PUT /_snapshot/es_bak_20240329/<snapshot-{now/d}>
PUT /_snapshot/es_bak_20240329/%3Csnapshot-%7Bnow%2Fd%7D%3E
{"indices": "my-index"}
//查看快照名
GET /_snapshot/es_bak_20240329/test_snapshot/_status
//删除快照
DELETE /_snapshot/es_bak_20240329/snaps*
```





[Elasticsearch 快照和恢复](https://blog.51cto.com/forlinkext/9023647)