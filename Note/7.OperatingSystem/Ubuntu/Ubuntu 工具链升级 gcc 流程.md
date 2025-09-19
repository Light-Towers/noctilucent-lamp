# Ubuntu 工具链升级 gcc 流程

背景：将 Ubuntu 20.04 LTS，上默认的 gcc-9，工具链升级至 gcc-11。

# 第一步：添加工具链

sudo add-apt-repository ppa:ubuntu-toolchain-r/test

# 第二步：fetch 工具链源

sudo apt update

sudo apt upgrade

# 第三步：安装 gcc-11

sudo apt install gcc-11 g++-11

> 如果本文发文时间比较旧，你所持有的发行版版本更高，可以访问：https://launchpad.net/~ubuntu-toolchain-r/+archive/ubuntu/test。确认你发行版所支持的更高版本。

# 第四步：配置新安装的 gcc 优先级

```bash
cd /usr/bin/

#降低旧版本优先级
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-9 10

sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-9 10

#给予新版本更高的优先级

sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-11 20

sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-11 20
```

# 第五步：确认

```bash
gcc -v
```

输出：

```bash
Using built-in specs.

COLLECT_GCC=gcc

COLLECT_LTO_WRAPPER=/usr/lib/gcc/x86_64-linux-gnu/11/lto-wrapper

OFFLOAD_TARGET_NAMES=nvptx-none:amdgcn-amdhsa

OFFLOAD_TARGET_DEFAULT=1

Target: x86_64-linux-gnu

Configured with: ../src/configure -v --with-pkgversion='Ubuntu 11.1.0-1ubuntu1~20.04' --with-bugurl=file:///usr/share/doc/gcc-11/README.Bugs --enable-languages=c,ada,c++,go,brig,d,fortran,objc,obj-c++,m2 --prefix=/usr --with-gcc-major-version-only --program-suffix=-11 --program-prefix=x86_64-linux-gnu- --enable-shared --enable-linker-build-id --libexecdir=/usr/lib --without-included-gettext --enable-threads=posix --libdir=/usr/lib --enable-nls --enable-bootstrap --enable-clocale=gnu --enable-libstdcxx-debug --enable-libstdcxx-time=yes --with-default-libstdcxx-abi=new --enable-gnu-unique-object --disable-vtable-verify --enable-plugin --enable-default-pie --with-system-zlib --enable-libphobos-checking=release --with-target-system-zlib=auto --enable-objc-gc=auto --enable-multiarch --disable-werror --disable-cet --with-arch-32=i686 --with-abi=m64 --with-multilib-list=m32,m64,mx32 --enable-multilib --with-tune=generic --enable-offload-targets=nvptx-none=/build/gcc-11-2V7zgg/gcc-11-11.1.0/debian/tmp-nvptx/usr,amdgcn-amdhsa=/build/gcc-11-2V7zgg/gcc-11-11.1.0/debian/tmp-gcn/usr --without-cuda-driver --enable-checking=release --build=x86_64-linux-gnu --host=x86_64-linux-gnu --target=x86_64-linux-gnu --with-build-config=bootstrap-lto-lean --enable-link-serialization=2

Thread model: posix

Supported LTO compression algorithms: zlib zstd

gcc version 11.1.0 (Ubuntu 11.1.0-1ubuntu1~20.04)
```



