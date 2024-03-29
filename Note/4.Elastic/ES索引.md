## Mapping

ES 的索引一旦建立，对Mapping的修改只能新增字段，不能对Mapping中已有的字段进行修改、删除。在默认情况下，Mapping的动态映射Dynamic = true，会自动推测字段的类型并添加到Mapping中。

如果是新增加的字段，根据 Dynamic 的设置分为以下三种状况：

- **true**：一旦有新增字段的文档写入，Mapping 也同时被更新 (default)。
- **false**：索引的 Mapping 是不会被更新的，新增字段的数据无法被索引，也就是无法被搜索，但是信息会出现在 _source 中。
- **strict**：文档写入会失败。
- **runtime**：新字段作为[runtime fields](https://www.elastic.co/guide/en/elasticsearch/reference/7.11/runtime.html)添加到Mapping中。这些字段不会被索引，而是在查询时从_source加载 (`7.11新增`)。

动态映射参数可以在创建索引时设置，`(TODO 待验证)`

```json
PUT users
{
  "mappings": {
    "dynamic": false
  }
}
```

## 删除索引(⚠慎用)

```bash
Delete 索引名称
Delete 索引名称1，索引名称2
# 删除以 xx开头的所有索引文件（如果配置文件中禁止后此方式不能使用）；
Delete 索引名称*
# 删除全部索引命令 【DELETE /_all】（配置文件中禁止后此方式不能使用） 或者 【DELETE /*】（配置文件中禁止后此方式不能使用）
Delete _all
```

**注意事项：**对数据安全来说，能够使用单个命令来删除所有的数据可能会带来很可怕的后果，所以，为了避免大量删除，可以在**elasticsearch.yml** 配置文件中修改 **action.destructive_requires_name: true**

　　设置之后只限于使用特定名称来删除索引，使用_all 或者通配符来删除索引无效（上述中说明配置文件中禁止后此方式不能使用）】

## 删除字段

> 参考：https://www.jianshu.com/p/c9f73f72c4ac





**删除数据（只删除数据）：**

1.：根据主键删除数据：【DELETE /索引名称/类型名称/主键编号】

```
Delete 索引名称/文档名称/主键编号
```

 

2：根据匹配条件删除数据：（注意请求方式是Post） 

```
POST 索引名称/文档名称/_delete_by_query   
{
  "query":{
    "term":{
      "_id":100000100
    }
  }
}
```

如果你想根据条件来删除你的数据，则在Query查询体中组织你的条件就可以了。

3：删除所有数据：（注意请求方式是Post，只删除数据，不删除表结构）

```
POST /testindex/_delete_by_query?pretty
{
    "query": {
        "match_all": {
        }
    }
}
```



**为什么同一索引下不同类型的结构如果字段名称相同会报错？**

　　这个问题比较坑，什么意思呢？用我们使用关系型数据库的逻辑来说，就是同一个数据库里面，如果有两个不同的表，表里面都有name字段，

　　如果A表中name是varchar类型，B表中name是int类型，完了，这就没办法创建索引了。所以，我们在创建索引的时候要注意，为什么会有这样的问题呢？

　　难道是Es的bug？这个主要是因为Es使用的是lucene的框架，具体的原因大家可以参考官方文档 [避免类型陷阱](https://www.elastic.co/guide/cn/elasticsearch/guide/current/mapping.html)

 

**创建索引结构时注意事项以及问题？**

　　1： 明确字段存储类型，是int类型还是文本类型；

　　　　ES字段类型参考：https://blog.csdn.net/chengyuqiang/article/details/79048800

　　2： 文本类型情况下，一种是keyword，一种是text ；

　　　　二者的区别请参考：https://www.cnblogs.com/Rawls/p/10069670.html

　　3： 字段结构类型一旦定义之后，是无法修改的，就跟你在数据库中一样，以前是int类型，

　　　　是无法改成varchar类型的（在sqlserver 表中没有数据情况下可以修改）。

　　4： 如果需要修改结构类型，只能在在这个文档类型中新增一个字段了。

　　5： 同一个索引下面，字段名称尽量不要重复；

　　6： 字段名称如果有重复情况，一定要使用相同的字段类型，要么都是integer，要么都是text。



## runtime fields

### 1、背景
有这么一种情况，比如我们的线路名称字段lineName字段在设置mapping的时候使用的是text类型，但是后期发现需要使用这个字段来进行聚合操作，那么我们除了对索引进行reindex操作外，还有什么办法可以解决这个问题呢？此处我们通过runtime field来解决。

### 2、runtime field介绍
#### 2.1 runtime field可以实现的功能
运行时字段是在查询时评估的字段。是在es7.11之后增加的运行时字段使您能够：

1. 将字段添加到现有文档，而无需重新索引数据
2. 在不了解数据结构的情况下开始处理数据
3. 在查询时覆盖从索引字段返回的值
4. 定义特定用途的字段，而不修改原始mapping

### 2.2 runtime field优缺点
1. runtime field是运行时增加的字段，不会被索引和存储，不会增加索引的大小。
2. runtime field 可以像普通字段一样使用，可以进行查询,排序,聚合等操作。
3. 可以动态的添加字段。
4. 可以在查询时覆盖字段的值。即fields中和_source中可以返回同名的字段，但是值可能不一样。
5. 阻止mapping爆炸，可以先使用后定义。
6. 针对经常被搜索或聚合等操作的字段，不适合使用runtime field，而应该定义在mapping中。
7. runtime field不会出现在_source中，需要通过fields api来获取。

## 3、创建runtime field的方式
### 3.1 通过mapping的方式创建

```http
## 添加runtime field
PUT /index_script_fields
{
  "mappings": {
    "runtime": {
      "aggLineName": {
        "type": "keyword",
        "script": {
          "source": "emit(doc['lineName'].value)"
        }
      }
    },
    "properties": {
      "lineId": {
        "type": "keyword"
      },
      "lineName": {
        "type": "text"
      }
    }
  }
}

## 更新 runtime field
POST /index_script_fields/_mapping
{
  "runtime": {
    "aggLineName": {
      "type": "keyword",
      "script": {
        "source": "emit(doc['lineName'].value)"
      }
    }
  }
}

## 删除 runtime field
POST /index_script_fields/_mapping
{
  "runtime": {
    "aggLineName": null 
  }
}
```

### 3.2 通过search request定义runtime field

```http
## 定义一个runtime field，字段名和_source字段名一致
GET /index_script_fields/_search
{
  "runtime_mappings": {
    "lineName": {
      "type": "keyword",
      "script": "emit(params['_source']['lineName']+'new')"
    }
  }, 
  "query": {
    "match_all": {}
  },
  "fields": [
    "lineName"
  ]
}

```

> [elasticsearch中使用runtime fields](https://blog.csdn.net/fu_huo_1993/article/details/128840069)

