Mongo Shell 使用：

```bash
#下载
wget https://fastdl.mongodb.org/linux/mongodb-shell-linux-x86_64-rhel70-3.6.23.tgz
#解压
tar -zxvf mongodb-shell-linux-x86_64-rhel70-3.6.23.tgz
```



```bash
#使用方式
mongo DATABASE_NAME --eval "var collection = 'COLL_NAME', query = {'caredAbout':true}" variety.js
#或者
./mongo --host "mongodb://root:mingyang100@192.168.100.23:27017/enterprise?replicaSet=rs0&connectTimeoutMS=10000&authSource=admin" --eval "var collection = 'ITJuZiInfo'" /root/mongo-test/variety-master/variety.js
```

显示结果如下

```bash
MongoDB shell version v3.6.23
connecting to: mongodb://192.168.100.23:27017/enterprise?authSource=admin&connectTimeoutMS=10000&gssapiServiceName=mongodb&replicaSet=rs0
2023-06-26T05:16:47.858-0400 I NETWORK  [thread1] Starting new replica set monitor for rs0/192.168.100.23:27017
2023-06-26T05:16:47.863-0400 I NETWORK  [thread1] Successfully connected to 192.168.100.23:27017 (1 connections now open to 192.168.100.23:27017 with a 5 second timeout)
2023-06-26T05:16:47.864-0400 I NETWORK  [thread1] changing hosts to rs0/192.168.100.22:27017,192.168.100.23:27017,192.168.100.24:27017 from rs0/192.168.100.23:27017
2023-06-26T05:16:47.865-0400 I NETWORK  [ReplicaSetMonitor-TaskExecutor-0] Successfully connected to 192.168.100.22:27017 (1 connections now open to 192.168.100.22:27017 with a 5 second timeout)
2023-06-26T05:16:47.866-0400 I NETWORK  [ReplicaSetMonitor-TaskExecutor-0] Successfully connected to 192.168.100.24:27017 (1 connections now open to 192.168.100.24:27017 with a 5 second timeout)
Implicit session: session { "id" : UUID("209b3f54-24a1-4149-9652-1a672f667fd5") }
MongoDB server version: 6.0.6
WARNING: shell and server versions do not match
Variety: A MongoDB Schema Analyzer
Version 1.5.1, released 02 October 2017
Using collection of "ITJuZiInfo"
Using query of { }
Using limit of 75762
Using maxDepth of 99
Using sort of { "_id" : -1 }
Using outputFormat of "ascii"
Using persistResults of false
Using resultsDatabase of "varietyResults"
Using resultsCollection of "ITJuZiInfoKeys"
Using resultsUser of null
Using resultsPass of null
Using logKeysContinuously of false
Using excludeSubkeys of [ ]
Using arrayEscape of "XX"
Using lastValue of false
Using plugins of [ ]
+---------------------------------------------------------------------------------------------------------------------+
| key                                             | types                       | occurrences | percents              |
| ----------------------------------------------- | --------------------------- | ----------- | --------------------- |
| COMFUNDSTATUSNAME                               | String                      |       75762 | 100.00000000000000000 |
| PID                                             | String (75761),null (1)     |       75762 | 100.00000000000000000 |
| _id                                             | ObjectId (75761),String (1) |       75762 | 100.00000000000000000 |
| COMPANYNAME                                     | String                      |       75750 |  99.98416092500197294 |
| INVEST                                          | Array                       |       75750 |  99.98416092500197294 |
| INVEST.XX.invse_month                           | String                      |       75750 |  99.98416092500197294 |
| INVEST.XX.invse_round_name                      | String                      |       75750 |  99.98416092500197294 |
| INVEST.XX.invse_similar_money_name              | String                      |       75750 |  99.98416092500197294 |
| INVEST.XX.invse_year                            | String                      |       75750 |  99.98416092500197294 |
| INVEST.XX.sec_invst                             | Array                       |       75750 |  99.98416092500197294 |
| INVEST.XX.invsp_or_fund                         | Array                       |       75749 |  99.98284100208547898 |
| INVEST.XX.ling_invst                            | Array                       |       75749 |  99.98284100208547898 |
| INVEST.XX.unstructured_invsp_or_fund            | Array                       |       75749 |  99.98284100208547898 |
| INVEST.XX.unstructured_ling_invst               | Array                       |       75749 |  99.98284100208547898 |
| INVEST.XX.unstructured_sec_invst                | Array                       |       75749 |  99.98284100208547898 |
| INVEST.XX.sec_invst.XX.name                     | String                      |       72726 |  95.99271402550091636 |
| URL                                             | String                      |       70966 |  93.66964969245796624 |
| URL_DESC                                        | String                      |       70966 |  93.66964969245796624 |
| WEBNAME                                         | String                      |       70966 |  93.66964969245796624 |
| updateDate                                      | Date (70964),String (2)     |       70966 |  93.66964969245796624 |
| htmlCacheId                                     | String                      |       69334 |  91.51553549272722421 |
| createDate                                      | Date (39271),String (2)     |       39273 |  51.83733269977033586 |
| TABLE                                           | String                      |       36473 |  48.14154853356563990 |
| CATID                                           | String                      |       36456 |  48.11910984398511459 |
| CATNAME                                         | String                      |       36456 |  48.11910984398511459 |
| COMBORNMONTH                                    | String                      |       36456 |  48.11910984398511459 |
| COMBORNYEAR                                     | String                      |       36456 |  48.11910984398511459 |
| COMCITY                                         | String                      |       36456 |  48.11910984398511459 |
| COMCONTADDR                                     | String                      |       36456 |  48.11910984398511459 |
| COMCONTEMAIL                                    | String                      |       36456 |  48.11910984398511459 |
| COMCONTTEL                                      | String                      |       36456 |  48.11910984398511459 |
| COMDES                                          | String                      |       36456 |  48.11910984398511459 |
| COMFUNDNEEDSNAME                                | String                      |       36456 |  48.11910984398511459 |
| COMID                                           | String                      |       36456 |  48.11910984398511459 |
| COMLOCATION                                     | String                      |       36456 |  48.11910984398511459 |
| COMLOGO                                         | String                      |       36456 |  48.11910984398511459 |
| COMLOGOARCHIVE                                  | String                      |       36456 |  48.11910984398511459 |
| COMNAME                                         | String                      |       36456 |  48.11910984398511459 |
| COMPANYID                                       | String                      |       36456 |  48.11910984398511459 |
| COMPROV                                         | String                      |       36456 |  48.11910984398511459 |
| COMRADARJUZIINDEX                               | String                      |       36456 |  48.11910984398511459 |
| COMSCALENAME                                    | String                      |       36456 |  48.11910984398511459 |
| COMSECNAME                                      | String                      |       36456 |  48.11910984398511459 |
| COMSLOGAN                                       | String                      |       36456 |  48.11910984398511459 |
| COMSTAGENAME                                    | String                      |       36456 |  48.11910984398511459 |
| COMSTATUSNAME                                   | String                      |       36456 |  48.11910984398511459 |
| COMTYPE                                         | String                      |       36456 |  48.11910984398511459 |
| COMURL                                          | String                      |       36456 |  48.11910984398511459 |
| COMVIDEO                                        | String                      |       36456 |  48.11910984398511459 |
| COMWEIBOURL                                     | String                      |       36456 |  48.11910984398511459 |
| COMWEIXINID                                     | String                      |       36456 |  48.11910984398511459 |
| CREATEDATE                                      | NumberLong                  |       36456 |  48.11910984398511459 |
| INVSETOTALMONEY                                 | String                      |       36456 |  48.11910984398511459 |
| SUBCATID                                        | String                      |       36456 |  48.11910984398511459 |
| SUBCATNAME                                      | String                      |       36456 |  48.11910984398511459 |
| TAGS                                            | Array                       |       36456 |  48.11910984398511459 |
| TAGS.XX.tag_id                                  | String                      |       36456 |  48.11910984398511459 |
| TAGS.XX.tag_name                                | String                      |       36456 |  48.11910984398511459 |
| UPDATETIME                                      | NumberLong                  |       36456 |  48.11910984398511459 |
| LOGOARCHIVEOSSID                                | String                      |       36427 |  48.08083207940656223 |
| SIMILARCOMPANY                                  | Array                       |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.COMPANYNAME                   | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.PID                           | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.cat_name                      | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.cat_name_order                | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_born_month                | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_born_time                 | NumberLong                  |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_born_year                 | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_id                        | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_logo                      | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_logo_archive              | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_name                      | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.com_prov                      | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.invese_round_name             | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.invse_currency_name           | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.invse_detail_money            | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.invse_month                   | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.invse_year                    | String                      |       36014 |  47.53570391489137137 |
| SIMILARCOMPANY.XX.similar                       | String                      |       36014 |  47.53570391489137137 |
| TEAM                                            | Array                       |       33842 |  44.66883134024973145 |
| TEAM.XX.des                                     | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.education                               | Array                       |       33842 |  44.66883134024973145 |
| TEAM.XX.everjob                                 | Array                       |       33842 |  44.66883134024973145 |
| TEAM.XX.introduce                               | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.per_id                                  | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.per_linkedin                            | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.per_logo                                | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.per_name                                | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.per_weibo                               | String                      |       33842 |  44.66883134024973145 |
| TEAM.XX.is_incumbency                           | String                      |       33827 |  44.64903249650220118 |
| LOGOOSSID                                       | String                      |       31030 |  40.95720809904701554 |
| NEWS                                            | Array                       |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_day                             | String                      |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_month                           | String                      |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_name                            | String                      |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_type_name                       | String                      |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_url                             | String                      |       18040 |  23.81140941369023878 |
| NEWS.XX.com_new_year                            | String                      |       18040 |  23.81140941369023878 |
| MILESTONE                                       | String (5893),Array (8467)  |       14360 |  18.95409308096407131 |
| TEAM.XX.education.XX.per_education_name         | String                      |       10737 |  14.17201235447849861 |
| MILESTONE.XX.com_id                             | String                      |        8467 |  11.17578733401969338 |
| MILESTONE.XX.com_mil_detail                     | String                      |        8467 |  11.17578733401969338 |
| MILESTONE.XX.com_mil_month                      | String                      |        8467 |  11.17578733401969338 |
| MILESTONE.XX.com_mil_year                       | String                      |        8467 |  11.17578733401969338 |
| PRODUCT                                         | Array                       |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_detail                       | String                      |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_id                           | String                      |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_name                         | String                      |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_type_id                      | String                      |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_type_name                    | String                      |        8432 |  11.12959003194213459 |
| PRODUCT.XX.com_pro_url                          | String                      |        8432 |  11.12959003194213459 |
| TEAM.XX.everjob.XX.per_ever_job_name            | String                      |        7983 |  10.53694464243288209 |
| INVEST.XX.fa                                    | Array                       |        4784 |   6.31451123254401914 |
| INVEST.XX.fa_id                                 | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_assess_money_id                 | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_assess_money_name               | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_currency_id                     | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_currency_name                   | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_day                             | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_detail_money                    | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_guess_particulars               | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_id                              | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_round_id                        | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_similar_money_id                | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.invse_stock_ownership                 | String                      |        4784 |   6.31451123254401914 |
| INVEST.XX.sec_invst.XX.id                       | String                      |        1760 |   2.32306433304295012 |
| INVEST.XX.sec_invst.XX.logo                     | String                      |        1760 |   2.32306433304295012 |
| INVEST.XX.sec_invst.XX.type                     | String                      |        1760 |   2.32306433304295012 |
| COMPANYFOREIGNINVESTMENT                        | Array (464),String (591)    |        1055 |   1.39251867690926856 |
| COMPANYFOREIGNINVESTMENT.XX.com_id              | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.com_name            | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.invse_currency_name | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.invse_date          | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.invse_id            | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.invse_money         | String                      |         464 |   0.61244423325677777 |
| COMPANYFOREIGNINVESTMENT.XX.invse_round_name    | String                      |         464 |   0.61244423325677777 |
| INVEST.XX.ling_invst.XX.id                      | String                      |         336 |   0.44349409994456324 |
| INVEST.XX.ling_invst.XX.logo                    | String                      |         336 |   0.44349409994456324 |
| INVEST.XX.ling_invst.XX.name                    | String                      |         336 |   0.44349409994456324 |
| INVEST.XX.ling_invst.XX.type                    | String                      |         336 |   0.44349409994456324 |
| COMPANYFOREIGNMERGER                            | String (223),Array (32)     |         255 |   0.33658034370792744 |
| INVEST.XX.invsp_or_fund.XX.id                   | String                      |         253 |   0.33394049787492408 |
| INVEST.XX.invsp_or_fund.XX.name                 | String                      |         253 |   0.33394049787492408 |
| INVEST.XX.invsp_or_fund.XX.type                 | String                      |         253 |   0.33394049787492408 |
| INVEST.XX.invsp_or_fund.XX.logo                 | String                      |         251 |   0.33130065204192077 |
| INVEST.XX.fa.XX.fa_id                           | String                      |          67 |   0.08843483540561231 |
| INVEST.XX.fa.XX.fa_name                         | String                      |          67 |   0.08843483540561231 |
| COMPANYFOREIGNMERGER.XX.combine_name            | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.invse_assess_money_name | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.invse_currency_name     | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_company_id       | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_company_name     | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_currency_id      | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_des              | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_detail_money     | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_equity_ratio     | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_id               | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_nearly_money_id  | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_show             | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_show_day         | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_show_month       | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_show_year        | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_title            | String                      |          32 |   0.04223753332805364 |
| COMPANYFOREIGNMERGER.XX.merger_type             | String                      |          32 |   0.04223753332805364 |
+---------------------------------------------------------------------------------------------------------------------+
```





## Refrence

[1]:https://blog.csdn.net/liuzehn/article/details/122697178	"CentOS 安装 MongoDB 客户端（命令行shell）"

[2]:https://blog.51cto.com/shanker/1745258 "MongoDB 表结构分析工具介绍 -- Variety"

