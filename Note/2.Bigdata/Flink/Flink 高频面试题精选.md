# Flink 高频面试题精选 (2024-2025)

> **面试官视角**: “我不关心你会不会调 API，我关心你在日均百亿数据量下，如何保证系统不崩、数据不丢、结果不迟。”

---

## 1. 核心理论：Checkpoint 到底是怎么做的？
**问**: “描述一下 Flink 的检查点协调流程。”
**答**: 
- **基础论据**: Flink 使用了 **分布式快照算法 (Chandy-Lamport)**。
- **流程**: 
    1. JobManager 的 Checkpoint Coordinator 向 Source 注入 **Barrier (屏障)**。
    2. Barrier 随着数据流下发，算子收到所有上游相同 ID 的 Barrier 后（**Barrier 对齐**），将本地状态异步快照到持久化存储。
    3. 算子向 JM 反馈快照成功。
    4. 所有算子确认后，由于 JM 记录元数据，Checkpoint 完成。
- **进阶**: 如果面试官问“非对齐检查点 (Unaligned Checkpoint)”，答：用于解决反压严重时 Barrier 传输慢的问题，通过同时快照算子状态和网络管道中的数据实现。

---

## 2. 生产实战：如何处理海量数据去重 (UV)？
**问**: “实时计算日活 (UV)，数据量太大状态存不下怎么办？”
**答**: 
1. **常规方案**: 使用 `KeyedState` 存储 UserID，利用 `MapState` 或 `Set`。缺点是内存压力大。
2. **优化方案 (Bloom Filter)**: 使用 **布隆过滤器** 结合状态。每个 Key（如天级）对应一个 Bitmap，极大降低存储开销，但有极微小的误差。
3. **外部存储**: 状态存入外部 **Redis (HyperLogLog)** 或高性能位图数据库（如 ClickHouse/Doris）。

---

## 3. 对比分析：Flink vs Spark Streaming 
**问**: “为什么实时计算目前首选 Flink 而不是 Spark？”
**答**: 
- **架构**: Flink 是真正的**原生流处理**（逐条触发），Spark Streaming 是**微批处理**（Micro-Batch）。
- **延迟**: Flink 延迟可达毫秒级，Spark 通常在秒级。
- **状态管理**: Flink 的状态管理（Managed State）更细粒度且支持增量检查点，适合超大状态。
- **时间语义**: Flink 对 Event Time 和 Watermark 的支持远比 Spark 完善。

---

## 4. 故障治理：数据倾斜的“套路”是什么？
**问**: “遇到 KeyBy 后的数据倾斜，你有哪些招数？”
**答**: 
1. **预聚合 (Local-Global Aggregation)**: 类似于 MapReduce 的 Combine，在 SQL 中开启 `mini-batch` 即可实现。
2. **两阶段聚合 (加盐打散)**: 
    - 第一阶段：给 Key 拼上随机随机前缀（盐），进行聚合。
    - 第二阶段：去掉前缀，进行全局聚合。这是处理非 SQL API 倾斜的杀手锏。
3. **自定义分区**: 针对由于数据规律导致的分布不均，重写 `Partitioner`。

---

## 5. 状态进阶：Checkpoint 与 Savepoint 的区别
**问**: “既然有 Checkpoint，为什么还需要 Savepoint？”
**答**: 
- **性质**: Checkpoint 是**自动**的，侧重容错；Savepoint 是**手动**的，侧重运维。
- **生命周期**: Checkpoint 随着任务停止通常会清理；Savepoint 是持久的，直到手动删除。
- **兼容性**: Savepoint 具有更好的跨版本/跨集群兼容性，常用于 **程序升级、集群迁移、扩缩容**。

---
*Last Updated: 2026-04-10*
