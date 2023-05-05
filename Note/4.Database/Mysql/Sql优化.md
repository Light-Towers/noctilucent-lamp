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



### Using filesort

**Using filesort** 是什么意思？

MySQL需要**额外的一次传递**，以找出如何按排序顺序检索行。通过根据联接类型浏览所有行并为所有匹配WHERE子句的行保存排序关键字和行的指针来完成排序。然后关键字被排序，并按排序顺序检索行。

filesort 有两种排序方式

1. 对需要排序的记录生成 **<sort_key,rowid>** 的元数据进行排序，该元数据仅包含排序字段和rowid。排序完成后只有按字段排序的rowid，因此还需要通过rowid进行**回表操作获取所需要的列的值**，可能会导致**大量的随机IO读消耗**；
2. 对需要排序的记录生成 **<sort_key,additional_fields>** 的元数据，该元数据包含排序字段和需要返回的所有列。排序完后不需要回表，但是元数据要比第一种方法长得多，**需要更多的空间用于排序**。
