# Dify 使用指南：聚焦工作流的最佳实践

---

## 1.引言
本文档旨在帮助公司开发人员快速掌握 Dify 平台的使用方法，特别是工作流的设计与实现技巧。通过本指南，您将了解大模型的基本原理，以及如何利用 Dify 平台构建高效、智能的应用。

---

## 2.大模型基础知识

### 2.1 什么是大模型
大模型（Large Language Model, LLM）是指具有海量参数和复杂计算结构的超大型机器学习模型，通常基于深度神经网络构建，参数规模可达数十亿至数千亿级别。这类模型通过训练海量数据，能够学习复杂的模式和特征，展现出强大的泛化能力、多任务处理能力，以及突破传统小模型局限的“涌现能力”。

#### 2.1.1 参数与规模
大模型的核心特征在于其参数规模庞大（如GPT-3包含1750亿参数），模型大小可达数百GB甚至更大。这种规模赋予其更强的表达能力和学习能力，能够处理自然语言处理（NLP）、计算机视觉（CV）、多模态数据等复杂任务

#### 2.1.2 涌现能力
当模型的训练数据与参数突破临界规模后，会突然展现出小模型不具备的复杂能力，例如逻辑推理、创造性生成、跨领域知识融合等，这种特性被称为 **涌现能力（Emergent Ability）**。例如，GPT-3在零样本学习任务中表现出色，无需微调即可完成翻译或问答

#### 2.1.3 泛化与迁移学习
大模型通过预训练（Pretraining）学习通用知识，再通过微调（Fine-tuning）适配特定任务，显著提高新任务的性能。

### 2.2 向量表示与嵌入
在大模型中，文本内容会被转换为向量（即嵌入，Embeddings）形式。向量是多维空间中的点，通过数学方式表示文字的语义信息。

![向量表示示意图](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/94e70-2020-02-13-2weidu-1.webp)

#### 2.2.1 向量表示的特点：
- 语义相似的文本，在向量空间中距离较近
- 向量运算可以捕捉语义关系（如"国王-男人+女人=王后"）
- 向量维度通常很高（如OpenAI的text-embedding-ada-002为1536维）
- 向量是实现文本检索、相似度计算、知识库问答的基础。

### 2.3 检索增强生成(RAG)

RAG（Retrieval-Augmented Generation，检索增强生成）是一种结合信息检索技术与大语言模型（LLM）的AI框架，旨在通过引入外部知识库增强生成内容的准确性和时效性。

![img](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/v2-c18bbf79b4f59411735255db5a90fbf6_1440w.jpg)

#### 2.3.1 核心定义
- **技术融合**：RAG将传统信息检索系统与生成式大语言模型结合，通过检索外部知识库中的相关内容，为LLM提供补充信息，从而减少模型“幻觉”（生成错误内容）和知识局限性问题
- **目标**：解决LLM因训练数据过时或非公开导致的回答偏差，提升在医疗、法律、金融等垂直领域的专业性。

#### 2.3.2 核心流程
RAG通过以下三步实现知识增强的生成：
1. **检索（Retrieval）**  
   - 从向量化知识库中检索与用户查询相关的信息片段（chunks）。 
   - 常用技术：文本嵌入模型（如BERT、GLM）将文本转为向量，并通过相似度计算（如余弦相似度）匹配最相关内容。
2. **增强（Augmentation）**  
   - 将检索到的信息整合到用户查询的上下文中，形成增强后的输入提示（Prompt）。  
3. **生成（Generation）**  
   - LLM基于增强后的Prompt生成最终回答，确保内容既符合通用语言逻辑，又融入领域专业知识。

#### 2.3.3 解决的问题
- **知识局限性**：LLM仅依赖训练数据，无法获取实时或私有数据，而RAG通过动态检索外部知识库弥补这一缺陷。
- **幻觉问题**：LLM可能生成看似合理但实际错误的内容，RAG通过引入权威数据降低错误概率。
- **数据安全**：企业无需上传私域数据训练模型，可直接通过本地知识库增强生成结果，保障数据隐私。

### 2.4 推理机制
大模型的推理是指模型根据输入内容生成输出的过程。主要推理方式包括：
- 自回归生成：模型依次预测下一个单词，每次预测都基于所有已生成的内容
- 提示词工程：通过精心设计的提示来引导模型生成期望的输出
- 思维链（Chain-of-Thought）：引导模型一步步思考，提高复杂推理能力

---

## 3.Dify 平台介绍

### 3.1 什么是 Dify

Dify 是一个开源的大语言模型(LLM)应用开发平台，它提供了一站式的解决方案，帮助开发者快速构建、部署和监控生产级的生成式 AI 应用。

> Dify 一词源自 Define + Modify，意指定义并且持续的改进你的 AI 应用，它是为你而做的（Do it for you）。

### 3.2 Dify 核心功能
- 应用构建：提供对话式和文本生成两种应用模式
- 提示词管理：可视化的提示词编辑和版本控制
- 数据集和知识库：支持文档导入和向量检索
- 工作流编排：可视化工作流设计，无需编码
- 可观测性：详细的使用分析和日志记录
- API 访问：RESTful API和SDK支持多语言集成

### 3.3 应用场景
* 个性化内容生成：生成博客文章、产品描述、营销材料等。提供大纲或主题，LLM 将生成高质量、结构化的内容。
* 知识库问答系统：集成到客户服务系统中，自动回答常见问题，减轻支持团队负担。LLM 理解上下文和意图，生成实时、准确的回答。
* 数据分析助手：分析大型知识库，生成报告或摘要。识别趋势、模式和洞察力，将原始数据转化为可操作的智能。
* 代码辅助工具：分析提交的代码，检查编码规范，识别潜在问题，提供改进建议。
* 教育培训系统：创建教育内容、课程大纲、学习材料等。提供个性化学习建议和反馈。
* 智能客服和聊天机器人：自动化处理客户查询，提供即时响应。支持多轮对话，理解复杂问题并提供解决方案。
* 任务自动化：与任务管理系统（如 Trello、Slack、Lark）集成，自动化项目和任务管理。通过自然语言处理，创建任务、更新状态、分配优先级。
* 邮件自动化处理：起草电子邮件、社交媒体更新和其他形式的沟通。提供简要大纲或关键要点，生成结构良好、连贯且与上下文相关的信息。

---

## 4.Dify 基础使用

本次介绍的功能基于 Dify `社区版 v1.1.3` 版本。

### 4.1 核心功能介绍

#### 4.1.1 模型供应商

路径：点击右上角头像->设置->模型供应商。 

功能：配置模型、API 密钥等

![image-20250307141415422](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307141415422.png)

#### 4.1.2 工作室

功能：查看和管理已创建的应用

 ![image-20250302123251818](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250302123251818.png)

聊天助手：对话型应用，采用一问一答模式与用户持续对话。支持切换为 Chatflow 编排。

Agent：自主对复杂的人类任务进行目标规划、任务拆解、工具调用、过程迭代，并在没有人类干预的情况下完成任务。

工作流：允许开发者以可视化、低代码方式定义 AI 应用的处理逻辑。工作流分为两种类型：

- **Chatflow**：面向对话类情景，包括客户服务、语义搜索、以及其他需要在构建响应时进行多步逻辑的对话式应用程序。
- **Workflow**：面向自动化和批处理情景，适合高质量翻译、数据分析、内容生成、电子邮件自动化等应用程序。

#### 4.1.3 知识库：管理知识库和数据集

* **知识库列表**

![image-20250302140619395](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250302140619395.png)

* **创建知识库**

  ![image-20250302141115914](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250302141115914.png)

* **文本分段与清洗处理**
  ![image-20250302142002377](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250302142002377.png)

    **1.分段设置**：一般使用默认分段配置，如果分段太细，可修改分段标识符、或增大分段最大长度（<=4000）。Q&A 分段：每一个分段生成 Q&A 匹配对，当用户提问时，系统会找出与之最相似的问题，然后返回对应的分段作为答案。(消耗额外的 token）

    **2.索引方式**：推荐选择内嵌模型，如：`bge-m3:latest` ，会将文档内容编码为向量存入数据库，实现精准检索。

    **3.检索设置**：推荐使用**混合检索模式**，并开启 Rerank 模型对检索内容重新语义排序。

* **知识库召回测试**

  ![image-20250307143713104](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307143713104.png)

#### 4.1.4 工具：

工具可以扩展 LLM 的能力，比如联网搜索、科学计算或绘制图片，赋予并增强了 LLM 连接外部世界的能力。Dify 提供了两种工具类型：**第一方工具** 和 **自定义工具**。

![image-20250307110340708](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307110340708.png)

---

## 5.Dify 工作流详解

### 5.1 工作流概念
工作流通过将复杂的任务分解成较小的步骤（节点）降低系统复杂度，减少了对提示词技术和模型推理能力的依赖，提高了 LLM 应用面向复杂任务的性能，提升了系统的可解释性、稳定性和容错性。

![image-20250302202506651](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250302202506651.png)

### 5.2 工作流的核心

**节点是工作流中的关键构成**，通过连接不同功能的节点，执行工作流的一系列操作。

#### 5.2.1 变量

变量作为一种动态数据容器，能够存储和传递不固定的内容，在不同的节点内被相互引用，实现信息在节点间的灵活通信。

1. **系统变量**：是在 Chatflow / Workflow 应用内预设的系统级参数，可以被其它节点全局读取。系统级变量均以 `sys` 开头。

2. **环境变量**：用于**保护工作流内所涉及的敏感信息**，例如运行工作流时所涉及的 API 密钥、数据库密码等。它们被存储在工作流程中，而不是代码中，以便在不同环境中共享。

3. **会话变量**：

> 会话变量面向多轮对话场景，而 Workflow 类型应用的交互是线性而独立的，不存在多次对话交互的情况，因此会话变量仅适用于 Chatflow 类型（聊天助手 → 工作流编排）应用。

**会话变量允许应用开发者在同一个 Chatflow 会话内，指定需要被临时存储的特定信息，并确保在当前工作流内的多轮对话内都能够引用该信息**，如上下文、上传至对话框的文件（即将上线）、 用户在对话过程中所输入的偏好信息等。好比为 LLM 提供一个可以被随时查看的“备忘录”，避免因 LLM 记忆出错而导致的信息偏差。

#### 5.2.2 节点

[**开始（Start）**](https://docs.dify.ai/zh-hans/guides/workflow/node/start)：定义一个 workflow 流程启动的初始参数。

[**结束（End）**](https://docs.dify.ai/zh-hans/guides/workflow/node/end)：定义一个 workflow 流程结束的最终输出内容。

[**回复（Answer）**](https://docs.dify.ai/zh-hans/guides/workflow/node/answer)：定义一个 Chatflow 流程中的回复内容。

[**大语言模型（LLM）**](https://docs.dify.ai/zh-hans/guides/workflow/node/llm)：调用大语言模型回答问题或者对自然语言进行处理。

[**知识检索（Knowledge Retrieval）**](https://docs.dify.ai/zh-hans/guides/workflow/node/knowledge-retrieval)：从知识库中检索与用户问题相关的文本内容，可作为下游 LLM 节点的上下文。

[**问题分类（Question Classifier）**](https://docs.dify.ai/zh-hans/guides/workflow/node/question-classifier)：通过定义分类描述，LLM 能够根据用户输入选择与之相匹配的分类。

[**条件分支（IF/ELSE）**](https://docs.dify.ai/zh-hans/guides/workflow/node/ifelse)：允许你根据 if/else 条件将 workflow 拆分成两个分支。

[**代码执行（Code）**](https://docs.dify.ai/zh-hans/guides/workflow/node/code)：运行 Python / NodeJS 代码以在工作流程中执行数据转换等自定义逻辑。

[**模板转换（Template）**](https://docs.dify.ai/zh-hans/guides/workflow/node/template)：允许借助 Jinja2 的 Python 模板语言灵活地进行数据转换、文本处理等。

[**变量聚合（Variable Aggregator）**](https://docs.dify.ai/zh-hans/guides/workflow/node/variable-aggregator)：将多路分支的变量聚合为一个变量，以实现下游节点统一配置。

[**参数提取器（Parameter Extractor）**](https://docs.dify.ai/zh-hans/guides/workflow/node/parameter-extractor)：利用 LLM 从自然语言推理并提取结构化参数，用于后置的工具调用或 HTTP 请求。

[**迭代（Iteration）**](https://docs.dify.ai/zh-hans/guides/workflow/node/iteration)：对列表对象执行多次步骤直至输出所有结果。

[**HTTP 请求（HTTP Request）**](https://docs.dify.ai/zh-hans/guides/workflow/node/http-request)：允许通过 HTTP 协议发送服务器请求，适用于获取外部检索结果、webhook、生成图片等情景。

[**工具（Tools）**](https://docs.dify.ai/zh-hans/guides/workflow/node/tools)：允许在工作流内调用 **Dify 内置工具、自定义工具、子工作流**等。

[**变量赋值（Variable Assigner）**](https://docs.dify.ai/zh-hans/guides/workflow/node/variable-assigner)：变量赋值节点用于向可写入变量（例如会话变量）进行变量赋值。


#### 5.2.3 模型选择

基于业务功能，选择配置好的合适大小的模型。
![image-20250307152148296](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307152148296.png)

根据需要调整模型参数。
<img src="https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307152317469.png" alt="image-20250307152317469" style="zoom: 67%;" />


**核心参数** 
 - 1️⃣ 温度（Temperature） 
   - **作用**：控制文本的创造性   
   - **当前值分析**：您当前设置为中高值（示例值如0.7）   
     -  ✅ **调高**（>1.0）：生成内容更发散，适合创意写作    
     -  ✅ **调低**（<0.5）：生成更保守，适合技术文档   
 - 2️⃣ Top-P
    - **作用**：控制候选词范围   
    - **当前值分析**：若设置为0.9   
      -  ✅ 模型仅从概率累加前90%的词中选择，平衡多样性与合理性   
 - 3️⃣ Top-K
   - **作用**：限制候选词数量   
   - **当前值分析**：若设置为50    
     - ✅ 每次仅从概率最高的50个词中选择，避免冷门词干扰
   

**辅助参数** 
 - 4️⃣ 重复惩罚（Repeat Penalty） 
   - **推荐场景**：需生成长文本时建议开启（如设置1.2-1.5）   
 - 5️⃣ 上下文窗口（Context Window）
   - **硬件关联**：2048 tokens约需8GB显存，与GPU层数参数联动   
 - 6️⃣ GPU层数（当前1层）
   - **性能提示**：增加层数可加速生成，但需更高显存

 | 任务类型       | 推荐参数组合                     |   
 |----------------|----------------------------------|   
 | 故事创作       | 温度↑（1.2）+ Top-P↑（0.95）    |   
 | 客服问答       | 温度↓（0.3）+ 重复惩罚↑（1.5）  |   
 | 代码生成       | Top-K↓（20）+ 温度↓（0.4）      |   


### 5.3 工作流高级技巧

#### 5.3.1 使用变量和上下文
工作流中的变量允许在节点间传递数据：
- 全局变量：在整个工作流中可用
- 节点输出：每个节点的输出可作为后续节点的输入
- 上下文管理：维护对话历史和状态

![节点的变量引用](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307115240895.png)

<img name="节点的输入变量和输出变量" src="https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307115501610.png" alt="image-20250307115501610" style="zoom: 67%;"  />


#### 5.3.2 工具调用集成
Dify支持在工作流中集成各种工具：
- 内置工具：
  - 网页抓取
  - 数学计算
  - 天气查询
  - 日期时间处理
  
- 自定义工具：
  - 通过API连接企业内部系统
  
  - 集成第三方服务
  
  - 实现特定业务逻辑
  

**插件市场**

![image-20250307181620354](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307181620354.png)

**工作流中调用示例：**

![image-20250307131353930](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307131353930.png)

#### 5.3.3 知识库结合工作流

将知识库与工作流结合，能够大大增强AI应用的专业性和准确性： 知识库与工作流结合（图）

最佳实践：

* 先使用LLM分析用户问题的核心需求

* 构造精准的知识库查询

* 结合检索结果和原始问题生成回答

  ![image-20250307131810409](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307131810409.png)

---

## 6. 工作流最佳实践与故障排除

### 6.1 模块化设计
#### 6.1.1 核心原则
* 将复杂流程拆分为功能明确的子模块
* 通过接口定义规范数据交互
* 建立版本控制机制

#### 6.1.2 常见问题：工作流执行异常
► **典型表现**  
- 节点配置参数错误
- 变量引用缺失或语法错误
- 条件分支逻辑冲突

► **解决方案**  
1. 使用可视化流程图校验节点连接  
2. 开启【调试模式】逐步执行验证  
3. 检查变量作用域和命名规范（建议采用`<模块前缀>_<描述>`格式）  
4. 查看执行轨迹日志定位异常节点
   <img src="https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/dify/image-20250307143109446.png" alt="image-20250307143109446" style="zoom: 67%;" />

### 6.2 健壮性设计
- **输入校验**：配置正则表达式过滤非法输入（如邮箱格式校验）
- **弹性策略**： 
  ▸ 设置API调用超时阈值（推荐5-15秒） 
  ▸ 配置指数退避重试机制（建议最大3次）  
- **异常通知**：绑定邮件/Webhook告警通道
- **日志规范**：记录完整上下文信息（建议包含session_id、timestamp、error_code）

### 6.3 性能优化
#### 6.3.1 LLM调用优化  
   - 对简单任务启用参数小的模型，对复杂任务启用参数大的模型。  
   - 使用流式响应减少首字节时间  
#### 6.3.2 计算资源优化
   - 对高耗时操作（>2s）启用异步执行  
   - 配置Redis缓存高频查询（TTL建议60-300秒）  
#### 6.3.3 并行化处理
   - 对无状态任务启用分支并行执行
   - 设置合并节点同步处理结果
#### 6.3.4 提示词优化
   - **结构优化**：采用 **[角色定义]-[任务描述]-[输出格式]** 三段式结构
   - **示例引导**：包含至少2个正例和1个反例说明
   - **Token控制**：使用`max_tokens=350`限制避免截断


### 6.4 知识库检索偏差
#### 6.4.1 数据预处理优化
1. **文档分块与摘要增强**  
   - **多向量检索器**：将文档拆分为逻辑独立的长段落，并为每个段落生成摘要，仅对摘要进行向量化存储。检索时先匹配摘要，再返回完整段落作为上下文，兼顾检索效率与信息完整性。  
   - **分块策略**：根据文档类型调整分块大小（如长文本采用1024 token块，社交媒体内容用128 token），结合递归分割和语义分割确保块内一致性。

2. **数据清洗与增强**  
   - **实体解析**：标准化术语（如将“LLM”统一为“大语言模型”），消除歧义。  
   - **元数据标注**：为文档添加摘要、时间戳、潜在用户问题等元数据，提升检索相关性。

#### 6.4.2 检索策略改进
1. **混合检索与路由机制**  
   - **混合检索**：结合向量检索（语义匹配）与关键词检索（精确匹配），通过RRF算法合并结果，最大化召回率。  
   - **路由机制**：根据问题类型（如时效性、领域性）选择不同索引，例如热点问题优先检索近期数据。

2. **重排序（Rerank）**  
   - 使用rank重排序模型对初始检索结果二次评分，解决“语义相似但内容无关”的问题。例如，检索前10个节点后重排序返回Top 2。
   

## 引用
[1]: https://docs.dify.ai/	"Dify官方文档"
[2]: https://www.promptingguide.ai/	"提示词工程指南"
[3]: http://internal-link-to-templates/	"内部工作流模板库"
[4]: https://easyai.tech/ai-definition/vector/	"向量 | vector"
[5]: https://www.53ai.com/news/dify/2024082864219.html  "如何增强 Dify 的知识库检索能力？"
[6]: https://www.promptingguide.ai/zh/techniques/rag "检索增强生成 (RAG)"
[7]: https://zhuanlan.zhihu.com/p/675509396 "一文读懂：大模型RAG（检索增强生成）含高级方法"
