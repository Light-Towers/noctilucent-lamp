# 修改daemon.json

```bash
sudo vim /etc/docker/daemon.json
# 镜像加速参考：https://gist.github.com/y0ngb1n/7e8f16af3242c7815e7ca2f0833d3ea6
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://rww1ubgb.mirror.aliyuncs.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "bip": "192.20.0.1/16",
  "default-address-pools": [
    {
        "base": "192.20.0.0/16",
        "size": 24
    },
    {
        "base": "192.168.0.0/16",
        "size": 24
    }
  ],
  "data-root": "/home/quant_group/docker"
}
```
#修改后重启docker服务
```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```
# daemon.json解析
Docker Engine V1.12 之后版本，用户可以自行创建 daemon.json 文件对 Docker Engine 进行配置和调整。要点如下：
* 该文件作为 Docker Engine 的配置管理文件, 里面几乎涵盖了所有 docker 命令行启动可以配置的参数。
* 不管是在哪个平台以何种方式启动, Docker 默认都会来这里读取配置。使用户可以统一管理不同系统下的 docker daemon 配置。
* 相关参数的使用说明，可以参阅 man dockerd 帮助信息，或者参阅官方文档。
该--config-file选项允许您以JSON格式为守护程序设置任何配置选项。此文件使用与键相同的标志名称，但允许多个条目的标志除外，它使用多个标志名称，例如，labels用于label标志。
配置文件中设置的选项不得与通过flags设置的选项冲突。如果文件和标志之间的选项重复，则docker守护程序无法启动，无论其值如何。我们这样做是为了避免静默忽略配置重新加载中引入的更改。例如，如果在配置文件中设置守护程序标签并且还通过--label标志设置守护程序标签，则守护程序无法启动。守护程序启动时将忽略文件中不存在的选项。
Linux上配置文件的默认位置是 /etc/docker/daemon.json。该--config-file标志可用于指定非默认位置。

```json
{
    "authorization-plugins": [],
    #Docker运行时使用的根路径,默认/var/lib/docker
    "data-root": "", 
    #设定容器DNS的地址，在容器的 /etc/resolv.conf文件中可查看
    "dns": [],
    #容器 /etc/resolv.conf 文件，其他设置
    "dns-opts": [],
    #设定容器的搜索域，当设定搜索域为 .example.com 时，在搜索一个名为 host 的 主机时，DNS不仅搜索host，还会搜索host.example.com。注意：如果不设置，Docker 会默认用主机上的 /etc/resolv.conf来配置容器。
    "dns-search": [],
    "exec-opts": [],
    "exec-root": "",
    "experimental": false,
    "features": {},
    "storage-driver": "",
    "storage-opts": [],
    #docker主机的标签，很实用的功能,例如定义：–label nodeName=host-121
    "labels": [],
    "live-restore": true,
    "log-driver": "",
    "log-opts": {},
    "mtu": 0,
    #Docker守护进程的PID文件
    "pidfile": "",
    "cluster-store": "",
    "cluster-store-opts": {},
    "cluster-advertise": "",
    "max-concurrent-downloads": 3,
    "max-concurrent-uploads": 5,
    "default-shm-size": "64M",
    "shutdown-timeout": 15,
    #启用debug的模式，启用后，可以看到很多的启动信息。默认false
    "debug": true,
    #设置容器hosts
    "hosts": [],
    "log-level": "",
    #默认 false, 启动TLS认证开关
    "tls": true,
    #默认 ~/.docker/ca.pem，通过CA认证过的的certificate文件路径
    "tlscacert": "",
    #默认 ~/.docker/cert.pem ，TLS的certificate文件路径
    "tlscert": "",
    #默认~/.docker/key.pem，TLS的key文件路径
    "tlskey": "",
    #默认false，使用TLS并做后台进程与客户端通讯的验证
    "tlsverify": true,
    "tls": true,
    "tlsverify": true,
    "tlscacert": "",
    "tlscert": "",
    "tlskey": "",
    "swarm-default-advertise-addr": "",
    "api-cors-header": "",
    #默认 false，启用selinux支持
    "selinux-enabled": false,
    "userns-remap": "",
    #Unix套接字的属组,仅指/var/run/docker.sock
    "group": "",
    "cgroup-parent": "",
    "default-ulimits": {
        "nofile": {
            "Name": "nofile",
            "Hard": 64000,
            "Soft": 64000
        }
    },
    "init": false,
    "init-path": "/usr/libexec/docker-init",
    "ipv6": false,
    "iptables": false,
    #默认true, 启用 net.ipv4.ip_forward ,进入容器后使用sysctl -a|grepnet.ipv4.ip_forward查看
    "ip-forward": false,
    "ip-masq": false,
    "userland-proxy": false,
    "userland-proxy-path": "/usr/libexec/docker-proxy",
    "ip": "0.0.0.0",
    "bridge": "",
    #网桥网段IP（切勿与宿主机同网段）
    "bip": "",
    "fixed-cidr": "",
    "fixed-cidr-v6": "",
    "default-gateway": "",
    "default-gateway-v6": "",
    "icc": false,
    "raw-logs": false,
    "allow-nondistributable-artifacts": [],
    #镜像加速的地址。
    "registry-mirrors": [],
    "seccomp-profile": "",
    #配置docker的私库地址
    "insecure-registries": [],
    "no-new-privileges": false,
    "default-runtime": "runc",
    "oom-score-adjust": -500,
    "node-generic-resources": ["NVIDIA-GPU=UUID1", "NVIDIA-GPU=UUID2"],
    "runtimes": {
        "cc-runtime": {
            "path": "/usr/bin/cc-runtime"
        },
        "custom": {
            "path": "/usr/local/bin/my-runc-replacement",
            "runtimeArgs": [
                "--debug"
            ]
        }
    },
    "default-address-pools":[
        {"base":"172.80.0.0/16","size":24},
        {"base":"172.90.0.0/16","size":24}
    ]
}
```
>注意：您不能将daemon.json已在守护程序启动时设置的选项设置为标志。在systemd用于启动Docker守护程序的系统上-H已设置，因此您无法使用该hosts键daemon.json来添加侦听地址。有关如何使用systemd drop-in文件完成此任务，请参阅https://docs.docker.com/engine/admin/systemd/#custom-docker-daemon-options。

修改配置文件之后需要重启docker生效
systemctl restart docker.service
参考资料：
[Daemon configuration file](https://www.jianshu.com/p/c7c7dc24b9e3)
[daemon.json的作用](https://www.jianshu.com/p/c7c7dc24b9e3)