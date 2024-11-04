## **一、编译Flink**

## **1 下载flink源码**

```bash
git clone https://github.com/apache/flink.git
git checkout release-1.17.1
# 或 flink官网：https://flink.apache.org/zh/downloads/  下载Apache Flink 1.17.1 -(Source)
```

## **2 增加maven镜像**

在maven的setting.xml文件的mirrors标签中增加如下mirror 

```xml
<mirrors>
    <mirror>
        <id>alimaven</id>
        <name>aliyun maven</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
    <mirror>
        <id>central</id>
        <name>Maven Repository Switchboard</name>
        <url>http://repo1.maven.org/maven2/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
    <mirror>
        <id>repo2</id>
        <mirrorOf>central</mirrorOf>
        <name>Human Readable Name for this Mirror.</name>
        <url>http://repo2.maven.org/maven2/</url>
    </mirror>
    <mirror>
        <id>ibiblio</id>
        <mirrorOf>central</mirrorOf>
        <name>Human Readable Name for this Mirror.</name>
        <url>http://mirrors.ibiblio.org/pub/mirrors/maven2/</url>
    </mirror>
    <mirror>
        <id>google-maven-central</id>
        <name>Google Maven Central</name>
        <url>https://maven-central.storage.googleapis.com</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
    <mirror>
        <id>maven.net.cn</id>
        <name>oneof the central mirrors in china</name>
        <url>http://maven.net.cn/content/groups/public/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
</mirrors>
```



## **3 执行编译命令**

```bash
# 进入flink源码根目录
cd /opt/software/flink/flink-1.17.1/
# 执行打包命令：
mvn clean install -DskipTests -Dfast -Drat.skip=true -Dhaoop.version=3.0.0-cdh6.3.2 -Pvendor-repos -Dinclude-hadoop -Dscala-2.12 -T2C
#等待15分钟编译成功。
#编译完成的结果就是flink/flink-dist/target/flink-1.17.1-bin目录下的flink-1.17.1文件夹，接下来把flink-1.17.1打包成tar包

# 进入打包结果目录
cd /opt/software/flink/flink-dist/target/flink-1.17.1-bin
# 执行打包命令, 得到了CDH6.3.2、Scala2.12的flink安装包
tar -zcf flink-1.17.1-bin-scala_2.12.tgz flink-1.17.1
```

## **二、编译parcel**

这里编译parcel使用flink-parcel工具

## **1 下载flink-parcel**

```bash
cd /opt/os_ws/
# 克隆源码
#如果下载不下来直接使用：git clone https://gitclone.com/github.com/YUjichang/flink-parcel.git
git clone https://github.com/pkeropen/flink-parcel.git
cd flink-parcel
```

## **2 修改参数**

vim flink-parcel.properties #FLINK 下载地址

```bash
FLINK_URL=https://archive.apache.org/dist/flink/flink-1.17.1/flink-1.17.1-bin-scala_2.12.tgz  #flink版本号
FLINK_VERSION=1.17.1 #扩展版本号
EXTENS_VERSION=BIN-SCALA_2.12#操作系统版本，以centos7为例
OS_VERSION=7 #CDH 小版本
CDH_MIN_FULL=5.15
CDH_MAX_FULL=6.3.2#CDH大版本
CDH_MIN=5
CDH_MAX=6
```

## **3 复制安装包**

**这里把之前编译打包好的flink的tar包上复制到flink-parcel项目的根目录**。flink-parcel在制作parcel时如果根目录没有flink包会从配置文件里的地址下载flink的tar包到项目根目录。如果根目录已存在安装包则会跳过下载，使用已有tar包。**注意：这里一定要用自己编译的包，不要用从链接下载的包！！！**

\# 复制安装包，根据自己项目的目录修改cp /opt/software/flink/flink-dist/target/flink-1.12.0-bin/flink-1.12.0-bin-scala_2.11.tgz /opt/software/flink-parcel

或者如果使用flink源码编译flink-1.12.0-bin-scala_2.11.tgz 不成功，直接在目录/opt/os_ws/flink-parcel

执行命令：wget https://archive.apache.org/dist/flink/flink-1.17.1/flink-1.17.1-bin-scala_2.12.tgz **亲测可以使用。**

## **4 编译parcel**

```bash
# 赋予执行权限
chmod +x ./build.sh
# 执行编译脚本
./build.sh parcel
```

编译失败：原因：找不到start-scala-shell.sh

![image2023-8-28_11-58-8](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image2023-8-28_11-58-8.png)

解决:

从flink源码包中找到 start-scala-shell.sh

复制到：

[root@master01 flink-parcel]# cp /opt/cloudera/parcels/FLINK-1.15.4-BIN-SCALA_2.12/lib/flink/bin/start-scala-shell.sh ./FLINK-1.17.1-BIN-SCALA_2.12/lib/flink/bin/

结果：

成功编译成 *FLINK-1.17.1-BIN-SCALA_2.12_build* 文件夹

![image2023-8-28_11-58-44](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image2023-8-28_11-58-44.png)

 

编译完会在flink-parcel项目根目录下生成FLINK-1.17.1-BIN-SCALA_2.12_build文件夹

## **5 编译csd**

```bash
# 编译standlone版本
./build.sh  csd_standalone
# 编译flink on yarn版本
./build.sh  csd_on_yarn
#个人测试执行：
[root@master01 flink-parcel] ./build.sh csd
```

![image2023-8-28_11-59-10](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image2023-8-28_11-59-10.png)

编译完成后在flink-parcel项目根目录下会生成2个jar包，FLINK-1.17.1.jar和FLINK_ON_YARN-1.17.1.jar

## **6 上传文件**

将编译parcel后生成的FLINK-1.17.1-BIN-SCALA_2.12_build文件夹内的3个文件复制到CDH Server所在节点的/opt/cloudera/parcel-repo目录。将编译csd生成后的FLINK_ON_YARN-1.17.1.jar复制到CDH Server所在节点的/opt/cloudera/csd目录（这里因为资源隔离的优势，选择部署flink on yarn模式）

```bash
cp FLINK-1.17.1-BIN-SCALA_2.12_build/* /opt/cloudera/parcel-repo/
cp FLINK_ON_YARN-1.17.1.jar /opt/cloudera/csd/

# 复制parcel，这里就是在主节点编译的，如果非主节点，可以scp过去cp 
scp * root@[manager01:/opt/cloudera/parcel-repo/](http://manager01/opt/cloudera/parcel-repo/)
scp * root@[master01:/opt/cloudera/parcel-repo/](http://master01/opt/cloudera/parcel-repo/)
scp * root@[master02:/opt/cloudera/parcel-repo/](http://master02/opt/cloudera/parcel-repo/)
scp * root@[master03:/opt/cloudera/parcel-repo/](http://master03/opt/cloudera/parcel-repo/)

cp FLINK_ON_YARN-1.17.1.jar /opt/cloudera/csd/
scp FLINK_ON_YARN-1.17.1.jar root@[manager01:/opt/cloudera/csd/](http://manager01/opt/cloudera/csd/)
scp FLINK_ON_YARN-1.17.1.jar root@[master01:/opt/cloudera/csd/](http://master01/opt/cloudera/csd/)
scp FLINK_ON_YARN-1.17.1.jar root@[master02:/opt/cloudera/csd/](http://master02/opt/cloudera/csd/)
scp FLINK_ON_YARN-1.17.1.jar root@[master03:/opt/cloudera/csd/](http://master03/opt/cloudera/csd/)
```

 

**重启CDH server和agent**

```bash
# 重启server（仅server节点执行）
systemctl stop cloudera-scm-server
systemctl start cloudera-scm-server
# 重启agent（所有agent节点都执行）
systemctl stop cloudera-scm-agent
systemctl start cloudera-scm-agent
```

 

## **CM操作步骤：**

 ![image-20241104091628994](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image-20241104091628994.png)
