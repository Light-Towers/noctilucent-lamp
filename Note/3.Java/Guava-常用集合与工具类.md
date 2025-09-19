# Guava 常用集合与工具类

本文档聚焦 Guava 不可变集合（Immutable 集合）的创建方式、示例代码及注意事项，并补充常见工具类的简短说明与使用建议。

## 概述

Guava 的 Immutable 集合：不可变（immutable）、线程安全、元素唯一（对于 Set）或按插入顺序固定（对于 List）。创建 Immutable 集合会复制传入的数据（创建副本），因此原始集合的修改不会影响 Immutable 集合。

主要优点：
- 线程安全（无需额外同步）
- 防止意外修改
- 性能优化（内部针对不可变数据结构做了优化）

适用场景：配置常量集合、缓存键集合、并发场景中共享只读集合等。

## 常见创建方式

Guava 提供两种主要的构建 Immutable 集合方式：静态的 of(...) 工厂方法和静态内部类 Builder。

### 1. of(...) 静态工厂方法

- 适用于元素数量已知且较少的情况。
- 提供了多种重载，例如 `ImmutableList.of()`、`ImmutableSet.of()`、`ImmutableMap.of()` 等。

示例：

```java
// 创建空的 ImmutableSet
ImmutableSet<Integer> empty = ImmutableSet.of();

// 创建含元素（自动去重）的 ImmutableSet
ImmutableSet<Integer> set = ImmutableSet.of(1, 2, 1); // 等效于 [1, 2]
```

### 2. Builder（静态内部类）

- 适用于元素较多或需要在多处 add 的场景。
- 支持链式调用，内部会根据需要扩容并检查空元素。

示例：

```java
ImmutableSet<Integer> set = ImmutableSet.<Integer>builder()
    .add(1)
    .add(2)
    .addAll(Arrays.asList(3, 4))
    .build();
```

Builder 概要（简要摘录实现要点）：
- 内部维护 Object[] contents 和 int size
- add(...) 会校验非空并 append
- addAll(Iterable) 对 Collection 做 size 检查以提前 ensureCapacity
- build() 调用 ImmutableSet.construct(...) 返回最终的不可变集合

## 复制现有集合

使用 `ImmutableSet.copyOf(collection)` 或 `ImmutableList.copyOf(collection)` 可以复制已有集合并返回一个不可变副本，同时去重（Set）。

示例：

```java
List<Integer> list = new ArrayList<>();
list.add(1);
list.add(2);
list.add(1);
ImmutableSet<Integer> copy = ImmutableSet.copyOf(list); // [1,2]
```

## 常见类速览（简要）

- ImmutableList：不可变 List，保持插入顺序。
- ImmutableSet：不可变 Set，元素唯一。实现会去重。
- ImmutableMap / ImmutableSortedMap：不可变 Map。
- ImmutableMultiset / ImmutableMultimap：不可变的 Multiset/Multimap 变体。

## 常用 Guava 工具类速览

- Lists：便捷的 List 操作，`Lists.partition`、`Lists.transform` 等。
- Sets：集合差集、交集、并集等工具方法（`Sets.difference` 等）。
- Maps：`Maps.filterKeys`、`Maps.uniqueIndex` 等工具。
- Multimap / Multiset：表示键映射到多个值或元素可重复计数的集合。
- Optional：避免 null 的容器（与 Java 8 的 Optional 相似，但早期 Guava Optional 在 Java 8 出现前常被使用）。
- Strings：字符串工具（`Strings.isNullOrEmpty`、`Strings.repeat`）
- MoreObjects：`MoreObjects.toStringHelper` 用于实现更友好的 toString。

## 使用注意事项

- Immutable 集合会复制输入，因此对非常大的集合会产生额外内存开销；对超大集合或流式构建时要注意内存。
- 对于 Map 的构建，优先使用 ImmutableMap.builder() 当需要逐条 put 时。
- 由于不可变，序列化时注意版本兼容性；长期存储应使用明确的格式。
- 若需要按需惰性加载或经常变更的集合，请使用可变集合（Collections.unmodifiableXXX 只是包装，原集合变更仍会影响视图）。

## 额外示例

```java
// ImmutableMultimap
ImmutableMultimap<String, Integer> mm = ImmutableMultimap.<String, Integer>builder()
    .put("a", 1)
    .putAll("b", Arrays.asList(2,3))
    .build();

// Optional（Guava）
com.google.common.base.Optional<String> opt = com.google.common.base.Optional.of("hello");
if (opt.isPresent()) {
    System.out.println(opt.get());
}

// Lists.partition
List<Integer> nums = IntStream.rangeClosed(1,10).boxed().collect(Collectors.toList());
List<List<Integer>> parts = Lists.partition(nums, 3); // [[1,2,3],[4,5,6],[7,8,9],[10]]
```

## 与 Java 原生不可变集合的对比

Java 9+ 提供了 List.of / Set.of / Map.of 等工厂方法来创建不可变集合。与 Guava 的 Immutable 集合比较：

- API/语义上类似：两者都返回不可变实例。
- Guava 提供的 Immutable 系列在早期 Java 版本（8及更早）非常有用，并且在功能上（比如 ImmutableMultimap / ImmutableMultiset）更丰富。
- Java 原生实现通常返回 JDK 自身的内部不可变实现，序列化策略与 Guava 不同；选择依据通常是项目是否已强依赖 Guava 功能。

## 性能与内存建议

- 小集合频繁创建：of(...) 与 builder 都很方便；对短生命周期对象无需过度优化。
- 大集合复制：copyOf 会分配新的内存，若来源集合本身可控，考虑直接构建或使用不可变视图以降低复制成本。

## 参考和延伸阅读

- Guava 官方文档（Immutable Collections）
- Java 9+ Collections 工厂方法比较

