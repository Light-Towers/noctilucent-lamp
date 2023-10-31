## 索引

### 1.创建索引

```python
import pymongo
client = pymongo.MongoClient()
db = client["test"]
collection = db["users"]
# 创建一个单字段索引
collection.create_index("username")
# 创建一个复合索引
collection.create_index([("username", pymongo.ASCENDING), ("account", pymongo.DESCENDING)])
# 创建一个文本索引
collection.create_index([("content", pymongo.TEXT)])


# 创建一个唯一索引，名称为 username_unique_index
collection.create_index("username", unique=True, name="username_unique_index")
# 创建一个只包含非空值的文本索引，名称为 content_text_index
collection.create_index([("content", pymongo.TEXT)], sparse=True, name="content_text_index")
```

\- `unique`：索引是否唯一，默认为 `False`。
\- `sparse`：字段是否为空不参与索引，默认为 `False`。
\- `background`：后台创建索引，默认为 `False`。
\- `name`：索引名称，默认为 `None`。
\- `expireAfterSeconds`：设置 TTL (Time to Live) 索引的过期时间。

### 2.删除索引

```python
# 删除指定名称的索引
collection.drop_index("username_1")
# 删除 username 和 account 字段组成的索引
collection.drop_index([("username", pymongo.ASCENDING), ("account", pymongo.DESCENDING)])
```

### 3.查看索引

```python
# 查看所有索引
for index in collection.list_indexes():
    print(index)
# 或者
for name, index in collection.index_information().items():
    print(index['ns'], index['v'], index['key'])
    ## 创建索引。  `**index_info` 传参是什么意思
    collection_B.create_index(index['key'], name=name, **index_info)
```

使用 `list_indexes()` 来查看一个集合中所有的索引，该方法返回一个游标。

### 4.使用索引

```python
# 使用 username 索引进行查询
result = collection.find({"username": "pidancode.com"}).explain()
print(result)
# 使用复合索引进行查询
result = collection.find({"username": "pidancode.com", "account": "12345"}).explain()
print(result)
```

上面的例子中，`explain()` 方法会返回一个文档，其中包含了查询的详细信息，包括使用了哪些索引、查询时间等。

### 5.其他操作

除了上述操作，还有一些其他的索引操作，例如：
\- `create_indexes()`：创建多个索引。
\- `drop_indexes()`：删除所有索引或者指定名称的索引。
\- `reindex()`：重建所有索引。
\- `ensure_index()`：同 `create_index()`，但是已经被废弃，不建议使用。

```python
import pymongo
client = pymongo.MongoClient()
db = client["test"]
collection = db["users"]
# 创建多个索引
collection.create_indexes([
    [("username", pymongo.ASCENDING), ("account", pymongo.DESCENDING)],
    [("content", pymongo.TEXT)]
])
# 删除所有索引
collection.drop_indexes()
# 重建所有索引
collection.reindex()
```

