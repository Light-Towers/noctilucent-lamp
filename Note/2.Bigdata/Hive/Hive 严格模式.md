7. Hive的严格模式和非严格模式
通过设置参数hive.mapred.mode来设置是否开启严格模式。目前参数值有两个：strict（严格模式）和nostrict（非严格模式，默认）。

通过开启严格模式，主要是为了禁止某些查询（这些查询可能造成意想不到的坏的结果），目前主要禁止3种类型的查询：

1）分区表查询

在查询一个分区表时，必须在where语句后指定分区字段，否则不允许执行。

因为在查询分区表时，如果不指定分区查询，会进行全表扫描。而分区表通常有非常大的数据量，全表扫描非常消耗资源。

2）order by 查询

order by语句必须带有limit 语句，否则不允许执行。

因为order by会进行全局排序，这个过程会将处理的结果分配到一个reduce中进行处理，处理时间长且影响性能。

3）笛卡尔积查询

数据量非常大时，笛卡尔积查询会出现不可控的情况，因此严格模式下也不允许执行。

在开启严格模式下，进行上述三种不符合要求的查询，通常会报类似FAILED: Error in semantic analysis: In strict mode, XXX is not allowed. If you really want to perform the operation,+set hive.mapred.mode=nonstrict+