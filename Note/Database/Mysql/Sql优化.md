## explain SQL

```sql
EXPLAIN SELECT * FROM `TABLE_NAME`;
```

**显示结果解析：**
| **id** | **select_type** | **table**  | **type** | **possible_keys** | **key** | **key_len** | **ref** | **rows** | **Extra** |
| ------ | --------------- | ---------- | -------- | ----------------- | ------- | ----------- | ------- | -------- | --------- |
| 1      | SIMPLE          | table_name | ALL      | NULL              | NULL    | NULL        | NULL    | 100      |           |

- table #显示该语句涉及的表
- type #这列很重要，显示了连接使用了哪种类别,有无使用索引，反映语句的质量。
- possible_keys #列指出MySQL能使用哪个索引在该表中找到行
- key #显示MySQL实际使用的键（索引）。如果没有选择索引，键是NULL。
- key_len #显示MySQL决定使用的键长度。如果键是NULL，则长度为NULL。使用的索引的长度。在不损失精确性的情况下，长度越短越好
- ref #显示使用哪个列或常数与key一起从表中选择行。
- rows #显示MySQL认为它执行查询时必须检查的行数。
- extra #包含MySQL解决查询的详细信息。

> 其中：Explain的type显示的是访问类型，是较为重要的一个指标，结果值从好到坏依次是：
>
> system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL（优-->差）　一般来说，得保证查询至少达到range级别，最好能达到ref，否则就可能会出现性能问题。

