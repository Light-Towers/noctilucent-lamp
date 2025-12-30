# Systemd 全面使用指南

## 一、Systemd 基础概念

### 1.1 什么是 Systemd
- 新一代初始化系统和服务管理器
- 取代传统的 SysVinit
- 提供并行启动、按需启动、服务依赖管理等功能

### 1.2 核心组件
```
systemctl    # 系统和服务管理命令
journalctl   # 日志查看命令
systemd-analyze  # 启动性能分析
systemd-run   # 临时运行服务
```

### 1.3 与传统 init 系统的对比
| 功能 | Systemd | SysVinit |
|------|---------|----------|
| 启动速度 | 快（并行启动） | 慢（串行启动） |
| 配置格式 | .ini 风格（简单） | Shell 脚本（复杂） |
| 依赖管理 | 内置 | 需要手动配置 |
| 日志管理 | 集中式（journald） | 分散到各文件 |

## 二、Systemctl 命令大全

### 2.1 服务生命周期管理
```bash
# 启动服务
sudo systemctl start service_name

# 停止服务
sudo systemctl stop service_name

# 重启服务
sudo systemctl restart service_name

# 重新加载配置（不重启）
sudo systemctl reload service_name

# 查看服务状态
sudo systemctl status service_name

# 启用开机自启
sudo systemctl enable service_name

# 禁用开机自启
sudo systemctl disable service_name

# 查看是否开机自启
sudo systemctl is-enabled service_name

# 屏蔽服务（防止手动或自动启动）
sudo systemctl mask service_name

# 取消屏蔽
sudo systemctl unmask service_name
```

### 2.2 服务状态查询
```bash
# 列出所有已加载的服务
sudo systemctl list-units --type=service

# 列出所有服务（包括未加载的）
sudo systemctl list-unit-files --type=service

# 查看服务依赖关系
sudo systemctl list-dependencies service_name

# 查看服务启动所需时间
sudo systemd-analyze blame | grep service_name

# 检查服务配置语法
sudo systemd-analyze verify /etc/systemd/system/service_name.service
```

### 2.3 运行级别（target）管理
```bash
# 查看当前运行级别
sudo systemctl get-default

# 设置默认运行级别
sudo systemctl set-default multi-user.target  # 命令行模式
sudo systemctl set-default graphical.target   # 图形界面

# 切换运行级别（立即生效）
sudo systemctl isolate multi-user.target

# 查看所有 target
sudo systemctl list-unit-files --type=target

# 查看当前运行的 target
sudo systemctl list-units --type=target
```

## 三、服务文件（.service）编写

### 3.1 服务文件位置
```bash
# 系统服务（推荐）
/etc/systemd/system/

# 软件包安装的服务
/usr/lib/systemd/system/

# 用户级服务
~/.config/systemd/user/
```

### 3.2 基本结构
```ini
[Unit]
Description=服务描述
Documentation=文档链接
Requires=依赖服务
After=启动顺序依赖

[Service]
Type=服务类型
ExecStart=启动命令
ExecStop=停止命令
Restart=重启策略
User=运行用户

[Install]
WantedBy=安装目标
```

### 3.3 Service Type 详解
```ini
# Type=simple（默认）
# 主进程启动后，systemd认为服务已启动
ExecStart=/usr/bin/command

# Type=forking
# 传统的守护进程，启动后父进程退出
# 需要配合PIDFile使用
ExecStart=/path/to/daemon
PIDFile=/var/run/daemon.pid

# Type=oneshot
# 执行一次就退出，通常配合RemainAfterExit
ExecStart=/path/to/script.sh
RemainAfterExit=yes

# Type=dbus
# 通过D-Bus启动
BusName=com.example.service

# Type=notify
# 服务通过sd_notify()通知systemd已启动
ExecStart=/path/to/notify-daemon

# Type=idle
# 等待所有任务完成后启动（用于低优先级服务）
```

### 3.4 完整示例模板

#### 示例1：前台服务（如Node.js）
```ini
[Unit]
Description=Node.js Web服务
After=network.target
Wants=network.target

[Service]
Type=simple
User=nodeuser
Group=nodegroup
WorkingDirectory=/opt/myapp
Environment=NODE_ENV=production
ExecStart=/usr/bin/node /opt/myapp/app.js
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=node-app

# 资源限制
LimitNOFILE=65536
LimitNPROC=4096
MemoryMax=512M
CPUQuota=80%

[Install]
WantedBy=multi-user.target
```

#### 示例2：传统守护进程（如Java）
```ini
[Unit]
Description=Java应用服务
After=network.target mysql.service
Requires=mysql.service

[Service]
Type=forking
User=appuser
WorkingDirectory=/data/app
Environment="JAVA_HOME=/usr/lib/jvm/java-11"
Environment="APP_OPTS=-Xmx2g"
ExecStart=/data/app/bin/start.sh
ExecStop=/data/app/bin/stop.sh
ExecReload=/bin/kill -HUP $MAINPID
PIDFile=/var/run/myapp.pid
Restart=on-failure
RestartSec=30
TimeoutStartSec=300
TimeoutStopSec=30

# 安全加固
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ReadWritePaths=/data/app/logs

[Install]
WantedBy=multi-user.target
```

### 3.5 常用配置参数

#### [Unit] 部分
```ini
Description=简短描述
Documentation=man:service(8) https://example.com
Requires=强依赖的服务
Wants=弱依赖的服务
Before=在哪些服务之前启动
After=在哪些服务之后启动
ConditionPathExists=检查文件是否存在
AssertPathExists=断言文件存在
```

#### [Service] 部分
```ini
# 基本配置
Type=服务类型
User=运行用户
Group=运行组
WorkingDirectory=工作目录
Environment=环境变量
EnvironmentFile=环境变量文件

# 进程管理
PIDFile=PID文件路径
ExecStart=启动命令
ExecStartPre=启动前命令
ExecStartPost=启动后命令
ExecStop=停止命令
ExecReload=重载命令
ExecStopPost=停止后命令

# 重启策略
Restart=重启策略
RestartSec=重启等待时间
StartLimitInterval=启动频率检查间隔
StartLimitBurst=最大启动次数

# 资源限制
LimitCPU=CPU限制
LimitMEM=内存限制
LimitNOFILE=文件描述符限制
LimitNPROC=进程数限制
MemoryMax=内存硬限制
MemoryHigh=内存软限制
CPUQuota=CPU配额

# 安全相关
NoNewPrivileges=禁止新权限
PrivateTmp=私有临时目录
ProtectSystem=保护系统文件
ReadWritePaths=可读写路径
ReadOnlyPaths=只读路径
```

#### [Install] 部分
```ini
WantedBy=安装到的target
RequiredBy=被哪些target需要
Alias=别名
Also=同时安装的服务
```

## 四、Journalctl 日志管理

### 4.1 基本日志查看
```bash
# 查看所有日志（实时）
sudo journalctl -f

# 查看特定服务日志
sudo journalctl -u service_name

# 查看优先级以上的日志
sudo journalctl -p err -b  # 本次启动的错误日志

# 查看特定时间段的日志
sudo journalctl --since "2023-01-01 00:00:00" --until "2023-01-02 00:00:00"

# 查看内核日志
sudo journalctl -k
```

### 4.2 高级日志查询
```bash
# 按进程ID查看
sudo journalctl _PID=1234

# 按用户查看
sudo journalctl _UID=1000

# 按执行路径查看
sudo journalctl /usr/bin/bash

# 组合查询
sudo journalctl _SYSTEMD_UNIT=service_name + _PID=1234

# 输出JSON格式
sudo journalctl -u service_name -o json-pretty

# 导出日志到文件
sudo journalctl -u service_name > service.log
```

### 4.3 日志维护
```bash
# 查看日志占用的磁盘空间
sudo journalctl --disk-usage

# 清理早于指定时间的日志
sudo journalctl --vacuum-time=2weeks

# 限制日志大小为指定大小
sudo journalctl --vacuum-size=500M

# 限制日志文件数量
sudo journalctl --vacuum-files=5
```

## 五、高级功能

### 5.1 服务依赖和顺序
```ini
[Unit]
# 强依赖：必须启动成功
Requires=network.target mysql.service

# 弱依赖：尝试启动但不强制
Wants=redis.service

# 冲突服务
Conflicts=old-service.service

# 启动顺序
After=network.target
Before=webapp.service
```

### 5.2 条件启动
```ini
[Unit]
# 条件检查
ConditionPathExists=/etc/config/app.conf
ConditionPathIsDirectory=/data/app
ConditionFileNotEmpty=/etc/passwd
ConditionUser=root
ConditionKernelCommandLine=debug
ConditionVirtualization=yes

# 断言（失败则启动失败）
AssertPathExists=/data/app/bin/start.sh
AssertUser=appuser
```

### 5.3 资源控制（CGroup）
```ini
[Service]
# CPU控制
CPUQuota=80%
CPUWeight=100
StartupCPUWeight=200

# 内存控制
MemoryMax=1G
MemoryHigh=800M
MemorySwapMax=500M

# IO控制
IOWeight=100
IOReadBandwidthMax=/dev/sda 10M
IOWriteBandwidthMax=/dev/sda 10M

# 进程数限制
TasksMax=500
```

### 5.4 安全沙箱
```ini
[Service]
# 基本安全
NoNewPrivileges=yes
PrivateTmp=yes
PrivateDevices=yes
ProtectHome=read-only
ProtectSystem=strict

# 内核能力
CapabilityBoundingSet=CAP_NET_BIND_SERVICE
AmbientCapabilities=CAP_NET_BIND_SERVICE

# 文件系统访问
ReadWritePaths=/var/log/app
ReadOnlyPaths=/etc/app
InaccessiblePaths=/root
```

## 六、实战案例

### 6.1 创建自定义服务步骤
```bash
# 1. 创建服务文件
sudo vim /etc/systemd/system/myapp.service

# 2. 设置权限
sudo chmod 644 /etc/systemd/system/myapp.service

# 3. 重载systemd配置
sudo systemctl daemon-reload

# 4. 测试服务
sudo systemctl start myapp
sudo systemctl status myapp

# 5. 设置开机自启
sudo systemctl enable myapp

# 6. 验证配置
sudo systemd-analyze verify /etc/systemd/system/myapp.service
```

### 6.2 调试服务问题
```bash
# 查看服务状态
sudo systemctl status myapp -l

# 查看详细日志
sudo journalctl -u myapp -n 100 -f

# 测试启动命令
sudo systemd-run --unit=test-myapp /path/to/command

# 查看环境变量
sudo systemctl show myapp

# 手动运行命令（排除权限问题）
sudo -u appuser /path/to/command
```

### 6.3 性能调优
```bash
# 分析启动时间
sudo systemd-analyze
sudo systemd-analyze blame
sudo systemd-analyze critical-chain

# 生成启动图表
sudo systemd-analyze plot > boot.svg

# 查看服务启动时间
sudo systemd-analyze service myapp.service
```

## 七、常用故障排查

### 7.1 服务启动失败
```bash
# 1. 查看状态和日志
sudo systemctl status myapp
sudo journalctl -u myapp -n 50

# 2. 检查依赖
sudo systemctl list-dependencies myapp

# 3. 手动执行命令
sudo -u appuser /path/to/command

# 4. 检查权限
ls -la /path/to/command
id appuser

# 5. 检查端口冲突
sudo ss -tlnp | grep :8080
```

### 7.2 开机自启失效
```bash
# 检查是否启用
sudo systemctl is-enabled myapp

# 检查启动顺序
sudo systemctl list-dependencies multi-user.target | grep myapp

# 查看启动日志
sudo journalctl -b | grep myapp

# 测试启动脚本
sudo systemctl start myapp
```

## 八、实用脚本和工具

### 8.1 服务监控脚本
```bash
#!/bin/bash
# monitor-service.sh

SERVICE="$1"

if ! systemctl is-active --quiet "$SERVICE"; then
    echo "$(date): $SERVICE is down, restarting..."
    systemctl restart "$SERVICE"
    
    if [ $? -eq 0 ]; then
        echo "$(date): $SERVICE restarted successfully"
    else
        echo "$(date): Failed to restart $SERVICE"
        # 发送报警
    fi
fi
```

### 8.2 自动生成服务文件
```bash
#!/bin/bash
# create-service.sh

read -p "Service name: " SERVICE_NAME
read -p "Description: " DESCRIPTION
read -p "Exec command: " EXEC_CMD
read -p "User (default root): " USER
USER=${USER:-root}

cat > /tmp/${SERVICE_NAME}.service << EOF
[Unit]
Description=${DESCRIPTION}
After=network.target

[Service]
Type=simple
User=${USER}
ExecStart=${EXEC_CMD}
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

echo "Service file created at /tmp/${SERVICE_NAME}.service"
```

## 九、最佳实践

1. **命名规范**：使用小写字母和连字符，如 `my-app.service`
2. **配置文件**：将配置放在 `/etc/default/` 或 `/etc/sysconfig/` 目录
3. **日志管理**：合理设置日志轮转，避免磁盘空间不足
4. **资源限制**：为所有服务设置合理的资源限制
5. **安全原则**：最小权限原则，使用非root用户运行服务
6. **版本控制**：将服务文件纳入版本控制
7. **文档化**：在服务文件中添加注释和文档链接

---

**最后更新：2025年12月30日**

这份笔记涵盖了systemd的核心用法，从基础命令到高级配置，可以作为日常工作的参考手册。在实际使用中，可以根据具体需求调整配置参数。