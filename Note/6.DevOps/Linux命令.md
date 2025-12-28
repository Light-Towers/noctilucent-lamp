# Linux 常用命令手册

## 一、文件与目录管理

### 1.1 基础文件操作

#### rm - 文件删除与恢复原理

**原理解析**：Linux下的rm命令实际上只是删除了文件的目录项，数据仍然保留在磁盘上，直到被新数据覆盖。恢复需要使用专业工具如extundelete、testdisk等。

**安全删除补充**：
- **shred命令**：用于安全删除文件，通过多次覆盖数据确保无法恢复
  ```bash
  shred -vzn 3 file.txt  # 3次覆盖后删除，-v显示进度，-z最后用0覆盖
  shred -u file.txt      # 覆盖后删除文件
  ```

**注意事项**：
- 使用rm前务必确认文件重要性
- 重要文件建议先备份再删除
- 生产环境慎用rm -rf命令
- 敏感文件使用shred进行安全删除

#### cp - 复制文件

**原理解析**：`cp` 命令用于复制文件或目录。

**常用选项**：
```bash
-r       递归复制目录
-p       保留文件属性（权限、时间戳等）
-a       归档模式（等同于-dpR）
-v       显示详细过程
-i       交互模式（覆盖前询问）
-u       只复制源文件中更新的文件
```

**实用示例**：
```bash
# 复制文件
cp file1.txt file2.txt

# 复制目录
cp -r dir1/ dir2/

# 保留文件属性复制
cp -p file1.txt file2.txt

# 归档模式复制
cp -a source/ destination/

# 交互式复制
cp -i file1.txt file2.txt
```

#### mv - 移动/重命名文件

**原理解析**：`mv` 命令用于移动或重命名文件/目录。

**常用选项**：
```bash
-v       显示详细过程
-i       交互模式（覆盖前询问）
-u       只移动更新的文件
```

**实用示例**：
```bash
# 重命名文件
mv oldname.txt newname.txt

# 移动文件到目录
mv file.txt dir/

# 移动多个文件到目录
mv file1.txt file2.txt dir/
```

#### mkdir - 创建目录

**原理解析**：`mkdir` 命令用于创建目录。

**常用选项**：
```bash
-p       创建父目录（如果不存在）
-v       显示详细过程
-m mode  设置目录权限
```

**实用示例**：
```bash
# 创建多级目录
mkdir -p dir1/dir2/dir3

# 创建目录并设置权限
mkdir -m 755 newdir
```

#### touch - 创建空文件或更新时间戳

**原理解析**：`touch` 命令用于创建空文件或更新文件的时间戳。

**常用选项**：
```bash
-a       只更改访问时间
-m       只更改修改时间
-c       不创建文件（如果文件不存在）
-t time  使用指定时间戳
```

**实用示例**：
```bash
# 创建空文件
touch file.txt

# 更新文件时间戳为当前时间
touch file.txt

# 指定时间戳
touch -t 202412231200.00 file.txt
```

#### vim - 文本编辑器

**原理解析**：`vim` 是Linux下功能强大的文本编辑器，是vi编辑器的增强版。

**常用模式**：
- **普通模式**：移动光标、删除、复制等操作
- **插入模式**：输入文本
- **命令模式**：执行保存、退出等命令

**基本操作**：
```bash
vim filename          # 打开文件
vim +10 filename      # 打开并定位到第10行
vim -o file1 file2    # 水平分割打开多个文件
vim -O file1 file2    # 垂直分割打开多个文件
```

**常用命令**：
```bash
i        进入插入模式
ESC      退出插入模式，返回普通模式
:w       保存文件
:q       退出vim
:wq      保存并退出
:q!      强制退出不保存
dd       删除当前行
yy       复制当前行
p        粘贴
u        撤销操作
/search  向前搜索
?search  向后搜索
```

### 1.2 文件查找与压缩

#### find - 文件查找

**基本语法**：
```bash
find [路径] [选项] [操作]
```

**常用选项**：
```bash
-name       按文件名查找
-type       按文件类型查找 (f-文件, d-目录)
-size       按文件大小查找
-mtime      按修改时间查找
-maxdepth   最大搜索深度
-exec       对找到的文件执行命令
```

**实用示例**：
```bash
# 查找指定目录下的文件
find /data3/ -maxdepth 1 -name "sdx_date_up_*_enterprise.zip"

# 精确路径匹配查找
find /data3/ -maxdepth 3 -wholename "*/company_base/split.json.gz" -type f

# 查找目录并执行命令
find /data0/file2hive/drawer -type d -name "exhibitor" -exec sh -c 'for file in "$0"/*; do echo "File: $file"; done' {} \;
```

#### 压缩与解压

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

### 1.3 文件权限与属性

#### chmod - 更改文件权限

**原理解析**：`chmod` 命令用于更改文件或目录的权限。

**权限表示法**：
- 数字表示法：755 (rwxr-xr-x)
- 符号表示法：u=rwx,g=rx,o=rx

**常用示例**：
```bash
# 数字表示法
chmod 755 file.txt        # rwxr-xr-x
chmod 644 file.txt        # rw-r--r--
chmod 600 file.txt        # rw-------

# 符号表示法
chmod u+x file.txt        # 给所有者添加执行权限
chmod g-w file.txt        # 移除组的写权限
chmod a+x file.txt        # 给所有人添加执行权限

# 递归更改目录权限
chmod -R 755 directory/
```

#### chown - 更改文件所有者

**原理解析**：`chown` 命令用于更改文件或目录的所有者和所属组。

**基本语法**：
```bash
chown [用户]:[组] 文件
```

**实用示例**：
```bash
# 更改文件所有者
chown user1 file.txt

# 更改文件所有者和组
chown user1:group1 file.txt

# 只更改文件所属组
chown :group1 file.txt

# 递归更改目录所有者
chown -R user1:group1 directory/
```

#### chgrp - 更改文件所属组

**原理解析**：`chgrp` 命令专门用于更改文件或目录的所属组。

**实用示例**：
```bash
# 更改文件所属组
chgrp group1 file.txt

# 递归更改目录所属组
chgrp -R group1 directory/
```

## 二、文本处理与编辑

### 2.1 文本处理三剑客

#### grep - 文本搜索工具

**原理解析**：`grep` 是Linux中最强大的文本搜索工具，使用正则表达式搜索文本，并将匹配的行打印出来。

**基本语法**：
```bash
grep [选项] 模式 [文件...]
```

**常用选项**：
```bash
-i       忽略大小写
-v       反向匹配（显示不包含模式的行）
-n       显示行号
-r       递归搜索目录
-l       只显示包含匹配模式的文件名
-c       显示匹配的行数
-E       使用扩展正则表达式（等同于egrep）
-A n     显示匹配行及其后n行
-B n     显示匹配行及其前n行
-C n     显示匹配行及其前后各n行
```

**实用示例**：
```bash
# 在文件中搜索关键词
grep "error" /var/log/syslog

# 忽略大小写搜索
grep -i "ERROR" /var/log/syslog

# 显示行号
grep -n "error" file.txt

# 递归搜索目录
grep -r "function" /home/user/src/
```

#### sed - 流编辑器

**原理解析**：`sed` 是一个强大的流编辑器，用于对输入流（文件或管道）进行基本的文本转换。

**基本语法**：
```bash
sed [选项] '命令' [输入文件]
```

**常用命令**：
```bash
s/正则/替换/     # 替换操作
p                # 打印行
d                # 删除行
a\text           # 在行后追加文本
i\text           # 在行前插入文本
```

**实用示例**：
```bash
# 替换文件中的文本
sed 's/old/new/g' file.txt

# 删除包含pattern的行
sed '/pattern/d' file.txt

# 打印第5-10行
sed -n '5,10p' file.txt

# 替换并保存到原文件
sed -i 's/old/new/g' file.txt
```

#### awk - 强大的文本处理工具

**原理解析**：`awk` 是一个强大的文本分析工具，特别适合处理结构化文本数据。

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

# 计算总和
awk '{ sum += $1 } END { print sum }' filename  # 第一列总和

# 条件筛选
awk '$1 == "apple" { print $0 }' filename  # 第一列等于"apple"

# 统计行数
awk 'END { print NR }' filename

# 格式化输出
awk '{printf "%-10s %-10d\n", $1, $2}' data.txt
```

## 三、系统管理与监控

### 3.1 系统监控工具

#### top - 实时系统监控

**原理解析**：`top` 命令提供实时的系统状态监控，显示系统摘要信息和进程列表。

**常用快捷键**：
```bash
1       显示所有CPU核心的详细统计
P       按CPU使用率排序
M       按内存使用率排序
N       按进程ID排序
T       按运行时间排序
k       杀死进程（输入进程ID）
q       退出top
```

**实用示例**：
```bash
# 启动top
top

# 批处理模式（输出一次）
top -b -n 1

# 指定刷新间隔
top -d 5
```

#### htop - 增强型系统监控

**安装**：
```bash
yum -y install htop        # CentOS/RHEL
apt-get -y install htop    # Ubuntu/Debian
```

**特点**：
- 彩色显示
- 鼠标支持
- 垂直和水平滚动
- 更好的进程管理界面

#### ps - 进程状态查看

**原理解析**：`ps` 命令显示当前系统的进程状态。

**常用参数组合**：
```bash
ps aux        # 显示所有进程详细信息
ps -ef        # 显示完整格式的进程信息
ps aux --sort=-%cpu    # 按CPU使用率降序排序
ps aux --sort=-%mem    # 按内存使用率降序排序
ps -p [PID] -o pid,ppid,cmd,%cpu,%mem    # 显示指定进程的详细信息
```

**实用示例**：
```bash
# 查看所有进程
ps aux

# 查找特定进程
ps aux | grep nginx

# 查看进程树
ps auxf

# 查看进程的完整命令行
ps -ef | grep java
```

#### free - 内存使用情况

**原理解析**：`free` 命令显示系统的内存使用情况。

**常用选项**：
```bash
-h       人类可读格式（自动转换单位）
-m       以MB为单位显示
-g       以GB为单位显示
-s n     每n秒刷新一次
-t       显示总计行
```

**实用示例**：
```bash
# 以人类可读格式显示
free -h

# 每5秒刷新一次
free -s 5

# 显示内存使用趋势
watch -n 1 free -h
```

#### iostat - 磁盘I/O监控

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

**实际应用场景**：
```bash
# 监控磁盘I/O性能瓶颈
iostat -x 1  # 每秒刷新一次详细统计

# 监控特定磁盘
iostat -p sda 1  # 监控sda磁盘

# 查看历史统计（需安装sysstat）
sar -d  # 查看磁盘历史统计
```

#### lsof - 列出打开的文件

**原理解析**：`lsof` 命令用于列出系统当前打开的文件，包括普通文件、目录、网络连接等。

**常用选项**：
```bash
-i       列出网络连接
-p pid   列出指定进程打开的文件
-u user  列出指定用户打开的文件
-c cmd   列出指定命令打开的文件
```

**实用示例**：
```bash
# 列出所有打开的文件
lsof

# 列出网络连接
lsof -i

# 列出指定端口的使用情况
lsof -i :80

# 列出指定进程打开的文件
lsof -p 1234

# 列出指定用户打开的文件
lsof -u username

# 查看谁在使用某个文件
lsof /var/log/syslog
```

### 3.2 系统日志与进程管理

#### journalctl - 系统日志查看

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

#### systemctl - 系统服务管理

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

# 重新加载服务配置
systemctl daemon-reload
```

#### screen/tmux - 终端多会话管理

**screen安装**：
```bash
yum -y install screen    # CentOS/RHEL
apt-get -y install screen  # Ubuntu/Debian
```

**tmux安装**：
```bash
yum -y install tmux      # CentOS/RHEL
apt-get -y install tmux  # Ubuntu/Debian
```

**screen常用命令**：
```bash
screen -S session_name  # 创建新会话
screen -ls              # 列出所有会话
screen -r session_name  # 恢复会话
screen -R               # 强制创建新会话
```

**screen会话内快捷键**：
```bash
Ctrl+a d      # 分离当前会话（后台运行）
Ctrl+a x      # 锁定会话（需要密码解锁）
Ctrl+a z      # 挂起会话（可用fg命令恢复）
Ctrl+d        # 终止当前会话
```

**tmux推荐说明**：
- **tmux优势**：更现代、功能更强大、支持面板分割、更好的配置管理
- **基本使用**：
  ```bash
  tmux new -s session_name  # 创建新会话
  tmux ls                   # 列出所有会话
  tmux attach -t session_name  # 附加到会话
  tmux kill-session -t session_name  # 结束会话
  ```
- **快捷键**：
  ```bash
  Ctrl+b %        # 垂直分割
  Ctrl+b "        # 水平分割
  Ctrl+b arrow    # 切换面板
  Ctrl+b d        # 分离会话
  ```

#### watch - 周期性执行命令

**原理解析**：`watch` 命令可以反复执行指定的命令，并全屏显示输出结果。它非常适合监控那些输出内容不断变化的命令。

**常用参数**：
```bash
-n       指定刷新间隔（秒），默认为2秒
-d       高亮显示两次输出之间有差异的部分
-t       不显示顶部的间隔时间等信息
```

**实用示例**：
```bash
# 每隔1秒刷新一次NVIDIA显卡状态
watch -n 1 -d nvidia-smi

# 监控磁盘空间变化
watch -d df -h

# 监控当前目录下的文件生成情况
watch -n 5 ls -l

# 杀掉所有显存占用进程
sudo fuser -v /dev/nvidia* -k
```

### 3.3 用户与组管理

#### useradd - 添加用户

**原理解析**：`useradd` 命令用于创建新用户账户。

**常用选项**：
```bash
-m       创建用户主目录
-s shell  指定用户登录shell
-g group  指定主组
-G groups 指定附加组
-d dir    指定主目录路径
-u uid    指定用户ID
-c comment 添加用户注释
```

**实用示例**：
```bash
# 创建用户并创建主目录
useradd -m username

# 创建用户并指定shell
useradd -m -s /bin/bash username

# 创建用户并指定多个附加组
useradd -m -G wheel,developers username
```

#### usermod - 修改用户属性

**原理解析**：`usermod` 命令用于修改现有用户账户的属性。

**常用选项**：
```bash
-s shell  修改登录shell
-g group  修改主组
-G groups 修改附加组
-aG group 添加附加组（不覆盖现有组）
-d dir    修改主目录
-m        移动主目录内容到新位置
-L        锁定用户账户
-U        解锁用户账户
```

**实用示例**：
```bash
# 修改用户shell
usermod -s /bin/zsh username

# 添加附加组
usermod -aG wheel username

# 锁定用户账户
usermod -L username
```

#### groupadd - 添加组

**原理解析**：`groupadd` 命令用于创建新用户组。

**常用选项**：
```bash
-g gid    指定组ID
-r        创建系统组
```

**实用示例**：
```bash
# 创建普通组
groupadd developers

# 创建组并指定GID
groupadd -g 1001 developers

# 创建系统组
groupadd -r systemgroup
```

#### passwd - 修改用户密码

**原理解析**：`passwd` 命令用于修改用户密码。

**实用示例**：
```bash
# 修改当前用户密码
passwd

# 修改指定用户密码（需要root权限）
passwd username

# 锁定用户密码
passwd -l username

# 查看密码状态
passwd -S username
```

## 四、网络管理与诊断

### 4.1 网络配置管理

#### nmcli - 网络配置命令行工具

**原理解析**：`nmcli` 是 NetworkManager 的命令行界面。它直接与 NetworkManager 守护进程通信，用于创建、显示、编辑、删除、激活和停用网络连接。

**详细说明**：
- **CentOS 7+/Ubuntu 18.04+**：推荐使用nmcli
- **旧系统**：可能使用ifconfig、route等传统工具

**常用命令**：
```bash
# 查看所有网络连接
nmcli connection show

# 查看活动的网络接口状态
nmcli device status

# 激活/禁用指定连接
nmcli connection up <conn_name>
nmcli connection down <conn_name>

# 修改静态IP示例（更详细）
nmcli connection modify eth0 \
  ipv4.addresses 192.168.1.10/24 \
  ipv4.gateway 192.168.1.1 \
  ipv4.dns "8.8.8.8 8.8.4.4" \
  ipv4.method manual

# 立即生效修改
nmcli connection up eth0

# 添加新的网络连接
nmcli connection add type ethernet con-name eth0-static ifname eth0
```

#### nmtui - 文本图形化网络配置

**命令说明**：`nmtui` 提供了一个基于终端的图形界面（Curses界面），适合不熟悉`nmcli`复杂语法的用户。

- **使用方式**：直接输入`nmtui`进入主菜单
- **功能选项**：
  - **Edit a connection**：修改IP、网关、DNS等
  - **Activate a connection**：快速启用或禁用网卡
  - **Set system hostname**：修改系统主机名

**网络服务管理**：
```bash
# 重启网络管理服务
systemctl restart NetworkManager

# 查看网络服务状态
systemctl status NetworkManager

# 传统网络服务（旧版本）
systemctl restart network  # CentOS 6及以下
```

### 4.2 网络诊断工具

#### ping - 网络连通性测试

**原理解析**：`ping` 使用ICMP协议测试主机之间的网络连通性。

**常用选项**：
```bash
-c count    发送指定数量的数据包
-i interval  设置数据包发送间隔（秒）
-s packetsize  设置数据包大小（字节）
-t ttl       设置TTL（生存时间）值
-W timeout   设置等待响应的超时时间（秒）
```

**实用示例**：
```bash
# 测试到目标的连通性
ping google.com

# 发送5个数据包
ping -c 5 google.com

# 设置数据包大小
ping -s 1000 google.com
```

#### traceroute - 路由跟踪

**原理解析**：`traceroute` 显示数据包到达目标主机所经过的路由路径。

**安装**：
```bash
yum -y install traceroute    # CentOS/RHEL
apt-get -y install traceroute  # Ubuntu/Debian
```

**常用选项**：
```bash
-n       不解析主机名（显示IP地址）
-m maxhops  设置最大跳数
-w waittime  设置等待时间（秒）
```

**实用示例**：
```bash
# 跟踪到目标的路由
traceroute google.com

# 不解析主机名
traceroute -n google.com

# 设置最大跳数
traceroute -m 30 google.com
```

#### netstat/ss - 网络连接统计

**原理解析**：`netstat`（传统）和`ss`（现代）用于显示网络连接、路由表、接口统计等信息。

**netstat常用选项**：
```bash
-a       显示所有连接和监听端口
-t       显示TCP连接
-u       显示UDP连接
-n       以数字形式显示地址和端口
-p       显示进程ID和程序名
-l       仅显示监听端口
-r       显示路由表
```

**ss常用选项**：
```bash
-t       显示TCP连接
-u       显示UDP连接
-n       不解析服务名称
-a       显示所有套接字
-l       显示监听套接字
-p       显示进程信息
-s       显示摘要统计
```

**实用示例**：
```bash
# 显示所有连接
netstat -an

# 显示TCP连接和进程信息
netstat -tunp

# 使用ss（更快的替代）
ss -tunp

# 显示网络统计摘要
ss -s
```

#### nslookup/dig - DNS查询工具

**原理解析**：用于查询DNS记录的诊断工具。

**nslookup示例**：
```bash
# 查询域名
nslookup google.com

# 查询指定DNS服务器
nslookup google.com 8.8.8.8

# 查询MX记录
nslookup -type=mx google.com
```

**dig示例**：
```bash
# 查询A记录
dig google.com

# 查询指定DNS服务器
dig @8.8.8.8 google.com

# 查询MX记录
dig google.com MX

# 短格式输出
dig +short google.com
```

### 4.3 网络工具

#### curl - 命令行HTTP客户端

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
```

#### ssh - 安全远程登录

**原理解析**：`ssh` 用于安全地远程登录到另一台计算机。

**基本语法**：
```bash
ssh [选项] [用户@]主机 [命令]
```

**常用选项**：
```bash
-p port    指定端口（默认22）
-i keyfile 指定私钥文件
-X         启用X11转发
-L         本地端口转发
-R         远程端口转发
```

**实用示例**：
```bash
# 远程登录
ssh user@192.168.1.100

# 指定端口登录
ssh -p 2222 user@192.168.1.100

# 使用密钥登录
ssh -i ~/.ssh/id_rsa user@192.168.1.100

# 执行远程命令
ssh user@192.168.1.100 "ls -l"

# 端口转发
ssh -L 8080:localhost:80 user@192.168.1.100
```

#### scp - 安全文件传输

**原理解析**：`scp` 基于ssh协议，用于在本地和远程主机之间安全地复制文件。

**基本语法**：
```bash
scp [选项] 源文件 目标文件
```

**常用选项**：
```bash
-r       递归复制目录
-p       保留文件属性
-P port  指定端口
```

**实用示例**：
```bash
# 本地复制到远程
scp file.txt user@192.168.1.100:/home/user/

# 远程复制到本地
scp user@192.168.1.100:/home/user/file.txt .

# 递归复制目录
scp -r dir/ user@192.168.1.100:/home/user/

# 指定端口
scp -P 2222 file.txt user@192.168.1.100:/home/user/

# 保留文件属性
scp -p file.txt user@192.168.1.100:/home/user/
```

## 五、安全与证书管理

### 5.1 OpenSSL 证书管理

#### 常用操作

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

#### 带SAN的CSR生成

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

## 六、实用技巧与最佳实践

### 6.1 安全注意事项

1. **慎用rm -rf**：特别是在生产环境中
2. **权限管理**：合理设置文件权限，避免777权限
3. **备份习惯**：重要操作前先备份
4. **日志监控**：定期检查系统日志
5. **定期更新系统**：保持系统补丁最新
6. **使用sudo而不是root**：最小权限原则
7. **审计日志**：定期检查安全日志
8. **防火墙配置**：合理配置iptables/firewalld
9. **安全删除**：敏感文件使用shred命令

### 6.2 性能优化建议

1. 使用iostat定期监控磁盘I/O
2. 合理使用tmux/screen管理长时间任务
3. 利用awk处理大量文本数据
4. 使用curl进行API测试和调试
5. 使用`vmstat`监控虚拟内存统计
6. 使用`sar`收集系统活动报告
7. 合理配置swap分区
8. 使用`ionice`调整进程I/O优先级
9. 使用lsof排查文件占用问题

### 6.3 故障排查

1. 使用journalctl查看系统日志
2. 使用systemctl管理服务状态
3. OpenSSL工具链用于证书问题排查
4. find命令用于文件定位和排查
5. 使用`dmesg`查看内核消息
6. 使用`strace`跟踪系统调用
7. 使用`lsof`查看打开的文件
8. 使用`tcpdump`进行网络包分析
9. 使用ssh/scp进行远程管理和文件传输

## 扩展阅读
- [Linux命令手册](https://man7.org/linux/man-pages/)
- [Linux性能优化](https://www.brendangregg.com/linuxperf.html)
- [OpenSSL官方文档](https://www.openssl.org/docs/)
- [Linux命令速查](https://www.commandlinefu.com/)
- [tmux使用指南](https://github.com/tmux/tmux/wiki)
