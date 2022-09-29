# 安装

官网github：https://github.com/google/s2geometry

Google S2、球体上的几何、细胞和希尔伯特曲线：https://blog.christianperone.com/2015/08/googles-s2-geometry-on-the-sphere-cells-and-hilbert-curve/

要求：

- [CMake](http://www.cmake.org/)
- A C++ compiler with C++11 support, such as [g++ >= 4.7](https://gcc.gnu.org/)
-  [Abseil](https://github.com/abseil/abseil-cpp) (standard library extensions) 
- [OpenSSL](https://github.com/openssl/openssl) (for its bignum library)
- [gflags command line flags](https://github.com/gflags/gflags), optional
- [glog logging module](https://github.com/google/glog), optional
- [googletest testing framework](https://github.com/google/googletest) (to build tests and example programs, optional)

系统本次使用的是ubuntu 20，

## 安装依赖包

```bash
sudo apt-get install cmake libgflags-dev libgoogle-glog-dev libgtest-dev libssl-dev
```

## Abseil安装

github：https://github.com/abseil/abseil-cpp

CMake安装Abseil：https://abseil.io/docs/cpp/tools/cmake-installs#using-abseil-via-cmake-installation-binaries

1. 下载源码

```bash
cd ~/Source
# 此处指定 20220623.1 版(对应C++11)
wget https://github.com/abseil/abseil-cpp/archive/refs/tags/20220623.1.tar.gz
tar -zxvf 20220623.1.tar.gz
```

2. 编译

```bash
cd abseil-cpp-20220623.1
mkdir build && cd build
cmake .. -DCMAKE_POSITION_INDEPENDENT_CODE=ON -DCMAKE_INSTALL_PREFIX=~/Source/CMakeProject/install-20220623.1
```

3. 构建安装
```bash
cmake --build . --target install
```

## S2geometry安装

1. 下载源码

```bash
cd ~/Source
git clone https://github.com/google/s2geometry.git
```

2. 构建

```bash
mkdir build && cd build
# You can omit -DGTEST_ROOT to skip tests; see above for macOS.
# Use the same CMAKE_CXX_STANDARD value that was used with absl.
cmake -DGTEST_ROOT=/usr/src/gtest -DCMAKE_PREFIX_PATH=/path/to/absl/install -DCMAKE_CXX_STANDARD=11 ..
make -j $(nproc)
make test ARGS="-j$(nproc)"  # If GTEST_ROOT specified above.
sudo make install
```

## Python接口

需要的依赖

- [SWIG 4](https://github.com/swig/swig)
- python3-dev

```bash
sudo apt-get install swig python3-dev
```


# 疑问
c++有多少版本，11、14、17 是lts版？Abseil的master分支使用的14版。
c++是否需要学习？

