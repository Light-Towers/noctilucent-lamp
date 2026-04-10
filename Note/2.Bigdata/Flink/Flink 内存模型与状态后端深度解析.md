# Flink 内存模型与状态后端深度解析

> **导读**: 在 Flink 生产运维中，80% 的问题（如 OOM、频繁 GC）都与内存配置和状态管理有关。本笔记基于 Flink 1.20+ 标准。

---

## 1. TaskManager 内存模型
当你设置 `taskmanager.memory.process.size` 时，Flink 实际上将其划分为两个大块：**JVM 内存** 和 **堆外内存**。

### 1.1 堆内内存 (JVM Heap)
- **Framework Heap**: 框架执行所需的内存。
- **Task Heap**: **用户代码执行**、算子内部逻辑使用的内存（如果使用 HashMapStateBackend，状态也在这里）。

### 1.2 堆外内存 (Off-Heap)
- **Managed Memory**: **托管内存**。由 Flink 框架直接管理，主要用于 **RocksDB 状态后端**、Batch 算子的排序/缓存。
- **Direct Memory**:
    - **Framework Off-Heap**: 框架堆外内存。
    - **Task Off-Heap**: 算子堆外内存（较少使用）。
    - **Network Memory**: **网络传输缓冲区**。任务之间数据传输使用的 Buffers。

> **调优技巧**: 
> - 如果使用 **RocksDB**，务必调大 `taskmanager.memory.managed.size`。
> - 如果作业 Task 很多，出现 `Direct Buffer OOM`，需调大 `taskmanager.memory.network` 相关参数。

---

## 2. 状态后端 (State Backends)
状态后端决定了 Checkpoint 时，快照数据存在哪里。

### 2.1 HashMapStateBackend (老版 Memory/FileSystem)
- **存储位置**: TaskManager 的 **Java 堆内存**。
- **特点**: 读写速度极快（内存访问），但受限于堆内存大小。
- **适用场景**: 状态较小（GB 以下）、低延迟要求极高。
- **风险**: 状态过大容易导致 OOM 或超长 GC 停顿。

### 2.2 EmbeddedRocksDBStateBackend
- **存储位置**: TaskManager 的 **本地磁盘 (RocksDB)**。
- **特点**: 状态可以“落地”，理论上支持 TB 级状态。支持**增量检查点**。
- **适用场景**: 大状态作业、高可靠性场景、TB 级数据同步。
- **风险**: 涉及磁盘 IO，读写性能略逊于内存。

---

## 3. 生产环境状态调优建议

### 3.1 开启增量检查点 (Incremental Checkpoint)
在 `config.yaml` 中配置，仅传输自上次快照以来的增量变化，极大减轻 HDFS/S3 压力。
```yaml
state.backend.incremental: true
```

### 3.2 状态保留策略 (TTL)
防止状态无限膨胀。为 Keyed State 设置过期时间。
```java
StateTtlConfig ttlConfig = StateTtlConfig
    .newBuilder(Time.days(7)) // 状态 7 天后自动清理
    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
    .build();

valueDescriptor.enableTimeToLive(ttlConfig);
```

---

## 4. 常见问题 (FAQ)

**Q: 为什么我设置了很高的 Heap 内存，TaskManager 还是 OOM？**
A: 检查是否是 **堆外内存溢出**。Flink 的网络缓冲区或托管内存（RocksDB）使用的是堆外内存。如果宿主机/容器总内存不足，JVM 进程会被操作系统 OOM-Killer 杀掉。

**Q: RocksDB 性能慢怎么办？**
A: 1. 检查磁盘是否是 SSD；2. 调大 Managed Memory；3. 开启 `state.backend.latency-track.enabled` 监控 RocksDB 执行耗时。

---
*Last Updated: 2026-04-10*
