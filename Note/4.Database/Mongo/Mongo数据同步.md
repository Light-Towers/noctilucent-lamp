# mongo数据同步的三种方案

### (一)直接复制data目录(需要停止源和目标的mongo服务)

1. 针对目标mongo服务已经存在，并正在运行的(mongo2-->mongo)。

**执行步骤：**

(1).停止源/目标服务器的mongo服务。

```bash
mongod --dbpath /usr/local/mongodb/data/db --logpath /usr/local/mongodb/logs/mongodb.log --shutdown
mongod --dbpath /usr/local/mongodb/data/db2 --logpath /usr/local/mongodb/logs/mongodb2.log --shutdown
```

(2).删除目标mongo服务的数据目录

```bash
rm -rf /usr/local/mongodb/data/db
```

(3).将待备份的mongo数据目录复制到目标服务绑定的数据目录

```bash
cp -r /usr/local/mongodb/data/db2 /usr/local/mongodb/data/db
```

(4).启动目标mongo服务

```bash
mongod -f /usr/local/mongodb/mongodb.conf
```

2. 对于目标mongo为新增，未启动的(mongo-->mongo3)

(1).新增配置文件

```bash
cp mongodb2.conf mongodb3.conf
```

然后修改相应的端口和数据目录

(2).将待备份的mongo数据目录复制到新mongo的数据目录

```bash
cp -r /usr/local/mongodb/data/db /usr/local/mongodb/data/db3
```

(3).启动mongo服务

### (二)通过mongodump、mongorestore同步

参数解释：

```bash
-h host，填写ip地址加上端口号
-u username， mongo的用户名
-p passwd，mongo的密码
-d database，mongo的数据库名
-c collection，mongo的数据表名
-q query，mongo的查询条件
-o output，mongodump数据存放位置
--forceTableScan 强制扫描整个表(解决版本不一致问题)
```

mongodump到库级别,mongodump database1 database2两个库

```bash
mongodump -h host:port -u username -p passwd --authenticationDatabase admin -d database1 database2 -o /mongotest --forceTableScan
```

mongodump到表级别,mongodump database1 的database1col表

```bash
mongodump -h host:port -u username -p passwd --authenticationDatabase admin -d database1 -c database1col -o /mongotest --forceTableScan
```

mongodump到具体查询条件,mongodump database1 的database1col表中id>1000的数据

```bash
mongodump -h host:port -u username -p passwd --authenticationDatabase admin -d database1 -c database1col -q {"id":{$gte:1000}} -o /mongotest --forceTableScan
```

mongorestore到库级别,mongorestore database1库

```bash
mongorestore -h host:port -u username -p passwd --authenticationDatabase admin -d database1 /mongotest/database1
```

mongorestore到表级别,mongorestore database1 的database1col表

```bash
mongorestore -h host:port -u username -p passwd --authenticationDatabase admin -d database1 -c database1col /mongotest/database1/database1col.bson --forceTableScan
#或者参数优化后
mongorestore -h host:port -u username -p passwd --authenticationDatabase admin \
 -d database1 -c database1col \
 --numParallelCollections 4 --numInsertionWorkersPerCollection 16 \
 --batchSize 1000 \
 --writeConcern="{w:0}" --noIndexRestore \
 --gzip /data/mongo/database1/database1col.bson.gz 
```

### (三) 通过db.copyDatabase实现

1.去源机器的源数据库新建一个账户(跟在admin新建的一致就行，每个数据库都需要新建一个)

```bash
mongo host:port -u username -p passwd --authenticationDatabase admin
use admin
db.createUser({user:'testuser',pwd:'testpass',roles:['userAdminAnyDatabase']})
use database1（即将复制的库）
db.createUser({user:'testuser',pwd:'testpass',roles:['readWrite']})
```

2.登录目标机器

```bash
mongo host:port -u username -p passwd --authenticationDatabase admin
```

3.执行复制命令

需要用到源机器的ip\端口\库名，目标机器--需要新增的库名\新建的用户名\密码

```bash
db.copyDatabase("database1","database1","host:port","testuser","testpass","SCRAM-SHA-1")
```

### 总结：三种方案的优缺点

方案一

优点：数据快，特别是对于数据量比较大的库，速度优势更明显。不会产生中间数据。

缺点：需要停止服务

方案二

优点：速度相对于第三种更快。支持的版本更丰富(3，4都支持)。不需要停服务。

缺点：1. 不会同步索引。会产生中间数据，需要额外的磁盘来存储。 2. mongodump备份数据除了备份速度慢外，还有另外的问题，就是通过mongorestore恢复的时候非常慢。

方案三

优点：会同步索引。不会产生中间数据。不需要停服务

缺点：版本4不支持。速度较慢。









*******

## mongorestore(100.9.4) 参数说明

**通用选项：**

1. **`--help`**
   - **作用**：打印使用说明。
   - **用途**：查看`mongorestore`命令的完整用法及所有选项。
2. **`--version`**
   - **作用**：打印工具版本并退出。
   - **用途**：快速查询所使用的`mongorestore`版本。
3. **`--config=`**
   - **作用**：指定配置文件路径。
   - **用途**：通过读取指定文件中的配置项来设置`mongorestore`的运行参数。

**日志输出级别选项：**

1. **`-v`, `--verbose=<level>`**
   - **作用**：增加日志输出详细程度（可多次指定以进一步提升详细度）。
   - **用途**：根据需要调整日志详细程度，便于调试或监控恢复过程。
2. **`--quiet`**
   - **作用**：隐藏所有日志输出。
   - **用途**：在不需要查看日志的情况下，使命令行界面更简洁。

**连接选项：**

1. **`-h`, `--host=<hostname>`**
   - **作用**：指定要连接的MongoDB主机（对于副本集，格式为`setname/host1,host2`）。
   - **用途**：设置恢复数据的目标MongoDB服务器地址。
2. **`--port=<port>`**
   - **作用**：指定服务器端口（也可在`--host`中使用`hostname:port`形式）。
   - **用途**：指定目标MongoDB服务器的监听端口。

**SSL选项：**

1. **`--ssl`**
   - **作用**：连接到启用了SSL的`mongod`或`mongos`实例。
   - **用途**：在需要加密连接时启用SSL通信。
2. **`--sslCAFile=<filename>`**
   - **作用**：指定包含根证书链的`.pem`文件。
   - **用途**：用于验证服务器证书链。
3. **`--sslPEMKeyFile=<filename>`**
   - **作用**：指定包含证书和密钥的`.pem`文件。
   - **用途**：提供客户端证书和密钥以进行SSL身份验证。
4. **`--sslPEMKeyPassword=<password>`**
   - **作用**：指定解密`sslPEMKeyFile`所需的密码（如有必要）。
   - **用途**：提供密钥文件的密码。
5. **`--sslCRLFile=<filename>`**
   - **作用**：指定包含证书撤销列表的`.pem`文件。
   - **用途**：检查服务器证书是否已被撤销。
6. **`--sslFIPSMode`**
   - **作用**：使用已安装OpenSSL库的FIPS模式。
   - **用途**：在满足合规性要求时启用FIPS兼容性。
7. **`--tlsInsecure`**
   - **作用**：跳过服务器证书链和主机名的验证。
   - **用途**：在测试或特殊情况需要降低安全要求时临时禁用证书验证。

**认证选项：**

1. **`-d`, `--db=<database-name>`**
   - **作用**：指定使用的数据库。
   - **用途**：设置恢复数据的目标数据库。
2. **`-c`, `--collection=<collection-name>`**
   - **作用**：指定使用的集合。
   - **用途**：设置恢复数据的目标集合（通常与特定数据恢复任务相关）。

**URI选项：**

1. `--uri=mongodb-uri`
   - **作用**：指定MongoDB URI连接字符串。
   - **用途**：以统一资源标识符（URI）的形式提供完整的连接信息，包括主机、端口、认证等。

**命名空间选项：**

1. **`--excludeCollection=<collection-name>`**
   - **作用**：跳过恢复过程中指定的集合（可多次指定以排除更多集合）。
   - **用途**：在恢复时选择性地忽略某些集合。
2. **`--excludeCollectionsWithPrefix=<collection-prefix>`**
   - **作用**：跳过恢复过程中具有指定前缀的集合（可多次指定以排除更多前缀）。
   - **用途**：根据集合名前缀批量排除某些集合。
3. **`--nsExclude=<namespace-pattern>`**
   - **作用**：排除匹配命名空间。
   - **用途**：使用正则表达式模式排除特定命名空间。
4. **`--nsInclude=<namespace-pattern>`**
   - **作用**：包含匹配命名空间。
   - **用途**：使用正则表达式模式仅恢复特定命名空间。
5. **`--nsFrom=<namespace-pattern>`**
   - **作用**：重命名匹配的命名空间，必须有对应的`nsTo`。
   - **用途**：在恢复时将源命名空间按正则表达式模式改名。
6. **`--nsTo=<namespace-pattern>`**
   - **作用**：重命名匹配的命名空间，必须有对应的`nsFrom`。
   - **用途**：与`nsFrom`配合，指定源命名空间改名后的目标命名空间。

**输入选项：**

1. **`--objcheck`**
   - **作用**：在插入前验证所有对象。
   - **用途**：确保恢复的数据符合MongoDB对象结构要求。
2. **`--oplogReplay`**
   - **作用**：为时间点恢复重放oplog。
   - **用途**：实现基于oplog的时间点恢复，恢复至备份后的某个时刻状态。
3. **`--oplogLimit=<seconds>[:ordinal]`**
   - **作用**：仅包含指定Timestamp之前的oplog条目。
   - **用途**：限制oplog回放的范围，用于精细控制恢复时间点。
4. **`--oplogFile=<filename>`**
   - **作用**：指定用于回放的oplog文件。
   - **用途**：指定单独的oplog文件进行恢复操作。
5. **`--archive=<filename>`**
   - **作用**：从指定的归档文件恢复dump。若未指定值，则从标准输入（stdin）读取归档。
   - **用途**：指定备份数据的压缩归档文件作为恢复源。
6. **`--restoreDbUsersAndRoles`**
   - **作用**：恢复给定数据库的用户和角色定义。
   - **用途**：在恢复数据的同时还原相关的数据库用户和角色权限。
7. **`--dir=<directory-name>`**
   - **作用**：指定输入目录，使用`-`表示从标准输入（stdin）读取。
   - **用途**：指定备份数据所在的本地目录作为恢复源。
8. **`--gzip`**
   - **作用**：解压gzipped输入数据。
   - **用途**：处理已使用gzip压缩的备份文件，在恢复时自动解压。

**存储选项：**

1. **`--drop`**
   - **作用**：在导入备份数据之前，先删除目标数据库中的每个集合。
   - **用途**：确保目标集合在恢复前为空，实现覆盖式恢复。
2. **`--dryRun`**
   - **作用**：仅显示恢复摘要而不实际执行导入操作。
   - **用途**：允许您预览恢复过程而不对实际数据库做出任何更改，常与`--verbose`（`-v`）标志一起使用以获取详细信息。
3. **`--writeConcern=<write-concern>`**
   - **作用**：指定恢复操作的写关注选项。
   - **用途**：控制写入操作所需确认和持久化的级别。例如，`--writeConcern majority`确保多数副本集成员确认写入后再返回确认。您也可以提供一个包含具体选项（如`{w: 3, wtimeout: 500, fsync: true, j: true}`）的JSON对象。
4. **`--noIndexRestore`**
   - **作用**：阻止从备份中恢复索引。
   - **用途**：如果您只想恢复数据并在之后（可能以后台进程方式）重建索引，使用此选项可加速恢复过程。
5. **`--convertLegacyIndexes`**
   - **作用**：自动移除无效索引选项并更新遗留索引选项值（如将`true`转换为`1`）。
   - **用途**：确保与较新MongoDB版本兼容，如果备份是使用具有不同索引语法或选项的旧版本创建的。
6. **`--noOptionsRestore`**
   - **作用**：禁用从备份中恢复集合选项（如capped大小、最大文档数）。
   - **用途**：如果您想在恢复数据后手动配置集合选项，或备份包含与当前MongoDB设置不兼容的选项，使用此选项。
7. **`--keepIndexVersion`**
   - **作用**：防止在恢复过程中更新索引版本。
   - **用途**：有助于保持与运行早期版本的MongoDB集群的兼容性，如果向此类集群恢复数据。
8. **`--maintainInsertionOrder`**
   - **作用**：按照输入源（备份文件）中相同的顺序恢复文档。默认情况下，文档以任意顺序插入。
   - **用途**：当数据顺序重要时（如处理时间序列数据或文档间存在依赖关系）使用此选项。启用此标志还会将`NumInsertionWorkersPerCollection`限制为1，并启用`--stopOnError`。
9. **`-j`, `--numParallelCollections=`**
   - **作用**：指定同时恢复的集合数量。
   - **用途**：对于包含许多集合的大型备份，通过同时处理多个集合可加速恢复过程。根据您的系统资源和网络带宽调整此值。
10. **`--numInsertionWorkersPerCollection=`**
    - **作用**：决定恢复过程中每个集合的并发插入操作数量。
    - **用途**：增加此值可通过并行插入文档来提高恢复速度，但过高的值可能导致资源过度使用或因竞争导致速度下降。根据CPU、内存和磁盘I/O等因素找到适合您系统的最优值。
11. **`--stopOnError`**
    - **作用**：在遇到插入过程中任何错误（如文档验证错误或重复键错误）时停止恢复过程。默认情况下，`mongorestore`尝试在出现此类错误时继续恢复尽可能多的数据。
    - **用途**：如果您希望恢复在遇到任何问题时立即失败，而非尽力挽救尽可能多的数据，使用此选项。与`--maintainInsertionOrder`结合使用时，确保错误发生后不再插入额外文档。
12. **`--bypassDocumentValidation`**
    - **作用**：在恢复过程中跳过文档验证。
    - **用途**：如果目标集合具有严格的模式验证规则，而备份中包含可能不符合这些规则的文档，使用此选项允许您恢复数据而不触发验证错误。注意绕过验证可能导致数据库中数据不一致或错误。
13. **`--preserveUUID`**
    - **作用**：保留备份中原有的集合UUID（默认禁用）。此选项需要使用`--drop`。
    - **用途**：如果您需要保持与原始数据库相同的集合UUID以确保恢复一致性，启用此选项并配合`--drop`使用。
14. **`--fixDottedHashIndex`**
    - **作用**：启用后，将所有对点字段（名称中包含点的字段）的哈希索引转换为目标数据库中的单字段升序索引。
    - **用途**：解决点字段在哈希索引中的兼容性问题，因为MongoDB版本低于4.2时不支持此特性。如果您向旧版本恢复或希望避免此类索引，使用此选项。