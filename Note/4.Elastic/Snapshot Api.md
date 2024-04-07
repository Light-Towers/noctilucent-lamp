# Elasticsearch 快照和恢复



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

```bash
#查看
##查看存储库列表
GET /_cat/repositories
##查看所有存储库详细信息
GET /_snapshot/_all
##查看指定存储库
GET /_snapshot/es_bak_repo

#创建
##创建存储库
PUT /_snapshot/es_bak_repo
{
  "type": "url",
  "settings": {
    "url": "https:#pan.baidu.com/disk/main#/transfer/send?surl=ABkAAAAddddAABEHbw"
  }
}
##或者
POST /_snapshot/es_bak_repo
{
  "type": "fs",
  "settings": {
    "location": "/usr/share/elasticsearch/backup",
    "max_restore_bytes_per_sec": "50mb",
    "max_snapshot_bytes_per_sec": "50mb",
    "compress": true
  }
}

#验证存储库
POST /_snapshot/es_bak_repo/_verify
#删除存储库
DELETE /_snapshot/es_bak_repo
```

### 快照

```bash
#查看快照
##查看所有快照
GET /_snapshot/es_bak_repo/_all
##查看快照详细状态信息，支持多个快照Id查询
GET /_snapshot/es_bak_repo/test_snapshot/_status

#创建快照
PUT /_snapshot/es_bak_repo/test_snapshot?wait_for_completion=true
{
    "indices":"my-index",
    "include_global_state": false,
    "metadata": {
        "message": "测试快照"
    }
}
##创建动态名称快照
##需要对特殊字符转义 PUT /_snapshot/es_bak_20240329/<snapshot-{now/d}>
PUT /_snapshot/es_bak_repo/%3Csnapshot-%7Bnow%2Fd%7D%3E
{"indices": "my-index"}

#删除快照
DELETE /_snapshot/es_bak_repo/snaps*
```

## 从快照恢复

```bash
#从快照中恢复
POST /_snapshot/es_bak_repo/test_snapshot/_restore
#恢复索引：重命名，修改副本
POST _snapshot/es_bak_repo/test_snapshot/_restore
{
  "indices": "exhibition_v5",
  "rename_pattern": "exhibition_v5",
  "rename_replacement": "exhibition_test",
  "index_settings": {
    "index.number_of_replicas": 0
  }
}
```



[Elasticsearch 快照和恢复](https:#blog.51cto.com/forlinkext/9023647)