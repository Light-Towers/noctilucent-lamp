import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import pymysql  # 或使用 Doris 的 Python 客户端

# 生成模拟数据
def generate_mock_data(num_rows=10000):
    dates = [datetime.now() - timedelta(days=x) for x in range(num_rows)]
    data = {
        "id": range(1, num_rows+1),
        "user_id": np.random.randint(1000, 9999, size=num_rows),
        "product_id": np.random.choice(["A001", "B002", "C003", "D004"], num_rows),
        "price": np.round(np.random.uniform(10, 1000, num_rows), 2),
        "quantity": np.random.randint(1, 10, num_rows),
        "order_date": dates,
        "region": np.random.choice(["North", "South", "East", "West"], num_rows),
        "is_completed": np.random.choice([True, False], num_rows)
    }
    return pd.DataFrame(data)

# 写入Doris
def write_to_doris(df, table_name="test_orders"):
    # Doris连接参数
    conn = pymysql.connect(
        host='192.168.100.41',
        port=6030,  # Doris MySQL协议端口
        user='root',
        password='',
        database='test'
    )
    
    cursor = conn.cursor()

    # 将Timestamp转换为字符串
    df['order_date'] = df['order_date'].dt.strftime('%Y-%m-%d %H:%M:%S')

    # 准备SQL
    columns = ', '.join(df.columns)
    placeholders = ', '.join(['%s'] * len(df.columns))
    sql = f"INSERT INTO {table_name} ({columns}) VALUES ({placeholders})"

    try:
        # 转换为元组列表
        data = [tuple(x) for x in df.to_numpy()]
        
        # 分批写入，每批1000条
        batch_size = 1000
        for i in range(0, len(data), batch_size):
            batch = data[i:i + batch_size]
            cursor.executemany(sql, batch)
            conn.commit()
            print(f"已写入 {min(i + batch_size, len(data))}/{len(data)} 条数据")
            
    except Exception as e:
        conn.rollback()
        print(f"写入失败: {e}")
    finally:
        conn.close()

# 生成并写入数据
df = generate_mock_data(10000)
write_to_doris(df)