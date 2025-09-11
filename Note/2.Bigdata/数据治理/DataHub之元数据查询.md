# DataHub 元数据查询

## 一、认证配置（建议设置为环境变量）
```bash
# 设置DataHub服务地址（根据实际环境修改）
export DATAHUB_URL="http://192.168.100.242:9002/api/graphql"

# 设置访问令牌（有效期有限，请及时更新）
export TOKEN="Bearer eyJhbGciOiJIUzI1NiJ9.eyJhY3RvclR5cGUiOiJVU0VSIiwiYWN0b3JJZCI6ImRhdGFodWIiLCJ0eXBlIjoiUEVSU09OQUwiLCJ2ZXJzaW9uIjoiMiIsImp0aSI6IjIzNzkzMWIxLTY0MmYtNDUxNS04MTAwLTMyM2NkOWE0MDY4ZCIsInN1YiI6ImRhdGFodWIiLCJpc3MiOiJkYXRhaHViLW1ldGFkYXRhLXNlcnZpY2UifQ._kaiqSxkuj9R8sRAQ0iHdLZlnV1qhvTTWkz5v68Iw-s"
```

## 二、核心查询操作

### 1. 域名管理
#### 查询子域名列表
```graphql
query listDomains {
  listDomains(input: { 
    start: 0, 
    count: 100, 
    parentDomain: "urn:li:domain:73a053bb-a7b4-4723-a418-3918359839ed"
  }) {
    start
    count
    total
    domains {
      urn
      properties { 
        name 
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
  -d '{ "query": "query listDomains { listDomains(input: {start: 0, count: 100, parentDomain: \"urn:li:domain:73a053bb-a7b4-4723-a418-3918359839ed\"}) {start,count,total,domains {urn,properties{name}}} }"}'
```

### 2. 数据集查询
#### 查询特定域名下的数据集
```graphql
query search {
  search(input: {
    type: DATASET
    query: "*"
    orFilters: [
      {and: { field: "domains", values: ["urn:li:domain:a3a0f5a5-7228-400f-bc51-6b8c715b5be1"] }}
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
        }
      }
    }
  }
}
```

#### 查询特定数据源的表（MySQL示例）
```graphql
query search {
  search(input: {
    type: DATASET,
    query: "app_mall.*",
    start: 0,
    count: 50,
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
        }
      }
    }
  }
}
```

### 3. 数据平台管理
#### 查询所有可接入平台
```graphql
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
          }
        }
      }
    }
  }
}
```

#### 查询已集成的数据源
```graphql
query listIngestionSources {
  listIngestionSources(input: {start: 0, count: 100}) {
    ingestionSources {
      urn
      type
      name
      config
    }
  }
}
```
**执行命令**：
```bash
curl -X POST "$DATAHUB_URL" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ listIngestionSources(input: {start: 0, count: 100}) { ingestionSources { urn type name config } } }"}'
```

## 三、使用说明
1. **令牌管理**：访问令牌有效期有限，建议通过DataHub UI生成新令牌
2. **URN获取**：通过`listDomains`查询获取目标域名的URN
3. **分页参数**：`start`和`count`用于控制分页，`total`返回总记录数
4. **字段过滤**：在`search`查询中可通过`orFilters`精确筛选结果
5. **平台标识**：`platform`字段使用DataHub内部标识（如"mysql"、"snowflake"）
