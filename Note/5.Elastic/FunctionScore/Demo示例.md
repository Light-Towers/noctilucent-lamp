# 1. 创建索引

```
PUT /product?pretty
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text"
      },
      "description": {
        "type": "text"
      },
      "customer_rating": {
        "type": "double"
      },
      "customer_rating_count": {
        "type": "long"
      },
      "brand": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
    }
  }
}
```

------

# 2. 查看索引

```
GET /product?pretty
```

------

# 3. 删除索引

```
DELETE /product?pretty
```

------

# 4. 批量插入测试数据

```
PUT /product/_bulk?refresh&pretty
{"index": {"_id": 1}}
{"customer_rating": 4.7, "brand": "Apple", "customer_rating_count": 100, "name": "苹果手机 iPhone 14", "description": "这里是苹果手机 iPhone 14 的长描述。"}
{"index": {"_id": 2}}
{"customer_rating": 4.9, "brand": "Huawei", "customer_rating_count": 800, "name": "华为手机 P30 Pro", "description": "这里是华为手机 P30 Pro 的长描述。"}
{"index": {"_id": 3}}
{"customer_rating": 4.5, "brand": "XiaoMi", "customer_rating_count": 300, "name": "小米手机 Mi 11 Pro", "description": "这里是小米手机 Mi 11 Pro 的长描述。"}
```

------

# 5. 查询样例

```
GET /product/_search
{
  "query": {
    "multi_match": {
      "query": "手机",
      "fields": ["name", "description"],
      "type": "phrase"
    }
  }
}
```

------

# 6. Function Score 查询样例

```
GET /product/_search
{
  "query": {
    "function_score": {
      "query": {
        "multi_match": {
          "query": "手机",
          "fields": ["name", "description"],
          "type": "phrase"
        }
      },
      // 增强模式为替换
      "boost_mode": "replace", 
      "script_score": {
        "script": {
          "lang": "painless",
          "params": {
            "customer_rating_count_threshold": 100,
            "super_brands": [
              "XiaoMi",
              "Huawei"
            ],
            "super_brand_cons": 1,
            "super_brand_relevance_threshold": 0.2
          },
          "source": """
          // 最终的评分为：cons + ratio * _score。
          // 在下面的过程中，通过文档中的相应字段计算 ration 和 cons


          double ratio = 1;
          // 仅当用户评价数量达到阈值时，才参与评分生成。防止较少的评价，干扰评分
          if (
              doc.containsKey('customer_rating_count') && 
              !doc['customer_rating_count'].empty &&
              doc['customer_rating_count'].value >= params.customer_rating_count_threshold) {
            // 不同的评分对应不同的系数，比如 5.0 分对应 X；4.8、4.9 对应 Y...
            if (doc['customer_rating'].value >= 5.0) {
              ratio = 1.2;
            } else if (doc['customer_rating'].value >= 4.8) {
              ratio = 1.1;
            } else if (doc['customer_rating'].value >= 4.6) {
              ratio = 1.0;
            } else if (doc['customer_rating'].value >= 4) {
              ratio = 0.9;
            } else {
              ratio = 0.8;
            }
          }


          double cons = 0;
          // 仅当相关度超过指定的阈值时，才增加常数项。
          // 防止在前面展示相关度过低的文档，影响体验
          double scoreThreshold = 0.5;
          if (params.containsKey('super_brand_relevance_threshold')) {
            scoreThreshold = params.super_brand_relevance_threshold;
          }
          if (
              params.containsKey('super_brands') && 
              !params.super_brands.empty && 
              params.super_brands instanceof List &&
              doc.containsKey('brand') &&
              !doc['brand.keyword'].empty &&
              _score >= scoreThreshold) {
            for (int i = 0; i < params.super_brands.length; ++i) {
              if (params.super_brands[i].toLowerCase().equals(doc['brand.keyword'].value.toLowerCase())) {
                if (params.containsKey('super_brand_cons')) {
                  cons = params.super_brand_cons;
                } else {
                  cons = 1;
                }
                break;
              }
            }
          }
          return cons + ratio * _score;
          """
        }
      }
    }
  }
}
```