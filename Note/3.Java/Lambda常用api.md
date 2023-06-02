# 使用Lambda对集合排序

```java
// 先按性别降序，如果年龄相同，再按年龄排序
list.sort(Comparator.comparing(User::getSex).reversed().thenComparing(User::getAge));
```

### Stream 求和

```java
// stream 对 double 求和
double sumPrice = costList.stream().mapToDouble(Cost::getPrice).sum();
// stream 对 bigdecimal 求和
BigDecimal sumPrice = costList.stream().map(Cost::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);	
```

### List 转 Map

```java
Map<String, Person> map = list.stream().collect(Collectors.toMap(Person::getId, Person -> Person));
// 按某字段分组
Map<String, List<Person>> map = list.stream().collect(Collectors.groupingBy(Person::getSex));
```



### Map按照Key进行排序

```java
map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> log.info(entry.getKey() + "|" + entry.getValue()));
```



