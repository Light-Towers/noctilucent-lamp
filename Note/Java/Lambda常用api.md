# 使用Lambda对集合排序

```java
// 先按性别降序，如果年龄相同，再按年龄排序
list.sort(Comparator.comparing(User::getSex).reversed().thenComparing(User::getAge));
```

