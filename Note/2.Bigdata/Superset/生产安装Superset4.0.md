# Python3.9 安装 Superset4.0

* python 版本: 3.9+

* Centos7  底包依赖
  `sudo yum install gcc gcc-c++ libffi-devel python3-devel python-pip python-wheel openssl-devel cyrus-sasl-devel openldap-devel`

* 升级 pip
  `pip install --upgrade setuptools pip`
  或者
  `python3 -m pip install --upgrade setuptools pip`

## 一、创建虚拟环境

保持环境隔离

```bash
# 安装virtualenv
pip install virtualenv
# 创建虚拟环境
python3 -m venv venv/superset
# 激活
. venv/superset/bin/activate
```

### pip安装mysqlclient、redis

默认使用SQLite，**生产配置**支持Mysql PostgreSQL

```bash
# mysqlclient 依赖 （python3-devel mysql-devel pkgconfig）, 没有的话安装下
yum install mysql-devel
# pip 安装
pip install mysqlclient
pip install redis
# (可选)其它一些必要的包
pip install pillow thrift thrift_sasl
```

## 二、安装superset

```bash
pip install apache-superset
```

### superset_config.py配置文件

生成SECRET_KEY，`openssl rand -base64 42`

```properties
# 位置随意：/opt/software/superset/superset_config.py
# Superset specific config
ROW_LIMIT = 5000
SECRET_KEY = 'W26oCWUu49V66dfgMQ8mzMTl4oCJKnhWOZVuBXLjBAEdo3PtNkwlE3UA'
# 数据库配置
SQLALCHEMY_DATABASE_URI = 'mysql://user:passwd@127.0.0.1:3306/superset'
# 暂不清除，先关掉
WTF_CSRF_ENABLED = False
# Set this API key to enable Mapbox visualizations
MAPBOX_API_KEY = ''
# Cache配置
FILTER_STATE_CACHE_CONFIG = {
    'CACHE_TYPE': 'RedisCache',
    'CACHE_DEFAULT_TIMEOUT': 86400,
    'CACHE_KEY_PREFIX': 'superset_filter_cache',
    'CACHE_REDIS_URL': 'redis://127.0.0.1:6379/1'
}
```

### 配置环境变量

```bash
export SUPERSET_CONFIG_PATH=/opt/software/superset/superset_config.py
# 配置superset环境变量，否则找不到superset命令
export FLASK_APP=superset
# 下一步数据库初始化时遇到libstdc++.so.6相关问题，参考自：https://qiita.com/katafuchix/items/5e7c05e58213608248ae
export LD_PRELOAD=/lib64/libstdc++.so.6
```


### 初始化数据库
```bash
superset db upgrade
```

### 创建管理员用户
```bash
# 根据提示创建管理员，密码需要自己输入
superset fab create-admin
```

### 加载案例数据（可选）
```bash
superset load_examples
```

### 初始化，创建默认角色、权限
```bash
superset init
```

### 启动 superset
```bash
superset run --host 0.0.0.0 -p 8088
```

### 启动脚本

编写 **start.sh** 启动脚本

```bash
#!/bin/bash

# 激活Python3虚拟环境
. /root/venv/superset/bin/activate

# 设置环境变量
export SUPERSET_CONFIG_PATH=/opt/software/superset/superset_config.py
export FLASK_APP=superset
export LD_PRELOAD=/lib64/libstdc++.so.6

# 运行Superset
nohup superset run --host 0.0.0.0 -p 8088 > ./log/superset.log 2>&1 &
```


