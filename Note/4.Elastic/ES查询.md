## 聚合查询

```http
GET /order/_search?size=0
{
  "aggs": {
    "shop": { // 聚合查询的名字，随便取个名字
      "terms": { // 聚合类型为: terms
        "field": "shop_id" // 根据shop_id字段值，分桶
      }
    }
  }
}
```

