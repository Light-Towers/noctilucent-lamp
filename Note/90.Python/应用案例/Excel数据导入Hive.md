字符串中存在特殊字符，使用`$`作为分隔符（如果还存在问题，需要对字符串做预处理）

```hive
drop table dw.load_huikan_test;
CREATE EXTERNAL TABLE `dw.load_huikan_test`(
      `com_name` string COMMENT '公司名称',
      `com_address` string COMMENT '公司地址',
      `email` string COMMENT '公司邮箱',
      `desc` string COMMENT '公司介绍',
      `tel_phone` string COMMENT '公司电话',
      `mobile_phone` string COMMENT '手机')
    PARTITIONED BY (`dt` string)
    ROW FORMAT DELIMITED
        FIELDS TERMINATED BY '$'
        LINES TERMINATED BY '\n'
    LOCATION 'hdfs://nameservice1/user/hive/warehouse/dw/load_huikan_test';
```



Python代码：

```python
# -*- coding: utf-8 -*-
# 1.导入pandas模块
import pandas as pd
import numpy as np
from impala.dbapi import connect
from datetime import datetime
import re

# 建立Hive连接
conn = connect(host='master02', port=10000, auth_mechanism='PLAIN', user='Python', password='hive', database='dw')

# 创建一个 cursor 对象
cursor = conn.cursor()

# 执行查询语句
query = "SELECT * FROM dw.dim_exchange_rate"
cursor.execute(query)

#pandas读取excel
def read_excel_to_dataframe():
    # 1.读取excel的某一个sheet
    df = pd.read_excel('D:/0-mingyang/文档/会刊/2023-04-08 300家 2023第28届中国中西部（合肥）医疗器械展览会.xlsx', sheet_name='展商信息')
    # print(df)
    # # 2.获取列标题
    # print(df.columns)
    # # 3.获取列行标题
    # print(df.index)
    # # 4.制定打印某一列
    # print(df["工资水平"])
    # # 5.描述数据
    # print(df.describe())
    return df


# 写入Hive
def write_dataframe_to_hive(df):
    # 获取当前日期
    date = datetime.now().date()
    insert_sql = f"""
        INSERT overwrite TABLE dw.load_huikan_test PARTITION (dt='{date}')
        VALUES 
    """
    # 逐行插入数据
    value_str = ""
    for row in df.itertuples():
        #row[0]是索引， 处理特殊字符。  对于每个字段使用引号包起来
        value_arr = ['\'{}\''.format(re.sub(r'[\n\r\']+', ' ', str(v))) if not (isinstance(v, float) and np.isnan(v)) else '\'null\'' for v in row[1:7]]
        # 使用 ', '.join() 替换原始的字符串拼接，提高代码可读性和效率
        value_str += ",({})".format(', '.join(value_arr))
        
    # print(insert_sql + value_str[1:])
    cursor.execute(insert_sql + value_str[1:])
    # 关闭连接
    conn.commit()
    cursor.close()
    conn.close()

if __name__ == '__main__':
    df = read_excel_to_dataframe()
    write_dataframe_to_hive(df)
```

