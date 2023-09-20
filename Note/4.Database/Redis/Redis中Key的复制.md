# Set集合复制

```shell
SUNIONSTORE destination key [key ...]
```

* destination：存放合并结果集的key。
* key [key …]：一个或者多个集合

**注意：**
如果是已经存在的集合，会删除原来的数据，只会保留合并结果。

## SortSet有序集合的复制

```shell
ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight] [SUM|MIN|MAX]
```

ZUNIONSTORE命令其实就是求并集的命令，他的参数说明如下：

- destination: 存放合并结果集的key。如果是已经存在的集合，会删除原来的数据，只会保留合并结果。
- numkeys：有几个集合的内容需要合并
- key [key …]： 一个或者多个集合
- [WEIGHTS weight]：分数乘积因子，默认是1.如果设置以后，在合并时，会将待合并的集合中的分数乘以乘积因子，然后将结果保存到合并结果集中。
- [SUM|MIN|MAX]：结果集的聚合方式。默认是SUM，如果要合并的集合中都有同一个名称的元素，它们的分数以何种方式存储到新的结果集中。SUM：相加；MIN：取最小的值；MAX：取最大的值。