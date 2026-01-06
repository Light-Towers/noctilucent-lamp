# HNSW 与 ANN 详解

## 📚 基本定义

### **ANN**（近似最近邻搜索）
**Approximate Nearest Neighbor Search** - 在高维空间中**近似**查找最近邻向量的算法家族。

**核心思想**：
- **牺牲少量精度**换取**数量级的速度提升**
- 避免暴力扫描全部数据（O(N)复杂度）
- 适合大规模高维向量检索场景

### **HNSW**（分层可导航小世界图）
**Hierarchical Navigable Small World** - 当前**最流行、性能最好的ANN算法之一**。

**特点**：
- 基于**多层图结构**的近似最近邻搜索算法
- 召回率高、查询速度快、支持动态更新
- 广泛应用于向量数据库和搜索系统

---

## 🔍 ANN 的深层解析

### **为什么需要ANN？**
| 场景 | 精确最近邻搜索的问题 | ANN的解决方案 |
|------|-------------------|-------------|
| 100万条128维向量 | 需计算100万次距离，耗时数秒 | 只需计算数千次，耗时毫秒级 |
| 电商图片搜索 | 精确匹配无法实时响应 | 近似匹配实现毫秒级响应 |
| 大模型RAG检索 | 需要从海量文档中快速找到相关内容 | 快速检索最相关文本片段 |

### **ANN算法分类**
```
ANN算法家族
├── 树-based（空间划分）
│   ├── KD-Tree
│   ├── Ball Tree
│   └── Annoy（Spotify开源）
├── 哈希-based
│   ├── LSH（局部敏感哈希）
│   └── Spectral Hashing
├── 量化-based
│   ├── PQ（乘积量化）
│   ├── SQ（标量化化）
│   └── LQ（学习量化）
└── 图-based ★ 当前主流
    ├── NSW（可导航小世界）
    ├── HNSW（分层NSW）★ 最流行
    ├── FANNG
    └── NGT（Yahoo开源）
```

### **精度-速度权衡曲线**
```
召回率(Recall)
    ↑ 100% │
        │  暴力搜索
        │    │
    98% │    │╲
        │    │ ╲
    95% │    │  ╲ HNSW
        │    │   ╲
    90% │    │    ╲
        │    │     ╲其他ANN算法
        └────┴───────→ 查询时间
          慢          快
```
*HNSW在相同速度下召回率最高，或相同召回率下速度最快*

---

## 🏗️ HNSW 算法原理详解

### **核心数据结构：分层图**
```
层级 2：    A ---------- C
            |           
层级 1：    A ---- B ---- C ---- D
            |      |      |      |
层级 0：    A ---- B ---- C ---- D ---- E
```
- **第0层**：包含所有数据点，连接最密集
- **更高层**：随机选择部分节点，连接逐渐稀疏
- **搜索路径**：从顶层开始，每层找到最近邻，然后进入下层细化

### **构建过程（插入新节点M）**
```python
def insert_hnsw(node, vector, max_layers):
    # 1. 随机分配层数（类似跳表）
    layer = random_level(max_layers)
    
    # 2. 从最高层开始搜索入口点
    entry_point = top_layer_entry
    for l in range(max_layers, layer, -1):
        entry_point = search_layer(entry_point, vector, l)
    
    # 3. 逐层插入并建立连接
    for l in range(min(layer, max_layers), -1, -1):
        # 在当前层查找最近邻
        neighbors = search_layer(entry_point, vector, l)
        # 建立双向连接
        connect_node(node, neighbors, l)
        # 调整连接数（启发式选择）
        prune_connections(node, l)
    
    # 4. 更新入口点（如果新节点在更高层）
    if layer > top_layer:
        update_entry_point(node)
```

### **搜索过程（查找最近邻）**
```python
def search_hnsw(query, k, ef_search):
    # 1. 从最高层入口点开始
    current = entry_point
    for layer in range(max_layer, 0, -1):
        current = greedy_search(current, query, layer)
    
    # 2. 在第0层进行精细化搜索
    candidates = PriorityQueue()
    visited = Set()
    
    # 初始化候选集
    candidates.add(current, distance(query, current))
    visited.add(current)
    
    # 3. 探索候选集的邻居
    while candidates:
        nearest = candidates.pop_nearest()
        results.add(nearest)
        
        if len(results) >= k and distance改进小于阈值:
            break
            
        for neighbor in nearest.neighbors[0]:  # 第0层邻居
            if neighbor not in visited:
                visited.add(neighbor)
                dist = distance(query, neighbor)
                if dist < candidates.farthest_distance():
                    candidates.add(neighbor, dist)
    
    return results.top_k(k)
```

### **关键参数解析**
| 参数 | 作用 | 典型值 | 影响 |
|------|------|--------|------|
| **M** | 每层最大连接数 | 16-64 | ↑则召回率↑、内存↑、构建时间↑ |
| **efConstruction** | 构建时的候选集大小 | 100-400 | ↑则构建质量↑、构建时间↑ |
| **efSearch** | 搜索时的候选集大小 | 50-400 | ↑则召回率↑、查询时间↑ |
| **max_layers** | 最大层数 | 自动计算 | 影响搜索路径长度 |

---

## ⚡ HNSW 性能优势

### **1. 时间复杂度对比**
| 算法 | 构建复杂度 | 查询复杂度 | 内存占用 |
|------|-----------|-----------|---------|
| 暴力搜索 | O(1) | O(N) | O(N) |
| KD-Tree | O(N log N) | O(log N)~O(N) | O(N) |
| LSH | O(N) | O(1)但需要多个哈希表 | O(N×L) |
| **HNSW** | O(N log N) | **O(log N)** | O(M×N) |

### **2. 实际性能数据**
在100万条128维向量数据集上：
```
算法          查询时间    召回率@10    内存占用
-----------  ---------  ----------  ---------
暴力搜索       3500ms     100%        512MB
Annoy          45ms       85%        200MB
IVF-PQ         12ms       78%        64MB
HNSW           **2ms**    **98%**     320MB
```
*HNSW在召回率和延迟上达到最佳平衡*

### **3. 独特优势**
- **支持动态更新**：可增量插入/删除，无需重建索引
- **高维友好**：维度灾难影响较小
- **无需训练**：与PQ等需要训练的算法相比，使用更简单
- **理论保证**：基于小世界网络理论，搜索路径对数增长

---

## 🛠️ HNSW 在 Doris 中的应用

### **创建带HNSW索引的表**
> Doris 中的向量索引不使用 `USING HNSW` 和 `COMMENT` 来定义参数，而是使用 `USING ANN` 配合 `PROPERTIES`。另外，`ARRAY` 类型定义中不需要指定维度，维度是在索引属性中指定的。
> [[Approximate Nearest Neighbor Search](https://doris.apache.org/docs/4.x/ai/vector-search/overview/#approximate-nearest-neighbor-search)]; [[ANN Index Management](https://doris.apache.org/docs/4.x/ai/vector-search/index-management/)]
```sql
-- 创建支持向量检索的表
CREATE TABLE product_embeddings (
    id BIGINT,
    product_name VARCHAR(255),
    embedding ARRAY<FLOAT> NOT NULL,
    INDEX ann_idx (embedding) USING ANN PROPERTIES (
        "index_type" = "hnsw",
        "metric_type" = "l2_distance", -- 或 inner_product
        "dim" = "128",
        "max_degree" = "32",           -- 对应算法中的 M
        "ef_construction" = "200"
    )
)
DISTRIBUTED BY HASH(id);

-- 创建倒排+向量复合索引
CREATE TABLE documents (
    doc_id BIGINT,
    content TEXT,
    embedding ARRAY<FLOAT> NOT NULL,
    FULLTEXT INDEX (content),
    INDEX vec_idx (embedding) USING ANN PROPERTIES (
        "index_type" = "hnsw",
        "metric_type" = "inner_product",
        "dim" = "768"
    )
);
```

### **混合查询示例**
> Doris 为了区分暴力计算和利用索引的近似计算，通常使用带有 `_approximate` 后缀的函数来显式触发 ANN 索引检索。
> 将 `cosine_distance` 或 `l2_distance` 替换为 **`l2_distance_approximate`** 或 **`inner_product_approximate`**。
> *   注：Doris 目前主要支持 L2 距离和内积（Inner Product）。余弦相似度通常通过归一化向量后的内积来实现。
> [[A vector index for vector search](https://doris.apache.org/docs/4.x/releasenotes/v4.0/release-4.0.0/#a-vector-index-for-vector-search)]
```sql
-- 纯向量相似度搜索
SELECT product_name, 
       l2_distance_approximate(embedding, [0.12, 0.34, ...]) as score
FROM product_embeddings
ORDER BY score ASC
LIMIT 10;

-- 全文检索 + 向量检索融合（RRF算法）
SELECT doc_id, content,
       (0.4 * bm25_score + 0.6 * (1 - inner_product_approximate(normalize(embedding), normalize(query_vec)))) as final_score
FROM documents
WHERE content MATCH '智能手机 评测'
  AND inner_product_approximate(normalize(embedding), normalize(query_vec)) > 0.7
ORDER BY final_score DESC
LIMIT 20;

-- 带过滤条件的向量检索
SELECT * FROM products
WHERE category = 'electronics'
  AND price < 1000
ORDER BY l2_distance_approximate(embedding, query_vec)
LIMIT 10;
```

### **性能调优建议**
> Doris 目前**不支持**通过 `ALTER TABLE ... MODIFY INDEX ... REBUILD` 这种语法直接修改索引参数。
> **Doris 正确操作**: 必须先删除索引，再重新创建并构建索引。
> ```sql
> -- 1. 删除旧索引
> ALTER TABLE my_table DROP INDEX ann_idx;
> -- 2. 创建新索引
> CREATE INDEX ann_idx ON my_table (embedding) USING ANN PROPERTIES (...);
> -- 3. 触发构建
> BUILD INDEX ann_idx ON my_table;
> ```
> [[DROP INDEX](https://doris.apache.org/docs/4.x/ai/vector-search/hnsw/#drop-index)]; [[Creating ANN Indexes](https://doris.apache.org/docs/4.x/ai/vector-search/index-management/#examples)]
```sql
-- 1. 删除旧索引
ALTER TABLE my_table DROP INDEX ann_idx;
-- 2. 创建新索引
CREATE INDEX ann_idx ON my_table (embedding) USING ANN PROPERTIES (
    "index_type" = "hnsw",
    "metric_type" = "l2_distance",
    "dim" = "128",
    "max_degree" = "48",
    "ef_construction" = "300"
);
-- 3. 触发构建
BUILD INDEX ann_idx ON my_table;

-- 4. 分区减少搜索范围
CREATE TABLE embeddings_partitioned (
    id BIGINT,
    vec ARRAY<FLOAT>(256),
    category VARCHAR(32)
)
PARTITION BY LIST(category) (
    PARTITION p1 VALUES IN ('电子产品'),
    PARTITION p2 VALUES IN ('服装')
);

-- 5. 使用预过滤加速
SELECT /*+ SET_VAR(enable_vector_filter_pushdown=true) */ 
       *
FROM large_table
WHERE region = '北京'
ORDER BY l2_distance_approximate(embedding, query_vec)
LIMIT 100;
```

### **HNSW+PQ 量化支持**
> 💡 **注意**：此功能已在 Apache Doris 4.0 中作为现有功能提供，并非未来发展方向。通过 `quantizer` 属性可启用PQ量化。

```sql
PROPERTIES (
    "index_type" = "hnsw",
    "quantizer" = "pq",      -- 指定使用PQ量化
    "pq_m" = "8",            -- 子向量切分数量
    "pq_nbits" = "8"         -- 量化位数
)
```

### **参数名称对应关系**
| 算法通用名 | Doris 参数名 | 说明 |
|-----------|------------|------|
| M | max_degree | 建表时在PROPERTIES中设置 |
| efConstruction | ef_construction | 建表时在PROPERTIES中设置 |
| efSearch | hnsw_ef_search | Session变量，需在查询前用 `SET hnsw_ef_search = 100;` 设置 |

---

## 📊 应用场景案例

### **案例1：电商图片搜索**
```sql
-- 用户上传图片 → 提取特征向量 → 搜索相似商品
-- 注意：此处的cosine_distance为精确计算，不利用ANN索引。如需加速，应使用归一化后的inner_product_approximate。
SELECT product_id, product_name, price,
       1 - cosine_distance(img_embedding, :query_vec) as similarity
FROM product_images
WHERE similarity > 0.85
  AND status = 'on_shelf'
ORDER BY similarity DESC
LIMIT 50;
-- 响应时间：< 50ms (1亿商品库)
```

### **案例2：大模型RAG检索**
```sql
-- 从知识库检索相关文档片段
-- 注意：此处的cosine_distance为精确计算，不利用ANN索引。在实际应用中，对于海量数据，应考虑使用ANN索引。
WITH ranked_chunks AS (
    SELECT chunk_id, content,
           cosine_distance(embedding, :question_vec) as dist
    FROM document_chunks
    WHERE dist < 0.25
    ORDER BY dist ASC
    LIMIT 100
)
-- 使用RRF融合多个检索结果
SELECT chunk_id, content,
       (0.7 * (1 - dist) + 0.3 * bm25_score) as relevance
FROM ranked_chunks
ORDER BY relevance DESC
LIMIT 5;
```

### **案例3：推荐系统召回**
```sql
-- 基于用户向量召回相似物品
EXPLAIN ANALYZE
SELECT item_id, 
       dot_product(user_embedding, item_embedding) as preference_score
FROM candidate_items
WHERE category IN ('电子产品', '数码配件')
ORDER BY preference_score DESC
LIMIT 100;
-- 使用HNSW索引后：从200ms → 8ms
```

---

## 🔮 未来发展与挑战

### **HNSW的改进方向**
1. **DiskANN**：将HNSW适配到磁盘存储，减少内存压力
2. **HNSW+PQ**：结合乘积量化，进一步压缩内存
3. **并行化HNSW**：支持GPU/多核并行构建与查询

### **在Doris中的演进**
- **更智能的参数调优**：基于数据分布自动优化M、ef参数
- **混合索引支持**：HNSW + 倒排 + 标量索引的联合优化
- **量化感知训练**：针对量化后的向量优化HNSW构建

---

## 💎 总结要点

| 特性 | HNSW | 传统ANN算法 |
|------|------|------------|
| **查询速度** | ⭐⭐⭐⭐⭐ (O(log N)) | ⭐⭐⭐ (通常O(√N)) |
| **召回率** | ⭐⭐⭐⭐⭐ (95%+) | ⭐⭐⭐ (70-90%) |
| **内存占用** | ⭐⭐⭐ (较高) | ⭐⭐⭐⭐ (较低) |
| **动态更新** | ⭐⭐⭐⭐⭐ (支持) | ⭐⭐ (通常需重建) |
| **实现复杂度** | ⭐⭐⭐ (中等) | ⭐⭐ (简单) |

**选择建议**：
- **首选HNSW当**：需要高召回、低延迟、支持动态更新
- **考虑其他方案当**：内存极度受限（用PQ）、维度<16（用KD-Tree）

**Doris的独特价值**：
- 将HNSW向量检索与SQL查询、OLAP分析深度集成
- 支持向量+全文+结构化数据的统一检索
- 借助MPP架构实现大规模向量数据的分布式处理

HNSW作为当前ANN算法的**黄金标准**，在Doris中的集成使其能够胜任现代AI应用所需的**实时向量检索**需求，是构建智能数据分析平台的关键技术组件。
