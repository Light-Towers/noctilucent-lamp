# NODE 环境变量

```bash
set NODE_ENV #查看是否存在环境变量，如果有则会输出，反之为空
set NODE_ENV=production #临时设置NODE_ENV为production
set NODE_ENV #查看设置效果
set NODE_ENV= #删除环境变量
```

# npm缓存
```bash
# 强制清除npm的缓存
npm cache clean --force

# 列出npm缓存中的内容
npm cache ls

# 验证npm缓存
npm cache verify
```


## Refrence

[1]: https://blog.csdn.net/weixin_46560512/article/details/123587249 "简单说说NODE_ENV"

