

## 问题概述

7-zip 的 GUI 界面不能设置文件名编码，因此会产生许多问题。例如同事的 Windows 的编码是GBK，传给我解压后出现乱码。

```cmd
# 查看windows编码
## 在命令行输入chcp查看当前windows默认编码
C:\Users\admin\Downloads>chcp
Active code page: 65001
## 激活
C:\Users\osmondy\Downloads>chcp 936
活动代码页: 936

# 使用7z解压：7z x src [-o dest] -mcp=code
"D:\Program Files\7-Zip\7z.exe" x  "我的压缩包.zip" -mcp=936
```

`src`为源文件路径，`dest`为输出文件路径，`code`为文件编码对应的代码页，如果不指定输出路径默认解压到当前目录。注意，相对路径须加`./`

## 编码列表

|   编码    | 代码页 |
| :-------: | :----: |
|    GBK    |  936   |
|   UTF-8   | 65001  |
|  UTF-16   |  1200  |
| Shift-JIS |  932   |



