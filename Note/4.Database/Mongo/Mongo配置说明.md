# MongoDB配置文件示例

```properties
# 数据库文件存放路径
storage:
  dbPath: /var/lib/mongodb
  journal:
    enabled: true

# 日志文件配置
systemLog:
  destination: file
  logAppend: true
  path: /var/log/mongodb/mongod.log
  verbosity: 0 # 日志详细程度，0为最少信息，数值越大日志越详细

# 网络设置
net:
  port: 27017 # MongoDB服务监听的端口
  bindIp: 127.0.0.1,::1 # 绑定的IP地址，::1表示IPv6的localhost，可改为0.0.0.0以允许所有IP访问

# 进程管理
processManagement:
  fork: true # 是否以后台守护进程方式运行

# 安全设置
security:
  authorization: enabled # 启用授权认证

# 代表性的其他配置项
replication:
  replSetName: "myReplicaSet" # 复制集名称，用于设置复制集

sharding:
  clusterRole: shardsvr # 如果是分片集群中的shard服务器，则设置此项

# 存储引擎设置（对于WiredTiger）
storage:
  engine: wiredTiger
  wiredTiger:
    engineConfig:
      cacheSizeGB: 1 # WiredTiger引擎的缓存大小，单位GB

# 高级选项
operationProfiling:
  mode: off # 操作剖析模式，可设置为slowOp（记录慢查询）或all（记录所有操作）

# 索引构建
indexBuilds:
  jobTimeoutMinutes: 30 # 索引构建作业的超时时间，单位分钟

# 诊断日志
diagnosticDataCollection:
  enabled: false # 是否开启诊断数据收集，默认关闭
```

