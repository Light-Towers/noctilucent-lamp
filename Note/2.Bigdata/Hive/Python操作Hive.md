## Impala

Python包：

```bash
python install bitarray
python install thrift
python install thrift-sasl
python install six
python install pure-sasl
python install impyla
```

代码：

```python
from impala.dbapi import connect
from impala.util import as_pandas
import pandas as pd
 
# 获取数据
def select_hive(sql):
    # 创建hive连接
    conn = connect(host='10.16.15.2', port=10000, auth_mechanism='PLAIN',user='hive', password='user@123', database='user')
    cur = conn.cursor()
    try:
        #cur.execute(sql)
        c = cur.fetchall()
        df = as_pandas(cur)
        return df
    finally:
        if conn:
            conn.close()
 
data = select_hive(sql = 'select * from user_huaxiang_wide_table limit 100')
```

