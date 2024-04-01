**1·nullif( a, b ) 主要是完成判断 a 与 b 是否相同 ， 相同返回 null ，否则返回 a,hive中a不为空，mysql中可为空**
Select NULLIF(1,1)
Select NULLIF(null,2)
Select NULLIF(null,null)
Select NULLIF(2,null)
Select NULLIF(2,3)

**2·只适用于mysql 如果expr1为NULL，返回值为 expr2，否则返回expr1。**
Select IFNULL(null,2)
Select IFNULL(null,null)
Select IFNULL(2,null)
Select IFNULL(1,2)

**3·如果expr1为NULL，返回值为 expr2，否则返回expr1。适用于数字型、字符型和日期型 ， 不适用于mysql 适用于 oracle 和hive**
Select NVL(null, 2)
Select NVL(2, null)
Select NVL(2, 2)
Select NVL(null, null)

**4·返回第一个非空项 hive与mysql通用**
Select COALESCE(null, 2,3)
Select COALESCE(2, null,3)
Select COALESCE(2, 3,null)
Select COALESCE(null, null,null)