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

