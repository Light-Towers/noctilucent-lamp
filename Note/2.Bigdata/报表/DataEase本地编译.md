DataEase V2.5 本地编译

## 安装JDK17

```shell
sudo yum install java-17-openjdk-devel
```

## 安装Maven

## 安装Node.js
```shell
sudo yum install nodejs
```

## 安装Git
```shell
sudo yum install git
```

## 克隆项目
```shell
git clone https://github.com/dataease/dataease.git
```

## 配置环境变量
```shell
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

## 编译项目
```shell
cd dataease
mvn clean install

cd core
mvn clean package -Pstandalone -U -Dmaven.test.skip=true
```

## windows遇到的问题
1. 'NODE_OPTIONS' 不是内部或外部命令，也不是可运行的程序或批处理文件。运行 
```shell
npm install -g win-node-env
```
2. Delete `␍`eslint(prettier/prettier) 错误的解决方案
```shell
git config --global core.autocrlf false
```
3. Apache Calcite 无法下载
使用Maven的初始化的settings.xml配置文件

## 创建数据库
```shell
mysql -uroot -p
create database dataease;
```

windows 下修改代码
```yaml
#修改application.yml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
datasource:
  url: jdbc:mysql://192.168.100.21:33306/dataeaseV2demo?autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
  username: root
  password: mingyang100100

#修改application-standalone.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dataease?autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456

#修改CalciteProvider.java
#    private final String FILE_PATH = "/opt/dataease2.0/drivers";
    private final String FILE_PATH = "D:\\Study\\github\\dataease\\drivers";
```

## 启动项目
```shell
# 后端服务启动
cd dataease-standalone/target
java -jar dataease-standalone-1.0.0.jar

# 前端服务启动
cd dataease-frontend
npm install
npm run dev
```