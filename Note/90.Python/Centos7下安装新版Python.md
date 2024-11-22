## 一、yum方式安装

只能安装第三方提供的`python38`，版本不可选择

一、安装源

```bash
yum install -y centos-release-scl
```

二、安装python3.8

```bash
#安装
yum install -y rh-python38

#可选，安装python开发包，其他相关包可用使用：yum search python38查看
yum install rh-python38-python-devel

#开启
scl enable rh-python38 bash
```

三、验证

```bash
python3 -V
```

# 二、源码编译

```bash
#只是将python3.12.3的安装包下载到 /opt/python-src 目录下(目录随意)
cd /opt/python-src
#下载最新的软件安装包
wget https://www.python.org/ftp/python/3.12.3/Python-3.12.3.tgz
#解压缩安装包
tar -xzf Python-3.12.3.tgz
#安装源码编译需要的编译环境
yum -y install gcc zlib zlib-devel libffi libffi-devel
#可以解决后期出现的方向键、删除键乱码问题，这里提前避免。
yum install readline-devel
#解决No module named '_bz2'
yum install bzip2-devel
#安装openssl之前先安装这个
yum install epel-release
#安装openssl11，后期的pip3安装网络相关模块需要用到ssl模块。
yum install openssl-devel openssl11 openssl11-devel
#设置编译FLAG，以便使用最新的openssl库
export CFLAGS=$(pkg-config --cflags openssl11)
export LDFLAGS=$(pkg-config --libs openssl11)
#进入刚解压缩的目录
cd /opt/python-src/Python-3.12.3
#1.不指定python安装目录（使用默认python安装目录）
#因为：不建议加--prefix=/usr/python，这样会导致你后续pip安装的可执行文件
#（比如virtualenv）都放在/usr/python/bin而不是默认的/usr/bin里面，不能直接用，还得逐个ln
#2.使用--with-openssl=<path_to_openssl>
# 可以通过使用which openssl查看openssl的安装路径, 我这里是/usr/bin/openssl
# 有人加了 --enable-optimizations 启用一些优化选项，提高Python的性能（待定）
# 最后的指令如下
./configure --with-openssl=/usr/bin/openssl
#然后就算源码编译并安装了，时间会持续几分钟。
make && make install
#指定链接，此后我们系统的任何地方输入python3就是我们安装的(可选)
#备份
mv /usr/bin/python3 /usr/bin/python3.bak
mv /usr/bin/pip3 /usr/bin/pip3.bak
ln -s /usr/local/bin/python3.12 /usr/bin/python3
ln -s /usr/local/bin/pip3 /usr/bin/pip3
#pip3安装包
pip3 install virtualenv
#指定virtualenv的链接
ln -s /usr/local/bin/virtualenv /usr/bin/virtualenv
#这个最新版python3了
```

## Conda方式安装（推荐）

## 替换pip镜像源

创建配置文件 `~/.pip/pip.conf`

```bash
mkdir -p ~/.pip
touch  ~/.pip/pip.conf
```

- 在`pip.conf`配置镜像源

```bash
[global]
timeout = 6000
index-url = https://pypi.tuna.tsinghua.edu.cn/simple
trusted-host = pypi.tuna.tsinghua.edu.cn
```

