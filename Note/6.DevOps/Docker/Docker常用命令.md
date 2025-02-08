# 常用命令 (Common Commands)
## run
* 功能：创建并运行一个新的容器。
* 用法：docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
* 示例：
    ```bash
    # 创建mysql容器
    ## windows下my.cnf文件不生效(权限)问题：`chmod 644 /etc/mysql/conf.d/my.cnf`
    docker run -itd --name mysql -v /d/mount/docker/MySQL/data:/var/lib/mysql -v /d/mount/docker/MySQL/conf:/etc/mysql/conf.d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8
    # 创建elasticsearch容器
    docker run -itd --name elasticsearch -p 9200:9200 -p 9300:9300 -e ES_JAVA_OPTS="-Xms2g -Xmx2g" -e "discovery.type=single-node" elasticsearch:7.14.2
    ```
  * -i 保持和docker容器内的交互。运行docker命令结束后，容器依然存活，没有退出。
  * -t 分配一个伪TTY
  * -d 后台运行并打印容器Id
  * -p 端口映射
  * -v volume 映射容器目录到主机目录
  * -e 设置容器启动参数
  * --name 指定容器名称
  * --rm 当容器退出时自动将其移除
  * 末尾加`/bin/bash`相当于，在容器启时执行`/bin/bash`命令
## exec
* 功能：在正在运行的容器中执行命令。
* 用法：docker exec [OPTIONS] CONTAINER COMMAND [ARG...]
* 示例：docker exec -it my_container /bin/bash（进入容器的交互式 shell）
## ps
* 功能：列出所有容器（默认只显示正在运行的容器）。
* 用法：docker ps [OPTIONS]
* 示例：docker ps -a（列出所有容器，包括已停止的）
```bash
#查看docker完整命令
docker ps -a --no-trunc
#停止所有容器
docker ps -a | grep "Exited" | awk '{print $1}' | xargs docker stop
#删除所有容器
docker ps -a | grep "Exited" | awk '{print $1}' | xargs docker rm
```
  * -a 显示所有容器 (默认运行中)
  * -f 过滤
  * --format 格式化输出(使用Go模板)
  * -n 显示最近创建的n个容器
  * -l 显示最近创建的1个容器
  * --no-trunc 不截断输出
  * -q 只展示容器Id
  * -s 容器大小(还有镜像大小)
## build
* 功能：从 Dockerfile 构建镜像。
* 用法：docker build [OPTIONS] PATH | URL | -
* 示例：docker build -t my_image .（从当前目录构建镜像并打标签为 my_image）
## pull
* 功能：从注册表下载镜像。不添加tag的话，默认使用`latest`。
* 用法：docker pull IMAGE [TAG|DIGEST]
* 示例：docker pull ubuntu:latest（下载最新的 Ubuntu 镜像）
## push
* 功能：将镜像上传到镜像仓库。往远程仓库push，需要和远程仓库名一致
* 用法：docker push NAME[:TAG]
* 示例：docker push my_user/my_image:latest
## images
* 功能：列出本地镜像。
* 用法：docker images [OPTIONS] [REPOSITORY[:TAG]]
* 示例：docker images（列出所有本地镜像）
## login
* 功能：登录到注册表。
* 用法：docker login [OPTIONS] [SERVER]
* 示例：docker login -u '用户名' -p '密码' docker.io
## logout
* 功能：从注册表登出。
* 用法：docker logout [SERVER]
* 示例：docker logout docker.io
## search
* 功能：在 Docker Hub 中搜索镜像。
* 用法：docker search nginx
## version
* 功能：显示 Docker 版本信息。
* 用法：docker version
## info
* 功能：显示系统范围的信息。
* 用法：docker info

* * *

# 管理命令 (Management Commands)
## builder
* 功能：管理构建。
* 用法：docker builder [COMMAND]
## buildx
* 功能：Docker Buildx（用于高级构建功能）。
* 用法：docker buildx [COMMAND]
## compose
* 功能：Docker Compose（用于多容器应用）。
* 用法：docker compose [COMMAND]
## container
* 功能：管理容器。
* 用法：docker container [COMMAND]
## context
* 功能：管理上下文。
* 用法：docker context [COMMAND]
## image
* 功能：管理镜像。
* 用法：docker image [COMMAND]
## manifest
* 功能：管理 Docker 镜像清单和清单列表。
* 用法：docker manifest [COMMAND]
## network
* 功能：管理网络。
* 用法：docker network [COMMAND]
## plugin
* 功能：管理插件。
* 用法：docker plugin [COMMAND]
## system
* 功能：管理 Docker 系统。
* 用法：docker system [COMMAND]
```bash
#清理空间
docker system prune
##该指令默认会清除所有如下资源，指令结尾处会显示总计清理释放的空间大小：
* 已停止的容器（container）
* 未被任何容器所使用的卷（volume）
* 未被任何容器所关联的网络（network）
* 所有悬空镜像，未被使用的镜像不会被删除（image）。
##参数
添加 -a 或 --all 参数，一并清除所有未使用的镜像和悬空镜像。
添加 -f 或 --force 参数，用以忽略相关告警确认信息。

#查看docker所占的硬盘大小
docker system df
#-----或者--------#
#列出所有镜像
docker images
#清理无用镜像
docker image prune -a
#-----或者--------#
#列出所有镜像，包括未使用的镜像
docker-compose images --all
#清除所有无用镜像
docker-compose image prune -a
```
## trust
* 功能：管理 Docker 镜像的信任。
* 用法：docker trust [COMMAND]
## volume
* 功能：管理卷。
* 用法：docker volume [COMMAND]

* * *

# Swarm 命令 (Swarm Commands)
## swarm
* 功能：管理 Swarm（Docker 的集群管理工具）。
* 用法：docker swarm [COMMAND]

* * *

# 其他命令 (Commands)
## attach
* 功能：将标准输入、输出和错误流附加到正在运行的容器。
* 用法：docker attach CONTAINER
## commit
* 功能：从容器的更改创建新的镜像。**提交期间暂停容器**
* 用法：docker commit [OPTIONS] CONTAINER [REPOSITORY[:TAG]]
## cp
* 功能：在容器和本地文件系统之间复制文件/文件夹。
用法：docker cp [OPTIONS] CONTAINER:SRC_PATH DEST_PATH 或 docker cp [OPTIONS] SRC_PATH * CONTAINER:DEST_PATH
## create
* 功能：创建一个新的容器但不启动它。
* 用法：docker create [OPTIONS] IMAGE [COMMAND] [ARG...]
    * --add-host=[]   在容器内添加一个主机名到IP地址的映射关系(通过/etc/hosts文件)
    * --device=[]     映射物理机上的设备到容器内
    * --dns-search=[] DNS 搜索域

## diff
* 功能：检查容器文件系统中的更改。
* 用法：docker diff CONTAINER
## events
* 功能：获取来自服务器的实时事件。
* 用法：docker events [OPTIONS]
## export
* 功能：将容器的文件系统导出为 tar 归档。
* 用法：docker export [OPTIONS] CONTAINER
```bash
#eg: docker export 11 centos2
```
## history
* 功能：显示镜像的历史。
* 用法：docker history IMAGE
## import
* 功能：从 tarball 导入内容以创建文件系统镜像。
* 用法：docker import [OPTIONS] file|URL|- [REPOSITORY[:TAG]]
```bash
#eg: docker import 11 centos:import2
```
## inspect
* 功能：查看镜像/容器信息，返回 Docker 对象的低级信息。
* 用法：docker inspect [OPTIONS] NAME|ID [NAME|ID...]
* 示例：
```bash
#查看 容器ip 地址
docker inspect --format='{{.NetworkSettings.IPAddress}}' ID/NAMES
docker exec -it ID/NAMES ip addr
#容器运行状态
docker inspect --format '{{.Name}} {{.State.Running}}' ID/NAMES
#查看镜像的依赖镜像
docker image inspect --format='{{.RepoTags}} {{.Id}} {{.Parent}}' $(docker image ls -q --filter since=f9efa309632c)
```
## kill
* 功能：杀死一个或多个正在运行的容器。
* 用法：docker kill [OPTIONS] CONTAINER [CONTAINER...]
## load
* 功能：从 tar 归档或 STDIN 加载镜像。
* 用法：docker load [OPTIONS]
```bash
# -i 从tar存档文件代替 STDIN 读取
docker load -i aa.tar
```
## logs
* 功能：获取容器的日志。
* 用法：docker logs [OPTIONS] CONTAINER
```bash
docker logs -tf --tail 10 ID/NAMES
```
## pause
* 功能：暂停一个或多个容器中的所有进程。
* 用法：docker pause CONTAINER [CONTAINER...]
```bash
#暂停容器
docker pause ID/NAMES
```
## port
* 功能：列出容器的端口映射或特定映射。
* 用法：docker port CONTAINER [PRIVATE_PORT[/PROTO]]
## rename
* 功能：重命名容器。
* 用法：docker rename OLD_NAME NEW_NAME
## restart
* 功能：重启一个或多个容器。
* 用法：docker restart [OPTIONS] CONTAINER [CONTAINER...]
## rm
* 功能：删除一个或多个容器。
* 用法：docker rm [OPTIONS] CONTAINER [CONTAINER...]
* 示例：
```bash
docker rm nginx
#删除所有的container
docker rm $(docker ps -a -q)
```
## rmi
* 功能：删除一个或多个镜像。
* 用法：docker rmi [OPTIONS] IMAGE [IMAGE...]
* 示例：
```bash
docker rmi ID/NAMES
#删除所有的container
docker rmi $(docker images -q)
```
## save
* 功能：将一个或多个镜像保存为 tar 归档（默认流式传输到 STDOUT）。
* 用法：docker save [OPTIONS] IMAGE [IMAGE...]
```bash
# -o 指定输出文件（tar格式存档）代替输出到 STDOUT
docker save -o aa.tar nginx:latest
```
* docker save 保存的是镜像（image），docker export 保存的是容器（container）；
* docker load 用来载入镜像包，docker import 用来载入容器包，但两者都会恢复为镜像；
* docker load 不能对载入的镜像重命名，而 docker import 可以为镜像指定新名称。
> export 和 import 导出的是一个容器的快照, 不是镜像本身, 也就是说没有 layer。你的 dockerfile 里的 workdir, entrypoint 之类的所有东西都会丢失，commit 过的话也会丢失。容器的快照文件将丢弃所有的历史记录和元数据信息（即仅保存容器当时的快照状态），而镜像存储文件将保存完整记录，体积也更大。
## start
* 功能：启动一个或多个停止的容器。
* 用法：docker start [OPTIONS] CONTAINER [CONTAINER...]
## stats
* 功能：显示容器资源使用统计信息的实时流。
* 用法：docker stats [OPTIONS] [CONTAINER...]
## stats
* 功能：显示容器资源使用统计信息的实时流。
* 用法：docker stats [OPTIONS] [CONTAINER...]
[docker 容器使用资源](https://www.cnblogs.com/sparkdev/p/7821376.html)
```bash
显示容器使用的系统资源
默认情况下，stats 命令会每隔 1 秒钟刷新一次输出的内容直到你按下 ctrl + c。下面是输出的主要内容：
[CONTAINER]：以短格式显示容器的 ID。
[CPU %]：CPU 的使用情况。
[MEM USAGE / LIMIT]：当前使用的内存和最大可以使用的内存。
[MEM %]：以百分比的形式显示内存使用情况。
[NET I/O]：网络 I/O 数据。
[BLOCK I/O]：磁盘 I/O 数据。
[PIDS]：PID 号。

# --no-stream 选项只输出当前的状态
docker stats --no-stream
```
## stop
* 功能：停止一个或多个正在运行的容器。
* 用法：docker stop [OPTIONS] CONTAINER [CONTAINER...]
## tag
* 功能：创建一个指向源镜像的标签。
* 用法：docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]
* 示例：docker tag centos1 1109065487/centos1:1.0
## top
* 功能：显示容器的运行进程。
* 用法：docker top CONTAINER [ps OPTIONS]
## unpause
* 功能：取消暂停一个或多个容器中的所有进程。
* 用法：docker unpause CONTAINER [CONTAINER...]
```bash
#取消暂停容器
docker unpause ID/NAMES
```
## update
* 功能：更新一个或多个容器的配置。
* 用法：docker update [OPTIONS] CONTAINER [CONTAINER...]
## wait
* 功能：阻塞直到一个或多个容器停止，然后打印它们的退出代码。
* 用法：docker wait CONTAINER [CONTAINER...]

* * *

# 全局选项 (Global Options)
* --config string：客户端配置文件的位置（默认为 /root/.docker）
* -c, --context string：要使用的上下文名称（覆盖 DOCKER_HOST 环境变量和默认上下文）
* -D, --debug：启用调试模式
* -H, --host list：要连接的守护程序套接字
* -l, --log-level string：设置日志级别（debug, info, warn, error, fatal），默认为 info
* --tls：使用 TLS（由 --tlsverify 暗示）
* --tlscacert string：仅信任此 CA 签名的证书（默认为 /root/.docker/ca.pem）
* --tlscert string：TLS 证书文件路径（默认为 /root/.docker/cert.pem）
* --tlskey string：TLS 密钥文件路径（默认为 /root/.docker/key.pem）
* --tlsverify：使用 TLS 并验证远程
* -v, --version：打印版本信息并退出
