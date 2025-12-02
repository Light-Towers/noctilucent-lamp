# 一、Elasticsearch 安装

## 1. 下载
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.2-x86_64.rpm
```
## 2. 安装
```bash
sudo rpm --install elasticsearch-7.10.2-x86_64.rpm
```
## 3. 配置外网访问与安全设置

### 配置外网访问
编辑配置文件：
```bash
vi /etc/elasticsearch/elasticsearch.yml
```

修改以下配置项：
```yaml
network.host: 192.168.100.79
node.name: node-1
discovery.seed_hosts: ["127.0.0.1"]
cluster.initial_master_nodes: ["node-1"]
```

### 开启X-Pack验证
在`elasticsearch.yml`中添加：
```yaml
http.cors.enabled: true
http.cors.allow-origin: "*"
http.cors.allow-headers: Authorization
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
```

> ⚠️ 修改后需重启服务：
> ```bash
> sudo systemctl restart elasticsearch.service
> ```

### 设置用户密码
```bash
sudo systemctl start elasticsearch.service
```

启动完成后执行：
```bash
cd /usr/share/elasticsearch/
sudo bin/elasticsearch-setup-passwords interactive
```

配置完成后重启服务：
```bash
sudo systemctl restart elasticsearch.service
```

### 启动/停止命令
```bash
sudo systemctl start elasticsearch.service  # 启动
sudo systemctl stop elasticsearch.service   # 停止
```


# 二、Zipkin 下载安装

## 1. 下载地址
[zipkin-server-2.23.2-exec.jar](https://github.com/openzipkin/zipkin/releases)

## 2. 启动命令
```bash
STORAGE_TYPE=elasticsearch \
ES_HOSTS=192.168.100.79:9200 \
ES_USERNAME=elastic \
ES_PASSWORD=123456 \
nohup java -jar zipkin-server-2.23.2-exec.jar >/data/myedu/zipkin/logs/zipkin-server.log 2>&1 &
```

> 📌 访问地址: [http://localhost:9411/](http://localhost:9411/)

## 3. 依赖组件安装（可选）
### 下载地址
[zipkin-dependencies-2.6.3.jar](https://gitee.com/mirrors/zipkin-dependencies#quick-start)

### 启动命令
```bash
STORAGE_TYPE=elasticsearch \
ES_HOSTS=192.168.100.79:9200 \
ES_USERNAME=elastic \
ES_PASSWORD=123456 \
nohup java -jar zipkin-dependencies-2.6.3.jar >/data/myedu/zipkin/logs/zipkin-dependencies.log 2>&1 &
```

> 📌 推荐使用阿里云Maven仓库加速下载：
> [https://developer.aliyun.com/mvn/search](https://developer.aliyun.com/mvn/search)

访问地址:http://localhost:9411/

Zipkin-dependencies 下载（可选）
https://gitee.com/mirrors/zipkin-dependencies#quick-start
启动命令:
STORAGE_TYPE=elasticsearch ES_HOSTS=192.168.100.79:9200 ES_USERNAME=elastic ES_PASSWORD=123456 nohup java -jar zipkin-dependencies-2.6.3.jar >/data/myedu/zipkin/logs/zipkin-dependencies.log 2>&1 &

可以使用阿里云maven仓库下载jar 比较快
https://developer.aliyun.com/mvn/search

# 三、项目集成 Zipkin

## 1. 引入依赖
```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-zipkin</artifactId>
   <version>2.2.8.RELEASE</version>
</dependency>
```

## 2. Spring Boot 配置
```yaml
spring:
  zipkin:
    enabled: true  # 开启链路追踪
    base-url: http://192.168.100.136:9411/
  sleuth:
    sampler:
      probability: 1 # 生产环境建议改为0.1采样率
```

## 3. MySQL 集成配置
```bash
jdbc:mysql://192.168.100.21:33307/my_news_dev?useUnicode=true&characterEncoding=UTF-8&queryInterceptors=brave.mysql8.TracingQueryInterceptor&exceptionInterceptors=brave.mysql8.TracingExceptionInterceptor
```

# 四、清理过期索引数据

## 1. 删除脚本
```bash
#!/bin/bash

###################################
# 删除早于15天的ES集群索引
###################################
function delete_indices() {
    comp_date=$(date -d "15 day ago" +"%Y-%m-%d")
    date1="$1 00:00:00"
    date2="$comp_date 00:00:00"

    t1=$(date -d "$date1" +%s)
    t2=$(date -d "$date2" +%s)

    if [ $t1 -le $t2 ]; then
        echo "$1时间早于$comp_date，进行索引删除"
        curl -XDELETE "http://elastic:123456@192.168.100.79:9200/zipkin-*-$1"
    fi
}

# 获取所有zipkin索引并删除
curl -XGET "http://elastic:123456@192.168.100.79:9200/_cat/indices" \
  | egrep "zipkin*" \
  | sort \
  | uniq \
  | awk -F" " '{print $3}' \
  | awk -F"-" '{ for(i=1; i<=2; i++){ $i=""; print $0 }' \
  | sed 's/ /-/g' \
  | sed 's/--//g' \
  | while read LINE
do
    delete_indices "$LINE"
done
```

## 2. 定时任务配置
```cron
59 01 * * * /data/myedu/zipkin/del_es_zipkin_index.sh
00 */1 * * * /data/myedu/zipkin/zipkin-dependencies-start.sh
```

> ⚠️ 注意事项：
> 1. 确保脚本文件路径与权限正确
> 2. 生产环境建议增加错误日志记录
> 3. 可先通过`chmod +x`赋予脚本执行权限
