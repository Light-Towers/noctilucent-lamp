查询库下的表和备注：

```sql
SELECT
	obj.name AS 'tableName',
	CASE WHEN pro.value IS NULL or pro.value = '' THEN '' ELSE CONVERT( NVARCHAR(MAX), pro.value) END AS 'tableDescription'
FROM
	(
		SELECT
			id
			 ,name
		FROM
			sys.sysobjects
		WHERE
			xtype in ('U') -- U：用户创建的表，S：系统自带的表
	) obj
		left join sys.extended_properties pro on pro.name = 'MS_Description' AND pro.minor_id = 0 and pro.major_id = obj.id
ORDER BY obj.name;
```

