# SeaTunnel Web 项目核心功能分析

## 一、项目概述

### 1.1 项目基本信息

| 项目属性 | 详情 |
|---------|------|
| **项目名称** | SeaTunnel Web |
| **项目地址** | https://github.com/weifuwan/seatunnel-web |
| **创建时间** | 2026年2月10日 |
| **项目状态** | 活跃开发中（暂无正式版本发布） |
| **项目描述** | 面向 Apache SeaTunnel 的现代化 Web 管理平台 |

### 1.2 项目定位

SeaTunnel Web 是 Apache SeaTunnel 的 Web 可视化管理平台，用于简化数据集成任务的创建、配置和运维管理。通过 DAG 可视化编排方式构建数据流水线，支持连接器配置、任务管理以及运行监控，使数据工程师可以更加高效地管理批处理与流处理的数据集成任务，而无需手动编写复杂的配置文件。

---

## 二、核心功能模块

### 2.1 可视化 Pipeline 构建（DAG 编排）

#### 功能描述
通过图形化界面设计数据集成流程，支持拖拽式操作。

#### 核心组件
| 组件名称 | 功能说明 |
|---------|---------|
| `DagGraph` | DAG 图数据结构定义 |
| `DagValidator` | DAG 验证器接口 |
| `CycleDetectionValidator` | 环路检测验证器 |
| `GraphConnectivityValidator` | 图连通性验证器 |
| `IsolatedNodeValidator` | 孤立节点检测验证器 |
| `TransformSingleConnectionValidator` | 单连接转换验证器 |
| `WholeSyncDagAssembler` | 批处理 DAG 组装器 |
| `StreamDagAssembler` | 流处理 DAG 组装器 |

#### 验证机制
- **环路检测**：防止任务形成死循环
- **连通性验证**：确保所有节点正确连接
- **孤立节点检测**：识别未连接的无效节点
- **单连接转换**：验证转换节点的连接合法性

---

### 2.2 数据源管理

#### 功能描述
支持多种数据源的统一管理和配置，采用插件化架构便于扩展。

#### 支持的数据源
- MySQL
- Oracle
- PostgreSQL
- 可扩展插件架构

#### 核心组件
| 组件名称 | 功能说明 |
|---------|---------|
| `DataSourceController` | 数据源管理控制器 |
| `DataSourceService` | 数据源业务服务 |
| `DataSourceCatalogController` | 数据源目录管理 |
| `DataSourceCatalogService` | 数据源目录服务 |
| `DatasourcePluginService` | 数据源插件服务 |
| `DataSourcePluginConfigController` | 插件配置管理 |

#### 模块结构
```
seatunnel-datasource-plugins/
├── seatunnel-datasource-api      # 数据源 API 接口
├── seatunnel-datasource-mysql    # MySQL 数据源插件
├── seatunnel-datasource-oracle   # Oracle 数据源插件
├── seatunnel-datasource-pgsql    # PostgreSQL 数据源插件
└── seatunnel-datasource-all      # 所有数据源聚合
```

---

### 2.3 任务管理

#### 功能描述
提供统一的 Web 界面创建、编辑、运行和管理 SeaTunnel 作业。

#### 任务类型
| 任务类型 | 说明 | 核心组件 |
|---------|------|---------|
| **批处理任务** | 离线数据处理 | `SeaTunnelBatchJobDefinitionController`<br>`SeaTunnelBatchJobDefinitionService` |
| **流处理任务** | 实时数据处理 | `SeaTunnelStreamJobDefinitionController`<br>`SeaTunnelStreamJobDefinitionService` |

#### 任务生命周期管理
| 阶段 | 组件 | 功能 |
|------|------|------|
| **定义** | JobDefinition | 创建和编辑任务配置 |
| **调度** | `SeaTunnelJobScheduleController`<br>`SeaTunnelJobScheduleService` | 配置任务调度策略 |
| **执行** | `SeaTunnelJobExecutorController`<br>`SeaTunnelJobExecutorService` | 执行任务 |
| **实例** | `SeaTunnelJobInstanceService` | 管理任务运行实例 |

---

### 2.4 监控与指标

#### 功能描述
实时监控任务执行状态，采集和展示任务运行指标。

#### 核心组件
| 组件名称 | 功能说明 |
|---------|---------|
| `SeaTunnelJobMetricsController` | 指标数据接口 |
| `SeaTunnelJobMetricsService` | 指标采集服务 |
| `SeaTunnelJobInstanceService` | 实例状态管理 |

#### 监控能力
- 任务执行状态实时跟踪
- 任务运行指标采集
- 历史执行记录查询
- 任务执行日志查看

---

### 2.5 AI 辅助功能（SeaTunnel Copilot）

#### 功能描述
集成 AI 能力，提供智能辅助配置和优化建议。

#### 核心组件
| 组件名称 | 功能说明 |
|---------|---------|
| `SeaTunnelAIController` | AI 功能接口 |
| `SeaTunnelAiService` | AI 服务实现 |
| `seatunnel-copilot` | AI 辅助独立模块 |

#### 技术栈
- Spring AI 1.1.0
- Spring AI Alibaba 1.1.0.0
- Spring AI Alibaba Extensions 1.1.2.0

---

### 2.6 用户与权限管理

#### 功能描述
提供用户认证、授权和会话管理功能。

#### 核心组件
| 组件名称 | 功能说明 |
|---------|---------|
| `SeaTunnelLoginController` | 登录认证 |
| `UsersController` | 用户管理 |
| `UsersService` | 用户服务 |
| `SessionService` | 会话管理 |

#### 安全特性
- 用户登录认证
- 会话状态管理
- 安全访问控制

---

## 三、技术架构

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端层 (seatunnel-ui)               │
│         React 18 + Ant Design Pro + ECharts             │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    后端服务层 (seatunnel-admin)          │
│              Spring Boot 3.3 + Java 21                  │
│  ┌──────────┬──────────┬──────────┬──────────┐         │
│  │Controller│ Service  │   DAG    │  Quartz  │         │
│  │   层     │   层     │  编排层  │  调度层  │         │
│  └──────────┴──────────┴──────────┴──────────┘         │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ 数据源插件层  │   │  AI 辅助层   │   │   公共模块   │
│ (datasource  │   │ (copilot)    │   │ (communal)   │
│  -plugins)   │   │              │   │              │
└──────────────┘   └──────────────┘   └──────────────┘
```

### 3.2 技术栈详情

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| **前端框架** | React | 18.3.0 |
| **UI 组件库** | Ant Design | 5.25.4 |
| **前端脚手架** | Ant Design Pro | 6.0.0 |
| **AI 组件** | Ant Design X | 2.2.2 |
| **图表库** | ECharts | 5.6.0 |
| **拖拽库** | dnd-kit | 0.0.2 |
| **后端框架** | Spring Boot | 3.3.13 |
| **Java 版本** | JDK | 21 |
| **任务调度** | Quartz | - |
| **实时通信** | WebSocket + STOMP | 7.1.0 |
| **AI 框架** | Spring AI | 1.1.0 |
| **AI 扩展** | Spring AI Alibaba | 1.1.0.0 |

### 3.3 前端技术栈

#### 核心依赖
```json
{
  "react": "^18.3.0",
  "antd": "^5.25.4",
  "@ant-design/pro-components": "^2.7.19",
  "@ant-design/x": "^2.2.2",
  "echarts": "^5.6.0",
  "dnd-kit": "^0.0.2",
  "@stomp/stompjs": "^7.1.0",
  "dayjs": "^1.11.13"
}
```

#### 前端目录结构
```
seatunnel-ui/src/
├── assets/          # 静态资源
├── components/      # 公共组件
├── locales/         # 国际化
├── pages/           # 页面组件
├── services/        # API 服务
├── utils/           # 工具函数
├── app.tsx          # 应用入口
└── access.ts        # 权限配置
```

---

## 四、项目模块划分

### 4.1 模块结构

```
seatunnel-web/
├── seatunnel-admin/              # 后端管理服务
│   └── src/main/java/org/apache/seatunnel/admin/
│       ├── controller/           # 控制器层
│       ├── service/              # 服务层
│       ├── dao/                  # 数据访问层
│       ├── dag/                  # DAG 编排模块
│       ├── config/               # 配置模块
│       ├── security/             # 安全模块
│       ├── quartz/               # 调度模块
│       ├── websocket/            # WebSocket 模块
│       └── utils/                # 工具类
│
├── seatunnel-ui/                 # 前端界面
│   ├── src/                      # 源代码
│   ├── public/                   # 静态资源
│   ├── config/                   # 配置文件
│   └── mock/                     # Mock 数据
│
├── seatunnel-copilot/            # AI 辅助模块
│   └── src/                      # 源代码
│
├── seatunnel-communal/           # 公共模块
│
├── seatunnel-datasource-connection/  # 数据源连接
│
├── seatunnel-datasource-plugins/     # 数据源插件
│   ├── seatunnel-datasource-api/
│   ├── seatunnel-datasource-mysql/
│   ├── seatunnel-datasource-oracle/
│   ├── seatunnel-datasource-pgsql/
│   └── seatunnel-datasource-all/
│
├── docs/                         # 文档
├── deploy/                       # 部署配置
├── jdbc-drivers/                 # JDBC 驱动
└── tools/                        # 工具脚本
```

### 4.2 模块职责

| 模块名称 | 职责说明 |
|---------|---------|
| `seatunnel-admin` | 核心业务逻辑，提供 REST API |
| `seatunnel-ui` | 前端界面，用户交互 |
| `seatunnel-copilot` | AI 辅助功能 |
| `seatunnel-communal` | 公共组件和工具 |
| `seatunnel-datasource-connection` | 数据源连接管理 |
| `seatunnel-datasource-plugins` | 数据源插件扩展 |

---

## 五、核心价值

### 5.1 解决的痛点

| 痛点 | 解决方案 |
|------|---------|
| 配置文件编写复杂 | 可视化界面配置，自动生成配置 |
| 任务管理分散 | 统一 Web 平台集中管理 |
| 监控不直观 | 实时监控面板，可视化指标展示 |
| 运维成本高 | 一站式运维管理平台 |
| 学习门槛高 | 拖拽式操作，降低使用门槛 |

### 5.2 核心优势

1. **降低使用门槛**
   - 无需手写复杂配置文件
   - 通过可视化界面完成数据管道构建
   - 拖拽式 DAG 编排

2. **提升开发效率**
   - 快速构建批处理/流处理任务
   - 模板化配置复用
   - 一键部署执行

3. **统一运维管理**
   - 集中管理数据源、任务、调度
   - 实时监控和告警
   - 历史记录追溯

4. **AI 赋能**
   - 集成 AI 辅助功能
   - 智能辅助配置
   - 优化建议

5. **可扩展架构**
   - 插件化数据源支持
   - 易于扩展新的数据源类型
   - 模块化设计

---

## 六、项目活跃度

| 指标 | 数据 |
|------|------|
| Star 数 | 25 |
| Fork 数 | 11 |
| 贡献者数 | 2 |
| 提交数 | 100+ |
| 开放 Issue | 8 |
| 最新提交 | 2026-03-13 |

---

## 七、总结

SeaTunnel Web 是一个面向 Apache SeaTunnel 的可视化运维管理平台，其核心价值在于：

**将 Apache SeaTunnel 的命令行配置方式转变为可视化 Web 操作**，通过 DAG 编排、数据源管理、任务调度、监控告警等功能，为数据工程师提供一站式的数据集成解决方案。

项目采用现代化的技术栈（Spring Boot 3 + React 18），架构清晰，模块化设计良好，具备良好的可扩展性。同时集成了 AI 辅助功能，体现了对智能化运维的探索。

---

*文档生成时间：2026年3月13日*
