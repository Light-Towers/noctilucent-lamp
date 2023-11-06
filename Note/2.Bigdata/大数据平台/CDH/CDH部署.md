## CM部署前准备

使用root账户

### 1.配置Host

```xml
172.18.28.136 hadoop101 hadoop101
172.18.28.132 hadoop102 hadoop102
172.18.28.134 hadoop103 hadoop103
172.18.28.135 hadoop104 hadoop104
172.18.28.133 hadoop105 hadoop105
```

* 注：阿里云服务器SSH经常过一会就断开连接解决办法。
	 ```xml
	ClientAliveInterval 30	 //客户端每隔多少秒向服务发送一个心跳数据
	ClientAliveCountMax 1800   //客户端多少秒没有响应，服务器自动断掉连接
	```

​	重启SSHD服务：`service sshd restart`

### 2.SSH免密登录

**生成公钥和私钥：\****

```bash
ssh-keygen -t rsa
```

然后敲（三个回车），就会生成两个文件id_rsa（私钥）、id_rsa.pub（公钥）。

**将公钥拷贝到要免密登录的目标机器上\****

```bash
ssh-copy-id hadoop101
ssh-copy-id hadoop102
...
```

重复上面操作，配置其余机器之间的免密登录

### 3.集群脚本

```bash
# root目录下创建bin目录
mkdir /root/bin
cd bin/
```

编辑xsync.sh，代码如下。

```bash
[root@hadoop101 ~]vi xsync.sh
#!/bin/bash
#1 获取输入参数个数，如果没有参数，直接退出
pcount=$#
if((pcount==0)); then
echo no args;
exit;
fi

#2 获取文件名称
p1=$1
fname=`basename $p1`
echo fname=$fname

#3 获取上级目录到绝对路径
pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir

#4 获取当前用户名称
user=`whoami`

#5 循环
for((host=102; host<106; host++)); do
        echo ------------------- hadoop$host --------------
        rsync -av $pdir/$fname $user@hadoop$host:$pdir
done
```

编辑xcall.sh，代码如下。

```bash
[root@hadoop101 ~]vi xcall.sh
#! /bin/bash
for i in hadoop105 hadoop106 hadoop107
do
    echo --------- $i ----------
    ssh $i "$*"
done
```

编辑完，赋权 `chmod 777 xsync.sh xcall. `

### 4.按照JDK

`注意：机器都需要使用官网提供的jdk。`

1. 在hadoop101的/opt目录下创建module和software文件夹

    ```bash
    mkdir /opt/module
    mkdir /opt/software
    ```

2. 上传`oracle-j2sdk1.8-1.8.0+update181-1.x86_64.rpm`至/opt/software目录并安装

   ```bash
   rpm -ivh oracle-j2sdk1.8-1.8.0+update181-1.x86_64.rpm
   ```


3. 配置环境变量

   ```bash
   vim /etc/profile.d/my_env.sh
   ...
   export JAVA_HOME=/usr/java/jdk1.8.0_181-cloudera
   export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib
   export PATH=$PATH:$JAVA_HOME/bin
   ...
   # 使配置生效
   source /etc/profile.d/my_env.sh
   # 验证是否安装成功
   java -version
   ```

4. 分发java安装目录和配置

   ```bash
   xsync /usr/java/
   xsync /etc/profile.d/my_env.sh
   # 使配置生效(其它机器执行)
   source /etc/profile.d/my_env.sh
   ```

### 5.安装Mysql

1. 安装前准备	

    ```bash
    # 查看MySQL是否安装
    [root@hadoop101 ~]rpm -qa|grep -i mysql
    mysql-libs-5.1.73-7.el6.x86_64
    # 如果安装了MySQL，就先卸载
    [root@hadoop101 ~]rpm -e --nodeps mysql-libs-5.1.73-7.el6.x86_64
    # 删除阿里云原有MySql依赖(如果有)
    [root@hadoop101 ~]yum remove mysql-libs
    # 下载MySql依赖并安装
    [root@hadoop101 ~]yum -y install libaio autoconf unzip
    # 切到 /opt/software 目录，下载相关安装包
    [root@hadoop101 software]wget https://downloads.mysql.com/archives/get/p/23/file/MySQL-shared-compat-5.6.24-1.el6.x86_64.rpm
    [root@hadoop101 software]wget https://downloads.mysql.com/archives/get/p/23/file/MySQL-shared-5.6.24-1.el6.x86_64.rpm
    # rpm安装
    [root@hadoop101 software]rpm -ivh MySQL-shared-5.6.24-1.el6.x86_64.rpm 
    [root@hadoop101 software]rpm -ivh MySQL-shared-compat-5.6.24-1.el6.x86_64.rpm
    # 上传 mysql-libs.zip 到 hadoop101 的 /opt/software 目录，并解压文件到当前目录
    [root@hadoop101 software]unzip mysql-libs.zip
    # mysql-libs 文件夹下内容
    [root@hadoop101 mysql-libs]ll
    总用量 76048
    -rw-r--r--. 1 root root 18509960 3月  26 2015 MySQL-client-5.6.24-1.el6.x86_64.rpm
    -rw-r--r--. 1 root root  3575135 12月  1 2013 mysql-connector-java-5.1.27.tar.gz
    -rw-r--r--. 1 root root 55782196 3月  26 2015 MySQL-server-5.6.24-1.el6.x86_64.rpm
    ```

2. 安装Mysql服务端

   ```bash
   [root@hadoop101 mysql-libs]rpm -ivh MySQL-server-5.6.24-1.el6.x86_64.rpm
   # 查看产生的随机密码
   [root@hadoop101 mysql-libs]cat /root/.mysql_secret
   OEXaQuS8IWkG19Xs
   # 查看MySQL状态
   [root@hadoop101 mysql-libs]service mysql status
   # 启动MySQL
   [root@hadoop101 mysql-libs]service mysql start
   ```

3. 安装MySQL客户端

   ```bash
   [root@hadoop101 mysql-libs]rpm -ivh MySQL-client-5.6.24-1.el6.x86_64.rpm
   # 链接MySQL（密码替换成产生的随机密码）
   [root@hadoop101 mysql-libs]mysql -uroot -pOEXaQuS8IWkG19Xs
   # 修改密码
   mysql>SET PASSWORD=PASSWORD('000000');
   # 退出
   mysql>exit
   ```

4. MySQL中user表中主机配置

   目标：配置只要是root用户+密码，在任何主机上都能登录MySQL数据库。

   ```bash
   # 进入MySQL
   [root@hadoop101 mysql-libs]mysql -uroot -p000000
   # 显示数据库
   mysql>show databases;
   # 使用MySQL数据库
   mysql>use mysql;
   # 展示MySQL数据库中的所有表
   mysql>show tables;
   # 展示user表的结构
   mysql>desc user;
   # 查询user表
   mysql>select User, Host, Password from user;
   # 修改user表，把Host表内容修改为%
   mysql>update user set host='%' where host='localhost';
   # 删除root用户的其他host
   mysql>delete from user where host!='%';
   # 刷新
   mysql>flush privileges;
   # 退出
   mysql>quit;
   ```

## CM安装部署

### 集群规划

| 节点       | hadoop101         | hadoop102 | hadoop103 | Hadoop104 | Hadoop105 |
| :------------: | :-----------------------------------: | :---------------------------: | :---------------------------: | :-----------------: | :-----------------: |
| 服务 | cloudera-scm-server、cloudera-scm-agent | cloudera-scm-agent            | cloudera-scm-agent            | cloudera-scm-agent  | cloudera-scm-agent  |

### MySQL中建库

```sql
-- 在MySQL中创建各组件需要的数据库
GRANT ALL ON scm.* TO 'scm'@'%' IDENTIFIED BY 'scm';

CREATE DATABASE scm DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE hive DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
CREATE DATABASE oozie DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
CREATE DATABASE hue DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
CREATE DATABASE sentry DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
```

### CM安装

```bash
# 将 mysql-connector-java-5.1.27-bin.jar 拷贝到/usr/share/java路径下，并重命名
[root@hadoop101 mysql-libs]tar -zxvf mysql-connector-java-5.1.27.tar.gz 
[root@hadoop101 mysql-libs]cd mysql-connector-java-5.1.27
[root@hadoop101 mysql-connector-java-5.1.27]mv mysql-connector-java-5.1.27-bin.jar mysql-connector-java.jar
[root@hadoop101 mysql-connector-java-5.1.27]mkdir /usr/share/java
[root@hadoop101 mysql-connector-java-5.1.27]cp mysql-connector-java.jar /usr/share/java/
[root@hadoop101 mysql-connector-java-5.1.27]xsync /usr/share/java/
```



## 相关命令

```bash
## 服务
systemctl status cloudera-scm-server
## 代理
systemctl status cloudera-scm-agent
```

