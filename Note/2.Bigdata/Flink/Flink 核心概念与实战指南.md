# Flink 核心概念与实战指南

> **导读**: 本笔记旨在将 Flink 的核心理论与项目中的代码实践相结合。
> 关联源码位置: `Code/flink-demo/flink/src/main/java/com/study/`

---

## 1. Flink 核心架构
Flink 是一个分布式、高性能、高可用的流处理框架。
- **JobManager**: 控制节点，负责任务调度、Checkpoint 协调等。
- **TaskManager**: 执行节点，负责具体的 Task 计算，内部划分为多个 **Slot**。

---

## 2. 数据流编程 (Source -> Transformation -> Sink)

### 2.1 数据源 (Source)
Flink 支持多种数据源接入：
- **常用实现**: Kafka, Socket, File, 自定义 Source。
- [参考代码: Kafka 数据源](../../../Code/flink-demo/flink/src/main/java/com/study/source/SourceKafka.java)
- [参考代码: 自定义数据生成 (ClickSource)](../../../Code/flink-demo/flink/src/main/java/com/study/source/ClickSource.java)

### 2.2 数据转换 (Transformation)
- **基本算子**: `map`, `filter`, `flatMap`。
- **聚合算子**: `keyBy`, `reduce`, `aggregate`。
- **多流转换**: `union`, `connect`。
- [参考代码: Connect 算子](../../../Code/flink-demo/flink/src/main/java/com/study/transfer/ConnectExample.java)

### 2.3 输出 (Sink)
常用的外部引擎输出：
- [参考代码: 输出到 MySQL](../../../Code/flink-demo/flink/src/main/java/com/study/sink/SinkMysql.java)
- [参考代码: 输出到 Elasticsearch](../../../Code/flink-demo/flink/src/main/java/com/study/sink/SinkEs.java)
- [参考代码: 输出到 HBase](../../../Code/flink-demo/flink/src/main/java/com/study/sink/SinkHbase.java)

---

## 3. 时间语义与水位线 (Time & Watermark)
在处理无界流数据时，**乱序** 是难以避免的。
- **Event Time**: 事件发生的真实时间（推荐使用）。
- **Watermark**: 一种衡量 Event Time 进展的机制。当 `Watermark(T)` 到达算子，表示 `T` 之前的数据已全部到齐。
- [参考代码: 自定义 Watermark](../../../Code/flink-demo/flink/src/main/java/com/study/watermark/CustomWatermarkTest.java)
- [参考代码: 结合窗口与水位线](../../../Code/flink-demo/flink/src/main/java/com/study/window/WatermarkExample.java)

---

## 4. 窗口机制 (Window)
将无限流切分为有限块进行计算。
- **Tumbling (滚动)**: 固定大小，不重叠。
- **Sliding (滑动)**: 固定大小，有重叠。
- **Session (会话)**: 根据时间间隔切分。
- [参考代码: 窗口聚合 (AggregateFunction)](../../../Code/flink-demo/flink/src/main/java/com/study/window/WindowAggregateFunctionExample.java)

---

## 5. 处理函数 (ProcessFunction)
Flink 提供的最底层、最灵活的 API，可以直接访问 **时间戳**、**水位线** 和 **状态 (State)**。
- 广泛用于实现自定义业务逻辑，如 **TopN 排序**、**侧输出流 (Side Output)**。
- [参考代码: 侧输出流](../../../Code/flink-demo/flink/src/main/java/com/study/process/SideOutputExample.java)
- [参考代码: 基于 KeyedProcessFunction 的 TopN 实现](../../../Code/flink-demo/flink/src/main/java/com/study/process/TopN/KeyedProcessTopN.java)

---

## 6. 状态管理与容错 (State & Checkpoint)
- **State**: 算子内部的本地变量，经过 Checkpoint 持久化。
- **Checkpoint**: 分布式一致性快照，是故障恢复的基石。
- **Exactly-once**: 通过两阶段提交（2PC）协议结合 Checkpoint 实现。

---
*Last Updated: 2026-04-10*
