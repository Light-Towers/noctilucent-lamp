- [ ] [Python中的并行处理(Pool.map()、Pool.starmap()、Pool.apply()](https://blog.csdn.net/csu_passer/article/details/102495104)
- [ ] [Python线程池及其原理和使用（超级详细）](http://c.biancheng.net/view/2627.html#:~:text=%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E4%BD%BF%E7%94%A8%20%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E5%9F%BA%E7%B1%BB%E6%98%AF%20concurrent.futures%20%E6%A8%A1%E5%9D%97%E4%B8%AD%E7%9A%84%20Executor%EF%BC%8CExecutor%20%E6%8F%90%E4%BE%9B%E4%BA%86%E4%B8%A4%E4%B8%AA%E5%AD%90%E7%B1%BB%EF%BC%8C%E5%8D%B3,ThreadPoolExecutor%20%E5%92%8C%20ProcessPoolExecutor%EF%BC%8C%E5%85%B6%E4%B8%AD%20ThreadPoolExecutor%20%E7%94%A8%E4%BA%8E%E5%88%9B%E5%BB%BA%E7%BA%BF%E7%A8%8B%E6%B1%A0%EF%BC%8C%E8%80%8C%20ProcessPoolExecutor%20%E7%94%A8%E4%BA%8E%E5%88%9B%E5%BB%BA%E8%BF%9B%E7%A8%8B%E6%B1%A0%E3%80%82)

- [ ] Python的并发下的全局变量操作
- [ ] Python/Node/Java 操作 MongoDB、ES、Mysql、Redis

- [ ] [十分钟学会如何用Python处理CSV文件](https://blog.csdn.net/m0_67393039/article/details/125389336)
- [ ] [Python datetime转换成string教程](https://www.fke6.com/html/95328.html)
- [ ] lambda 使用，集合操作 OR [Java中的List与Set转换](https://blog.csdn.net/qq_33036061/article/details/103968822)
- [ ] [编程之路--Python 定义数组](https://www.cjavapy.com/article/1516/)
- [ ] [Python 自动化操作 Excel 看这一篇就够了](https://zhuanlan.zhihu.com/p/259583430)



```bash
# python 安装环境依赖包
pip3 install -r requirements.txt
```



## [Python元组操作及方法总结](https://www.jianshu.com/p/44d3a02ef34b)





### 执行另一个脚本

```python
import os
# 显示当前文件夹下的全部目录和文件夹
# dir 显示磁盘目录命令
os.system('dir')

# 删除指定文件夹下的文件
os.system('del e:\\test\\test.txt')

# 删除一个空文件夹
# rd(RMDIR):在DOS操作系统中用于删除一个目录 + 要删除文件夹
os.system('rd e:\\test')

# 关闭进程
# taskkill是用来终止进程的 + 进程名
os.system('taskkill  /F /IM chrome.exe')

## 执行另一个Python脚本
## python命令 + B.py + 参数：IC.txt'
str=('python B.py IC.txt')
p=os.system(str)
## //打印执行结果 0表示 success ， 1表示 fail
print(p)
```





## Pandas使用

```python
import pandas as pd

# 1.读取某一个excel的某一个sheet
df = pd.read_excel('D:/0-mingyang/文档/会刊/2023-04-08 300家 2023第28届中国中西部（合肥）医疗器械展览会  第二版.xlsx', sheet_name=0)
# print(df)
# 2.获取列标题
df.columns
# 3.获取列行标题
df.index
# 4.制定打印某一列
df["工资水平"]
# 5.描述数据
df.describe()
```

