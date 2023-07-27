## 更新字段值

```json
POST ka_company_info_prod/_update/2
{
  "script" : "ctx._source.baikePopularity=25311775"
}
```

> 参考：
>
> ​	[修改数据](https://endymecy.gitbooks.io/elasticsearch-guide-chinese/content/getting-started/modifying-data.html)

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
    "match_all": {
    }
  }
}
## 修改分片、副本
PUT ka_exhibition_info_prod/_settings
{
    "index": {
        "number_of_shards" : 1,
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

```json
POST _reindex?refresh
{
  "source": {
    "index": "ka_company_info_prod",
    "size": 5000
  },
  "dest": {
    "index": "ik_company_info_prod",
    "version_type": "internal"
  }
}
```

> 参考：
>
> ​	[ES数据库重建索引——Reindex(数据迁移) ](https://www.cnblogs.com/Ace-suiyuan008/p/9985249.html)
>
> ​	[使用es reindex api 修改和迁移数据](https://blog.csdn.net/weixin_38920212/article/details/102461563)
