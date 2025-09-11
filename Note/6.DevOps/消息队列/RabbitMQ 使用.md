# RabbitMQ 使用

## 一、核心角色
1. **生产者(Producer)**  
   - 消息的发送方，负责将数据封装为消息并发送到交换机
   - 示例：`channel.basic_publish()`

2. **消费者(Consumer)**  
   - 从队列获取消息并处理
   - 示例：`channel.basic_consume()`

3. **交换机(Exchange)**  
   - 接收生产者消息并路由到队列
   - 类型：
     - `direct`：精确匹配路由键
     - `fanout`：广播模式
     - `topic`：模糊匹配路由键
     - `headers`：基于消息头路由

4. **队列(Queue)**  
   - 存储消息的缓冲区
   - 特性：
     - 支持持久化
     - 支持多消费者组
     - 支持TTL(生存时间)

5. **绑定(Binding)**  
   - 连接交换机与队列的规则
   - 示例：`queue.bind(exchange, routing_key='key')`

## 二、工作原理
1. **消息流转流程**  
   ```
   生产者 → [交换机] → [绑定规则] → 队列 → 消费者
   ```

2. **关键机制**  
   - **确认机制**：消费者需发送ack确认消息处理完成
   - **持久化**：通过`durable=True`参数实现消息持久化
   - **死信队列**：未被消费的消息自动转入死信交换机
   - **QoS控制**：`basic.qos(prefetch_count=1)`限制未确认消息数量

3. **集群架构**  
   - 镜像队列：跨节点复制
   - 节点类型：
     - 磁盘节点：持久化元数据
     - 内存节点：仅存储元数据在内存

## 三、消息类型
1. **基本消息**  
   - 简单的文本/二进制数据
   - 示例：`channel.basic_publish(body='Hello World')`

2. **延迟消息**  
   - 需安装`rabbitmq_delayed_message_exchange`插件
   - 通过`x-delayed-type`参数设置延迟类型

3. **优先级消息**  
   - 队列需声明`x-max-priority`参数
   - 消息可设置`priority`属性

4. **死信消息**  
   - 来源：
     - 消费者拒绝
     - 消息TTL过期
     - 队列满

5. **持久化消息**  
   - 需同时设置：
     - 交换机`durable=True`
     - 队列`durable=True`
     - 消息`delivery_mode=2`

## 四、安装部署
1. **Docker Compose部署**  
   ```yaml
   services:
     rabbitmq:
       restart: always
       image: rabbitmq:management
       container_name: rabbitmq
       hostname: rabbit
       ports:
         - 5672:5672
         - 15672:15672
       environment:
         TZ: Asia/Shanghai
         RABBITMQ_DEFAULT_USER: rabbit
         RABBITMQ_DEFAULT_PASS: 123456
       volumes:
         - /data/rabbitmq/data:/var/lib/rabbitmq
         - ./conf:/etc/rabbitmq
   ```

2. **管理插件启用**  
   ```bash
   # 进入容器
   docker exec -it rabbitmq bash
   
   # 查看可用插件
   rabbitmq-plugins list
   
   # 启用管理插件
   rabbitmq-plugins enable rabbitmq_management
   ```

3. **源码编译安装**  
   ```bash
   # 安装依赖
   apt-get update && apt-get install -y build-essential autoconf libncurses5-dev
   # 下载源码
  # 下载指定版本源码
  wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.13.0/rabbitmq-server-3.13.0.tar.xz
  # 解压编译
   # 解压编译
   tar -xf rabbitmq-server-3.13.0.tar.xz
   cd rabbitmq-server-3.13.0
   make
   # 启动服务
   ./scripts/rabbitmq-server
   ```

4. **配置文件说明**  
   - 主配置文件：`rabbitmq.conf`
   - 环境变量文件：`enabled_plugins`
   - 示例配置：
     ```conf
     # 禁用guest用户远程登录
     loopback_users.guest = false
     # 开启管理插件指标收集
     management_agent.disable_metrics_collector = false
     # 配置内存阈值
     vm_memory_high_watermark.relative = 0.6
     ```

5. **集群部署**  
   - 节点初始化：
     ```bash
     rabbitmqctl stop_app
     rabbitmqctl reset
     rabbitmqctl start_app
     ```
   - 镜像队列策略：
     ```bash
     rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all","ha-sync-mode":"automatic"}'
     ```
   - 查看集群状态：
     ```bash
     rabbitmqctl cluster_status
     ```
   - 节点加入集群：
     ```bash
     rabbitmqctl join_cluster rabbit@node1
     ```

## 五、Spring Boot 集成
### 1. 依赖配置
在`pom.xml`中添加Spring Boot Starter AMQP依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 2. 配置文件
在`application.yml`中配置RabbitMQ连接信息：
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: rabbit
    password: 123456
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        prefetch: 1
        acknowledge-mode: manual
```

### 3. 消息生产者
```java
@Component
public class MessageProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData -> {
            // 设置消息持久化
            correlationData.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return correlationData;
        });
    }
}
```

### 4. 消息消费者
```java
@Component
@RabbitListener(queues = "queue.name")
public class MessageConsumer {
    @RabbitHandler
    public void processMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 处理消息
            System.out.println("Received message: " + message);
            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 拒绝消息，不重新入队
            channel.basicReject(deliveryTag, false);
        }
    }
}
```

**参数详解：**
- **消息体参数**：可直接接收转换后的消息内容
  - 支持类型：String、byte[]、自定义对象等
  - 示例：`public void processMessage(String message)`

- **Message对象**：获取原始消息内容和属性
  - 示例：`public void processMessage(Message message)`

- **Channel参数**：用于手动确认、拒绝等操作
  - 示例：`public void processMessage(String message, Channel channel)`
  - **关键作用**：提供与RabbitMQ服务器的通信通道，用于执行确认、拒绝等操作

- **deliveryTag参数**：消息投递唯一标识
  - 由RabbitMQ在消息投递时生成，每个Channel内单调递增
  - 作用：标识特定消息投递，用于确认(ack)、拒绝(nack)、重入队等操作
  - 特性：
    - Channel级别唯一：不同Channel的deliveryTag可能重复
    - 递增特性：同一Channel内新消息的deliveryTag > 旧消息
    - 临时性：Channel关闭后计数器重置
  - 使用示例：
    ```java
    // 确认消息处理完成
    channel.basicAck(deliveryTag, false);
    
    // 拒绝消息且不重新入队
    channel.basicReject(deliveryTag, false);
    
    // 拒绝消息并重新入队
    channel.basicNack(deliveryTag, false, true);
    ```
  - **重要提示**：必须与接收到消息的同一Channel配合使用，跨Channel操作会导致异常

- **@Header注解**：获取消息头中的特定属性
  - RabbitMQ支持消息头（Headers Exchange类型）
  - 示例：`@Header("custom-header") String customValue`
  - 常用系统头：
    - `AmqpHeaders.DELIVERY_TAG`：消息投递标签
    - `AmqpHeaders.RECEIVED_ROUTING_KEY`：接收的路由键
    - `AmqpHeaders.CONTENT_TYPE`：消息内容类型

- **@Headers注解**：获取所有消息头
  - 示例：`public void processMessage(String message, @Headers Map<String, Object> headers)`

**消息头使用示例：**
```java
// 生产者添加消息头
MessageProperties props = new MessageProperties();
props.setHeader("priority", "high");
props.setHeader("source", "web");
Message message = new Message(payload.getBytes(), props);
rabbitTemplate.send(exchange, routingKey, message);

// 消费者获取消息头
@RabbitHandler
public void processMessage(String message, 
                          @Header("priority") String priority,
                          @Header("source") String source) {
    System.out.println("Priority: " + priority);
    System.out.println("Source: " + source);
}
```

**注解说明：**
- **@RabbitListener**：可以标注在类或方法上
  - 标注在类上：表示该类为消息监听器，需配合@RabbitHandler使用
  - 标注在方法上：直接指定该方法为消息处理方法
  - 类级别使用场景：当需要一个监听器处理多种类型消息时（通过方法重载实现）
  
- **@RabbitHandler**：必须与类级别的@RabbitListener配合使用
  - 标识具体处理消息的方法
  - 支持方法重载，可根据消息类型自动选择处理方法
  - 示例：
    ```java
    @RabbitHandler
    public void handleString(String message) { /* 处理字符串消息 */ }
    
    @RabbitHandler
    public void handleOrder(Order order) { /* 处理订单对象 */ }
    ```

### 5. 重要配置说明
- **消息确认机制**：开启`publisher-confirm-type`和`publisher-returns`确保消息可靠投递（RabbitMQ 3.13.0验证）
- **消费者QoS**：通过`prefetch`控制未确认消息数量（Spring Boot 3.2.5兼容配置）
- **手动ACK**：确保消息处理成功后再确认，防止消息丢失（需配合Channel正确使用）
- **死信队列**：通过`@RabbitListener`的`arguments`配置死信交换机（3.13.0版本特性）
