## 一、准备工作

### 1.1 备份Hive库

```bash
mysqldump -uroot -proot hive > ./hive.sql
```

### 1.2 下载Hive-3.1.3

```bash
# hive-3.1.3 下载到 /opt/software/hive
cd /opt/software/hive
wget https://dlcdn.apache.org/hive/hive-3.1.3/apache-hive-3.1.3-bin.tar.gz
# 解压
tar -zxvf apache-hive-3.1.3-bin.tar.gz
```

### 1.3 备份Hive目录

```bash
# 切换到CDH lib目录
/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib
## 备份原Hive包
cp -r hive/ hive.212.bak
```

## 二、替换Hive相关文件

**该步骤需要集群整体替换**

```bash
# 切换到Hive目录
cd hive
## 替换Hive相关Jar包
# 删除hive开头Jar包
rm -rf lib/hive-*.jar
# 替换成Hive3的Jar包
cp /opt/software/hive/apache-hive-3.1.3-bin/lib/hive-* lib/

# 替换执行文件
cp /opt/software/hive/apache-hive-3.1.3-bin/bin/* bin/
```

## 三、升级数据库

```mysql
-- 登录Mysql
mysql -uroot -p
-- 使用Hive库
use hive;
-- 执行升级sql
source upgrade-2.1.0-to-2.2.0.mysql.sql;
source upgrade-2.2.0-to-2.3.0.mysql.sql;
source upgrade-2.3.0-to-3.0.0.mysql.sql;
source upgrade-3.0.0-to-3.1.0.mysql.sql;
```

## 四、重启Hive服务
