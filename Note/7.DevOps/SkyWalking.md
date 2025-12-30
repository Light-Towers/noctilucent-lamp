skywalking核心组件

- skywalking-collector:链路数据归集器，数据可以落地ElasticSearch/H2
- skywalking-ui:web可视化平台，用来展示落地的数据
- skywalking-agent:探针，用来收集和发送数据到归集器



agent目录结构说明

- activations/: 当前skywalking正在使用的功能组件。

- config/agent.config: 文件是 SkyWalking Agent 的唯一配置文件。

- plugins/: 目录存储了当前 Agent 生效的插件。

- optional-plugins/: 目录存储了一些可选的插件（这些插件可能会影响整个系统的性能或是有版权问题），如果需要使用这些插件，需将相应 jar 包移动到 plugins 目录下。

- skywalking-agent.jar: 是 Agent 的核心 jar 包，由它负责读取 agent.config 配置文件，加载上述插件 jar 包，运行时收集到 的 Trace 和 Metrics 数据也是由它发送到 OAP 集群的。

```
agent
    ├── activations
    │   ├── apm-toolkit-kafka-activation-8.3.0.jar
    │   ├── ...
    │   └── apm-toolkit-trace-activation-8.3.0.jar
    ├── config # Agent 配置文件
    │   └── agent.config
    ├── logs # 日志文件
    ├── optional-plugins # 可选插件
    │   ├── apm-customize-enhance-plugin-8.3.0.jar
    │   ├── apm-gson-2.x-plugin-8.3.0.jar
    │   └── ... ...
    ├── bootstrap-plugins # jdk插件
    │   ├── apm-jdk-http-plugin-8.3.0.jar
    │   └── apm-jdk-threading-plugin-8.3.0.jar
    ├── plugins # 当前生效插件
    │   ├── apm-activemq-5.x-plugin-8.3.0.jar
    │   ├── apm-armeria-0.84.x-plugin-8.3.0.jar
    │   ├── apm-armeria-0.85.x-plugin-8.3.0.jar
    │   └── ... ...
    ├── optional-reporter-plugins
    │   └── kafka-reporter-plugin-8.3.0.jar
    └── skywalking-agent.jar【应用的jar包】
```



agent 配置覆盖：

配置优先级：探针配置 > JVM配置 > 系统环境变量配置 > agent.config文件默认值

探针配置：

```bash
#格式: -javaagent:agent.jar=[option1]=[value1],[option2]=[value2]
java -javaagent:/path/skywalking-agent.jar=agent.service_name=app-service -jar app.jar
```

JVM配置：

```bash
#格式：-javaagent:agent.jar -D
java -javaagent:/path/skywalking-agent.jar -Dskywalking.agent.service_name=app-service -jar app.jar
java -javaagent:/path/skywalking-agent.jar -DSW_AGENT_NAME=app-service -jar app.jar
```





UI面板

- Global Heatmap：热力图，从全局展示了某段时间请求的热度。
- Global Percent Response：展示了全局请求响应时间的 P99、P95、P75 等分位数。
- Global Brief：展示了 SkyWalking 能感知到的 Service、Endpoint 的个数。
- Global Top Troughput：展示了吞吐量前几名的服务。
- Global Top Slow Endpoint：展示了耗时前几名的 Endpoint。
- Service (Avg) ResponseTime：展示了指定服务的（平均）耗时。
- Service (Avg) Throughput：展示了指定服务的（平均）吞吐量。
- Service (Avg) SLA：展示了指定服务的（平均）SLA（Service Level Agreement，服务等级协议）。
- Service Percent Response：展示了指定服务响应时间的分位数。
- Service Slow Endpoint：展示了指定服务中耗时比较长的 Endpoint 信息。
- Running ServiceInstance：展示了指定服务下的实例信息。









性能分析

分布式链路追踪两个流派：代码埋点、字节码增强

​	无论使用哪种方式，底层都逃不过面向切面这个基础逻辑。因为只有这样才可以做到大面积的使用。这也就决定了它只能做到框架级别和RPC粒度的监控。

​	



## Refrence

[1]: https://blog.csdn.net/smooth00/article/details/96479544	"Skywalking的存储配置与调优"
[2]: https://greeeg.com/en/issues/differences-between-logging-tracing-profiling	"日志记录、跟踪和分析之间有什么区别？"
[3]:https://blog.csdn.net/SpringHASh/article/details/125065843?spm=1001.2014.3001.5502 "Springcloud 集成 Skywalking 实现全链路追踪"
[4]: https://github.com/apache/skywalking-java/blob/main/docs/en/setup/service-agent/java-agent/Application-toolkit-logback-1.x.md "skywalking与logback继承"
