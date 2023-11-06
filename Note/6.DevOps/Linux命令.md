# [iostart 命令](https://www.cnblogs.com/brianzhu/p/8550251.html)

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

```
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