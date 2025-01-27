

Docker的restart参数用于指定自动重启Docker容器的策略，包含四个选项，分别是：

- no：默认值，表示容器退出时，Docker不自动重启容器。
- on-failure[:times]：若容器的退出状态非0，则Docker自动重启容器，还可以指定重启次数，若超过指定次数未能启动容器则放弃。
- always：容器退出时总是重启。
- unless-stopped：容器退出时总是重启，但不考虑Docker守护进程启动时就已经停止的容器。(**除非用户手动停止容器**)


## 修改容器的启动参数
```bash
docker container update --restart=always 容器名字
##修改时区 
-e TZ="Asia/Shanghai" 
```