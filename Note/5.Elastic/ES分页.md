## 分页的几种方案

1. ### from/size方案

2. ### search after方案

3. ### scroll api方案

## 总结
from/size方案的优点是简单，缺点是在深度分页的场景下系统开销比较大，占用较多内存。

search after基于ES内部排序好的游标，可以实时高效的进行分页查询，但是它只能做下一页这样的查询场景，不能随机的指定页数查询。

scroll方案也很高效，但是它基于快照，不能用在实时性高的业务场景，建议用在类似报表导出，或者ES内部的reindex等场景。



## Refrence

[1]:https://blog.csdn.net/pony_maggie/article/details/105478557 "ES分页看这篇就够了"
