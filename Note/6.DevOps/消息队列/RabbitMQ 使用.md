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
   wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.13.0/rabbitmq-server-3.13.0.tar.xz
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
