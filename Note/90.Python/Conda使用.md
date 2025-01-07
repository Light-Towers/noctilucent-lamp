# conda 安装

1.下载Miniconda安装脚本。可以从Miniconda的官方网站下载脚本，或者使用以下命令来下载最新版本的脚本：

```bash
wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
```

2.运行安装脚本。运行以下命令：

```shell
sh Miniconda3-latest-Linux-x86_64.sh
```

3.根据提示进行安装。

```bash
#1.阅读许可协议，按回车继续
In order to continue the installation process, please review the license
agreement.
Please, press ENTER to continue
>>> ENTER

#2.接受许可协议，yes
Do you accept the license terms? [yes|no]
>>> yes

#3.设置安装路径，此处使用/opt/miniconda3
Miniconda3 will now be installed into this location:
/root/miniconda3

  - Press ENTER to confirm the location
  - Press CTRL-C to abort the installation
  - Or specify a different location below

[/root/miniconda3] >>> /opt/miniconda3

#4.更新环境变量，yes
Do you wish to update your shell profile to automatically initialize conda?
This will activate conda on startup and change the command prompt when activated.
If you'd prefer that conda's base environment not be activated on startup,
   run the following command when conda is activated:

conda config --set auto_activate_base false

You can undo this by running `conda init --reverse $SHELL`? [yes|no]
[no] >>> yes
```



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

