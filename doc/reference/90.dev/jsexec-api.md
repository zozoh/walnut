---
title: JS 执行环境（JsExec）
module: 90.dev
keywords: [JS, jsc, JsExec, nashorn, $wn, sys.exec, jsbin, _g.js]
source: [jsexec/, impl/box/cmd/cmd_jsc.java, impl/box/cmd/jsc/]
related: [custom-command, extend-regapi, ../40.commands/01_cmd_simple.md]
---

# JS 执行环境（JsExec）

Walnut 的 JS 脚本由 `JsExec` 引擎执行（`src/com/site0/walnut/jsexec/`），默认引擎 nashorn（`web.properties` 的 `jsc-dft-engine`）。`jsc` 命令、`.js` 钩子、`.js` 文件命令都走同一套环境。

## 脚本里有什么

每个脚本运行前自动注入这些全局变量（见 `JsExec.java` 的 bindings）：

- **`sys`**：`WnSystem` —— 命令沙箱入口
- **`$wn`**：Walnut 助手库（`jsc_walnut.js`）
- **`$log` / `log` / `logx`**：日志（带 trace ID）
- **`wc`**：`Wn.WC()` 当前上下文
- **`_`**：lodash（预载）
- **`$g`**：域级全局 JS 对象（见下）
- **`_TRACE_ID`**：本次执行链路 ID

## WnSystem 在 JS 中的用法

```js
// 我是谁
var me = sys.getMe();          // WnUser
var myName = sys.getMyName();

// 执行命令（结果=stdout 字符串）
var list = sys.exec2("o ~/data -l -json -cqn");

// 执行命令（结果丢弃/直接输出）
sys.exec("echo hello");

// I/O（与 Java WnIo 同能力）
var o = sys.io.readPath(".");
sys.io.writeText(sys.io.create(null, "~/out.txt", WnRace.FILE), "hi");

// 当前对象（API 场景=请求对象）
var req = sys.getCurrentObj();
var uid = req.getString("http-qs-uid");
```

## $wn 助手库（jsc_walnut.js）

`$wn` 封装了高频操作（源码 `impl/box/cmd/jsc/jsc_walnut.js`）：

```js
var id  = $wn.genId();                        // 生成 ID（val @gen 封装）
var ids = $wn.genIds(10);                      // 批量
var o   = $wn.getObjById("45g..");             // 取对象（返回 JS 对象）
var nm  = $wn.getMyName();
var lock = $wn.tryLock("myJob", 600);          // 申请锁（返回锁对象）
$wn.freeLock(lock);                            // 释放
```

## 域级全局库：jsbin/_g.js

每个域可放 `~/jsbin/_g.js` 定义域内公共函数，`DomainGlobalJsCache` 按域缓存，脚本里经 `$g` 调用——把公共逻辑（如统一响应格式、数据库访问函数）沉淀在这里。

## jsc 命令

```bash
jsc ~/scripts/foo.js                 # 执行 JS 文件
echo 'sys.exec2("ls")' | jsc        # 从管道执行
```

## 与 Java 的互操作

- JS 里可直接使用绑定进来的 Java 对象（`sys`、`wc`）；
- Map/Collection 自动经 `NashornObjectWrapper` 转成 JS 友好形式；
- 需要 Nutz 工具类时可 `Java.type("org.nutz.lang.Times")` 等方式取用。

## 注意事项

- nashorn 已随 JDK 演进有变化，高版本 JDK 需关注 `jsc-dft-engine` 的可用性；
- 长循环/大内存操作别放在 API 的 JS 里——投递给 [任务栈](../60.sched-async/cron-pipeline.md)；
- JS 错误栈会进 `BOX` 日志，配合 `_TRACE_ID` 串联。

## 延伸阅读

- [jsc 命令](../40.commands/01_cmd_simple.md)
- [实战：扩展 API](extend-regapi.md)
- [钩子](../60.sched-async/hooks.md) — .js 钩子的执行环境
