## 得分

### 评分计算

`bool` 查询会为每个文档计算相关度评分 `_score` ，再将所有匹配的 `must` 和 `should` 语句的分数 `_score` 求和，最后除以 `must` 和 `should` 语句的总数。

`must_not` 语句不会影响评分；它的作用只是将不相关的文档排除。



## function_score 自定义评分

function_score 查询提供了多种类型的评分函数。

* script_score: script脚本评分
* weight: 字段权重评分
* random_score: 随机评分
* field_value_factor: 字段值因子评分
* decay functions: 衰减函数(gauss, linear, exp)

> decay functions衰减函数太过复杂，这里暂时不作介绍。
>
> 参考:[ES自定义评分机制:function_score查询详解](https://blog.csdn.net/w1014074794/article/details/120523550)

## field_value_factor

* modifier
  * 应用于字段值的计算修饰符: none, log, log1p, log2p, ln, ln1p, ln2p, square(平方), sqrt(平方根), or reciprocal(倒数)，默认 none

>- [function_score 查询](https://www.elastic.co/guide/cn/elasticsearch/guide/current/function-score-query.html)











Elasticsearch Function Score 查询：http://timd.cn/elasticsearch/function-score/。
EFK Docker Compose 搭建：http://timd.cn/code-examples/elasticsearch-filebeat-kibana-demo/。
Elasticsearch Function Score Demo：http://timd.cn/code-examples/elasticsearch-function-score-demo/。
Painless 的官方文档：https://www.elastic.co/guide/en/elasticsearch/painless/current/index.html。









## Refrence

[1]:https://www.elastic.co/guide/cn/elasticsearch/guide/current/bool-query.html	"组合查询"
[2]:https://www.elastic.co/guide/cn/elasticsearch/guide/current/_boosting_query_clauses.html	"查询语句提升权重"

