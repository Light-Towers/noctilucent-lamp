# [原理解析：linux下rm删除与恢复](https://www.sklinux.com/posts/devops/rm%E6%96%87%E4%BB%B6%E5%88%A0%E9%99%A4%E4%B8%8E%E6%81%A2%E5%A4%8D/)



# [iostart](https://www.cnblogs.com/brianzhu/p/8550251.html)

Linux系统中的 iostat命令可以对系统的磁盘IO和CPU使用情况进行监控。iostat属于sysstat软件包，可以用yum -y install sysstat 直接安装。

格式：

```
iostat[参数][时间][次数]
```

命令格式：

```
通过iostat命令可以查看CPU、网卡、tty设备、磁盘、CD-ROM 等等设备的活动情况, 负载信息等，在这里只说明cpu和磁盘io的使用说明
```

参数：

```
-c 显示CPU使用情况

-d 显示磁盘使用情况

-k 以 KB 为单位显示

-m 以 M 为单位显示

-N 显示磁盘阵列(LVM) 信息

-n 显示NFS 使用情况

-p[磁盘] 显示磁盘和分区的情况

-t 显示终端和CPU的信息

-x 显示详细信息

-V 显示版本信息
```

实例：

```bash
[root@BrianZhu test]# iostat -k 或者 -m 1 10  # 查看磁盘i/o每一秒刷新1次 刷新10次
Linux 3.10.0-693.11.1.el7.x86_64 (BrianZhu)     03/12/2018     _x86_64_    (1 CPU)

avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           0.10    0.02    0.08    0.02    0.00   99.79

Device:            tps    kB_read/s    kB_wrtn/s    kB_read    kB_wrtn
vda               0.36         1.20         4.47     334723    1249412
dm-0              0.01         0.10         0.18      27264      50672
dm-1              0.00         0.05         0.01      13024       3137
```

 参数解读：

```
%user：用户进程消耗cpu的比例

%nice：用户进程优先级调整消耗的cpu比例

%sys：系统内核消耗的cpu比例

%iowait：等待磁盘io所消耗的cpu比例

%idle：闲置cpu的比例（不包括等待磁盘io的s）

 

tps：该设备每秒的传输次数。“一次传输”意思是“一次I/O请求”。多个逻辑请求被合并为“一次I/O请求”。“一次传输”请求的大小是未知的。

kB_read/s：每秒从设备（drive expressed）读取的数据量

kB_wrtn/s：每秒向设备（drive expressed）写入的数据量

kB_read：读取的总数据量

kB_wrtn：写入的总数量数据量

这些单位都为Kilobytes。
```



# screen

### 安装：

```bash
# centos
yum -y install screen
# unbuntu
apt-get -y install screen
```

### 参数：

```bash
-A 　将所有的视窗都调整为目前终端机的大小。
-d     <作业名称> 　将指定的screen作业离线。
-h     <行数> 　指定视窗的缓冲区行数。
-m 　即使目前已在作业中的screen作业，仍强制建立新的screen作业。
-r      <作业名称> 　恢复离线的screen作业。
-R 　先试图恢复离线的作业。若找不到离线的作业，即建立新的screen作业。
-s 　指定建立新视窗时，所要执行的shell。
-S    <作业名称> 　指定screen作业的名称。
-v 　显示版本信息。
-x 　恢复之前离线的screen作业。
-ls或--list 　显示目前所有的screen作业。
-wipe 　检查目前所有的screen作业，并删除已经无法使用的screen作业。
```

**在每个screen session 下，命令都以 ctrl+a、ctrl-a，常用的几个操作如下：**

```
ctrl-a x   # 锁住当前的shell window，需用用户密码解锁
ctrl-a d   # detach，暂时离开当前session，将当前 screen session 转到后台执行，并会返回没进 screen 时的状态，此时在 screen session 里，每个shell client内运行的 process (无论是前台/后台)都在继续执行，即使 logout 也不影响
ctrl-a z   # 把当前session放到后台执行，用 shell 的 fg 命令则可回去。
```

### 中止 screen 会话

有几种方法来中止 screen 会话。你可以按下 Ctrl+d ，或者在命令行中使用 exit 命令。





# journalctl

```bash
# 指定开始时间
journalctl -u kibana.service --since "08:00:00"
# 实时
journalctl -u kibana.service -f
```


# 压缩
```bash
# tar压缩文件夹
tar -zcvf /tmp/test.tar.gz /tmp/test
# tar解压至指定目录
tar -zxvf /data3/bigdata/202410/software_registration.tar.gz -C /tmp

# unzip解压文件至指定目录
unzip xiaowang_stock.zip -d /tmp

# gunzip解压至指定目录文件
gunzip -c /data3/20231115/company_abnormal/split.json.gz > /tmp/20231115_company_abnormal.json

```

# find
```bash
# -maxdepth 最大搜索深度
# -type 查找类型
# 指定目录，模糊匹配查找文件
find /data3/ -maxdepth 1 -name "sdx_date_up_*_enterprise.zip"
# 指定目录，精确匹配查找文件
find /data3/ -maxdepth 3 -wholename "*/company_base/split.json.gz" -type f
# 查找目录下所有文件夹，并执行命令：打印文件名绝对路径
find /data0/file2hive/drawer -type d -name "exhibitor" -exec sh -c 'for file in "$0"/*; do echo "File: $file"; done' {} \;
```

# awk
awk 是一种强大的文本处理工具，适用于在 Linux/Unix 系统中进行数据提取和报告生成。
它可以读取文件或标准输入（stdin），并根据指定的模式对每一行进行处理。
```bash
awk 'pattern { action }' filename
```
* pattern：可选，用于选择特定的行。如果省略，则默认对所有行执行操作。
* action：对匹配的行执行的操作，默认是打印整行。
## 常用选项
* -F fs：指定字段分隔符 fs，默认为空格或制表符。
* -v var=value：定义用户变量。
* --field-separator fs：与 -F 相同，指定字段分隔符。

示例
```bash
#打印文件中的所有行
awk '{ print }' filename
#打印第二列
awk '{ print $2 }' filename
#使用自定义分隔符
awk -F: '{ print $1 }' filename  # 使用冒号作为分隔符
#根据条件筛选
awk '$1 == "apple" { print $0 }' filename  # 打印第一列等于 "apple" 的行
#计算总和
awk '{ sum += $1 } END { print sum }' filename  # 计算第一列的总和
```


# systemctl
设置开机自启
```bash
systemctl enable docker.service
## 查看已启用的服务
systemctl list-unit-files | grep enable
```