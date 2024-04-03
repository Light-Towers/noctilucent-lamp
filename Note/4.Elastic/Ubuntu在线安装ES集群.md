# Ubuntu在线安装ES集群

## 前提：
### 1. 创建elasticsearch用户

```bash
# 创建elasticsearch组
groupadd elasticsearch
# 创建用户
useradd elasticsearch -g elasticsearch -p elasticsearch
```

### 2. 添加到sudo组, (NOPASSWD)免密
```bash
# 修改/etc/sudoers文件，需要先申请写权限
chmod u+w /etc/sudoers
在 ## Allow root to run any commands anywhere 之后添加一行
elasticsearch   ALL=(ALL) NOPASSWD: ALL
# 保存退出，返还写权限
chmod u-w /etc/sudoers
```

### 3. 系统配置修改

```bash
#修改主机内核参数，然后执行sysctl -p
vim /etc/sysctl.conf
vm.max_map_count = 655350
# 解除文件数量打开限制
vim /etc/security/limits.conf
...
elasticsearch         hard      nofile        65536
elasticsearch         soft      nofile        65536
*               soft      nproc          4096
*               hard      nproc          4096
...
```

验证：
su elasticsearch -c 'ulimit -Hn'

### 4. 创建目录

```bash
# 创建数据、日志目录，并赋权给elasticsearch用户
mkdir -p /data0/elasticsearch /var/log/elasticsearch
chown -R elasticsearch.elasticsearch    /data0/elasticsearch
chown -R elasticsearch.elasticsearch    /var/log/elasticsearch
```

## 安装
### 1.基于Debian包安装

* 在线安装
```bash
# 1.导入PGP Key
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
# 2.添加Elasticsearch apt源
## 2.1 需要先安装apt-transport-https
sudo apt-get install apt-transport-https
## 2.2 添加源
echo "deb https://artifacts.elastic.co/packages/7.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-7.x.list
# 3.从APT repository中安装Elasticsearch
sudo apt-get update && sudo apt-get install elasticsearch
```

* 手动安装
```bash
# 1.下载二进制安装包：
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.6.2-amd64.deb
# 2.安装
sudo dpkg -i elasticsearch-7.6.2-amd64.deb
```

### 2. 配置文件修改 elasticsearch.yml

```yaml
cluster.name: es-cluster
node.name: node-1
path.data: /data0/elasticsearch
path.logs: /var/log/elasticsearch
network.host: 0.0.0.0
http.port: 9200
discovery.seed_hosts: ["192.168.100.209"]
cluster.initial_master_nodes: ["node-1"]
```

### 3. 启动
```bash
# 启动
sudo systemctl start elasticsearch.service
# 停止
sudo systemctl stop elasticsearch.service
# 配置开机自启
sudo /bin/systemctl daemon-reload
sudo /bin/systemctl enable elasticsearch.service
```

集群健康状态
curl http://localhost:9200/_cluster/health?pretty
查看集群主节点
curl http://localhost:9200/_cat/nodes?pretty

## 集群认证配置

### 1.CA证书生成

```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-certutil ca
Please enter the desired output file [elastic-stack-ca.p12]:        //回车
Enter password for elastic-stack-ca.p12 :                           //证书的密码：mingyang100
```

### 2.集群证书生成

```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-certutil cert --ca elastic-stack-ca.p12
Enter password for CA (elastic-stack-ca.p12) :                       //输入CA证书密码：mingyang100
Please enter the desired output file [elastic-certificates.p12]:     //回车
Enter password for elastic-certificates.p12 :                        //集群证书密码：mingyang100p12
```

### 3.拷贝证书：

```bash
sudo cp /usr/share/elasticsearch/elastic-certificates.p12 /etc/elasticsearch/
如果组没有读权限，执行下面命令
sudo chmod g+r /etc/elasticsearch/elastic-certificates.p12
```

分发到各集群节点

### 4.修改elasticsearch.yml
```yaml
# 修改elasticsearch.yml配置，在末尾添加
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate 
xpack.security.transport.ssl.client_authentication: required
xpack.security.transport.ssl.keystore.path: elastic-certificates.p12
xpack.security.transport.ssl.truststore.path: elastic-certificates.p12
```

重启ES集群

// 如果在创建节点证书时输入了密码，运行以下命令以将密码存储在Elasticsearch密钥库中：

```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password
sudo /usr/share/elasticsearch/bin/elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
```

### 5.密码配置
```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-setup-passwords interactive
```

### 6.携带用户名、密码验证ES
```bash
curl --user elastic:mingyang100 http://localhost:9200/_cat/health?v
```

## 分词器安装

### 在线安装

```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-ik
```

### 手动安装

```bash
#github 下载地址：https://github.com/medcl/elasticsearch-analysis-ik/releases
#创建ik文件夹
mkdir /usr/share/elasticsearch/plugins/ik
#解压
unzip elasticsearch-analysis-ik-7.6.2.zip -d /usr/share/elasticsearch/plugins/ik
```



## 调优

### 关闭swap

```bash
# 临时关闭
swapoff -a
# 永久关闭， #注释掉最后一行 /swap.img
vim /etc/fstab
```

### es内存

设置es内存不超过物理内存的一半，以64G为例

```bash
vim /etc/elasticsearch/jvm.options
...
-Xms32g
-Xmx32g
...
```



# Kibana安装

参照ES安装方式

## 前提：

### 1. 创建Kibana用户

```bash
# 创建kibana组
groupadd kibana
# 创建kibana用户
useradd kibana -g kibana -p kibana
```

### 2. 添加到sudo组, (NOPASSWD)免密

```bash
# 修改/etc/sudoers文件，需要先申请写权限
chmod u+w /etc/sudoers
在 ## Allow root to run any commands anywhere 之后添加一行
kibana   ALL=(ALL) NOPASSWD: ALL
# 保存退出，返还写权限
chmod u-w /etc/sudoers
```

## 安装

### 1.基于Debian包安装

* 在线安装

```bash
# 从APT repository中安装Elasticsearch （上文已配置过APT）
sudo apt-get update && sudo apt-get install kibana
```

* 手动安装

```bash
# 1.下载二进制安装包：
wget https://artifacts.elastic.co/downloads/kibana/kibana-7.6.2-amd64.deb
# 2.安装
sudo dpkg -i kibana-7.6.2-amd64.deb
```

### 2.修改Kibana配置

```bash
vim /etc/kibana/kibana.yml
...
#elasticsearch用户名、密码
elasticsearch.username: "elastic"
elasticsearch.password: "mingyang100"
#日志
logging.dest: /var/log/kibana/kibana.log
#汉化
i18n.locale: "zh-CN"
...
```

### 3.给目录赋权

```bash
chown -R kibana:kibana /usr/share/kibana/
chown -R kibana:kibana /etc/kibana/
chown -R kibana:kibana /var/lib/kibana/
chown -R kibana:kibana /var/log/kibana/
```

### 4. 启动

```bash
# 启动
sudo systemctl start kibana.service
# 停止
sudo systemctl stop kibana.service
# 配置开机自启
sudo /bin/systemctl daemon-reload
sudo /bin/systemctl enable kibana.service
```



## Refrence

[1]:https://www.elastic.co/guide/en/elasticsearch/reference/7.6/deb.html	"Install Elasticsearch with Debian Package"
[2]:https://juejin.cn/post/7079955586330132487	"解决Elasticsearch集群开启账户密码安全配置自相矛盾的坑"
[3]:https://www.cnblogs.com/kevingrace/p/10671063.html	"Elasticsearch 集群和索引健康状态及常见错误说明"