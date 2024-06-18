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

