---
title: $log 注入对象
module: 40.commands
keywords: [jsc, $log, jsc_log.js, 日志, traceID, ajax_ok, ajax_error, printJsError]
source:
  - impl/box/cmd/jsc/jsc_log.js
  - util/log/WnJsLogProxy.java
related:
  - 20-cmd-jsc.md
  - 21-cmd-jsc_sys.md
---

# $log 注入对象概述

`$log` 是注入脚本的**日志与输出契约对象**，源码是 `jsc_log.js`——
与 `$wn` 一样是每次 `jsc` 执行时用当次的 `sys`、`logx`、`_TRACE_ID`
生成的新实例。

它解决两件事：

- 日志统一走 `logx`（`WnJsLogProxy`），自动带上**标签与 trace ID**，
  在日志系统里可以按一次调用链路串联
- API 类脚本的响应体契约（`ajax_ok` / `ajax_error`），输出格式全局统一

## 初始化：setup

脚本开头通常先 setup 一次：

```js
$log.setup("my_script", false, null);
// 参数：tag（日志标签）, debug（是否同时打印到 stdout）, prefix（行前缀）, quiet
```

- `tag`：日志行的标签列，定位日志时按它过滤
- `debug`：`true` 时 `info` 级别日志会同时打印到 `sys.out`
  （warn/error 始终打到 `sys.err` 侧）
- `quiet`：`true` 时 `info` 日志完全静默（warn/error 不受影响）
- `prefix`：给每条日志消息加固定前缀

也有单独的 setter：`setTag` / `setDebug` / `setPrefix` / `setTraceID`。

## 输出日志

四个方法签名都是变参 printf 风格（`%s` 占位）：

```js
$log.info("begin with id=%s", id);
$log.warn("config missing, use default");
$log.error("fail to load: %s", errCode);

try {
  // ...
} catch (err) {
  // JS 异常专用：自动附加错误栈
  $log.printJsError(err);
}
```

- `info` 受 `quiet` 开关控制；`warn` / `error` 不受
- 所有日志都带 `[tag] [traceID]` 前缀写入日志系统

## API 响应契约

给 regapi 写后端时，响应体统一由这两个方法产出：

```js
// 成功：输出 {"ok":true,"data":...} 到标准输出
$log.ajax_ok({id: id, name: "Be"});

// 失败：输出 {"ok":false,"errCode":"e.xxx","data":"原因"}，同时记错误日志
$log.ajax_error("e.pet.noexist", "宠物不存在");

// 也可以直接传 Error 对象（来自 $wn.exec 的抛错）
try {
  $wn.exec("o ~/x @update 'tp:media'");
  $log.ajax_ok("done");
} catch (err) {
  $log.ajax_error(err);
}
```

- 两者都是**先写日志、再向 `sys.out` 打印 JSON**——响应体就是这一行 JSON
- `ajax_re(obj)` 是它们的底层：直接向 stdout 输出一段 JSON（字符串或对象），
  成功失败自己拿捏时用

## 典型骨架

jsbin 规范里的标准用法（详见 101.dev/10-write-jsbin.md）：

```js
$log.setup("my_script", debug, prefix);
try {
  $log.info("my_script begin, id=%s", id);
  __main(id);
} catch (err) {
  $log.printJsError(err);
  $log.error("my_script error: %s", err);
} finally {
  $log.info("my_script done in %sms", Date.now() - msBegin);
}
```

# 延伸阅读

- 21-cmd-jsc_sys.md —— `sys`：`$log` 的 debug 输出与 API 响应都写到它的 out/err
- 101.dev/10-write-jsbin.md —— 脚本骨架与输出契约的完整约定
- 源码：src/com/site0/walnut/impl/box/cmd/jsc/jsc_log.js
