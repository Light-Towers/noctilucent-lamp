这份关于 **Arthas**（阿里开源的 Java 诊断工具）的深度实战笔记，旨在帮助你从基础监控跨越到能够处理复杂的生产环境问题。

------



# 🛠️ Arthas 深度实战笔记：从入门到 "手术级" 诊断



Arthas 是 Java 生产环境的**“瑞士军刀”**。它最大的价值在于：**在不重启服务、不修改代码的情况下，动态地透视和修改运行中的 Java 程序。**



## 1. 基础速查 (Cheat Sheet)



在进入高级技巧前，确保你掌握了这些高频基础命令：

| **命令**    | **作用**     | **典型场景**                                        |
| ----------- | ------------ | --------------------------------------------------- |
| `dashboard` | 全局监控面板 | 查看 CPU 飙高、线程数、内存使用情况                 |
| `thread`    | 线程管理     | `thread -n 3` 查看最忙的3个线程；`thread -b` 找死锁 |
| `jad`       | 反编译       | 确认生产环境跑的代码版本是否正确                    |
| `sc` / `sm` | 搜索类/方法  | 查找类加载器，查看方法信息                          |

------



## 2. 高级使用技巧 (Advanced Techniques)



这是本笔记的核心。普通用户用来看面板，高级用户用来做“微创手术”。



### 技巧一：由“点”及“面”的精准观测 (`watch`)



watch 是最强大的命令之一，能观察函数的调用情况。

痛点： 线上偶发 bug，不知道入参是什么，或者返回值为什么不对。

- **基础用法：** 查看返回值

  Bash

  ```bash
  watch com.example.demo.ArthasTest testMethod returnObj
  ```

- **高级用法 1：同时看入参、返回值、异常，并展开对象结构**

  - `-x 2`: 展开对象层级（默认为 1，只看 toString，2 可以看属性）。
  - `{params, returnObj, throwExp}`: OGNL 表达式，组合输出。

  Bash

  ```bash
  watch com.example.demo.ArthasTest testMethod "{params, returnObj, throwExp}" -x 2
  ```

- **高级用法 2：条件过滤 (只抓取特定情况)**

  - **场景：** 只有当第一个参数是 "admin" 且 耗时超过 200ms 时才打印。

  Bash

  ```bash
  watch com.example.demo.ArthasTest testMethod "{params, returnObj}" 'params[0]=="admin" && #cost>200' -x 2
  ```



### 技巧二：性能瓶颈定位 (`trace` vs `stack`)



**痛点：** 接口响应慢，不知道是 SQL 慢、RPC 慢还是逻辑慢。

- trace (调用链耗时追踪)：

  它会像 Chrome Network 面板一样，打印出方法内部调用路径及耗时，自动高亮最耗时的部分。

  Bash

  ```bash
  # 过滤掉 JDK 原生方法，只看业务代码，且耗时超过 100ms
  trace com.example.demo.ArthasTest testMethod  '#cost > 100' --skipJDKMethod false
  ```

  > **注意：** `trace` 只能看到当前方法的下一层调用。如果瓶颈在深层，需要动态增强下一层方法。

- stack (反向追踪调用源)：

  痛点： 这个方法被谁调用了？（用于寻找莫名其妙的调用来源）。

  Bash

  ```bash
  stack com.example.demo.ArthasTest testMethod
  ```



### 技巧三：时光倒流 (`tt` - Time Tunnel)



痛点： 为了复现一个 Bug，需要不断地手动触发请求（填表单、点按钮）。

tt 命令可以记录下方法的调用记录，并在之后随时回放。

1. **开始记录：**

   Bash

   ```bash
   tt -t com.example.demo.ArthasTest testMethod
   ```

   *(屏幕会输出一个表格，包含 INDEX, TIMESTAMP, COST 等)*

2. 查看记录详情：

   假设 INDEX 为 1002。

   Bash

   ```bash
   tt -i 1002
   ```

3. ⚡️ 重放请求 (核心技巧)：

   不需要前端配合，直接在 Arthas 内部重放该次调用。

   Bash

   ```bash
   tt -i 1002 -p
   ```

   *(如果是并发问题，可以加 `--replay-times 5 --replay-interval 1000` 进行压力回放)*



### 技巧四：线上热更新代码 (Hot Swap)



**痛点：** 线上发现简单的逻辑错误（如 `if (a == 1)` 写成了 `if (a == 2)`），不想走漫长的发布流程。

**操作流程 (必须熟练)：**

1. **反编译代码到文件：**

   Bash

   ```bash
   jad --source-only com.example.demo.UserController > /tmp/UserController.java
   ```

2. 编辑代码：

   使用 vim /tmp/UserController.java 修改逻辑。

3. 内存编译 (Memory Compiler)：

   找到该类的 ClassLoader Hash (使用 sc -d *UserController 查看 classLoaderHash)。

   Bash

   ```bash
   mc -c <ClassLoaderHash> /tmp/UserController.java -d /tmp
   ```

   *(这会在 /tmp 下生成 .class 文件)*

4. **热加载 (Retransform)：**

   Bash

   ```bash
   retransform /tmp/com/example/demo/UserController.class
   ```

   *成功后，代码立即生效！*



### 技巧五：火焰图分析 (`profiler`)



**痛点：** CPU 莫名飙高，`thread -n` 看不出来明显的规律。

- **启动采样：**

  Bash

  ```bash
  profiler start
  ```

- **查看采样状态：**

  Bash

  ```bash
  profiler status
  ```

- **停止并生成火焰图：**

  Bash

  ```bash
  profiler stop --format html
  ```

  Arthas 会输出一个 HTML 文件路径。将其下载到本地浏览器打开，**平顶且宽**的色块就是 CPU 热点。

------



## 3. OGNL 表达式：Arthas 的灵魂



Arthas 的很多高级过滤依赖 OGNL (Object-Graph Navigation Language)。掌握以下几个套路，你的诊断能力会提升一个档次：

- 调用静态变量/方法：

  想要查看 Spring Context 或者某个静态 Map 的值？

  Bash

  ```bash
  # 语法: @全限定类名@静态字段/方法
  getstatic com.example.demo.GlobalConfig CONFIG_MAP
  ```

- 获取 Spring Bean (高级)：

  如果代码里有类似 SpringContextHolder 的静态工具类，你可以直接拿到 Bean 并调用其方法：

  Bash

  ```bash
  # 获取 UserService 并调用 getUserById(1)
  ognl '@com.example.utils.SpringContextHolder@getBean("userService").getUserById(1)'
  ```

------



## 4. 生产环境安全守则 (必读)



1. **不要全量 Trace：** 在高并发的核心接口上使用 `trace` 或 `watch` 且不加条件过滤，可能会导致服务吞吐量急剧下降甚至卡死。**务必加上 `#cost > x` 或限制 `limit`。**
2. **退出习惯：** 使用完 Arthas 后，不要直接 `Ctrl+C` 退出，应该执行 `stop` 或 `shutdown`。
   - `quit`/`exit`：只是退出客户端，Arthas 服务端还在运行。
   - `stop`：彻底关闭 Arthas 服务端，并**重置所有被增强的类**（恢复原样）。
3. **批量操作：** 如果有一组复杂的命令，可以写成脚本 `auth_script.as`，然后 `java -jar arthas-boot.jar -f auth_script.as` 批量执行。

------

# Arthas 的原理

Arthas 的核心原理可以总结为一个公式：

$$\text{Arthas} = \text{Java Agent} + \text{Instrumentation API} + \text{ASM (字节码增强)}$$

通俗地说，Arthas 就像是一个**“纳米机器人”**，利用 JVM 提供的官方后门（Attach API）钻进正在运行的进程中，利用字节码技术对你的代码进行**动态修改（增强）**，插入监控逻辑，并在使用完后自动恢复原状。

以下是其核心原理的深度拆解：

------



### 1. 如何“混进”正在运行的 JVM？



**核心技术：Java Attach API**

你可能知道启动 Java 应用时可以使用 `-javaagent` 参数加载代理。但 Arthas 不需要重启，它是怎么做到的？

- Dynamic Attach（动态挂载）：

  JDK 6 以后引入了 Attach API。Arthas 的启动程序（Client）会调用这个 API，连接到目标 JVM 进程。

- 加载 Agent：

  连接成功后，Arthas 会命令目标 JVM 加载 arthas-agent.jar。此时，Arthas 的代码就正式在你的业务 JVM 内部运行了。



### 2. 如何“看清”并“修改”代码？



**核心技术：Instrumentation API + ASM**

这是 Arthas 最核心的“黑科技”部分。当你执行 `watch`、`trace` 或 `jad` 时，背后发生了一系列精密操作：



#### A. 查找类 (Find)



通过 Instrumentation 接口，Arthas 可以获取 JVM 中所有已加载的类信息（`getAllLoadedClasses`），这也是 `sc` (Search Class) 命令的原理。



#### B. 字节码增强 (Bytecode Instrumentation) —— AOP 的动态版



当你输入 `trace com.example.Demo methodA` 时：

1. **抓取元数据：** Arthas 找到 `Demo` 类。

2. **代码重写 (Retransform)：** Arthas 利用 **ASM**（一个操作字节码的框架）在内存中修改 `methodA` 的字节码。它会在方法的开头、结尾、异常抛出点插入 Arthas 的统计代码（即**探针**）。

   - *修改前：*

     Java

     ```java
     public void methodA() {
         process();
     }
     ```

   - *修改后（逻辑上的等价代码）：*

     Java

     ```java
     public void methodA() {
         long start = System.nanoTime(); // Arthas 插入的
         try {
             process();
             // Arthas 插入：记录返回值，计算耗时，发送给客户端
             Arthas.end(start, returnValue); 
         } catch (Exception e) {
             // Arthas 插入：记录异常
             Arthas.exception(e); 
             throw e;
         }
     }
     ```

3. **热替换：** 修改后的字节码通过 `Instrumentation.retransformClasses()` 方法提交给 JVM。JVM 接着就会运行这段“加料”后的代码，从而实现监控。

> **注意：** 当你执行 `stop` 命令时，Arthas 会再次调用 `retransformClasses`，把字节码还原成原始状态，做到“来去无痕”。



### 3. 为什么 Arthas 不会搞挂我的业务？



**核心技术：独立 ClassLoader 隔离**

你可能会担心：Arthas 自己用了很多第三方库（如 Netty, Fastjson 等），会不会和我的业务代码里的 jar 包版本冲突？

- ArthasClassLoader：

  Arthas 所有的代码都是通过一个自定义的 ArthasClassLoader 加载的。

- 隔离机制：

  这个 ClassLoader 独立于你的 Spring Boot 或 Tomcat 的 ClassLoader。这意味着 Arthas 内部的类对外是不可见的，反之亦然（除非显式反射）。这种物理上的隔离保证了 Arthas 的侵入性极低，不会导致 ClassNotFoundException 或 NoSuchMethodError 等依赖冲突问题。



### 4. 怎么跟我交互的？



**核心技术：C/S 架构 + Netty**

Arthas 本质上是一个跑在 JVM 里的服务器。

- **Server 端：** 也就是注入到 JVM 里的 Agent，它启动了一个基于 Netty 的服务端，监听端口（默认 3658）。
- **Client 端：** 当你运行 `arthas-boot.jar` 时，其实是启动了一个 Telnet/WebSocket 客户端去连接这个 Server。
- **优势：** 这就是为什么你可以通过浏览器（Web Console）或者远程终端去诊断一台无法直接登录的服务器。

------



### 原理总结图解



代码段

```
graph TD
    User[用户 (Client)] -->|1. 输入 trace 命令| ArthasBoot
    ArthasBoot -->|2. Attach 到目标 PID| TargetJVM[目标 JVM 进程]
    
    subgraph TargetJVM
        direction TB
        Agent[Arthas Agent (Server)]
        Instrumentation[Instrumentation API]
        OriginalClass[原始业务类 Class]
        
        Agent -->|3. 收到命令| Instrumentation
        Instrumentation -->|4. 读取字节码| OriginalClass
        Instrumentation -->|5. ASM 修改字节码| ModifiedClass[增强后的字节码\n(插入了耗时统计逻辑)]
        ModifiedClass -->|6. Hot Swap (retransform)| OriginalClass
    end
    
    OriginalClass -->|7. 运行并产生数据| Agent
    Agent -->|8. 回传结果| User
```

------



### 💡 深入思考：Arthas 的局限性



了解原理后，你就知道 Arthas 也有做不到的事：

1. **无法增加/删除字段或方法：** 基于 JVM 的 `retransform` 限制，你只能修改方法体内部的逻辑，不能改变类的结构（比如加一个成员变量）。如果修改了结构，热加载会失败。
2. **本地变量无法直接窥探：** 在 `watch` 时，我们通常只能看到入参、返回值和成员变量。方法内部的临时变量（Local Variables）如果没有被编译器优化掉，理论上能看，但 Arthas 目前主要聚焦在方法切面。
3. **性能开销：** 虽然是内存操作，但如果 `trace` 一个被高频调用（如每秒几万次）的方法，插入的探针代码和网络传输量会瞬间压垮 CPU 和 IO。

------



### 我能为你做的下一步



既然了解了原理，你可能对**“如何利用这个原理做一些除了监控之外更骚的操作”**感兴趣。

**你想了解下面哪一个？**

1. **手动写一个简易的 Java Agent**：只需 2 个文件，亲手实现一个极简版 Arthas，体验 `premain` 和 `agentmain` 的威力。
2. **深入 `retransform` 失败的场景**：详细讲解为什么有时候修改了代码却热部署失败（Schema change 问题）。