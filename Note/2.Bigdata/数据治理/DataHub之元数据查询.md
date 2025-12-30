# DataHub 元数据查询指南（基于 GraphQL）

## 一、概述与基础配置

### 1.1 认证配置（建议设置为环境变量）

**环境变量设置**：
```bash
# 设置DataHub服务地址（根据实际环境修改）
export DATAHUB_URL="http://localhost:9002/api/graphql"

# 设置访问令牌（从DataHub UI生成）
export TOKEN="Bearer <your-access-token>"
```

**直接使用curl命令的示例**：
```bash
# 一次性查询示例
curl -X POST "http://localhost:9002/api/graphql" \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ listDomains(input: {start: 0, count: 10}) { total domains { urn properties { name } } } }"}'
```

### 1.2 GraphiQL 工具使用指南

GraphiQL 是 DataHub 内置的交互式 GraphQL 查询工具，适用于开发和调试。

**访问方式**：
- 浏览器访问：`http://localhost:9002/api/graphiql`
- 或在 DataHub UI 右上角用户菜单中找到 GraphiQL 入口

**主要功能**：
- **自动补全**：输入时按 `Ctrl+Space` 触发
- **文档查询**：右侧"Docs"按钮查看所有可用查询和变更操作
- **变量支持**：底部 Variables 区定义查询变量
- **历史记录**：自动保存查询历史

**认证配置**：
```json
// 在HTTP Headers区域添加
{
  "Authorization": "Bearer <your-access-token>"
}
```

### 1.3 GraphQL 基础语法

GraphQL 采用结构化查询语法，主要操作类型：

#### 查询（Query） - 读取数据
```json
query GetDatasetInfo {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,sample_db.table,PROD)") {
    name
    description
    properties {
      qualifiedName
      externalUrl
    }
  }
}
```

#### 变更（Mutation） - 修改数据
```json
mutation AddTagToDataset {
  addTag(input: {
    tagUrn: "urn:li:tag:important",
    resourceUrn: "urn:li:dataset:(urn:li:dataPlatform:mysql,sample_db.table,PROD)"
  })
}
```

#### 变量（Variables） - 参数化查询
```json
query GetDataset($urn: String!) {
  dataset(urn: $urn) {
    name
    description
  }
}
```
```json
// Variables区域
{
  "urn": "urn:li:dataset:(urn:li:dataPlatform:mysql,test_db.table,PROD)"
}
```

## 二、核心查询操作

### 2.1 域名管理

#### 查询所有域名（根域名）
```json
query listRootDomains {
  listDomains(input: { 
    start: 0, 
    count: 100
    # 不指定parentDomain则查询根域名
  }) {
    start
    count
    total
    domains {
      urn
      properties { 
        name 
        description
      }
    }
  }
}
```
**执行命令**：
```bash
curl -X POST "$DATAHUB_URL" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "query": "query { listDomains(input: {start: 0, count: 100}) { start count total domains { urn properties { name description } } } }" }'
```

#### 查询子域名列表
```json
query listSubDomains {
  listDomains(input: { 
    start: 0, 
    count: 100, 
    parentDomain: "urn:li:domain:parent-domain-urn"
  }) {
    start
    count
    total
    domains {
      urn
      properties { 
        name 
        description
      }
    }
  }
}
```

### 2.2 数据集查询

#### 查询特定域名下的数据集
```json
query searchDatasetsByDomain {
  search(input: {
    type: DATASET
    query: "*"  # 搜索所有数据集
    start: 0
    count: 50
    orFilters: [
      {
        and: { 
          field: "domains", 
          values: ["urn:li:domain:target-domain-urn"] 
        }
      }
    ]
  }) {
    start
    count
    total
    searchResults {
      entity {
        ... on Dataset {
          urn
          name
          type
          platform {
            name
          }
        }
      }
    }
  }
}
```

#### 查询特定数据源的表（MySQL示例）
```json
query searchMySQLTables {
  search(input: {
    type: DATASET,
    query: "app_mall.*",  # 支持通配符
    start: 0,
    count: 100,
    orFilters: [
      {
        and: [
          {
            field: "platform",
            values: ["mysql"],
            condition: CONTAIN
          }
        ]
      }
    ]
  }) {
    start
    count
    total
    searchResults {
      entity {
        ... on Dataset {
          urn
          name
          type
          properties {
            qualifiedName
            description
          }
        }
      }
    }
  }
}
```

#### 查询数据集详细信息
```json
query getDatasetDetails {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    urn
    name
    type
    properties {
      qualifiedName
      description
      externalUrl
      created {
        time
        actor
      }
      lastModified {
        time
        actor
      }
    }
    platform {
      name
      displayName
    }
    schemaMetadata(version: 0) {
      fields {
        fieldPath
        type
        nativeDataType
        description
        isPartOfKey
        nullable
      }
    }
  }
}
```

### 2.3 数据平台管理

#### 查询所有可接入平台
```json
query listDataPlatforms {
  search(input: {
    type: DATA_PLATFORM
    query: ""
    start: 0
    count: 100
  }) {
    searchResults {
      entity {
        ... on DataPlatform {
          urn
          name
          type
          properties {
            displayName
            description
          }
        }
      }
    }
  }
}
```

#### 查询已集成的数据源
```json
query listIngestionSources {
  listIngestionSources(input: {start: 0, count: 100}) {
    ingestionSources {
      urn
      type
      name
      config {
        version
        debugMode
      }
      schedule {
        interval
        timezone
      }
    }
  }
}
```
**执行命令**：
```bash
curl -X POST "$DATAHUB_URL" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ listIngestionSources(input: {start: 0, count: 100}) { ingestionSources { urn type name config schedule { interval timezone } } } }"}'
```

## 三、高级查询功能

### 3.1 数据血缘查询

#### 查询数据集的血缘关系
```json
query datasetLineage {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:hive,SampleHiveDataset,PROD)") {
    urn
    name
    # 查询上游血缘（来源）
    upstream: relationships(input: { 
      types: ["DownstreamOf"], 
      direction: INCOMING,
      start: 0,
      count: 50
    }) {
      start
      count
      total
      relationships {
        entity {
          urn
          ... on Dataset {
            name
            type
            platform {
              name
            }
          }
        }
        type
      }
    }
    # 查询下游血缘（影响）
    downstream: relationships(input: { 
      types: ["DownstreamOf"], 
      direction: OUTGOING,
      start: 0,
      count: 50
    }) {
      start
      count
      total
      relationships {
        entity {
          urn
          ... on Dataset {
            name
            type
          }
        }
        type
      }
    }
  }
}
```

#### 查询字段级血缘
```json
query getColumnLineage($urn: String!) {
  dataset(urn: $urn) {
    urn
    name
    # 核心字段：字段级血缘
    fineGrainedLineages {
      # 上游字段列表
      upstreams {
        urn
      }
      # 下游字段列表（通常是当前表中的字段）
      downstreams {
        urn
      }
      # 转换逻辑描述
      transformOperation
      # 如果是通过 SQL 解析出来的，这里可能有对应的 SQL
      query
    }
  }
}
```

#### 查询血缘影响分析（数据溯源）
```json
query lineageImpactAnalysis {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,critical_table,PROD)") {
    name
    # 完整的血缘路径
    lineage(input: {
      startTimeMillis: "1704067200000",  # 开始时间戳
      endTimeMillis: "1704153600000",    # 结束时间戳
      separateByEntityType: true
    }) {
      upstream {
        entities {
          urn
          type
        }
        paths {
          path {
            urn
            type
          }
        }
      }
      downstream {
        entities {
          urn
          type
        }
        paths {
          path {
            urn
            type
          }
        }
      }
    }
  }
}
```

### 3.2 数据指标查询

#### 查询数据集的使用统计
```json
query datasetUsageStats {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    name
    usageStats(startTimeMillis: "1704067200000", endTimeMillis: "1704153600000") {
      buckets {
        bucket
        metrics {
          totalSqlQueries
          uniqueUserCount
          topUsers(first: 10) {
            edges {
              node {
                user {
                  urn
                  username
                }
                count
              }
            }
          }
          frequentFields(first: 10) {
            edges {
              node {
                fieldPath
                count
              }
            }
          }
        }
      }
    }
  }
}
```

#### 查询数据质量指标
```json
query dataQualityMetrics {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    name
    # 断言（数据质量规则）执行结果
    assertions(start: 0, count: 50) {
      total
      assertions {
        urn
        info {
          type
          description
          lastUpdated {
            time
            actor
          }
        }
        runEvents(status: COMPLETE, start: 0, count: 10) {
          total
          runEvents {
            result {
              type
              actualAggValue
              externalUrl
            }
            timestampMillis
          }
        }
      }
    }
  }
}
```

#### 查询数据集新鲜度指标
```json
query datasetFreshness {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:kafka,topic,PROD)") {
    name
    # 时间序列统计
    timeSeriesStats(metricName: "eventCount", startTimeMillis: "1704067200000", endTimeMillis: "1704153600000") {
      buckets {
        bucket
        value
      }
    }
    # 最后更新时间
    properties {
      lastModified {
        time
      }
    }
  }
}
```

### 3.3 标签与术语查询

#### 查询业务术语表
```json
query listGlossaryTerms {
  search(input: {
    type: GLOSSARY_TERM
    query: ""
    start: 0
    count: 100
  }) {
    searchResults {
      entity {
        ... on GlossaryTerm {
          urn
          name
          description
          properties {
            definition
            termSource
            customProperties {
              key
              value
            }
          }
          parentNodes {
            ... on GlossaryNode {
              urn
              properties {
                name
              }
            }
          }
        }
      }
    }
  }
}
```

#### 查询数据集标签
```json
query datasetTags {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    name
    tags {
      tags {
        tag {
          urn
          name
          description
        }
        associatedUrn
        associatedTime
      }
    }
    # 全局标签（非特定于数据集的）
    globalTags {
      tags {
        tag {
          urn
          name
        }
      }
    }
  }
}
```

### 3.4 用户与权限查询

#### 查询数据资产的所有者
```json
query datasetOwners {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    name
    ownership {
      owners {
        owner {
          urn
          ... on CorpUser {
            username
            info {
              displayName
              email
              title
            }
          }
          ... on CorpGroup {
            name
            info {
              displayName
              email
            }
          }
        }
        type
      }
      lastModified {
        time
        actor
      }
    }
  }
}
```

#### 查询访问权限信息
```json
query accessPermissions {
  dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") {
    name
    # 访问策略
    policies {
      policies {
        urn
        name
        description
        privileges
        actors {
          users
          groups
          resourceOwners
        }
      }
    }
  }
}
```

## 四、实用查询示例

### 4.1 常用查询片段

#### 批量查询多个数据集信息
```json
query batchDatasetInfo {
  # 查询数据集1
  dataset1: dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table1,PROD)") {
    name
    description
    schemaMetadata(version: 0) {
      fields {
        fieldPath
        type
      }
    }
  }
  # 查询数据集2
  dataset2: dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table2,PROD)") {
    name
    description
    schemaMetadata(version: 0) {
      fields {
        fieldPath
        type
      }
    }
  }
}
```

#### 分页查询所有表及字段信息
```json
query allDatasetsWithSchema {
  search(input: {type: DATASET, query: "*", start: 0, count: 20}) {
    start
    count
    total
    searchResults {
      entity {
        ... on Dataset {
          name
          platform {
            name
          }
          schemaMetadata(version: 0) {
            fields {
              fieldPath
              type
              description
            }
          }
        }
      }
    }
  }
}
```

### 4.2 变量使用示例

#### 使用变量进行参数化查询
```json
query getDatasetWithVariables($urn: String!, $includeSchema: Boolean = true) {
  dataset(urn: $urn) {
    name
    description
    properties {
      qualifiedName
    }
    schemaMetadata(version: 0) @include(if: $includeSchema) {
      fields {
        fieldPath
        type
      }
    }
  }
}
```
```json
// Variables
{
  "urn": "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)",
  "includeSchema": true
}
```

### 4.3 变更（Mutation）操作示例

#### 给数据集添加标签
```json
mutation addTagToDataset {
  addTag(input: {
    tagUrn: "urn:li:tag:important",
    resourceUrn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)"
  })
}
```

#### 更新数据集描述
```json
mutation updateDatasetDescription {
  updateDatasetDescription(input: {
    description: "这是更新后的数据集描述，包含更多业务上下文信息。",
    resourceUrn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)"
  })
}
```

#### 添加数据资产所有者
```json
mutation addDatasetOwner {
  addOwner(input: {
    ownerUrn: "urn:li:corpuser:user123",
    ownerType: TECHNICAL_OWNER,
    resourceUrn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)"
  })
}
```

## 五、注意事项与最佳实践

### 5.1 性能优化建议

1. **分页查询**：始终使用 `start` 和 `count` 参数控制返回数据量
2. **字段选择**：只查询需要的字段，避免查询 `*` 所有字段
3. **缓存策略**：对于不常变的数据，可适当缓存查询结果
4. **批量操作**：使用变量和批量查询减少请求次数

### 5.2 安全注意事项

1. **令牌管理**：
   - 访问令牌有效期有限，定期更新
   - 不要在代码中硬编码令牌，使用环境变量
   - 通过DataHub UI的"Settings" -> "Access Tokens"生成新令牌

2. **权限控制**：
   - 遵循最小权限原则，按需分配查询权限
   - 敏感数据查询需额外授权
   - 定期审计查询日志

3. **数据安全**：
   - 避免在查询中暴露敏感业务逻辑
   - 生产环境使用HTTPS
   - 启用查询日志审计

### 5.3 常见问题解答

**Q1: 如何获取数据集的URN？**
- 方法1：在DataHub UI中打开数据集详情页，点击"Copy URN"按钮
- 方法2：通过搜索查询获取：`search { searchResults { entity { ... on Dataset { urn name } } } }`

**Q2: 查询返回空结果怎么办？**
1. 检查认证令牌是否有效
2. 确认查询的URN格式正确
3. 验证数据是否已成功摄取到DataHub
4. 检查是否有权限访问该数据资产

**Q3: 如何查询特定时间范围的数据？**
- 使用时间戳参数：`startTimeMillis` 和 `endTimeMillis`
- 时间格式：Unix毫秒时间戳，如 `1704067200000` 表示 2024-01-01 00:00:00 UTC

**Q4: GraphQL查询语法错误如何处理？**
1. 使用GraphiQL工具的语法验证功能
2. 查看GraphiQL右侧的文档说明
3. 逐步构建查询，先验证简单查询再扩展

**Q5: 如何优化复杂血缘查询性能？**
1. 限制查询的时间范围
2. 使用分页参数控制返回数据量
3. 考虑使用异步查询或后台任务处理

### 5.4 实用脚本示例

#### Python查询脚本示例
```python
import requests
import os
import json

# 配置
DATAHUB_URL = os.getenv("DATAHUB_URL", "http://localhost:9002/api/graphql")
TOKEN = os.getenv("DATAHUB_TOKEN", "")

def query_datahub(graphql_query, variables=None):
    """执行GraphQL查询"""
    headers = {
        "Authorization": TOKEN,
        "Content-Type": "application/json"
    }
    
    payload = {"query": graphql_query}
    if variables:
        payload["variables"] = variables
    
    response = requests.post(DATAHUB_URL, headers=headers, json=payload)
    response.raise_for_status()
    return response.json()

# 示例：查询数据集信息
dataset_query = """
query GetDatasetInfo($urn: String!) {
  dataset(urn: $urn) {
    name
    description
    properties {
      qualifiedName
    }
    schemaMetadata(version: 0) {
      fields {
        fieldPath
        type
      }
    }
  }
}
"""

variables = {
    "urn": "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)"
}

result = query_datahub(dataset_query, variables)
print(json.dumps(result, indent=2, ensure_ascii=False))
```

#### Shell脚本批量查询示例
```bash
#!/bin/bash

# 配置
DATAHUB_URL="http://localhost:9002/api/graphql"
TOKEN="Bearer <your-token>"

# 查询函数
query_datahub() {
    local query=$1
    curl -s -X POST "$DATAHUB_URL" \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$query\"}" | jq .
}

# 示例：查询所有数据集
echo "查询所有数据集..."
query_datahub '{ search(input: {type: DATASET, query: "*", start: 0, count: 10}) { total searchResults { entity { ... on Dataset { name urn } } } } }'

# 示例：查询数据血缘
echo "查询数据血缘..."
query_datahub 'query { dataset(urn: "urn:li:dataset:(urn:li:dataPlatform:mysql,db.table,PROD)") { name upstream: relationships(input: { types: ["DownstreamOf"], direction: INCOMING, start: 0, count: 10 }) { total relationships { entity { ... on Dataset { name } } } } } }'
```

---

**更新记录**：
- 2025-12-29：全面优化文档结构，新增数据血缘、数据指标查询，整合GraphiQL指南
- 原始内容保留在备份文件：`DataHub之元数据查询_backup.md`

**参考资料**：
- [DataHub官方文档](https://datahubproject.io/docs/)
- [GraphQL官方文档](https://graphql.org/learn/)
- [DataHub GitHub仓库](https://github.com/datahub-project/datahub)
