# **conda常用命令**

```bash
#查看conda版本，验证是否安装
conda --version
#查看环境中已经安装了的软件包
conda list
#检查更新至最新版本，也会更新其它相关包
conda update conda
#更新所有包
conda update --all
#更新指定的包
conda update [package_name]

#查看当前存在那些虚拟环境
conda env list  或 conda info -e
#创建虚拟环境
conda create -n [env_name] python=3.9
#删除虚拟环境
conda remove -n [env_name] --all
#激活[env_name]虚拟环境
conda activate [env_name]
#退出环境
conda deactivate
#重命名虚拟环境
conda rename -n [old_name] [new_name]
#克隆环境
conda create -n [new_name] --clone [old_name]

#下载安装包
conda install [package_name]
#在指定环境中安装包
conda install -n [env_name] [package_name]
#删除当前环境中的包及其依赖包。  remove/uninstall 指令功能一样
conda uninstall [package_name]
#删除当前环境中的包，但不删除其依赖包。
conda uninstall [package_name] --force
#删除指定环境中的包
conda remove -n [env_name] [package_name]
```

