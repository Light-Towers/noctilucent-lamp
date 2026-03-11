# Elasticsearch 集群缩容

介绍 **Elasticsearch 7.6** 集群从 3 节点（node-1, node-2, node-3）安全缩容至单节点（node-1）的全过程。

---

## 🏗️ 缩容操作逻辑流程图

```text
┌─────────────────────────────────────────────────────────────┐
│                  1. 准备阶段 (集群 Green 状态)                │
│       副本归零 (Replica: 0) + 排除数据节点 (Exclude: 2,3)     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  2. 核心授权 (投票排除)                      │
│      执行 POST /_cluster/voting_config_exclusions           │
│      确保 node-1 获得独立主导权 (Quorum Reconfiguration)    │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  3. 异常清理 (系统索引)                      │
│       监控索引报红？-> 临时关闭采集 -> 删除残留分片          │
│       确保集群恢复 Green，排除列表 (Exclusions) 已生效      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  4. 物理关机 (下线 2,3)                      │
│      停止 node-2/3 进程 -> node-1 自动接管 Master 角色      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  5. 落地收尾 (配置文件)                      │
│     discovery.type: single-node + 清理 persistent 设置       │
│     确保重启后单节点自愈，不寻找已不存在的节点               │
└─────────────────────────────────────────────────────────────┘

```

---

## 📘 核心概念：为什么 7.x 缩容很危险？

从 7.x 版本开始，ES 引入了全新的集群协调层。**法定人数 (Quorum)** 的概念非常关键：

* **集群共识**：一个 3 节点集群默认需要 2 个节点在线才能选举 Master。
* **锁死风险**：如果你直接暴力关闭 2 个节点，剩下的 node-1 会因为无法获得“过半数投票”而停止服务。
* **解决机制**：`voting_config_exclusions` 允许我们手动从“投票名单”中划掉即将离开的节点，让 node-1 意识到自己现在是“1 个人说了算”。

---

## 🛠️ 分阶段操作步骤

### 第一阶段：搬迁数据

1. **副本清零**：单节点无法存放副本，必须执行。
```http
PUT /_all/_settings { "index.number_of_replicas": 0 }

```


2. **强制排空节点**：告知集群将 node-2/3 的数据全部迁移至 node-1。
```http
PUT /_cluster/settings {
  "persistent": { "cluster.routing.allocation.exclude._name": "node-2,node-3" }
}

```



### 第二阶段：安全投票授权 (最关键)

3. **提交投票排除**：这是保障 node-1 关机后不锁死的唯一手段。
```http
POST /_cluster/voting_config_exclusions/node-2,node-3

```


> **注意**：若命令无响应，请通过 `GET /_cluster/state?filter_path=metadata.cluster_coordination.voting_config_exclusions` 确认列表是否已包含这两个节点。



### 第三阶段：解决监控索引报红 (Troubleshooting)

4. **关闭监控采集**：防止监控索引在 node-1 无法创建导致集群变红。
```http
PUT /_cluster/settings { "persistent": { "xpack.monitoring.collection.enabled": false } }

```


5. **清理残留分片**：删除所有状态为 `UNASSIGNED` 的监控索引。
```http
DELETE /.monitoring-es-7-*,/.monitoring-kibana-7-*

```



### 第四阶段：收尾配置

6. **修改 `elasticsearch.yml**`：
```yaml
discovery.type: single-node
# 注释掉 discovery.seed_hosts 和 cluster.initial_master_nodes

```


7. **清理全局排除规则**：
```http
PUT /_cluster/settings { "persistent": { "cluster.routing.allocation.exclude._ip": null } }

```



---

## ⚠️ 关键注意事项

> [!IMPORTANT]
> **1. 磁盘空间**：缩容前务必确认 node-1 磁盘能够容纳 3 个节点的总数据量。
> **2. IP 黑名单**：操作中若使用了 `exclude._ip`，务必检查是否误伤了 node-1 本身，否则新分片将无法分配。
> **3. 备份**：单节点模式下失去物理冗余，**必须配置 Snapshot（快照）** 定期备份到异机。
> **4. 重平衡开关**：确保 `cluster.routing.rebalance.enable` 为 `all`，以免未来扩容时数据不自动均衡。

---

## 📝 常用排查命令

* **查看分片分配失败原因**：`GET /_cluster/allocation/explain`
* **查看节点投票权排除列表**：`GET /_cluster/state` (查看 `voting_config_exclusions` 路径)
* **查看集群健康度**：`GET _cluster/health`
* **查看节点状态**：`GET _cat/nodes?v`

---
