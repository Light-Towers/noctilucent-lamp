**[深度探索 Elasticsearch 8.X：function_score 参数解读与实战案例分析](https://zhuanlan.zhihu.com/p/646115404)**

ES 默认是根据相关度算分 （must/should）（_score）来排序，但是也支持自定义方式对搜索结果排序。除text字段类型外，可排序字段类型有：keyword类型、数值类型、地理坐标类型、日期类型等。

### 1.普通字段排序

keyword、数值、日期类型排序的语法基本一致。

**语法**：

```http
GET /indexName/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "FIELD": "desc"  // 排序字段、排序方式ASC、DESC
    }
  ]
}
```

### 2.地理坐标排序

地理坐标排序略有不同。

**lon(longitude): 经度**
**lat(latitude): 纬度**

**语法说明**：

```http
GET /indexName/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "_geo_distance" : {
          "FIELD" : "纬度，经度", // 文档中geo_point类型的字段名、目标坐标点
          "order" : "asc", // 排序方式
          "unit" : "km" // 排序的距离单位
      }
    }
  ]
}
```

- 指定一个坐标，作为目标点
- 计算每一个文档中，指定字段（必须是geo_point类型）的坐标 到目标点的距离是多少
- 根据距离排序
