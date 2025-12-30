`function_score` 允许修改查询检索到的文档的评分。比如，在评分函数的计算开销很大，并且在过滤后的文档集合上计算分数足够时，很有用。

为使用 `function_score`，用户必须定义查询和一或多个函数，这些函数为查询返回的每个文档计算新评分。

`function_score` 仅能与一个函数一起使用，类似这样：

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "query": { "match_all": {} },
      "boost": "5",
      "random_score": {}, 
      "boost_mode": "multiply"
    }
  }
}
'
```

此外，还可以组合多个函数。在这种情况下，仅当文档匹配给定的过滤查询时，才能选择应用函数

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "query": { "match_all": {} },
      "boost": "5", 
      "functions": [
        {
          "filter": { "match": { "test": "bar" } },
          "random_score": {}, 
          "weight": 23
        },
        {
          "filter": { "match": { "test": "cat" } },
          "weight": 42
        }
      ],
      "max_boost": 42,
      "score_mode": "max",
      "boost_mode": "multiply",
      "min_score": 42
    }
  }
}
'
```

> 注意
>
> 每个函数的过滤查询生成的评分无关紧要。

不为函数指定过滤器，等价于指定 `"match_all": {}`。

首先，被定义的函数对每篇文档进行评分。参数 `score_mode` 指定如何组合评分：

| 值         | 描述                       |
| ---------- | -------------------------- |
| `multiply` | 评分相乘（默认）           |
| `sum`      | 评分相加                   |
| `avg`      | 平均评分                   |
| `first`    | 应用匹配过滤器的第一个函数 |
| `max`      | 使用最大评分               |
| `min`      | 使用最小评分               |

因为评分可以在不同的范围上（比如，对于衰减函数，在 0 到 1 之间，对于 `field_value_factor`，是任意值），另外因为有时期望不同的函数对评分的影响不同，所以可以使用用户自定义的 `weight` 调整每个函数的评分。`functions` 数组中的每个函数都可以定义 `weight`（比如上例），它与各自函数计算的评分相乘。如果指定权重，但未指定任何其它函数声明，那么 `weight` 将充当简单地返回 `weight` 的函数。

在 `score_mode` 被设置为 `avg` 的情况下，各评分将以**加权**平均的方式合并。比如，如果两个函数分别返回评分 1 和 2，并且它们各自的权重分别是 3 和 4，那么它们的评分将以 `(1*3+2*4)/(3+4)` 的方式合并，而**不是**  `(1*3+2*4)/2`。

通过设置 `max_boost` 参数，可将新评分限制不超过特定的限制。`max_boost` 的默认值是 FLT_MAX。

新计算的评分将与查询的评分结合。参数 `boost_mode` 定义如何结合：

| 值         | 描述                           |
| ---------- | ------------------------------ |
| `multiply` | 查询评分与函数评分相乘（默认） |
| `replace`  | 仅使用函数评分，忽略查询评分   |
| `sum`      | 查询评分与函数评分相加         |
| `avg`      | 平均                           |
| `max`      | 查询评分和函数评分的最大值     |
| `min`      | 查询评分和函数评分的最小值     |

默认情况下，修改评分不改变匹配的文档。为排除不满足特定评分阈值的文档，可将 `min_score` 参数设置为期望的评分阈值。

> **注意**
>
> 为使 `min_score` 生效，查询返回的所有文档都需要被评分，然后逐个过滤。

`function_score` 查询提供多种类型的评分函数。

- script_score
- weight
- random_score
- field_value_factor
- 衰退函数：`guass`、`linear`、`exp`

------

# 1. 脚本评分

`script_score` 函数允许包装另一个查询，以及通过使用脚本表达式从文档中的其它数值字段值衍生的计算自定义查询的评分。下面是简单的样例：

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "query": {
        "match": { "message": "elasticsearch" }
      },
      "script_score": {
        "script": {
          "source": "Math.log(2 + doc[\u0027my-int\u0027].value)"
        }
      }
    }
  }
}
'
```

> **重要**
>
> 在 Elasticsearch 中，都是正的 32 位符点数。
>
> 如果 `script_score` 函数生成的评分精度更高，那么其将被转换为最接近的 32 位符点数。
>
> 类似地，评分必须是非负数。否则，Elasticsearch 返回错误。

在不同的脚本字段值和表达式之上，可以使用 `_score` 脚本参数检索基于包装查询的评分。

为更快的执行，脚本编译将被缓存。如果脚本有需要考虑的参数，最好复用脚本，并且为其提供参数：

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "query": {
        "match": { "message": "elasticsearch" }
      },
      "script_score": {
        "script": {
          "params": {
            "a": 5,
            "b": 1.2
          },
          "source": "params.a / Math.pow(params.b, doc[\u0027my-int\u0027].value)"
        }
      }
    }
  }
}
'
```

注意，与 `custom_score` 查询不同，这种查询的评分与脚本评分的结果相乘。如果希望阻止该行为，设置 `"boost_mode": "replace"`。

------

# 2. 权重

`weight` 评分允许将评分与提供的权重相乘。有时期望这样，因为在特定查询上设置的 boost 值被规范化，而对于该评分函数则不是这样。数值值属于类型 float。

```
"weight": number
```

------

# 3. 随机

`random_score` 生成从 0 到不包括 1 的均匀分布的评分。默认情况下，它使用内部的 Lucene 文档 ID 作为随机性的源，这非常有效，但不幸的是，不可复制，因为文档可能因合并而被重新编号。

在这种情况下，如果你希望评分可复制，那么提供 `seed` 和 `field`。将基于该种子、文档的 `field` 的最小值以及基于索引名称和分片 ID 计算而来的盐计算最终评分，因此具有相同值，但存储在不同索引中的文档将获得不同的评分。注意，在相同分片中，并且具有相同 `field` 值的文档仍将获得相同的评分。因此，对于所有文档，通常期望使用拥有唯一值的字段。比较好的默认选择可能是使用 `_seq_no` 字段，它的唯一缺点是，如果文档更新，那么评分将改变，因为更新操作也更新 `_seq_no` 字段的值。

> **注意**
>
> 可以在不设置字段的情况下，设置种子，但是这种用法已被弃用，因为这样做需要加载 `_id` 字段上的字段数据，将消耗大量内存。

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "random_score": {
        "seed": 10,
        "field": "_seq_no"
      }
    }
  }
}
'
```

------

# 4. 字段值因子

`field_value_factor` 函数允许使用文档中的字段来影响评分。它与使用 `script_score` 函数相似，但可避免脚本的开销。如果在多值字段上使用，那么在计算中仅使用该字段的第一个值。

举个例子，假设你有一个用数值类型的 `my-int` 字段索引的文档，并且希望用该字段影响文档的评分，这样做的示例如下：

```
curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "function_score": {
      "field_value_factor": {
        "field": "my-int",
        "factor": 1.2,
        "modifier": "sqrt",
        "missing": 1
      }
    }
  }
}
'
```

这将转换为如下的评分公式：

```
sqrt(1.2 * doc['my-int'].value)
```

`field_value_factor` 函数有很多选项：

| 值         | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| `field`    | 从文档中提取的字段                                           |
| `factor`   | 与该字段值相乘的可选因子，默认为 1                           |
| `modifier` | 应用到字段值的修改器，可以是其中之一：`none`、`log`、 `log1p`、`log2p`、 `ln`、`ln1p`、`ln2p`、`square`、 `sqrt` 或 `reciprocal`。默认为 `none` |

| 修改器       | 含义                                                         |
| ------------ | ------------------------------------------------------------ |
| `none`       | 不应用任何乘数到字段值                                       |
| `log`        | 取字段值的公共对数。因为如果在 0 到 1 之间的值上使用，该函数将返回负值，导致错误，所以建议使用 `log1p` 代替 |
| `log1p`      | 将字段值加 1，然后取公共对数                                 |
| `log2p`      | 将字段值加 2，然后取公共对数                                 |
| `ln`         | 取字段值的自然对数。因为如果在 0 到 1 之间的值上使用，该函数将返回负值，导致错误，所以建议使用 `ln1p` 代替 |
| `ln1p`       | 将字段值加 1，然后取自然对数                                 |
| `ln2p`       | 将字段值加 2，然后取自然对数                                 |
| `square`     | 字段值的平方（与自身相乘）                                   |
| `sqrt`       | 取字段值的平方根                                             |
| `reciprocal` | 字段值的倒数，与 `1/x` 相同，其中 `x` 是字段的值             |

**missing**

如果文档没有 `field` 指定的字段，那么使用该值。修改器和因子仍被应用到它，就好像它是从文档中读取的一样。

> **注意**
>
> `field_value_score` 函数生成的分数必须是非负的，否则将抛出错误。当在 0 到 1 之间的值上使用 `log` 和 `ln` 修改器时，将产生负值。请确保使用范围过滤器限制字段的值，以避免这种情况，或使用 `log1p` 和 `ln1p`。

> **注意**
>
> 牢记取 0 的 log()，或负数的平方根是非法操作，将抛出异常。请确保使用范围过滤器限制字段的值，以避免这种情况，或使用 `log1p` 和 `ln1p`。

------

# 5. 衰减函数

衰减函数使用一个函数对文档进行评分，该函数的衰减取决于文档的数值字段值与用户给定的原点的距离。与范围查询类似，但使用平滑边缘而非方框。

要在具有数值字段的查询上使用距离评分，用户必须为每个字段定义原点和刻度。需要用 `origin` 来定义计算距离的“中心点”，用 `scale` 来定义衰减速率。衰减函数被指定为

```
"DECAY_FUNCTION": { 
    "FIELD_NAME": { 
          "origin": "11, 12",
          "scale": "2km",
          "offset": "0km",
          "decay": 0.33
    }
}
```

其中：

1. `DECAY_FUNCTION` 必须是 `linear`、`exp` 或 `gauss` 中的一个
2. 指定的字段必须是数值、日期或 GeoPoint 字段

在上面的例子中，字段类型是 [`geo_point`](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/geo-point.html)，以 Geo 格式提供原点。在这种情况下，刻度和偏移量必须指定单位。

关于衰减函数的更多细节，请参阅 [官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/query-dsl-function-score-query.html#function-decay)。