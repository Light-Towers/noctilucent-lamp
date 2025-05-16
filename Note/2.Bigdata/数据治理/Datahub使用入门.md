# 背景
为什么要做数据治理？ 
* 广义：数据治理要解决数据质量，数据管理，数据资产，数据安全等问题，`而数据治理的关键就在于元数据管理`，我们要知道数据的来龙去脉，才能对数据进行全方位的管理，监控，洞察。

* 狭义：
    1. 业务繁多，数据繁多，业务数据不断迭代。人员流动，文档不全，逻辑不清楚，对于数据很难直观理解，后期很难维护。
    2. 在大数据研发中，原始数据就有着非常多的数据库，数据表。而经过数据的聚合以后，又会有很多新的维度表。
    3. 数据分析团队需要正确的数据用于分析。虽然构建了高度可扩展的数据存储，实时计算等等能力，但是团队仍然在浪费时间寻找合适的数据集来进行分析。

# 元数据
元数据(meta data)——“data about data” **关于数据的数据**，是指从信息资源中抽取出来的用于说明其特征、内容的结构化的数据，用于**组织、描述、检索、保存、管理信息和知识资源**。
例如：存储在数据库中用于规定描述表信息，字段的长度、类型，索引信息等。

## 元数据管理示例

以前，数据资产可能是 Oracle 数据库中的一张表。然而，在现代企业中，我们拥有一系列令人眼花缭乱的不同类型的数据资产。可能是关系数据库或 NoSQL 存储中的表、实时流数据、 AI 系统中的功能、指标平台中的指标，数据可视化工具中的仪表板。现代元数据管理应包含所有这些类型的数据资产，并使数据工作者能够更高效地使用这些资产完成工作。

一些常见的用例和它们需要的元数据类型的示例：
* 搜索和发现：数据表、字段、标签、使用信息

* 访问控制：访问控制组、用户、策略

* 数据血缘：管道执行、查询

* 合规性：数据隐私/合规性注释类型的分类

* 数据管理：数据源配置、摄取配置、保留配置、数据清除、导出策略

* AI可解释性、可重现性：特征定义、模型定义、训练运行执行、问题陈述

* 数据操作：管道执行、处理的数据分区、数据统计

* 数据质量：数据质量规则定义、规则执行结果、数据统计



# 主流元数据工具对比


| 数据源名称                                                   | 优点                                                         | 缺点                                                         | 适用场景                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| [LinkedIn DataHub](https://github.com/linkedin/datahub)      | - **实时流架构**：支持 Kafka 实时同步元数据，延迟低<br>- **动态 Schema**：灵活扩展元模型，支持自定义注解<br>- **细粒度权限控制**：支持列级和数据集级 RBAC<br>- **社区活跃**：CNCF 孵化项目，版本迭代快，连接器丰富 | - **部署复杂**：依赖 Kafka 和多存储层，运维成本高<br>- **中文支持弱**：界面和文档本地化不足<br> | 现代数据栈（如 Snowflake、Kafka）的元数据管理；需实时元数据同步和跨团队协作的场景 |
| [Apache Atlas](https://github.com/apache/atlas)              | - **深度集成 Hadoop**：原生支持 Hive、HBase 等，血缘追踪完善<br>- **安全管控**：与 Apache Ranger 集成，支持行列级权限<br>- **成熟稳定**：经过大型企业验证（如阿里） | - **生态局限**：对非 Hadoop 组件支持差<br>- **界面复杂**：用户体验老旧，学习成本高<br>- **性能损耗**：元数据采集可能影响系统性能 | Hadoop 生态下的数据治理；需强安全管控的场景（如金融、医疗）  |
| [Lyft Amundsen](https://github.com/amundsen-io/amundsen)     | - **轻量易用**：部署简单，支持快速数据发现<br>- **多后端支持**：兼容 Neo4j、AWS Neptune 等<br>- **数据预览**：直接展示数据样本，提升上下文理解 | - **治理功能弱**：缺乏细粒度权限和自动化合规工具<br>- **血缘支持有限**：仅支持表级血缘，列级功能待完善<br>- **社区较小**：中文资料少 | 中小型企业快速搭建数据目录；需简化数据搜索和协作的场景       |
| [OpenMetadata](https://github.com/open-metadata/OpenMetadata) | - **统一元数据模型**：标准化 JSON Schema，支持跨系统元数据整合<br>- **数据质量集成**：内置 Great Expectations，支持无代码质量测试<br>- **扩展性强**：支持 80+ 连接器，覆盖主流数据源<br>- **协作友好**：提供评论、任务管理等协作功能 | - **架构限制**：依赖 MySQL 和 ES，复杂关系处理能力不足<br>- **技术要求高**：需 Java/Python 开发能力<br />- **血缘受限**：无专用图数据库，复杂血缘处理受限 | 需要统一元模型和多工具集成的场景（如混合云环境）；注重数据质量和协作的中大型企业 |

 

# DataHub-快速开始

## 环境准备
* 2 CPUs, 8GB RAM, 2GB Swap area, and 10GB disk space
* **Docker** & **Docker Compose** v2
* **Python 3.8+** & pip（sudo easy_install pip）

```bash
# 如果是新系统，一些系统底包必须要安装
sudo yum groupinstall -y 'Development Tools'
sudo yum install -y gcc gcc-c++ kernel-devel librdkafka-dev python-devel.x86_64 cyrus-sasl-devel.x86_64 python3-ldap libldap2-dev libsasl2-dev libsasl2-modules ldap-utils libxslt-devel libffi-devel openssl-devel python-devel python3-devel

# 安装docker（使用的是AWS的Linux2）
sudo amazon-linux-extras install docker
# docker服务，启动｜状态｜停止
sudo service docker start|status|stop
# 给当前用户加入docker用户组
sudo usermod -aG docker ${USER}
# 修改docker默认配置
sudo vim /etc/docker/daemon.json
{
  "registry-mirrors": [
    "https://docker.1ms.run/",
    "https://docker.anyhub.us.kg/",
    "https://registry.docker-cn.com",
    "http://hub-mirror.c.163.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "data-root": "/data/docker"
}

# 安装docker-compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## 快速上手
1. 安装 DataHub CLI
```bash
python3 -m pip install --upgrade pip wheel setuptools
# sanity check - ok if it fails
python3 -m pip uninstall datahub acryl-datahub || true 
python3 -m pip install --upgrade acryl-datahub
python3 -m datahub version
```
2. 部署 DataHub
```bash
python3 -m datahub docker quickstart
```
这将使用 docker-compose 部署一个 DataHub 实例。docker-compose.yaml 文件下载到用户主目录下的 .datahub/quickstart 下。

```bash
Fetching docker-compose file https://raw.githubusercontent.com/datahub-project/datahub/master/docker/quickstart/docker-compose-without-neo4j-m1.quickstart.yml from GitHub
Pulling docker images...
Finished pulling docker images!

[+] Running 11/11
.......
✔ DataHub is now running
Ingest some demo data using `datahub docker ingest-sample-data`,
or head to http://localhost:9002 (username: datahub, password: datahub) to play around with the frontend.
Need support? Get in touch on Slack: https://datahub.com/slack/
```

至此，DataHub搭建完成，登录DataHub UI：`http://localhost:9002`, username & password: `datahub`



# 特性

1. **Assertions（断言）**
   用于定义和监控数据质量规则，包括表、字段、模式的质量检查（如新鲜度、数据量、SQL自定义断言等），支持与第三方工具（如 DBT、Great Expectations）集成。
2. **Access Management（访问管理）**
   支持将外部系统的角色与 DataHub 数据资产关联，实现统一的访问控制视图，便于数据消费者发现和申请访问权限。
3. **Automations（自动化）**
   提供元数据管理自动化能力，如文档传播、术语传播、标签同步等，提升治理效率和一致性。
4. **Business Attributes（业务属性）**
   允许定义具有业务含义的属性，并可在多个数据实体间复用，实现标准化的业务元数据管理。[Beta]
5. **Business Glossary（业务术语表）**
   支持构建和管理企业级的业务术语词汇表，便于统一数据语言和理解。
6. **Compliance Forms（合规表单）**
   通过表单方式批量收集、补充和验证关键元数据，推动合规治理落地。
7. **Data Contract（数据契约）**
   明确数据生产者与消费者之间的数据质量和结构约定，结合断言实现可验证的数据契约。
8. **Data Products（数据产品）**
   支持以产品化方式管理和发布数据资产。
9. **Dataset Usage and Query History（数据集使用与查询历史）**
   展示数据集被访问和查询的历史，帮助理解数据流动和使用情况。
10. **Domains（领域）**
    支持将数据资产分组到逻辑领域（如部门、业务线），便于分布式治理。
11. **Incidents（事件/告警）**
    提供数据质量或元数据变更的事件通知和追踪能力。[DataHub Cloud]
12. **Ingestion（元数据采集）**
    介绍如何从各类数据源采集元数据到 DataHub。
13. **Lineage（血缘）**
    展示和管理数据资产之间的上下游依赖关系。
14. **Metadata Tests（元数据测试）**
    提供无代码配置的元数据监控与自动化操作。[DataHub Cloud]
15. **Ownership（所有权）**
    管理数据资产的负责人和责任人。
16. **Policies（策略）**
    配合角色实现细粒度的权限控制。
17. **Posts（公告/动态）**
    支持在平台内发布公告或动态。
18. **Properties（属性）**
    包括自定义属性和结构化属性，用于扩展和补充元数据。
19. **Schema history（模式历史）**
    跟踪数据集的结构变更历史。
20. **Search（搜索）**
    介绍如何通过搜索发现各类数据资产。
21. **Sync Status（同步状态）**
    展示元数据同步的最新状态。
22. **Tags（标签）**
    用于对数据资产进行灵活的标签分类和检索。




## 元数据摄取

> 官网文档： https://docs.datahub.com/docs/metadata-ingestion

1. 选择数据源

   ![datahub_ingest-datasource](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/datahub_ingest-datasource.png)

1. 参数配置

   ![datahub_ingest-config](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/datahub_ingest-config.png)

   yaml配置如下：
    ```yaml
    # A sample recipe that pulls metadata from Mysql and puts it into DataHub
    # using the Rest API.
    source:
      type: mysql
      config:
        username: sa
        password: ${MSSQL_PASSWORD}
        database: DemoData
   
    transformers:
      - type: "fully-qualified-class-name-of-transformer"
        config:
          some_property: "some.value"
   
    sink:
      type: "datahub-rest"
      config:
        server: "https://datahub-gms:8080"
    ```

    * Hive 配置
    ```yaml
    source:
        type: hive
        config:
            database: null
            username: datahub		# 不需要配置密码
            stateful_ingestion:
                enabled: true
            host_port: '192.168.100.xx:10000'
            profiling:
                enabled: false
                profile_table_level_only: true
    
    sink:
        type: datahub-rest
        config:
            server: 'http://datahub-gms:8080'
    ```

3. 使用corn表达式指定脚本摄取时间

   ![datahub_ingest-corn](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/datahub_ingest-corn.png)

4. 配置完成信息

   ![datahub_ingest-finish](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/datahub_ingest-finish.png)

## 数据血缘

支持时间范围与字段级的数据血缘追踪

![datahub-lineage](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/datahub-lineage.png)

## CLI 命令使用

```bash
# 通过CLI摄取元数据
## 安装所需插件
python3 -m pip install 'acryl-datahub[datahub-rest]'
python3 -m pip install 'acryl-datahub[mysql]'
python3 -m datahub ingest -c ./examples/recipes/mysql_to_datahub.yml

# 启动/更新
python3 -m datahub docker quickstart
## 自定义安装：docker-compose.yaml 下载地址 https://raw.githubusercontent.com/datahub-project/datahub/master/docker/quickstart/docker-compose-without-neo4j-m1.quickstart.yml
python3 -m datahub docker quickstart --quickstart-compose-file docker-compose.yaml

## 停止
python3 -m datahub docker quickstart --stop

## 重置
python3 -m datahub docker nuke

## 备份
python3 -m datahub docker quickstart --backup   # 默认存放路径  ~/.datahub/quickstart/backup.sql, # 指定备份文件 --backup-file <path to backup file>

## 恢复
datahub docker quickstart --restore  # default  # 指定从备份文件恢复  --restore-file /home/my_user/datahub_backups/quickstart_backup_2002_22_01.sql
datahub docker quickstart --restore-indices  # 只恢复索引
datahub docker quickstart --restore --no-restore-indices	# 只恢复主数据状态
```



# 参考

[DataHub官网](https://datahubproject.io/docs/)
[一站式元数据治理平台——Datahub入门宝典](https://www.cnblogs.com/tree1123/p/15743253.html)
[DataHub：流行的元数据架构介绍](https://engineering.linkedin.com/blog/2020/datahub-popular-metadata-architectures-explained?spm=a2c6h.12873639.0.0.68682190lPaUkw)