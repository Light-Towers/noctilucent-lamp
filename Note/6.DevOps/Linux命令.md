# Linux 常用命令手册

## 文件与目录操作

### rm - 文件删除与恢复原理

**原理解析**：Linux下的rm命令实际上只是删除了文件的目录项，数据仍然保留在磁盘上，直到被新数据覆盖。恢复需要使用专业工具如extundelete、testdisk等。

**注意事项**：
- 使用rm前务必确认文件重要性
- 重要文件建议先备份再删除
- 生产环境慎用rm -rf命令

### find - 文件查找

```bash
# 基本语法
find [路径] [选项] [操作]

# 常用选项
-name       按文件名查找
-type       按文件类型查找 (f-文件, d-目录)
-size       按文件大小查找
-mtime      按修改时间查找
-maxdepth   最大搜索深度
-exec       对找到的文件执行命令

# 实用示例
# 查找指定目录下的文件
find /data3/ -maxdepth 1 -name "sdx_date_up_*_enterprise.zip"

# 精确路径匹配查找
find /data3/ -maxdepth 3 -wholename "*/company_base/split.json.gz" -type f

# 查找目录并执行命令
find /data0/file2hive/drawer -type d -name "exhibitor" -exec sh -c 'for file in "$0"/*; do echo "File: $file"; done' {} \;
```

### 压缩与解压

```bash
# tar压缩文件夹
tar -zcvf /tmp/test.tar.gz /tmp/test

# tar解压至指定目录
tar -zxvf /data3/bigdata/202410/software_registration.tar.gz -C /tmp

# unzip解压文件至指定目录
unzip xiaowang_stock.zip -d /tmp

# gunzip解压至指定文件
gunzip -c /data3/20231115/company_abnormal/split.json.gz > /tmp/20231115_company_abnormal.json
```

## 系统监控与性能分析

### iostat - 磁盘I/O监控

**安装**：
```bash
yum -y install sysstat  # CentOS/RHEL
apt-get -y install sysstat  # Ubuntu/Debian
```

**命令格式**：
```bash
iostat [参数] [时间间隔] [次数]
```

**常用参数**：
```bash
-c  显示CPU使用情况
-d  显示磁盘使用情况  
-k  以KB为单位显示
-m  以MB为单位显示
-N  显示磁盘阵列(LVM)信息
-p  显示磁盘和分区情况
-x  显示详细信息
```

**示例输出解读**：
```bash
# 每1秒刷新，刷新10次
iostat -k 1 10

# 输出示例：
avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           0.10    0.02    0.08    0.02    0.00   99.79

Device:            tps    kB_read/s    kB_wrtn/s    kB_read    kB_wrtn
vda               0.36         1.20         4.47     334723    1249412
```

**参数含义**：
- `%user`：用户进程消耗CPU比例
- `%nice`：用户进程优先级调整消耗CPU比例  
- `%system`：系统内核消耗CPU比例
- `%iowait`：等待磁盘I/O所消耗CPU比例
- `%idle`：闲置CPU比例
- `tps`：设备每秒传输次数（一次I/O请求）
- `kB_read/s`：每秒读取数据量
- `kB_wrtn/s`：每秒写入数据量

### journalctl - 系统日志查看

```bash
# 查看指定服务的日志
journalctl -u kibana.service

# 指定开始时间查看
journalctl -u kibana.service --since "08:00:00"

# 实时跟踪日志
journalctl -u kibana.service -f

# 查看最近100条日志
journalctl -u kibana.service -n 100

# 按优先级过滤
journalctl -p err  # 只显示错误日志
```

## 进程与会话管理

### screen - 终端多会话管理

**安装**：
```bash
yum -y install screen    # CentOS/RHEL
apt-get -y install screen  # Ubuntu/Debian
```

**常用命令**：
```bash
# 创建新会话
screen -S session_name

# 列出所有会话
screen -ls

# 恢复会话
screen -r session_name

# 强制创建新会话
screen -R
```

**会话内快捷键**：
```bash
Ctrl+a d      # 分离当前会话（后台运行）
Ctrl+a x      # 锁定会话（需要密码解锁）
Ctrl+a z      # 挂起会话（可用fg命令恢复）
Ctrl+d        # 终止当前会话
```

**实用场景**：
- 长时间运行任务防止中断
- 多个任务并行管理
- 远程会话保持

### systemctl - 系统服务管理

```bash
# 查看服务状态
systemctl status service_name

# 启动/停止/重启服务
systemctl start service_name
systemctl stop service_name  
systemctl restart service_name

# 设置开机自启
systemctl enable service_name

# 禁用开机自启
systemctl disable service_name

# 查看是否开机自启
systemctl is-enabled service_name

# 查看所有已启用的服务
systemctl list-unit-files --type=service | grep service_name

# 重新加载服务配置
systemctl daemon-reload
```

## 文本处理

### awk - 强大的文本处理工具

**基本语法**：
```bash
awk 'pattern { action }' filename
```

**常用选项**：
```bash
-F fs   指定字段分隔符（默认空格或制表符）
-v var=value  定义用户变量
```

**实用示例**：
```bash
# 打印所有行
awk '{ print }' filename

# 打印第二列
awk '{ print $2 }' filename

# 使用自定义分隔符
awk -F: '{ print $1 }' filename  # 冒号分隔

# 条件筛选
awk '$1 == "apple" { print $0 }' filename  # 第一列等于"apple"

# 计算总和
awk '{ sum += $1 } END { print sum }' filename  # 第一列总和

# 统计行数
awk 'END { print NR }' filename

# 字段操作
awk '{ print $1, $3 }' filename  # 打印第1和第3列
awk '{ print $NF }' filename     # 打印最后一列
```

## 网络工具

### curl - 命令行HTTP客户端

**常用参数**：
```bash
-X          指定请求方法（GET、POST等）
-H          添加请求头
-d          发送数据（POST请求）
-F          上传文件（multipart/form-data）
-o          将输出保存到文件
-I          查看响应头
-u          基本认证（用户名:密码）
-k          跳过SSL证书验证
-v          显示详细请求过程
```

**使用示例**：
```bash
# GET请求
curl -X GET http://example.com

# POST请求（JSON数据）
curl -X POST -H "Content-Type: application/json" -d '{"key":"value"}' http://example.com/api

# 上传文件
curl -F "file=@/path/to/file" http://example.com/upload

# 下载文件
curl -o output.txt http://example.com/file.txt

# 查看响应头
curl -I http://example.com

# 基本认证
curl -u username:password http://example.com

# 跳过SSL验证
curl -k https://example.com

# 显示详细过程
curl -v http://example.com
```

## OpenSSL 证书管理

### 常用操作

```bash
# 查看版本
openssl version -a

# 生成RSA私钥
openssl genrsa -out server.key 2048
openssl genpkey -algorithm RSA -out server.key -pkeyopt rsa_keygen_bits:2048

# 生成带密码的私钥
openssl genrsa -aes256 -out server_enc.key 2048

# 生成CSR（证书签名请求）
openssl req -new -key server.key -out server.csr -subj "/C=CN/ST=Beijing/L=Beijing/O=ExampleCorp/CN=example.com"

# 生成自签名证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout server.key -out server.crt -subj "/C=CN/ST=Beijing/L=Beijing/O=ExampleCorp/CN=example.com"

# 查看证书内容
openssl x509 -in server.crt -noout -text

# 查看CSR内容  
openssl req -in server.csr -noout -text

# 格式转换（PEM ↔ DER）
openssl x509 -in server.crt -outform der -out server.der
openssl x509 -in server.der -inform der -out server.pem

# 验证证书
openssl verify -CAfile ca_bundle.crt server.crt

# 测试TLS连接
openssl s_client -connect example.com:443 -servername example.com -showcerts
```

### 带SAN的CSR生成

创建配置文件 `san.cnf`：
```ini
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req

[req_distinguished_name]

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = example.com
DNS.2 = www.example.com
IP.1 = 192.0.2.1
```

生成CSR：
```bash
openssl req -new -key server.key -out server.csr -config san.cnf
```

## 实用技巧与最佳实践

### 安全注意事项
1. **慎用rm -rf**：特别是在生产环境中
2. **权限管理**：合理设置文件权限，避免777权限
3. **备份习惯**：重要操作前先备份
4. **日志监控**：定期检查系统日志

### 性能优化建议
1. 使用iostat定期监控磁盘I/O
2. 合理使用screen管理长时间任务
3. 利用awk处理大量文本数据
4. 使用curl进行API测试和调试

### 故障排查
1. 使用journalctl查看系统日志
2. 使用systemctl管理服务状态
3. OpenSSL工具链用于证书问题排查
4. find命令用于文件定位和排查

## 扩展阅读
- [Linux命令手册](https://man7.org/linux/man-pages/)
- [Linux性能优化](https://www.brendangregg.com/linuxperf.html)
- [OpenSSL官方文档](https://www.openssl.org/docs/)
- [Linux命令速查](https://www.commandlinefu.com/)










## TODO
nmcli con show
nmtui
systemctl restart network

watch -n 1 -d nvidia-smi #每隔1秒刷新一次