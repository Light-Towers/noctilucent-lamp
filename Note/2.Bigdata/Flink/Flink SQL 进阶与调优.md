# Flink SQL 进阶与调优

> **导读**: Flink SQL 不仅仅是简单的查询，它拥有强大的流式计算能力。本笔记重点介绍 1.13+ 版本引入的 **TVF 窗口** 以及常用的 SQL 优化手段。

---

## 1. 现代窗口函数 (Window TVF)
Flink 1.13 引入了表值函数（Table-Valued Function, TVF）形式的窗口，语法更标准，性能更优。

### 1.1 滚动窗口 (Tumble)
```sql
SELECT window_start, window_end, SUM(price)
FROM TABLE(
    TUMBLE(TABLE Bid, DESCRIPTOR(bidtime), INTERVAL '10' MINUTES))
GROUP BY window_start, window_end;
```

### 1.2 滑动窗口 (Hop)
```sql
SELECT window_start, window_end, SUM(price)
FROM TABLE(
    HOP(TABLE Bid, DESCRIPTOR(bidtime), INTERVAL '5' MINUTES, INTERVAL '10' MINUTES))
GROUP BY window_start, window_end;
```

### 1.3 累计窗口 (Cumulate) - 非常适合大屏统计
用于计算从整点开始到当前时间的累计值（如每 10 分钟统计一次当天的累计 GMV）。
```sql
SELECT window_start, window_end, SUM(price)
FROM TABLE(
    CUMULATE(TABLE Bid, DESCRIPTOR(bidtime), INTERVAL '10' MINUTES, INTERVAL '1' DAY))
GROUP BY window_start, window_end;
```

---

## 2. 常用 Joins
Flink SQL 支持多种 Join 模式以适应流处理场景：

- **Regular Join**: 标准 Join，两边状态都会保留（注意状态清理）。
- **Interval Join**: 限定时间范围的 Join。
- **Lookup Join (维表关联)**: 关联 Redis/HBase/MySQL。
    - *优化*: 开启 `lookup.cache.max-rows` 和 `lookup.cache.ttl`。

---

## 3. SQL 性能调优 (Mini-Batch)
在高并发场景下，逐条处理数据会导致严重的 CPU 压力。开启 Mini-Batch 可以显著提升吞吐。

```sql
-- 开启 mini-batch
SET 'table.exec.mini-batch.enabled' = 'true';
-- 预分配 5 秒的数据，或达到 5000 条才触发计算
SET 'table.exec.mini-batch.allow-latency' = '5s';
SET 'table.exec.mini-batch.size' = '5000';
-- 开启 Local-Global 优化（解决 Count(distinct) 的热点问题）
SET 'table.exec.local-hash-aggregation.enabled' = 'true';
```

---

## 4. 常见问题排查建议
- **数据倾斜**: 如果某个 Task 执行极慢，检查 `keyBy` 的字段是否存在热点数据。SQL 中可以使用 `Local-Global` 优化或手动两阶段聚合。
- **State TTL**: 务必为 SQL 任务设置状态过期时间，防止状态无限膨胀导致 OOM。
    ```sql
    SET 'table.exec.state.ttl' = '24h';
    ```

---
*Last Updated: 2026-04-10*
