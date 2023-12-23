# 快速开始
## 环境准备
* 2 CPUs, 8GB RAM, 2GB Swap area, and 10GB disk space
* **Docker** & **Docker Compose** v2
* **Python 3.7+** & pip（sudo easy_install pip）



> 如果是纯净版的新系统，需要安装以下包

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
```
python3 -m datahub docker quickstart
```
至此，DataHub搭建完成，登录DataHub UI：`http://localhost:9002`, username & password: `datahub`

## 元数据摄取
1. 定义食谱（Recipes）
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
        username: datahub
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



2. 通过CLI摄取元数据

```bash
#安装所需插件
python3 -m pip install 'acryl-datahub[datahub-rest]'
python3 -m pip install 'acryl-datahub[mysql]'
python3 -m datahub ingest -c ./examples/recipes/mysql_to_datahub.yml
```

## 停止

```shell
python3 -m docker quickstart --stop
```

## 重置

```shell
python3 -m datahub docker nuke
```

## 更新
```shell
python3 -m datahub docker quickstart
```

## 自定义安装

download the [docker-compose.yaml](https://raw.githubusercontent.com/datahub-project/datahub/master/docker/quickstart/docker-compose-without-neo4j-m1.quickstart.yml) used by the cli tool

```shell
python3 -m datahub docker quickstart --quickstart-compose-file <path to compose file>
```

## 备份

```shell
python3 -m datahub docker quickstart --backup  # default , ~/.datahub/quickstart/backup.sql
# --backup-file <path to backup file>  # 指定备份文件
```

## 恢复

```shell
datahub docker quickstart --restore  # default
# --restore-file /home/my_user/datahub_backups/quickstart_backup_2002_22_01.sql  # 指定从备份文件恢复

datahub docker quickstart --restore-indices  # 只恢复索引
datahub docker quickstart --restore --no-restore-indices	# 只恢复主数据状态
```



# 参考

[DataHub官网](https://datahubproject.io/docs/)
[一站式元数据治理平台——Datahub入门宝典](https://www.cnblogs.com/tree1123/p/15743253.html)
[DataHub：流行的元数据架构介绍](https://engineering.linkedin.com/blog/2020/datahub-popular-metadata-architectures-explained?spm=a2c6h.12873639.0.0.68682190lPaUkw)
