# Ambari-2.7.7 编译安装（未成功）

## 开发环境准备：

|     软件     |                版本                |
| :----------: | :--------------------------------: |
|     java     |             1.8.0_201              |
|    maven     |         Apache Maven 3.9.1         |
|  rpm-build   |  rpm-build-4.11.3-48.el7_9.x86_64  |
|   gcc-c++    |               4.8.5                |
|    python    |               2.7.5                |
| python-devel | python-devel-2.7.5-92.el7_9.x86_64 |
|     git      |              1.8.3.1               |
| nodejs + npm |          v10.24.1 + 6.4.1          |

```bash
# 安装 rpm-build
yum install rpm-build -y
# 安装 git
yum install git -y
# 安装 python-devel
yum install python-devel -y
# 安装 g++
yum install gcc-c++ -y
# 安装node
wget https://nodejs.org/dist/v10.24.1/node-v10.24.1-linux-x64.tar.gz
tar -zxvf node-v4.5.0-linux-x64.tar.gz

# 安装 setuptools for python27
wget https://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg#md5=fe1f997bc722265116870bc7919059ea
sh setuptools-0.6c11-py2.7.egg


wget https://dlcdn.apache.org/ambari/ambari-2.7.7/apache-ambari-2.7.7-src.tar.gz --no-check-certificate
```

### maven 配置文件

```xml
    <mirror>
        <id>alicentral</id>
        <name>aliyun maven central</name>
        <url>https://maven.aliyun.com/repository/central</url>
        <mirrorOf>central,apache.snapshots.https,maven2-repository.dev.java.net,maven2-glassfish-repository.dev.java.net,maven2-repository.atlassian,apache.staging.https,oss.sonatype.org,spring-milestones</mirrorOf>
    </mirror>
    <mirror>
        <id>alipublic</id>
        <name>aliyun maven public</name>
        <url>https://maven.aliyun.com/repository/public</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
    <mirror>
        <id>nexus-hortonworks</id>
        <name>Nexus hortonworks</name>
        <url>https://repo.hortonworks.com/content/groups/public/</url>
        <mirrorOf>central</mirrorOf> 
    </mirror>
    <mirror>
        <id>HDPReleases</id>
        <name>HDP Releases</name>
        <url>https://repo.hortonworks.com/content/repositories/releases/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
```



## 配置全局代理（科学上网）

```bash
sudo vim /etc/profile.d/my_env.sh
# Proxy
export proxy="http://192.168.1.18:7890"
export http_proxy=$proxy
export https_proxy=$proxy
export ftp_proxy=$proxy
export no_proxy="localhost, 127.0.0.1, ::1"
# 配置生效
source /etc/profile
```

配置镜像加速

npm config set registry https://registry.npm.taobao.org





![image-20230715222144478](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image-20230715222144478.png)