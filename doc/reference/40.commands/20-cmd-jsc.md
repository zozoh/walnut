---
title: jsc 命令
module: 40.commands
keywords: [jsc, jsbin, JsExec, nashorn, 脚本引擎, sys, $wn, $log, -vars]
source: [impl/box/cmd/cmd_jsc.java, jsexec/JsExec.java, jsexec/DomainGlobalJsCache.java, impl/box/cmd/jsc/jsc_walnut.js]
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

- **`sys`** —— 命令沙箱入口：**注意它不是 `WnSystem`**，而是它的裁剪封装
  `JvmJsExecContext`；执行命令、读写对象，是脚本与 Walnut 交互的主通道
  （详见 21-cmd-jsc_sys.md）
- **`$wn`** —— 助手函数库（`jsc_walnut.js`）：`exec` / `execAsJson` /
  `genId` / `tryLock` 等高频操作的封装（详见 22-cmd-jsc_walnut.md）
- **`$log`** —— 带 trace ID 的日志接口（`info` / `warn` / `error` /
  `ajax_ok` / `ajax_error`）（详见 23-cmd-jsc_log.md）
- **`wc`** —— 当前线程上下文 `Wn.WC()`
- **`_`** —— lodash（预载）
- **`$g`** —— 域级全局对象：域的 `~/jsbin/_g.js` 会被加载到这里，
  公共函数沉淀其中
- **`_TRACE_ID`** —— 本次执行链路的 ID，用于在日志里串联一次调用

`jsc` 命令自身的常用参数：

```bash
jsc <脚本路径> [-vars '{a:1,...}'] [-engine 名称] [-debug] [-lsengine] [-clear]
```

- `-vars` —— 注入脚本的全局变量（JSON），脚本里直接 `var a = a || 0;` 读取
- `-debug` —— 输出更多调试信息
- `-lsengine` —— 列出可用引擎
- `-clear` —— 清掉当前域的 `_g.js` 缓存，见下文「缓存机制」

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

## 缓存机制

`jsc` 执行时注入的对象分三层缓存，理解这三层是排查
「我改了文件为什么不生效」「我的全局变量怎么串了」这类问题的关键。

### `_g.js`：按域缓存，编译结果常驻内存

每个域可以有一个 `~/jsbin/_g.js`，用来沉淀域级的公共函数
（自己的 `$g`）。系统对它的处理（`DomainGlobalJsCache`）是：

- 以**域**为单位隔离：缓存键是 `_g.js` 展开后的绝对路径
  `/home/{domain}/jsbin/_g.js`，不同域各自一份
- 首次执行时读取文件、**编译并求值**，结果（应当是一个函数）永久放入
  JVM 内存的 HashMap，之后每次 `jsc` 都直接复用，**永不过期**
- 文件不存在时，缓存一个空函数 `(function(){})`
- 脚本里拿到的 `$g`，就是以 `(sys, $log, $wn, _)` 调用这个函数
  的返回值——所以 `_g.js` 要写成 `function(sys,$log,$wn,_){ ... return {...} }`
  的形式

由此带来两个必须知道的后果：

1. **改了 `_g.js` 不会自动生效**——缓存里还是旧版本，必须执行
   `jsc -clear` 让当前域的缓存条目失效，下次执行才会重新读文件编译
2. **先缓存了空函数，后补文件也不生效**——如果某域第一次跑 `jsc` 时
   `_g.js` 还不存在，空函数就被缓存了；后来创建了文件，
   依然要等 `jsc -clear`

```bash
# 编辑完 ~/jsbin/_g.js 后，清当前域的缓存
jsc -clear
```

注意 `-clear` 只清**当前域**（当前登录用户所在域）的那一条，
其他域不受影响。

### 引擎与预载库：JVM 级，进程生命周期

`JsExec` 内部还有两个全局缓存（`JsExec.java`）：

- **ScriptEngine**：按引擎名缓存，nashorn 引擎整个 JVM 只有一份，
  所有域、所有执行共享。线程安全靠「每次执行创建独立的 Bindings」保证——
  脚本变量不可能跨请求串扰
- **预载库**（构造时编译一次，永不失效）：lodash（注入为 `_`）、
  `jsc_walnut.js`（编译成 `__use_wn` 函数）、
  `jsc_log.js`（编译成 `__use_log` 函数）

### 每次执行新建的：无缓存，天然隔离

以下对象**每次 `jsc` 执行都全新创建**，不存在任何缓存共享问题：

- `Bindings`（本次执行的变量容器）
- `$wn` / `$log` 实例——它们不是缓存对象本身，而是每次执行时用
  当次的 `sys`、`logx`、`_TRACE_ID` **调用** `__use_wn` / `__use_log`
  生成的新对象；因此 `$log` 里的 trace ID 每次都是本次链路唯一的
- `sys`、`wc`、`logx`、`_TRACE_ID`、`-vars` 注入的变量

一句话总结：`_g.js` 是唯一「改完要手动清」的东西，用 `jsc -clear`；
其余注入对象要么 JVM 级永不变（引擎、lodash），要么每次执行全新
（`sys`/`$log`/vars），不用担心串状态。

## lodash 的坑

脚本注入的 lodash 是完整版 4.17.15（`lodash.min.js`），
但**不要假设它的行为和浏览器里完全一致**——脚本里的很多对象
来自 Java 侧（`NashornObjectWrapper` 包裹的 Map/集合、`sys` 这样的
Java 接口代理），nashorn 的对象模型与浏览器不同，
某些函数对这些对象的表现会出人意料。两个已知的大坑：

### `_.assign` 不工作，用 `_.assignIn`

```js
// ❌ 静默失效：_.assign 只拷贝「自身可枚举属性」（基于 Object.keys），
//    而 nashorn 里的很多对象属性挂在原型链上，拷贝结果是个空对象
var copy = _.assign({}, $wn);

// ✅ 用 _.assignIn：基于 for...in，包含原型链上的可枚举属性
var copy = _.assignIn({}, $wn);
```

### `_.isEmpty` 不可靠，用 `_.keys(obj).length > 0`

```js
// ❌ 对 nashorn 对象（尤其是包裹过的 Map）的判断结果可能与浏览器不一致
if (_.isEmpty(obj)) { ... }

// ✅ 显式数 key 的个数
if (_.keys(obj).length == 0) { ... }
```

这两条是长期实践踩出来的约定：在 jsbin 里合并对象一律 `_.assignIn`，
判断对象为空一律 `_.keys(obj).length > 0`，不要图省事用浏览器习惯写法。

## 典型场景

- **regapi 的后端**：API 对象的命令模板里 `jsc ~/api/xxx.js`，
  读 `sys.getCurrentObj()`（请求对象）里的 `http-qs-*` 参数，输出 JSON 响应
- **cron 定时任务**：命令模板就是 `jsc`，周期性执行业务逻辑
- **钩子**：`.js` 钩子的执行环境与本篇是同一套引擎与注入对象

# 延伸阅读

- 21-cmd-jsc_sys.md —— `sys` 注入对象：命令执行、对象读写、输出输入
- 22-cmd-jsc_walnut.md —— `$wn` 助手函数库：错误处理、ID、锁、对象查询
- 23-cmd-jsc_log.md —— `$log` 日志对象：setup、trace ID、API 响应契约
- 101.dev/10-write-jsbin.md —— jsbin 编写规范：脚本骨架、参数约定、
  输出契约、sys/$wn/$log 用法与专门场景示例
- 101.dev/30-extend-regapi.md —— jsc 在 regapi 里的完整实战
