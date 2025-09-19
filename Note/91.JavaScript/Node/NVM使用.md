nvm安装路径不能有`空格、中文`

安装完nvm后，可打开安装文件夹中的目录下的setting.txt文件，修改镜像

```bash
node_mirror: https://npm.taobao.org/mirrors/node/
npm_mirror: https://npm.taobao.org/mirrors/npm/
```

> 在运行`nvm install` 的时候，有可能会出现无权限安装的问题，如果遇到此问题，请 `以管理员身份运行` cmd。

```bash
#查看本地安装的所有版本；有可选参数available，显示所有可下载的版本。
nvm list [available]
#安装指定版本
nvm install 14.15.4
#安装最新版本node
nvm install latest
#切换版本
nvm use 14.15.4
#卸载指定版本
nvm uninstall 14.15.4
#设置别名为current-version
nvm alias current-version 14.15.4
#取消别名
nvm unalias current-version
#设置 default 这个特殊别名
nvm alias default node

# 其它命令
nvm arch #显示node是运行在32位还是64位系统上的
nvm on #开启nodejs版本管理
nvm off #关闭nodejs版本管理
nvm proxy [url] #设置下载代理。不加可选参数url，显示当前代理。将url设置为none则移除代理。
nvm node_mirror [url] #设置node镜像。默认是https://nodejs.org/dist/。如果不写url，则使用默认url。设置后可至安装目录settings.txt文件查看，也可直接在该文件操作。
nvm npm_mirror [url] #设置npm镜像。https://github.com/npm/cli/archive/。如果不写url，则使用默认url。设置后可至安装目录settings.txt文件查看，也可直接在该文件操作。
nvm root [path] #设置存储不同版本node的目录。如果未设置，默认使用当前目录。
nvm version #显示nvm版本。version可简化为v。

```





# Refrence

[1]: https://blog.csdn.net/weixin_42152058/article/details/130197300	"nvm的安装及全局依赖配置（详细）"

