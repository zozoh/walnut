---
title: 命令模型（沙箱、管道与三种写法）
module: impl.box
keywords: [命令, 沙箱, 管道, 重定向, 简单命令, 复杂命令, 过滤命令, man]
source:
  - util/WnRun.java
  - impl/box/WnSystem.java
  - impl/box/JvmExecutorFactory.java
  - impl/box/JvmExecutor.java
  - impl/box/JvmHdlExecutor.java
  - impl/box/JvmFilterExecutor.java
related: [01-std-obj, session-env, custom-command]
---

# 命令模型（沙箱、管道与三种写法）

在 Walnut 里，**一切操作都是命令**，命令模型基本高仿 Linux/Unix：
有标准三流（stdin/stdout/stderr）、有管道 `|`、有重定向 `>` `>>`，命令可以拼装成程序。
命令可以模块化扩展——新增功能就是新增一个 `cmd_*` 类加一篇手册，不动内核。

本篇讲「是什么、为什么」；每个命令的写法和全部清单见 [40.commands](../40.commands/README.md)。

## 执行模型

```bash
用户输入命令行
   │
   ▼
WnRun（命令行解析：管道 |、重定向 >、>>、变量 ${...}）
   │
   ▼
WnBoxService 分配沙箱（WnBox）── 每个沙箱绑定一个会话身份
   │
   ▼
JvmExecutorFactory 按命令名加载 cmd_* 类
   │
   ▼
cmd_*.exec(WnSystem sys, String[] args)
   WnSystem = {in, out, err, io, session, auth, jef, cmdOriginal, ...}
```

**安全模型**：每条命令都在 `Wn.WC().su(user)` 的线程上下文中执行——命令能做什么，
取决于它的**会话身份**，而不是谁发起了 HTTP 请求。HTTP API 之所以安全，
就是因为 API 文件所有者的权限决定了命令的边界。

## 三种命令写法

系统现有 73 个内置命令 + 84 个扩展命令 = 157 个，按**写法（继承关系）**分三种模式：

- **简单命令**（基类 `JvmExecutor`）：一个源文件，参数进来、结果出去。内置命令全部属于这种。见 [简单命令](../40.commands/01_cmd_simple.md)。
- **复杂命令**（基类 `JvmHdlExecutor`）：子命令是 `hdl/` 目录下的独立类，关键逻辑封装为服务类供其他模块复用。见 [复杂命令](../40.commands/02_cmd_complex.md)。
- **过滤命令**（基类 `JvmFilterExecutor`）：子命令是 `@过滤器`，共享一个上下文对象，按顺序加工、任意搭配。见 [过滤命令](../40.commands/03_cmd_filter.md)。

```bash
echo hello > ~/a.txt                  # 简单命令：参数直接消费
o ~/docs @query -l @json               # 过滤命令：@query 填上下文，@json 输出
sqlx @vars '{id:1}' @query pet.query  # 过滤命令 + SqlProcessor 服务复用
```

## 管道与重定向

```bash
# 管道：前一个命令的 stdout 是后一个的 stdin
cat ~/data.txt | grep ERROR | wc -l

# 重定向：写入对象（> 覆盖，>> 追加）
echo "hello" > ~/docs/a.txt
cat ~/docs/a.txt >> ~/docs/b.txt

# 模板渲染后批量执行（o -tmpl 生成命令文本，run 逐行执行）
o /domain -tmpl 'o id:@{id} -u \'tp:"A"\';' | run
```

完整规则（执行模型、变量替换、run 语义、注意事项）见 [命令管道](../40.commands/04_cmd_pipe.md)
与 [命令输入输出](../40.commands/05_cmd_std_in_out.md)。

## 帮助系统

每个命令配中文手册，与源码同包，运行时直接查：

```bash
man o            # o 总览（含全部 @子命令列表）
man o -k size    # 在手册里搜关键词
man sqlx query   # 子命令手册
cmds             # 列出全部可用命令
```

- 内置命令手册：`src/com/site0/walnut/impl/box/cmd/man/zh_cn/`
- 扩展命令手册：各 `src/com/site0/walnut/ext/**/man/zh_cn/`
- 域用户可在 `~/bin/` 放同名 JS/脚本**覆盖系统命令**

## 命令即 API

命令的三流天然映射 HTTP：regapi（`.regapi` 文件）执行命令时，请求体是 stdin、
响应体是 stdout、异常变成错误响应。写 API 只需关心 stdout 输出什么——
这是 Walnut 最高效的集成方式（详见 [regapi](../50.regapi/api-object.md)）。

## 深入阅读

- [40.commands 命令参考](../40.commands/README.md)：三种写法的完整参考与全部命令清单
- [自定义命令](../90.dev/custom-command.md)：编写自己的命令
- [会话与环境变量](03-session-env.md)：命令执行的身份与变量
