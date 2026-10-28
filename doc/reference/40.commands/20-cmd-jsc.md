---
title: jsc 命令
module: 40.commands
keywords: [jsc, jsbin, JsExec, nashorn, 脚本引擎, sys, $wn, $log, -vars]
source: [impl/box/cmd/cmd_jsc.java, jsexec/JsExec.java, impl/box/cmd/jsc/jsc_walnut.js]
related: [../101.dev/10-write-jsbin.md]
---

# jsc 命令概述

## 为什么需要 jsc

`Walnut` 的定位是业务系统基座，业务逻辑天然是多变、常改的。
如果每改一点逻辑都要改 Java 代码、重新打包部署，迭代速度会被拖死。

`jsc` 命令让业务逻辑可以用 **JavaScript 脚本**编写，脚本就是普通对象
（`~/scripts/foo.js`），改完即生效，无需重启。同时脚本能调用全部
Walnut 命令，能力上不比 Java 代码差多少。

因此 Walnut 的业务开发约定是：

- 框架性、性能敏感的逻辑 → Java（自定义命令、模块）
- 业务性、经常变的逻辑 → jsbin 脚本（`jsc` 执行）

## 它是怎么调用脚本引擎的

`jsc` 由 `cmd_jsc` 实现，执行流程是：

1. 读取脚本内容（文件路径，或管道的标准输入）
2. 从 `-vars` 参数解析变量，通过 ScriptEngine 的上下文**注入为脚本的全局变量**
3. 由 `JsExec`（`src/com/site0/walnut/jsexec/`）创建引擎并执行
4. 脚本的 stdout 即命令的 stdout，可以被管道继续消费

### 用什么引擎

默认引擎是 **nashorn**（配置项 `jsc-dft-engine`，在 `web.properties`），
仅支持 **ES5** 语法——没有 `let`、箭头函数、模板字符串。
可用 `jsc -lsengine` 查看当前 JVM 里全部可用引擎，用 `-engine <名>` 临时切换。

### 注入了哪些内置对象

每个脚本运行前，`JsExec` 自动注入这些全局对象（定义在 `JsExec.java` 的 bindings）：

- **`sys`** —— 命令沙箱入口（`WnSystem`）：执行命令、读写对象、会话信息，
  是脚本与 Walnut 交互的主通道
- **`$wn`** —— 助手函数库（`jsc_walnut.js`）：`exec` / `execAsJson` /
  `genId` / `tryLock` 等高频操作的封装
- **`$log`** —— 带 trace ID 的日志接口（`info` / `warn` / `error` /
  `ajax_ok` / `ajax_error`）
- **`wc`** —— 当前线程上下文 `Wn.WC()`
- **`_`** —— lodash（预载）
- **`$g`** —— 域级全局对象：域的 `~/jsbin/_g.js` 会被加载到这里，
  公共函数沉淀其中
- **`_TRACE_ID`** —— 本次执行链路的 ID，用于在日志里串联一次调用

`jsc` 命令自身的常用参数：

```bash
jsc <脚本路径> [-vars '{a:1,...}'] [-engine 名称] [-debug] [-lsengine]
```

- `-vars` —— 注入脚本的全局变量（JSON），脚本里直接 `var a = a || 0;` 读取
- `-debug` —— 输出更多调试信息
- `-lsengine` —— 列出可用引擎

## 一个基本的例子

在控制台依次执行：

```bash
# 1. 创建一个脚本对象
cat > ~/hello.js <<'EOF'
$log.setup("hello", false, null);

sys.exec("echo I am " + sys.getAccountName());

var who = who || "world";
sys.out.printlnf("hello %s", who);

$log.info("hello.js done");
EOF

# 2. 运行它（不带参数）
jsc ~/hello.js

# 3. 带参数运行
jsc ~/hello.js -vars '{who:"walnut"}'
```

输出大致是：

```text
I am demo
hello world
```

这个例子里演示了三件事：执行一条 Walnut 命令（`sys.exec`）、
读取注入变量（`who`）、写日志（`$log.info`）。

## 典型场景

- **regapi 的后端**：API 对象的命令模板里 `jsc ~/api/xxx.js`，
  读 `sys.getCurrentObj()`（请求对象）里的 `http-qs-*` 参数，输出 JSON 响应
- **cron 定时任务**：命令模板就是 `jsc`，周期性执行业务逻辑
- **钩子**：`.js` 钩子的执行环境与本篇是同一套引擎与注入对象

# 延伸阅读

- 101.dev/10-write-jsbin.md —— jsbin 编写规范：脚本骨架、参数约定、
  输出契约、sys/$wn/$log 用法与专门场景示例
- 101.dev/30-extend-regapi.md —— jsc 在 regapi 里的完整实战
