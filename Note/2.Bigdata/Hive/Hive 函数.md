* 判断字段中是否包含字符串

locate(字符串/字段，字段)，如果包含，返回大于0的index位置；否则，返回0；

```sql
select * from temp where locate("aaa", name) > 0;
```

