## 创建别名

```http
POST /_aliases
{
    "actions" : [
        { "add" : { "index" : "index_test", "alias" : "company_alias" } }
    ]
}
## 或者
POST /_aliases
{
    "actions" : [
        { "add" : { "index" : "index_test", "alias" : "gudong" } },
        { "add" : { "index" : "index_test", "alias" : "gudong", "is_write_index": true } }
    ]
}
## 或者，列表形式
POST /_aliases
{
    "actions" : [
        { "add" : { "indices" : ["index_test", "index_test2"], "alias" : "gudong" } }
    ]
}
```

## 删除别名

```http
POST /_aliases
{
    "actions" : [
        { "remove" : { "index" : "index_test", "alias" : "company_alias" } }
    ]
}
```

## 重命名别名 

```http
## 此为原子操作
POST /_aliases
{
  "actions": [
    {"remove": {"index": "index_test", "alias": "a1"}},
    {"add": {"index": "index_test", "alias": "a2"}}
  ]
}
```

