# Flink-Demo
Flink样例

## Sink
* Hbase
  这里埋了一个坑，需要自定义Sink到Hbase中。HbaseSinkFuction 是给table api用的。


```
├── base          #基础包。自定义实体、工具
│   └── util
├── process       #处理函数
│   └── TopN
├── sink          #flink sink操作
├── source        #flink source操作
├── transfer      #多流转换操作: 流合并与拆分
├── watermark     #水位线：自定义水位线。水位线的设定
├── webUI         #WebUI。感觉没什么用
└── window        #内置丰富的窗口函数
```