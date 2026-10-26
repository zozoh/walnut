---
title: Walnut 命令机制
module: impl.box
keywords: [命令, 命令机制, 简单命令, 复杂命令, 过滤命令, 管道, 重定向, man]
source:
  - impl/box/JvmExecutor.java
  - impl/box/JvmHdlExecutor.java
  - impl/box/JvmFilterExecutor.java
  - impl/box/JvmAtomRunner.java
related: [../10.concepts/04-command-model.md, custom-command, o, sqlx, tmpl]
---

# Walnut 命令机制

在 Walnut 里，**一切操作都是命令**。本目录是命令机制的参考手册：

```bash
40.commands/
|-- README.md             # 本篇：命令机制概览
|-- 01-cmd-simple.md      # 简单命令：一个源文件，一次调用一件事
|-- 02-cmd-complex.md     # 复杂命令：hdl 子命令 + 服务类
|-- 03-cmd-filter.md      # 过滤命令：@过滤器共享上下文，任意搭配
|-- 04-cmd-pipe.md        # 命令管道：| 组合多个命令
|-- 05-cmd-std-in-out.md  # 命令输入输出：三流与 > >> 2>&1 重定向
|-- 60-cmd-table.md       # 命令总表：158 个命令的来源/写法/分类
```

重点命令（`o` / `sqlx` / `webx` / `mailx` / `tmpl`）的手册在各自源码包的
`man/zh_cn/` 下，运行时 `man <命令名>` 查看。

## 命令机制要解决的问题

核心诉求是：**功能可以模块化地扩展，而不必改动系统核心逻辑**。

- 新增一个功能 = 新增一个 `cmd_*` 类 + 一篇手册，放到约定的包路径下即可被系统发现（当然，这还是需要你重新编译部署的）
- 命令是 Walnut 的统一扩展点：HTTP API（regapi）、定时任务、钩子，最终都在执行命令
- 命令天然携带身份与权限：每条命令在会话用户的权限上下文里执行，不需要额外的访问控制代码
- 命令里还可以再嵌套调用其他命令
- 通过 `cmd_jsc` 命令，可以调用用户编写 `ES5` 兼容脚本完成任意业务逻辑

系统现有 **73 个内置命令**（`impl/box/cmd/`，随系统发布）+ **85 个扩展命令**（`ext/**/`，随系统发布），调用方式完全一致，只是代码位置与维护归属不同。全部命令的清单见 60-cmd-table。

## 三种基本模式 + 两种组合手段

按**写法（继承关系）**分，命令有三种基本模式，分别对应不同的场景复杂度：

- **简单命令** —— 基类 `JvmExecutor`，一般一个源文件：参数进来、结果出去。适合单一功能。
  例：`echo`、`cat`。详细介绍见 简单命令。
- **复杂命令** —— 基类 `JvmHdlExecutor`，子命令是 `hdl/` 目录下的独立类，关键逻辑封装为服务类供其他模块复用。适合「一个领域、多种操作」。
  例：`xapi`、`cron`、`task`。详细介绍见 复杂命令。
- **过滤命令** —— 基类 `JvmFilterExecutor`，子命令是 `@过滤器`，共享一个上下文对象，按顺序依次加工，可任意搭配。适合「输入 → 多步加工 → 输出」。
  例：`o`、`sqlx`、`tmpl`。详细介绍见 过滤命令。

三种模式之上，还有两个横向的组合手段：

- **命令管道** —— 用 `|` 把多个命令首尾相接，前一个的标准输出作为后一个的标准输入，实现更复杂的操作。见 命令管道。
- **命令输入输出** —— 标准三流（stdin/stdout/stderr）+ `>` `>>` 重定向到 Walnut 对象，命令的输出可以落盘、可以作为 API 响应。见 命令输入输出。

## 有特点的命令举例

```bash
# 简单命令：直接消费参数，输出结果
echo "hello" | md5sum
cat ~/docs/readme.md | grep 安装

# 过滤命令：@子命令共享上下文，任意搭配
#   查询 tp 为 A 的数据库记录，更新 star 字段，以 JSON 输出
o ~/docs @query 'tp:"A"' @update 'star:1' @json -c
#   模板来自文件、变量来自管道
echo '{name:"wendal"}' | tmpl @load -f ~/tpl.txt @vars @render

# 复杂命令：子命令分发 + 服务类复用
xapi req weixin pay /order/create -force    # 生成第三方 API 请求
cron add '0 3 * * *' 'o ~/logs @clean'      # 定期任务

# 管道组合：查询 → 渲染成命令 → 批量执行
o /domain -tmpl "o id:@{id} -u \"tp:'A'\";" | run

# 重定向：输出落到对象上
cat ~/logs/app.log | grep ERROR > ~/archive/errors.txt
```

每个命令的详细用法，运行时查 `man <命令名> <子命令?>`（如 `man o`、`man xapi req`）；
手册源文件与源码同包，见 简单命令·文档放在哪里。

## 扩展阅读

- 命令管道 / 命令输入输出：横向组合手段的完整规则
- 命令执行的整体流程：从命令行文本到沙箱执行
- 自定义命令：如何编写自己的命令（覆盖系统命令或新增扩展）
- 命令总表：158 个命令的来源、写法与逻辑分类
- 全量索引：按关键词定位全部参考文档
