CentOS 7 生命周期结束

修改文件 `vim CentOS-SCLo-scl-rh.repo`, 将baseurl改为阿里云镜像地址
```bash
[centos-sclo-rh]
name=CentOS-7 - SCLo rh
# 阿里云镜像
baseurl=https://mirrors.aliyun.com/centos/7/sclo/x86_64/rh/
```