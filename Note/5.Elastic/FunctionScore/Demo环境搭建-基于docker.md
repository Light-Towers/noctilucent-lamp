# 1. 运行环境

- Docker：Docker version 20.10.17, build 100c701
- Docker Compose：Docker Compose version v2.10.2

------

# 2. 项目结构

```
$ tree efk-demo-compose
efk-demo-compose
├── docker-compose.yml
├── elasticsearch_conf
│   └── elasticsearch.yml
├── filebeat_conf
│   ├── create_pipeline.sh
│   ├── filebeat.yml
│   └── pipeline.json
├── kibana_conf
│   └── kibana.yml
└── nginx_conf
    └── start.sh


4 directories, 8 files
```

docker-compose.yml：

```
version: "3"


services:
  elasticsearch:
    container_name: elasticsearch
    image: elasticsearch:8.6.1
    restart: on-failure
    environment:
      - ES_JAVA_OPTS=-Xms1024m -Xmx1024m
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
      - ./elasticsearch_conf/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - ./elasticsearch_conf/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    ports:
      - "9200:9200/tcp"
    networks:
      efk_demo:


  kibana:
    container_name: kibana
    image: kibana:8.6.1
    restart: on-failure
    volumes:
      - ./kibana_conf/kibana.yml:/usr/share/kibana/config/kibana.yml
    depends_on:
      - elasticsearch
    ports:
      - "5601:5601/tcp"
    networks:
      efk_demo:


  filebeat:
    container_name: filebeat
    image: docker.elastic.co/beats/filebeat:8.6.1
    restart: on-failure
    volumes:
      - ./filebeat_conf/filebeat.yml:/usr/share/filebeat/filebeat.yml
      - nginx_logs:/opt/logs
    depends_on:
      - elasticsearch
    networks:
      efk_demo:


  nginx:
    container_name: nginx
    image: nginx:1.23.3
    restart: on-failure
    volumes:
      - nginx_logs:/var/log/nginx
      - ./nginx_conf/start.sh:/start.sh
    ports:
      - "10000:80/tcp"
    networks:
      efk_demo:
    command: /start.sh


networks:
  efk_demo:
    driver: bridge


volumes:
  nginx_logs:
    driver: local
  elasticsearch_data:
    driver: local
```

elasticsearch_conf/elasticsearch.yml：

```
cluster.name: "docker-cluster"
network.host: "0.0.0.0"
cluster.routing.allocation.disk.threshold_enabled: true
cluster.routing.allocation.disk.watermark.low: "5120mb"
cluster.routing.allocation.disk.watermark.high: "2560mb"
cluster.routing.allocation.disk.watermark.flood_stage: "1280mb"
discovery.type: "single-node"


xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.keystore.type: PKCS12
xpack.security.transport.ssl.verification_mode: certificate
xpack.security.transport.ssl.keystore.path: elastic-certificates.p12
xpack.security.transport.ssl.truststore.path: elastic-certificates.p12
xpack.security.transport.ssl.truststore.type: PKCS12
xpack.security.transport.ssl.keystore.password: 123456
xpack.security.transport.ssl.truststore.password: 123456
xpack.security.audit.enabled: true
```

filebeat_conf/create_pipeline.sh：

```
curl \
    -X PUT \
    -H "Content-Type: application/json" \
    -d @pipeline.json \
    -u elastic:123456 \
    "http://127.0.0.1:9200/_ingest/pipeline/access-log-pipeline?pretty"
```

filebeat_conf/filebeat.yml：

```
filebeat.inputs:
  - type: filestream
    id: filestream-1
    enabled: true
    paths:
      - /opt/logs/access.log
    fields:
      service_name: "efk_demo"
    fields_under_root: true
    close_removed: false
    close_renamed: false
processors:
 - drop_fields:
     fields: ["prospector", "event", "dataset", "ecs", "agent", "input"]
queue.mem:
  events: 8192
  flush.timeout: 0
output.elasticsearch:
  hosts: ["http://elasticsearch:9200"]
  username: "elastic"
  password: "123456"
  pipeline: "access-log-pipeline"
```

filebeat_conf/pipeline.json：

```
{
    "description": "access log pipeline",
    "processors": [
        {
            "grok": {
                "field": "message",
                "patterns": [
                    "%{DATA:remote_addr} - %{DATA:remote_user} \\[%{HTTPDATE:time_local}\\] \"%{WORD:request_method} %{DATA:request_uri} %{DATA:scheme}\" %{NUMBER:status:int} %{NUMBER:body_bytes_sent:int} \"%{DATA:http_referer}\" \"%{DATA:http_user_agent}\" \"%{DATA:http_x_forwarded_for}\""
                ]
            }
        }
    ]
}
```

kibana_conf/kibana.yml：

```
server.name: "kibana"
server.host: "0.0.0.0"
server.shutdownTimeout: "5s"
elasticsearch.hosts: ["http://elasticsearch:9200"]
elasticsearch.username: "kibana"
elasticsearch.password: "123456"
xpack.monitoring.ui.container.elasticsearch.enabled: true
i18n.locale: "zh-CN"
```

nginx_conf/start.sh：

```
#!/bin/sh


logfile=/var/log/nginx/access.log


if [ -L "$logfile" ] ; then
    rm -f "$logfile"
fi


exec /usr/sbin/nginx -g "daemon off;"
```

------

# 3. 环境初始化

### 3.1. 创建 Elasticsearch 证书

```
# 启动 ES 容器
$ docker run -itd --name es elasticsearch:8.6.1


$ docker exec -it es ./bin/elasticsearch-certutil ca
# 第一次提示输入时，按回车
# 第二次提示输入时，输入 123456


$ docker exec -it es ./bin/elasticsearch-certutil cert --ca elastic-stack-ca.p12
# 第一次提示输入时，输入 123456
# 第二次提示输入时，按回车
# 第三次提示输入时，输入 123456


# 先 cd 到 efk-demo-compose/ 目录
$ docker cp es:/usr/share/elasticsearch/elastic-certificates.p12 elasticsearch_conf/


# 关闭、删除容器
$ docker kill es
$ docker rm es
```

### 3.2. 启动 Docker Compose

```
$ docker compose up -d
```

### 3.3. 设置 Elasticsearch 密码

```
$ docker exec -it elasticsearch ./bin/elasticsearch-setup-passwords interactive
# 第一次提示输入时，输入 y
# 接下来都输入 123456
```

### 3.4. 创建 Elasticsearch Ingest Pipeline

```
# 先 cd 到 efk-demo-compose/filebeat_conf/ 目录
$ sh create_pipeline.sh
{
  "acknowledged" : true
}
```

------

# 4. 验证

- 打开 [http://127.0.0.1:5601](http://127.0.0.1:5601/)
- 输入账号/密码：elastic/123456
- 创建数据视图、通过开发工具，查看 Ingest Pipeline 等

------

# 5. 启动/重启/关闭

```bash
# 启动
$ docker compose up -d
# 重启
$ docker compose restart
# 关闭
$ docker compose down
```