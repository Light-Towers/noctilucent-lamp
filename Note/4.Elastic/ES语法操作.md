# 集群信息

## 获取集群分配状态

```http
GET /_cluster/allocation/explain?pretty
```

## 节点信息

```http
GET _cat/nodes?v
```

## 集群健康状态

```http
GET _cat/health
```




# 文档操作

## 替换/索引文档

```http
PUT customer/_doc/2 
{
  "name": "Jane Doe"
}
```

## 更新文档

```http
POST ka_company_info_prod/_update/2
{
  "doc": { "name": "Jane Doe", "age": 20 }
}
// 或者
{
  "script" : "ctx._source.baikePopularity=25311775"
}
```

> 参考：
>
>     [修改数据](https://endymecy.gitbooks.io/elasticsearch-guide-chinese/content/getting-started/modifying-data.html)

## 批量更新字段值

```bash
POST ik_company_bd_info_prod/_update_by_query
{
  "script": {
    "lang": "painless",
    "source": "if(ctx._source.exhibitorFlag==true){ctx._source.exhibitorFlag=false}"
  }
}
# 或者
POST ik_company_bd_info_prod/_update_by_query
{
  "query": {
    "term":{
      "exhibitorFlag" : true
    }
  },
  "script": {
    "inline": "ctx._source['exhibitorFlag'] = false"
  }
}
```

```json
## 清空索引数据
POST indexName/_delete_by_query
{
  "query": { 
    "match_all": {}
  }
}
## 设置分片(初始化设置，不可修改)，修改副本
PUT ka_exhibition_info_prod/_settings
{
    "index": {
        "number_of_replicas" : 0
    }
}
```

## 分词器

```json
GET /_analyze
{
  "text": "2015上海工业博览会暨第19届长三角工业设计商品交易会",
  "analyzer": "ik_smart"
}
```

## 索引数据迁移

```http
POST _reindex?refresh
{
  "source": {
    "index": "ka_company_info_prod",
    "size": 5000
  },
  "dest": {
    "index": "ik_company_info_prod",
    "version_type": "internal"
  }，
  "script": {
    "source": "ctx._source.exhNameBak = ctx._source.remove(\"exhName\"); ctx._source.exhStartDateBak = ctx._source.remove(\"exhStartDate\")"
  }
}
```

> 参考：
>
>     [ES数据库重建索引——Reindex(数据迁移) ](https://www.cnblogs.com/Ace-suiyuan008/p/9985249.html)
>
>     [使用es reindex api 修改和迁移数据](https://blog.csdn.net/weixin_38920212/article/details/102461563)
>
>     [通过reindex迁移ES数据](http://dbaselife.com/project-16/doc-884/)
>
>     [ES索引重建reindex详解](https://blog.csdn.net/w1014074794/article/details/120483334)

## 取消reindex操作

```json
#获取任务ID
GET _tasks?detailed=true&actions=*reindex
#取消任务
POST _tasks/Eoc8_cOHR1WUpfoGfKJLuQ:9180876/_cancel
```

> 参考：[如何在Elasticsearch中停止重建索引？](https://www.soinside.com/question/kehDPLDxL9R88tSsVP4DFY)

## 主分片迁移

```json
POST /_cluster/reroute
{
    "commands": [
        {
            "move": {
                "index": "ik_company_info_prod",
                "shard": 2,
                "from_node": "node-34",
                "to_node": "node-33"
            }
        },
        {
            // 分配副本（仅未分配副本适用）
            "allocate_replica": {
                "index": "ik_company_info_prod",
                "shard": 5,
                "node": "node-34"
            }
        }
    ]
}
```

分片迁移前需要关闭集群的动态分片；删除副本视情况而定。

```json
PUT _cluster/settings 
{ 
  "persistent": { 
    "cluster.routing.allocation.enable": "none"		//"none":关闭； "all":开启
  }
}
```

## 二、字段操作

## 增加新的字段

```python
PUT /zq_test/_mapping
{
    "properties": {
        "hight": {
            "type": "integer"
        }
    }
}
```

## ES 删除字段

ES 已经建立好的索引数据，无法直接删除一个字段。除非新建索引。但是我们通过文档删除字段。通过脚本更新的方式，删除设计文档中的字段内容，达到该字段数据为 `None` 的形式

### 删除指定文档中所有的某一个字段数据

```python
body = {
        "script": f"ctx._source.remove('{field}')",
        "query": {
            "bool": {
                "must": [
                    {
                        "exists": {
                            "field": field
                        }
                    }
                ]
            }
        }
    }
es.update_by_query(index, body)
```

```bash
PUT ik_company_info_prod_change_2/_mapping
{
  "dynamic": true
}
```

## 内嵌字段统计

```bash
GET my_index/_search
{
  "size": 0, 
  "aggs": {
    "nest_user": {
      "nested": {
        "path": "user"
      },
      "aggs": {
        "user_count": {
          "value_count": {
            "field": "user.first"
          }
        }
      }
    }
  }
}
```


## 内嵌字段查询

```bash
POST my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "nested": {
            "path": "recruitment",
            "query": {
              "bool": {
                "must": [
                  {
                    "match": {
                      "recruitment.jobName": "大数据"
                    }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  }
}
```
