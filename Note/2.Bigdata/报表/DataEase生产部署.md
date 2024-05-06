## 一、环境要求

- 操作系统: Ubuntu 22.04 / CentOS 7 64 位系统
- CPU/内存: 4核8G
- 磁盘空间: 200G

## 二、安装方式

### 1.在线安装

```bash
curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash
```

### 2.离线安装

#### 2.1 下载离线安装包

请自行下载 DataEase 最新版本的基础安装包，并复制到目标机器的 /tmp 目录下。
安装包下载链接: https://community.fit2cloud.com/#/products/dataease/downloads

#### 2.2 解压安装包

```bash
cd /tmp
# 解压安装包（dataease-offline-installer-v2.0.0.tar.gz 为示例安装包名称，操作时可根据实际安装包名称替换）
tar zxvf dataease-offline-installer-v2.0.0.tar.gz
```

#### 2.3 设置安装参数（可选）

DataEase 支持以配置文件的形式来设置安装参数，如安装目录、服务运行端口、数据库配置参数等，具体参数请参见安装包中的 install.conf 文件：

```bash
# 基础配置
## 安装目录
DE_BASE=/opt
## Service 端口
DE_PORT=8100
## 登录超时时间，单位min。如果不设置则默认8小时，也就是480
DE_LOGIN_TIMEOUT=480
## 安装模式，community | enterprise
DE_INSTALL_MODE=community

# 数据库配置
## 是否使用外部数据库
DE_EXTERNAL_MYSQL=false
## 数据库地址
DE_MYSQL_HOST=mysql-de
## DataEase 数据库库名
DE_MYSQL_DB=dataease
## 数据库用户名
DE_MYSQL_USER=root
## 数据库密码
DE_MYSQL_PASSWORD=Password123@mysql
## 数据库参数
DE_MYSQL_PARAMS="autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true"
```

#### 2.4 执行安装脚本

```bash
# 进入安装包目录（dataease-offline-installer-v2.0.0 为示例安装包目录名称，操作时可根据实际安装包名称替换）
cd dataease-offline-installer-v2.0.0
# 运行安装脚本
/bin/bash install.sh
```

* Mysql 相关配置

如果使用外部数据库进行安装，**只能使用 MySQL 8.X 版本**。同时 DataEase 对数据库部分配置项有要求，请参考下附的数据库配置，修改环境中的数据库配置文件

```bash
[mysqld]
datadir=/var/lib/mysql

default-storage-engine=INNODB
character_set_server=utf8
lower_case_table_names=1
table_open_cache=128
max_connections=2000
max_connect_errors=6000
innodb_file_per_table=1
innodb_buffer_pool_size=1G
max_allowed_packet=64M
transaction_isolation=READ-COMMITTED
innodb_flush_method=O_DIRECT
innodb_lock_wait_timeout=1800
innodb_flush_log_at_trx_commit=0
sync_binlog=0
group_concat_max_len=1024000
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION
skip-name-resolve

[mysql]
default-character-set=utf8

[mysql.server]
default-character-set=utf8
```

特别注意以下几个参数的设置：

```bash
character_set_server=utf8
lower_case_table_names=1
group_concat_max_len=1024000
```

请参考文档中的建库语句创建 DataEase 使用的数据库，DataEase 服务启动时会自动在配置的库中创建所需的表结构及初始化数据。

```bash
CREATE DATABASE `dataease` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

* 安装目录说明

安装脚本使用 /opt/dataease2.0 作为默认安装目录，DataEase 的配置文件、数据及日志等均存放在该安装目录 安装目录目录结构说明：

```bash
/opt/dataease2.0/
├── apisix                                      #-- 存放 APISIX 组件的配置文件以及其日志文件持久化目录
├── bin                                         #-- 安装过程中需要加载到容器中的脚本
├── cache                                       #-- 存放 Ehcache 的缓存文件，主要缓存的是权限相关的数据
├── conf                                        #-- DataEase 各组件及数据库等中间件的配置文件
├── data                                        #-- DataEase 各组件及数据库等中间件的数据持久化目录
├── docker-compose-apisix.yml                   #-- DataEase 内建的 APISIX 所需的 Docker Compose 文件
├── docker-compose-mysql.yml                    #-- DataEase 内建的 MySQl 所需的 Docker Compose 文件
├── docker-compose.yml                          #-- DataEase 基础 Docker Compose 文件，定义了网络等基础信息
├── logs                                        #-- DataEase 各组件的日志文件持久化目录
└── templates                                   #-- DataEase 各组件及数据库等中间件的配置文件的原始文件
```

#### 2.5 登录访问

```bash
安装成功后，通过浏览器访问如下页面登录：
- 访问地址 : http://目标服务器IP地址:服务运行端口
- 登录用户名: admin
- 登录密码: DataEase@123456
```

### 三、离线升级

按照本文档 [离线安装](https://dataease.io/docs/v2/installation/offline_INSTL_and_UPG/) 步骤，下载新版本安装包并上传解压后，重新执行安装命令进行升级。

```bash
# 进入项目目录
cd dataease-release-v2.x.y-offline
# 运行安装脚本
/bin/bash install.sh
# 查看 DataEase 状态
dectl status
```

**注意：升级前做好数据库的备份工作是一个良好的习惯，可参考[备份还原](https://dataease.io/docs/v2/installation/backup_faq/)。**

## 四、命令行工具

### 1. DataEase Service

DataEase 在安装的时候默认向系统中添加了相应的 dataease Service，支持的 Service 命令有：

```bash
service dataease start # 启动 DataEase 服务；
service dataease stop # 停止 DataEase 服务，并删除相关的运行容器、docker 网络等资源；
service dataease restart # 停止后启动 DataEase 服务，相当于先执行 stop，再执行 start 命令；
service dataease status # 查看 DataEase 服务当前各容器运行状态。
```

### 2. dectl

DataEase 默认内置了命令行运维工具（dectl），通过执行 dectl help 命令，可以查看相关的帮助文档。
**请注意**，backup、restore 命令需要 DataEase 版本在 v2.4 及以上，使用方式见：https://dataease.io/docs/v2/change-v2-4/#71-dectl

```bash
Usage:
  ./dectl [COMMAND] [ARGS...]
  ./dectl --help

Commands:
    status       查看 DATAEASE 服务运行状态   
    start        启动 DATAEASE 服务   
    stop         停止 DATAEASE 服务  
    restart      重启 DATAEASE 服务  
    reload       重新加载 DATAEASE 服务
    upgrade      在线升级 DATAEASE 服务
    version      查看 DATAEASE 版本信息
    clear-images 清理 DATAEASE 旧版本的相关镜像
    backup       进行 DATAEASE 备份
    restore      进行 DATAEASE 恢复
```

## 五、备份还原

**DataEase 安装后，相关文件的分布路径如下：**

- /opt/dataease2.0：默认运行路径，在安装时可设置。主要存放 DataEase 运行时所需的配置文件及运行时产生的数据，包括日志文件等
- /usr/bin：默认 docker 及 docker-compose 的运行程序被放置在此目录下
- /usr/local/bin/dectl：DataEase 的命令行工具
- /var/lib/docker：默认 docker 镜像加载在此



**综上所述，备份 DataEase 主要需要备份运行路径，如 /opt/dataease2.0 目录即可。还原步骤如下：**

- **该方式适用于相同版本 DataEase 的迁移，请在新环境里安装同一个版本的 DataEase，安装时请选择相同的配置参数**
- 停止两个环境里的 DataEase 服务，执行命令： service dataease stop
- 把原环境里的运行目录 /opt/dataease2.0 整个目录覆盖掉新环境里的 /opt/dataease2.0 目录
- 启动新环境里的 DataEase 服务： service dataease start



**基于DataEase 命令进行备份和恢复：**

- 备份操作：dectl backup
- 恢复操作：dectl restore DataEase备份文件.tar.gz

dectl backup 命令将 DataEase 排除日志目录以外的运行目录（如 /opt/dataease2.0）进行备份压缩，生成备份文件 DataEase备份文件.tar.gz。
在安装了同样版本的 DataEase 服务器上，用户可以通过执行 dectl restore DataEase备份文件.tar.gz 将 DataEase 还原为备份的内容。
**注意：备份文件中并不包含完整的镜像文件，所以备份和还原操作只能在同版本的情况下执行。**
