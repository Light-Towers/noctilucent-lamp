## 创建别名

```http
POST /_aliases
{
    "actions" : [
        { "add" : { "index" : "ik_company_info_prod", "alias" : "company_alias" } }
    ]
}
##或者
POST /_aliases
{
    "actions" : [
        { "add" : { "index" : "ik_company_info_prod", "alias" : "gudong" } },
        { "add" : { "index" : "ik_company_info_prod", "alias" : "gudong" } }
    ]
}
##或者
POST /_aliases
{
    "actions" : [
        { "add" : { "indices" : ["ik_company_info_prod", "ik_company_info_prod2"], "alias" : "gudong" } }
    ]
}
```

## 删除别名

```http
POST /_aliases
{
    "actions" : [
        { "remove" : { "index" : "ik_company_info_prod", "alias" : "company_alias" } }
    ]
}
```

