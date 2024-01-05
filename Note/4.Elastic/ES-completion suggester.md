# [Elasticsearch - completion suggester](https://www.cnblogs.com/Neeo/articles/10695019.html)



目录

- [前言](https://www.cnblogs.com/Neeo/articles/10695019.html#前言)
- 完成建议器：completion suggester
  - [在索引阶段提升相关性](https://www.cnblogs.com/Neeo/articles/10695019.html#在索引阶段提升相关性)
  - [在搜索阶段提升相关性](https://www.cnblogs.com/Neeo/articles/10695019.html#在搜索阶段提升相关性)
  - [其他](https://www.cnblogs.com/Neeo/articles/10695019.html#其他)



- [返回Elasticsearch目录](https://www.cnblogs.com/Neeo/p/10864123.html#database)

# 前言[#](https://www.cnblogs.com/Neeo/articles/10695019.html#前言)

我们来看一下自动完成的建议器——是一个导航功能，提供自动完成、搜索功能，可以在用户输入时引导用户查看相关结果，从而提高搜索精度。但并不适用于拼接检查或者像`term`和`phrase`建议那样的功能。
如果说在2000年左右，自动完成还是很炫酷的功能，那么现在它是必备的了——任何没有自动完成功能的搜索引擎都是很古老的。用户期望一个良好的自动完成来帮助用户实现更快的（特别是移动端）以及更好的（比如输入`e`，搜索引擎就应该知道用户想要查找的是`elasticsearch`）搜索。
一个优秀的自动完成将降低搜索引擎的负载，特别是在用户有一些快速搜索可用时，也就是直接跳转到主流的搜索结果而无须执行完整的搜索。
除此之外，一个优秀的自动完成必须是和快速的、相关的：

- 快速是因为它必须在用户不断输入的时候产生建议。
- 相关则是用户并不希望建议一个没有搜索结果或者没有用处的结果。

那我们依靠之前学过的`match_phrase_prefix`最左前缀查询来完成该功能，但是这样的查询可能不够快，因为理想的情况下，搜索引擎需要在用户输入下一个字符前返回建议结果。
完成建议器和后面的上下文建议器可以帮助用户构建更快的自动完成，它们是基于Lucene的suggest建议模块而构建的，将数据保存在内存中的有限状态转移机中（FST）。FST实际上是一种图。它可以将词条以压缩和易于检索的方式来存储。

[![img](https://img2018.cnblogs.com/blog/1168165/201904/1168165-20190424213357491-463297622.png)](https://img2018.cnblogs.com/blog/1168165/201904/1168165-20190424213357491-463297622.png)

上图展示了词条`index、search、suggest`是如何存储的。当然实际中的实现更加复杂，比如它允许我们添加权重。
FST（[Finite StateTransducers](https://en.wikipedia.org/wiki/Finite-state_transducer)），通常中文译作有限状态传感器，FST目前在语音识别和自然语言搜索、处理等方向被广泛应用。
FST的功能更类似于字典，Lucene4.0在查找Term时使用了FST算法，用来快速定位Term的位置。FST的数据结构可以理解成（`key:value`）的形式。
在同义词过滤器（[SynonymFilter](http://lucene.apache.org/core/3_6_0/api/all/org/apache/lucene/analysis/synonym/SynonymFilter.html)）的实现中甚至可以用[HashMap](https://baike.baidu.com/item/hashmap)代替，不过相比较于HashMap，它的优点是：

- 以O(1)的时间复杂度找到key对应的value。
- 以字节的方式来存储所有的Term，重复利用Term Index的前缀和后缀，使Term - Index小到可以放进内存，减少存储空间，不过相对的也会占用更多的cpu资源。
- FST还可以用来快速确定term是否在系统中。

# 完成建议器：completion suggester[#](https://www.cnblogs.com/Neeo/articles/10695019.html#完成建议器completion-suggester)

为了告诉elasticsearch我们准备将建议存储在自动完成的FST中，需要在映射中定义一个字段并将其`type`类型设置为`completion`：

```bash
PUT s5
{
  "mappings":{
    "doc":{
      "properties": {
        "title": {
          "type": "completion",
          "analyzer": "standard"
        }
      }
    }
  }
}

PUT s5/doc/1
{
  "title":"Lucene is cool"
}

PUT s5/doc/2
{
  "title":"Elasticsearch builds on top of lucene"
}

PUT s5/doc/3
{
  "title":"Elasticsearch rocks"
}

PUT s5/doc/4
{
  "title":"Elastic is the company behind ELK stack"
}

PUT s5/doc/5
{
  "title":"the elk stack rocks"
}

PUT s5/doc/6
{
  "title":"elasticsearch is rock solid"
}

GET s5/doc/_search
{
  "suggest": {
    "my_s5": {
      "text": "elas",
      "completion": {
        "field": "title"
      }
    }
  }
}
```

建议结果不展示了！
上例的特殊映射中，支持以下参数：

- analyzer，要使用的索引分析器，默认为simple。
- search_analyzer，要使用的搜索分析器，默认值为analyzer。
- preserve_separators，保留分隔符，默认为true。 如果您禁用，您可以找到以Foo Fighters开头的字段，如果您建议使用foof。
- preserve_position_increments，启用位置增量，默认为true。如果禁用并使用停用词分析器The Beatles，如果您建议，可以从一个字段开始b。注意：您还可以通过索引两个输入来实现此目的，Beatles并且 The Beatles，如果您能够丰富数据，则无需更改简单的分析器。
- max_input_length，限制单个输入的长度，默认为50UTF-16代码点。此限制仅在索引时使用，以减少每个输入字符串的字符总数，以防止大量输入膨胀基础数据结构。大多数用例不受默认值的影响，因为前缀完成很少超过前缀长于少数几个字符。

除此之外，该建议映射还可以定义在已存在索引字段的多字段：

```armasm
PUT s6
{
  "mappings": {
    "doc": {
      "properties": {
        "name": {
          "type": "text",
          "fields": {
            "suggest": {
              "type": "completion"
            }
          }
        }
      }
    }
  }
}

PUT s6/doc/1
{
  "name":"KFC"
}
PUT s6/doc/2
{
  "name":"kfc"
}

GET s6/doc/_search
{
  "suggest": {
    "my_s6": {
      "text": "K",
      "completion": {
        "field": "name.suggest"
      }
    }
  }
}
```

如上示例中，我们需要索引餐厅这样的地点，而且每个地点的`name`名称字段添加`suggest`子字段。
上例的查询将肯德基（KFC）和开封菜（kfc）都返回。

## 在索引阶段提升相关性[#](https://www.cnblogs.com/Neeo/articles/10695019.html#在索引阶段提升相关性)

在进行普通的索引时，输入的文本在索引和搜索阶段都会被分析，这就是为什么上面的示例会将`KFC`和`kfc`都返回了。我们也可以通过`analyzer`和`search_analyzer`选项来进一步控制分析过程。如上例我们可以只匹配`KFC`而不匹配`kfc`：

```armasm
PUT s7
{
  "mappings": {
    "doc": {
      "properties": {
        "name": {
          "type": "text",
          "fields": {
            "suggest": {
              "type": "completion",
              "analyzer":"keyword",
              "search_analyzer":"keyword"
            }
          }
        }
      }
    }
  }
}

PUT s7/doc/1
{
  "name":"KFC"
}
PUT s7/doc/2
{
  "name":"kfc"
}
GET s7/doc/_search
{
  "suggest": {
    "my_s7": {
      "text": "K",
      "completion": {
        "field": "name.suggest"
      }
    }
  }
}
```

建议结果如下：

```scss
{
  "took" : 0,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 0,
    "max_score" : 0.0,
    "hits" : [ ]
  },
  "suggest" : {
    "my_s7" : [
      {
        "text" : "K",
        "offset" : 0,
        "length" : 1,
        "options" : [
          {
            "text" : "KFC",
            "_index" : "s7",
            "_type" : "doc",
            "_id" : "1",
            "_score" : 1.0,
            "_source" : {
              "name" : "KFC"
            }
          }
        ]
      }
    ]
  }
}
```

上述的建议结果中，只有`KFC`被返回。更多的细节控制可以搭配不同的分析器来完成。
多数的情况下，我们将在单独的字段中、单独的索引中甚至是单独的集群中保存建议。这对于主搜索引擎的性能提升和扩展建议器都是非常有利的。

除此之外，还可以使用`input`和可选的`weight`属性，`input`是建议查询匹配的预期文本，`weight`是建议评分方式（也就是权重）。例如：

```armasm
PUT s8
{
  "mappings": {
    "doc":{
      "properties":{
        "title":{
          "type": "completion"
        }
      }
    }
  }
}
```

添加数据的几种形式：

```bash
PUT s8/doc/1
{
  "title":{
    "input":"blow",
    "weight": 2
  }
}
PUT s8/doc/2
{
  "title":{
    "input":"block",
    "weight": 3
  }
}
```

上例分别添加两个建议并设置各自的权重值。

```css
PUT s8/doc/3
{
  "title": [  
    {
      "input":"appel",
      "weight": 2
    },
    {
      "input":"apple",
      "weight": 3
    }
  ]
}
```

上例以列表的形式添加建议，设置不同的权重。

```bash
PUT s8/doc/4
{
  "title": ["apple", "appel", "block", "blow"],
  "weght": 32
}
```

上例是为多个建议设置相同的权重。
查询的结果由权重决定：

```bash
GET s8/doc/_search
{
  "suggest": {
    "my_s8": {
      "text": "app",
      "completion": {
        "field": "title"
      }
    }
  }
}
```

比如，我们在设置建议的时候，将`apple`建议的权重`weight`设置的更高，那么在如上例的查询中，`apple`将会排在建议的首位。

## 在搜索阶段提升相关性[#](https://www.cnblogs.com/Neeo/articles/10695019.html#在搜索阶段提升相关性)

当在运行建议的请求时，可以决定出现哪些建议，就像其他建议器一样，`size`参数控制返回多少项建议（默认为5项）；还可以通过`fuzzy`参数设置模糊建议，以对拼写进行容错。当开启模糊建议之后，可以设置下列参数来完成建议：

- fuzziness，可以指定所允许的最大编辑距离。
- min_length，指定什么长度的输入文本可以开启模糊查询。
- prefix_length，假设若干开始的字符是正确的（比如block，如果输入blaw，该字段也认为之前输入的是对的），这样可以通过牺牲灵活性提升性能。

这些参数都是在建议的`completion`对象的下面：

```bash
GET s8/doc/_search
{
  "suggest": {
    "my_s9": {
      "text": "blaw",
      "completion": {
        "field": "title",
        "size": 2,
        "fuzzy": {
          "fuzziness": 2,
          "min_length": 3,
          "prefix_length": 2
        }
      }
    }
  }
}
```

结果如下：

```scss
{
  "took" : 0,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 0,
    "max_score" : 0.0,
    "hits" : [ ]
  },
  "suggest" : {
    "my_s9" : [
      {
        "text" : "blow",
        "offset" : 0,
        "length" : 4,
        "options" : [
          {
            "text" : "block",
            "_index" : "s8",
            "_type" : "doc",
            "_id" : "3",
            "_score" : 6.0,
            "_source" : {
              "title" : {
                "input" : "block",
                "weight" : 3
              }
            }
          },
          {
            "text" : "blow",
            "_index" : "s8",
            "_type" : "doc",
            "_id" : "2",
            "_score" : 4.0,
            "_source" : {
              "title" : {
                "input" : "blow",
                "weight" : 2
              }
            }
          }
        ]
      }
    ]
  }
}
```

## 其他[#](https://www.cnblogs.com/Neeo/articles/10695019.html#其他)

**_source**
为了减少不必要的响应，我们可以对建议结果做一些过滤，比如加上`_source`：

```bash
GET s8/doc/_search
{
  "suggest": {
    "completion_suggest": {
      "text": "appl",
      "completion": {
        "field": "title"
      }
    }
  },
  "_source": "title"
}
```

好吧，虽然我们只有一个字段！

**size**
除了`_source`，我们还可以指定`size`参数：

```bash
GET s8/doc/_search
{
  "suggest": {
    "completion_suggest": {
      "prefix": "app",
      "completion": {
        "field": "title",
        "size": 1
      }
    }
  },
  "_source": "title"
}
```

`size`参数指定返回建议数（默认为5），需要注意的是，`size must be positive`，也就是说`size`参数必须是积极的——非0非负数！

**skip_duplicates**
我们的建议可能是来自不同的文档，这其中就会有一些重复建议项，我们可以通过设置`skip_duplicates:true`来修改此行为，如果为`true`则会过滤掉结果中的重复建议文档：

```bash
GET s8/doc/_search
{
  "suggest": {
    "completion_suggest": {
      "prefix": "app",
      "completion": {
        "field": "title",
        "size": 5,
        "skip_duplicates":true
      }
    }
  },
  "_source": "title"
}
```

但需要注意的是，该参数设置为`true`的话，可能会降低搜索速度，因为需要访问更多的建议结果项，才能过滤出来前N个。
最后，完成建议器还支持正则表达式查询，这意味着我们可以将前缀表示为正则表达式：

```bash
GET s5/doc/_search
{
  "suggest": {
    "completion_suggest": {
      "regex": "e[l|e]a",
      "completion": {
        "field": "title"
      }
    }
  }
}
```

------

see also：[completion suggester](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html) | [Weighted Finite-State Transducer Algorithms
An Overview](https://cs.nyu.edu/~mohri/pub/fla.pdf) | [FST（Finite-State Transducer) 原理](https://blog.csdn.net/qq_36289377/article/details/83110202)