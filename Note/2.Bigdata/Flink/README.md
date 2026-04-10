# Flink 技术专题

本专题涵盖了 Flink 从基础配置、核心原理到高级 CDC 数据同步的完整笔记体系，并深度关联了 `Code/flink-demo/` 目录下的实战源码。

## 📖 核心文档
1. **[Flink 核心概念与实战指南](./Flink%20核心概念与实战指南.md)**  
   *重点回顾 Flink 架构、Watermark、Window、ProcessFunction 等核心基石。*

4. **[Flink SQL 进阶与调优](./Flink%20SQL%20%E8%BF%9B%E9%98%B6%E4%B8%8E%E8%B0%83%E4%BC%98.md)**  
   *涵盖 TVF 窗口、Join 模式选择以及 Mini-Batch 性能开关。*

5. **[Flink 性能调优实战指南](./Flink%20%E6%80%A7%E8%83%BD%E8%B0%83%E4%BC%98%E5%AE%9E%E6%88%98%E6%8C%87%E5%8D%97.md)**  
   *实战解析反压识别、数据倾斜治理及序列化优化。*

6. **[Flink CDC 数据同步](./Flink%20CDC%20%E6%95%B0%E6%8D%AE%E5%90%8C%E6%AD%A5.md)**  
   *专注于基于 Flink CDC 的异构数据库同步方案（MySQL, Oracle, Kafka 等）。*

7. **[flink-conf 详细解析](./flink-conf%E7%9B%B8%E5%85%B3%E9%85%8D%E7%BD%AE.md)**  
   *针对 TaskManager 内存、并行度、存储路径等核心参数的调优配置。*

8. **[Flink 高频面试题精选](./Flink%20%E9%AB%98%E9%A2%91%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89.md)**  
   *收录深度理论、海量数据去重及生产治理相关的硬核面试考点。*



---

## 📈 版本演进 (Roadmap)
- **Flink 1.x (当前主流)**: 1.17 - 1.20。1.19+ 引入了新的 `config.yaml` 格式。
- **Flink 2.0 (未来趋势)**: 
    - **云原生**: 深度集成 K8s，支持自动扩缩容 (Autoscaling)。
    - **API 简化**: 移除老旧的 DataSet API，核心转向流批一体的 DataStream。
    - **存算分离**: 更好的远程状态存储支持。

---


## 🛠️ 工具与命令
- **[Flink 常用命令汇总](./Flink%20%E5%B8%B8%E7%94%A8%E5%91%BD%E4%BB%A4.md)**: 包含 Job 提交、检查点手动触发、Savepoint 管理。
- **[SQL Client 使用指南](./SQL%20Client%E4%BD%BF%E7%94%A8.md)**: Flink SQL 的交互式开发环境配置。
- **[Flink SQL 与 Ambari 集成](./Flink%20SQL%E4%B8%8EAmbari%E9%9B%86%E6%88%90.md)**: 集群化管理实践。

## 💻 关联源码
- **核心示例**: [`Code/flink-demo/flink`](../../../Code/flink-demo/flink)
- **CDC 集成**: [`Code/flink-demo/flink-cdc`](../../../Code/flink-demo/flink-cdc)

---
*建议学习路径：从“核心概念与实战指南”入手，结合源码进行调试学习。*
