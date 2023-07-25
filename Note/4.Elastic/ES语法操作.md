## 批量更新字段

```bash
POST ik_company_bd_info_prod/_update_by_query
{
  "script": {
    "lang": "painless",
    "source": "if(ctx._source.exhibitorFlag==true){ctx._source.exhibitorFlag=false}"
  }
}
```

