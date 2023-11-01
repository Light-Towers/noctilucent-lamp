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

## function_score 查询

```http
{
    "from": 0,
    "size": 5,
    "timeout": "120s",
    "track_total_hits": true,
    "query": {
        "bool": {
            "must": [
                {
                    "function_score": {
                        "query": {
                            "query_string": {
                                "query": "(名洋灏正)",
                                "fields": [
                                    "appName^8.5",
                                    "b2bOpScope^1.0",
                                    "b2bProduct^3.5",
                                    "contactAddress^2.2",
                                    "entName^15.0",
                                    "historyName^5.0",
                                    "jobName^2.2",
                                    "opScope^5.0",
                                    "patentName^5.0",
                                    "regAddress^2.2",
                                    "semKeyword^3.5",
                                    "siteName^9.9",
                                    "tenderName^3.0",
                                    "trademarkName^8.5",
                                    "wechatName^10.0"
                                ],
                                "type": "best_fields",
                                "default_operator": "and",
                                "max_determinized_states": 10000,
                                "enable_position_increments": true,
                                "fuzziness": "AUTO",
                                "fuzzy_prefix_length": 0,
                                "fuzzy_max_expansions": 50,
                                "phrase_slop": 0,
                                "escape": false,
                                "auto_generate_synonyms_phrase_query": true,
                                "fuzzy_transpositions": true,
                                "boost": 1.0
                            }
                        },
                        "functions": [
                            {
                                "filter": {
                                    "query_string": {
                                        "query": "(名洋灏正)",
                                        "fields": [
                                            "appName^8.5",
                                            "b2bOpScope^1.0",
                                            "b2bProduct^3.5",
                                            "contactAddress^2.2",
                                            "entName^15.0",
                                            "historyName^5.0",
                                            "jobName^2.2",
                                            "opScope^5.0",
                                            "patentName^5.0",
                                            "regAddress^2.2",
                                            "semKeyword^3.5",
                                            "siteName^9.9",
                                            "tenderName^3.0",
                                            "trademarkName^8.5",
                                            "wechatName^10.0"
                                        ],
                                        "type": "best_fields",
                                        "default_operator": "and",
                                        "max_determinized_states": 10000,
                                        "enable_position_increments": true,
                                        "fuzziness": "AUTO",
                                        "fuzzy_prefix_length": 0,
                                        "fuzzy_max_expansions": 50,
                                        "phrase_slop": 0,
                                        "escape": false,
                                        "auto_generate_synonyms_phrase_query": true,
                                        "fuzzy_transpositions": true,
                                        "boost": 1.0
                                    }
                                },
                                "weight": 280.0
                            },
                            {
                                "filter": {
                                    "match_all": {
                                        "boost": 1.0
                                    }
                                },
                                "weight": 0.01,
                                "field_value_factor": {
                                    "field": "entTypeScore",
                                    "factor": 1.0,
                                    "missing": 1.0,
                                    "modifier": "square"
                                }
                            },
                            {
                                "filter": {
                                    "terms": {
                                        "entStatus": [
                                            1,
                                            4,
                                            9
                                        ],
                                        "boost": 1.0
                                    }
                                },
                                "weight": 250.0
                            },
                            {
                                "filter": {
                                    "match_all": {
                                        "boost": 1.0
                                    }
                                },
                                "weight": 5.0,
                                "field_value_factor": {
                                    "field": "regCapUnify",
                                    "factor": 1.0,
                                    "missing": 0.0,
                                    "modifier": "ln2p"
                                }
                            },
                            {
                                "filter": {
                                    "term": {
                                        "entName.raw": {
                                            "value": "名洋灏正",
                                            "boost": 1.0
                                        }
                                    }
                                },
                                "weight": 400.0
                            },
                            {
                                "filter": {
                                    "term": {
                                        "historyName.raw": {
                                            "value": "名洋灏正",
                                            "boost": 1.0
                                        }
                                    }
                                },
                                "weight": 100.0
                            }
                        ],
                        "score_mode": "sum",
                        "boost_mode": "multiply",
                        "max_boost": 100000.0,
                        "boost": 1.0
                    }
                }
            ],
            "filter": [
                {
                    "bool": {
                        "must": [
                            {
                                "bool": {
                                    "adjust_pure_negative": true,
                                    "boost": 1.0
                                }
                            }
                        ],
                        "adjust_pure_negative": true,
                        "boost": 1.0
                    }
                }
            ],
            "adjust_pure_negative": true,
            "boost": 1.0
        }
    },
    "sort": [
        {
            "_score": {
                "order": "desc"
            }
        }
    ]
}
```



## _source关键字使用

* `"_source": false` - 无需返回_source内容。
* `"_source": "name"` - 只显示所指定的字段。
* `"_source": ["name","level"]` - 使用数组指定多个字段
* 使用includes 和 excludes来包含显示或排除字段

```http
GET _search
{
  "_source": false,
// "_source": "name",
// "_source": ["name","level"],
// "_source": {
//     "includes": ["name","level"],
//	   "excludes": []
//   },
  "query": {
    "term": { 
      "level": {  #需要搜索的字段名字
        "value": 2  #搜索的结果
      }
    }
  }
}
```

