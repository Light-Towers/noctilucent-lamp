[toc]
## search
搜索镜像
```docker
docker search nginx
```
## pull
拉取镜像，不添加tag的话，默认使用`latest`
```docker
docker pull nginx
```
## rm
删除容器
```docker
docker rm ID/NAMES
#删除所有的container
docker rm $(docker ps -a -q)
```
## rmi
删除镜像
```docker
docker rmi ID/NAMES
#删除所有的container
docker rmi $(docker images -q)
```
## run
构建并运行新容器
```docker
docker  run -itd -p 8888:8888 -v /d/tensorflow/:/tensorflow tensorflow/tensorflow:2.2.0-jupyter /bin/bash
#末尾加/bin/bash相当于，在容器启时执行/bin/bash命令
```
* -i 保持和docker容器内的交互。运行docker命令结束后，容器依然存活，没有退出。
* -t 分配一个伪TTY
* -d 后台运行并打印容器Id
* --rm 当容器退出时自动将其移除

**eg：**

1. Mysql
```docker
docker run --name mysql -v /d/MySQL/conf:/etc/mysql/conf.d -v /d/MySQL/logs:/logs -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456-d mysql
```
2. ES
```docker
docker run -itd --name elasticsearch -p 9200:9200 -p 9300:9300 -e ES_JAVA_OPTS="-Xms2g -Xmx2g" -e "discovery.type=single-node" elasticsearch:7.14.2
```
## commit
```docker
#提交期间暂停容器
docker commit CONTAINER [REPOSITORY[:TAG]]
```
## inspect
```docker
#查看镜像/容器信息
docker inspect ID/NAMES
#查看 容器ip 地址
docker inspect --format='{{.NetworkSettings.IPAddress}}' ID/NAMES
docker exec -it ID/NAMES ip addr
#容器运行状态
docker inspect --format '{{.Name}} {{.State.Running}}' ID/NAMES
```
## login
```docker
docker login -u 用户名 -p 密码
```
## logout
```docker
docker logout
```
## push
```docker
#往远程仓库push:需要和远程仓库名一致.可以使用tag命令重新创建镜像
docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]
#eg: docker tag centos1 1109065487/centos1:1.0

docker push [OPTIONS] NAME[:TAG]
#eg: docker push 1109065487/centos1:1.0
```
## logs
```docker
docker logs -tf --tail 10 ID/NAMES
```
## pause/unpause
```docker
#暂停容器
docker pause ID/NAMES
#取消暂停容器
docker unpause ID/NAMES
```
## port
```docker
#查看docker完整命令
docker ps -a --no-trunc
```
## ps
```docker
#查看docker完整命令
docker ps -a --no-trunc
```
* -a 显示所有容器 (默认运行中)
* -f 过滤
* --format 格式化输出(使用Go模板)
* -n 显示最近创建的n个容器
* -l 显示最近创建的1个容器
* --no-trunc 不截断输出
* -q 只展示容器Id
* -s 容器大小(还有镜像大小)
## export/import
导出/导入容器(只能一个)
```docker
docker export -o xx [OPTIONS] CONTAINER
#eg: docker export 11 centos2

# 导入容器以生成镜像
docker import [OPTIONS] file|URL|- [REPOSITORY[:TAG]]
#eg: docker import 11 centos:import2
```
## save/load
保存/加载镜像(可多个)
```docker
# -o 指定输出文件（tar格式存档）代替输出到 STDOUT
docker save -o xx IMAGE [IMAGE...]

# -i 从tar存档文件代替 STDIN 读取
docker load -i xx
```
* docker save 保存的是镜像（image），docker export 保存的是容器（container）；
* docker load 用来载入镜像包，docker import 用来载入容器包，但两者都会恢复为镜像；
* docker load 不能对载入的镜像重命名，而 docker import 可以为镜像指定新名称。
> export 和 import 导出的是一个容器的快照, 不是镜像本身, 也就是说没有 layer。你的 dockerfile 里的 workdir, entrypoint 之类的所有东西都会丢失，commit 过的话也会丢失。容器的快照文件将丢弃所有的历史记录和元数据信息（即仅保存容器当时的快照状态），而镜像存储文件将保存完整记录，体积也更大。

 
## start/stop
```docker
#启动容器
docker start ID/NAMES
#停止容器
docker stop ID/NAMES
```

## stats
```docker
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
[docker 容器使用资源](https://www.cnblogs.com/sparkdev/p/7821376.html)

## tag
```docker
# 给镜像重命名
docker tag IMAGEID REPOSITORY:tag
eg:
docker tag b97ef5736782 apache/hive:3.1.3
```

## top
```docker
#查看容器中进程信息
docker top ID/NAMES
```

## system
```docker
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
* * *
## 其它：
```docker
#查看镜像的依赖镜像
docker image inspect --format='{{.RepoTags}} {{.Id}} {{.Parent}}' $(docker image ls -q --filter since=f9efa309632c)

#停止所有容器
docker ps -a | grep "Exited" | awk '{print $1}' | xargs docker stop
#删除所有容器
docker ps -a | grep "Exited" | awk '{print $1}' | xargs docker rm
```
正常退出但不关闭容器 `Ctrl+P+Q`

***
![cd5127a38488d34d246958349da7ee5d.png](en-resource://database/882:1)
create 命令与容器运行模式相关的选项
![a0021de384c53bc2c0e238d351ab1fbb.png](en-resource://database/883:1)
create 命令与容器环境和配置相关的选项
![dc0f8eeffeb5310ac8e60344697e1f8f.png](en-resource://database/884:1)
![f05c7093975d1d0b595b5422dbb5c8d0.png](en-resource://database/885:1)

## 设置开机自启
```bash
systemctl enable docker.service
## 查看已启用的服务
systemctl list-unit-files | grep enable
```

## 修改容器的启动参数
```bash
docker container update --restart=always 容器名字
##修改时区 
-e TZ="Asia/Shanghai" 
```



