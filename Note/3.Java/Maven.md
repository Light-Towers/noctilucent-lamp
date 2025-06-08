### 一、核心命令（生命周期）

| 命令 | 作用 | 执行阶段 |
|------|------|----------|
| `mvn clean` | 清理项目（删除 `target` 目录） | `clean` 生命周期 |
| `mvn validate` | 验证项目正确性（POM 结构、依赖是否可用） | `validate` |
| `mvn compile` | 编译主代码（`src/main/java`） | `compile` |
| `mvn test` | 编译并运行测试代码（`src/test/java`） | `test` |
| `mvn package` | 打包（生成 JAR/WAR 到 `target/`） | `package` |
| `mvn verify` | 运行集成测试 | `verify` |
| `mvn install` | 安装到本地 Maven 仓库（`~/.m2/repository`） | `install` |
| `mvn deploy` | 部署到远程仓库（需配置 `distributionManagement`） | `deploy` |

### 二、常用组合命令

| 命令 | 作用 |
|------|------|
| `mvn clean package` | 清理后重新打包 |
| `mvn clean install` | 清理后安装到本地仓库 |
| `mvn clean test` | 清理后运行测试 |
| `mvn clean deploy` | 清理后部署到远程仓库 |

### 三、实用参数

#### 1. 跳过阶段
| 参数 | 作用 |
|------|------|
| `-DskipTests` | 跳过测试（编译但不执行） |
| `-Dmaven.test.skip=true` | 完全跳过测试（不编译也不执行） |
| `-DskipITs` | 跳过集成测试 |
| `-Dcheckstyle.skip=true` | 跳过代码检查 |

#### 2. 多模块控制
| 参数 | 作用 |
|------|------|
| `-pl <模块名>` | 指定构建的模块（e.g. `-pl dinky-core,dinky-common`） |
| `-am` | 同时构建依赖的模块（需配合 `-pl`） |
| `-amd` | 同时构建依赖此模块的模块 |
| `-rf <模块名>` | 从指定模块继续构建（e.g. `-rf dinky-flink-1.16`） |

#### 3. 性能优化
| 参数 | 作用 |
|------|------|
| `-T 4` | 使用 4 个线程并行构建（推荐 `-T 1C` 表示每个核心一个线程） |
| `-o` | 离线模式（使用本地缓存，不检查远程仓库） |

#### 4. 调试与日志
| 参数 | 作用 |
|------|------|
| `-X` | 开启 Debug 日志 |
| `-e` | 显示错误堆栈 |
| `-q` | 安静模式（只输出错误） |
| `-l <logfile>` | 输出日志到文件（e.g. `-l build.log`） |

### 四、依赖管理命令

| 命令 | 作用 |
|------|------|
| `mvn dependency:tree` | 打印依赖树 |
| `mvn dependency:resolve` | 下载依赖到本地 |
| `mvn dependency:copy-dependencies` | 复制依赖到 `target/dependency` |
| `mvn dependency:analyze` | 分析依赖问题（未声明/未使用的依赖） |

### 五、插件命令

| 命令 | 作用 |
|------|------|
| `mvn help:effective-pom` | 查看生效的 POM（合并父子 POM 后） |
| `mvn versions:set -DnewVersion=1.2.0` | 批量修改模块版本 |
| `mvn spring-boot:run` | 运行 Spring Boot 项目（需插件） |
| `mvn jacoco:report` | 生成测试覆盖率报告 |

### 六、场景示例

#### 1. 仅构建特定模块及其依赖
```bash
mvn clean install -pl dinky-flink-1.16 -am
```

#### 2. 跳过测试并并行构建
```bash
mvn package -DskipTests -T 4
```

#### 3. 分析依赖冲突
```bash
mvn dependency:tree -Dverbose -Dincludes=com.google.guava:guava
```

#### 4. 部署到私有仓库
```bash
mvn clean deploy -DaltDeploymentRepository=myrepo::default::https://repo.example.com/releases
```

### 七、配置文件激活

| 命令 | 作用 |
|------|------|
| `mvn install -P prod` | 激活 `prod` Profile（在 POM 中定义） |
| `mvn install -P !dev` | 禁用 `dev` Profile |

### 八、环境变量传递

```bash
mvn package -Dapp.env=production
```
在代码中可通过 `System.getProperty("app.env")` 读取。

### 九、常见问题解决

#### 1. **依赖下载失败**  
- 检查网络或镜像配置（`~/.m2/settings.xml`）
- 删除本地缓存后重试：  
```bash
rm -rf ~/.m2/repository/path/to/dependency
```

#### 2. **构建卡顿**  
- 使用 `-X` 查看卡在哪一步
- 尝试 `-o` 离线模式避免远程检查

#### 3. **版本冲突**  
- 用 `dependency:tree` 定位冲突依赖
- 在 POM 中通过 `<exclusions>` 排除冲突版本

---

### 十、POM 高级配置：动态版本管理

（属于 Maven POM 配置与属性管理范畴）

在 Maven 的 POM 文件中，`${revision}` 是用于多模块项目统一管理版本的动态占位符，主要解决以下问题：
1. 多模块项目的版本同步
2. CI/CD 中的自动版本控制
3. 环境相关的版本配置

#### 1. 在父 POM 中定义默认值
```xml
<properties>
    <revision>1.0.0-SNAPSHOT</revision>
</properties>
```

#### 2. 命令行覆盖
```bash
mvn clean install -Drevision=2.1.0
```

#### 3. CI/CD 集成
```bash
# 读取 Git 标签作为版本号
REVISION=$(git describe --tags)
mvn deploy -Drevision=$REVISION
```

#### 4. 多属性组合
```xml
<properties>
    <revision>1.5.0</revision>
    <changelist>-SNAPSHOT</changelist>  <!-- 动态后缀 -->
    <sha1/> <!-- 可注入 Git SHA -->
</properties>

<version>${revision}${changelist}</version>
```

#### 5. 使用 flatten-maven-plugin 发布
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>flatten-maven-plugin</artifactId>
            <version>1.5.0</version>
            <configuration>
                <updatePomFile>true</updatePomFile>
                <flattenMode>resolveCiFriendliesOnly</flattenMode>
            </configuration>
            <executions>
                <execution>
                    <id>flatten</id>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>flatten</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 6. 环境变量赋值
```bash
# Linux/macOS
export MAVEN_OPTS="-Drevision=3.0.0"

# Windows
set MAVEN_OPTS=-Drevision=3.0.0
```

### 最佳实践

#### 1. 多模块项目配置
```xml
<!-- 父 pom.xml -->
<groupId>com.company</groupId>
<artifactId>parent</artifactId>
<version>${revision}</version>

<!-- 子模块继承 -->
<parent>
  <groupId>com.company</groupId>
  <artifactId>parent</artifactId>
  <version>${revision}</version>
</parent>
```

#### 2. 版本规范
- 快照版：`<revision>1.2.3-SNAPSHOT</revision>`  
- 正式版：`<revision>1.2.3</revision>`  

#### 3. CI/CD 集成示例
```yaml
# GitLab CI
deploy:
  script:
    - mvn deploy -Drevision=$CI_COMMIT_TAG
  rules:
    - if: $CI_COMMIT_TAG
```

### 常见问题

**问题**：构建警告 `[WARNING] 'version' contains an expression but should be a constant`  
**解决**：在子模块中移除 `<version>` 标签，完全继承父 POM

**问题**：发布的 pom.xml 包含 `${revision}`  
**解决**：配置 `flatten-maven-plugin` 插件
