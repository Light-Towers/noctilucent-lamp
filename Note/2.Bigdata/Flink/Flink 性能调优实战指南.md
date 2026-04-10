# Flink 性能调优实战指南

> **导读**: Flink 任务跑得通不代表跑得快。本笔记涵盖了从反压识别到数据倾斜处理的实战经验。

---

## 1. 反压 (Backpressure) 识别与治理
反压是 Flink 任务常见的亚健康状态。

### 1.1 快速识别
- **Web UI 观测**: 在反压面板（Back Pressure）查看具体算子的状态。
- **监控指标**: `outPoolUsage` (输出缓冲池使用率) 高说明下游慢；`inPoolUsage` 高说明本算子处理慢。

### 1.2 治理思路
1. **下游慢**: 调大下游算子的并行度，或者检查下游外部 Sink 的瓶颈。
2. **本算子慢**: 复用对象（Object Reuse）、减少复杂的序列化操作、业务逻辑优化。
3. **网络瓶颈**: 如果是网络拥堵，调大 `taskmanager.network.memory.fraction`。

---

## 2. 数据倾斜 (Data Skew) 处理
数据倾斜是分布式系统的头号杀手。

### 2.1 表现
- 个别 SubTask 的数据量远超其他 Task。
- 该 Task 的 Checkpoint 时间异常偏长。

### 2.2 解决方案
- **两阶段聚合**: 先进行局部聚合（Local Aggregate），再进行全局聚合。
- **打散 Key**: 在 Key 上拼随机后缀，聚合后再还原。
- **自定义分区**: 使用 `partitionCustom` 代替默认的 `keyBy` 哈希逻辑。

---

## 3. 序列化性能优化
Flink 内部传输数据时涉及频繁的序列化。

- **POJO 优化**: 确保你的 Bean 符合 POJO 规范（有无参构造、Getter/Setter），这样 Flink 会使用高性能的 `PojoSerializer` 而不是通用的 `Kryo`。
- **开启对象重用**: 在 `env` 中开启 `enableObjectReuse()`。但注意，开启后不能在算子间互相直接修改对象，需谨慎使用。
    ```java
    env.getConfig().enableObjectReuse();
    ```

---

## 4. 资源配置策略
- **内存比例**: 建议维持 JVM Heap 与 Managed Memory (RocksDB 使用) 的动态平衡。
- **Slot 分布**: 尽可能让计算密集的算子分布在不同的 TaskManager 上，避免 CPU 争抢。

---
*Last Updated: 2026-04-10*
