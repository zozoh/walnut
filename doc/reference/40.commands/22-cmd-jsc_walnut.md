---
title: $wn 注入对象
module: 40.commands
keywords: [jsc, $wn, jsc_walnut.js, execAsJson, genId, tryLock, 助手函数]
source:
  - impl/box/cmd/jsc/jsc_walnut.js
related:
  - 20-cmd-jsc.md
  - 21-cmd-jsc_sys.md
---

# $wn 注入对象概述

`$wn` 是注入脚本的**助手函数库**，源码是 `jsc_walnut.js`——
一个 `(function(sys, $log){ return {...} })` 形式的模块，
每次 `jsc` 执行时用当次的 `sys`/`$log` 调用一次生成新实例。

它做的事本质上都基于 `sys.exec2`：把高频操作封装成 JS 函数，
顺带统一了错误处理。因此本篇的函数都可以自己用 `sys.exec2` 写出来，
只是用 `$wn` 更短、更稳。

## 执行命令（带错误处理）

```js
// 执行命令，失败时直接抛异常（错误串 e.xxx:原因 会被转成 Error 对象）
var re = $wn.exec("o ~/data/pet.json @json -cqn");

// 执行命令并把输出解析为 JSON 对象
var pet = $wn.execAsJson("o ~/data/pet.json @json -cqn");

// 带标准输入
var re = $wn.exec("jsonx @schema", JSON.stringify({nm:"demo"}));
```

对比 `sys.exec2`：`sys.exec2` 失败返回错误**字符串**，要自己做判断；
`$wn.exec` 失败直接**抛异常**，配合标准骨架里的 try/catch 更顺手。

错误判断的逻辑在 `checkExecReturn`：返回串以 `e.` 开头即视为错误，
转成带 `code` / `reason` / `ok=false` 的 Error 对象。
`checkExecReturnAndParseJson(re, returnError)` 是其 JSON 版：
非错误则解析 JSON，是错误则按 `returnError` 决定抛还是返回错误对象。

## ID 生成

```js
var id  = $wn.genId();              // 一个 snowQ 模式 ID
var ids = $wn.genIds(5);            // 一次生成 5 个
var id2 = $wn.genId("symb::6");     // 指定模式，对应 val @gen 的语法
```

底层是 `val @gen '<mode>' -n <n> -json`，模式写法见 `val` 命令的 man 文档。

## 对象查询

```js
// 按 ID 取对象（两段式 ID 也支持），返回解析好的 JS 对象
var pet = $wn.getObjById("3h..4x");

// 解析两段式 ID "homeId:selfId"
var pid = $wn.parseObjId("abc:3h..4x");   // => {homeId:"abc", selfId:"3h..4x"}
var pid2 = $wn.parseObjId("3h..4x");     // => {selfId:"3h..4x"}

// 当前用户名
var me = $wn.getMyName();
```

## 锁

```js
// 尝试加锁，返回锁对象（含 name / privateKey），拿不到锁返回 null
var lock = $wn.tryLock("myjob", 3600);
if (lock) {
  try {
    // 干活
  } finally {
    $wn.freeLock(lock);
  }
}

// 阻塞式加锁：拿不到就重试（默认 3 次、间隔 3 秒）
var lock = $wn.tryLockBlock("myjob", 3600, 3, 3);
```

锁的语义由 `lock` 命令实现，`tryLock` 只是它的封装，
细节见 `lock` 命令的 man 文档。

## 其他工具

- `$wn.toJsonStr(jsobj)` —— 对象转 JSON 字符串；已是字符串则原样返回
- `$wn._toObj(obj)` —— 字符串形式的 JSON 自动 `eval` 成对象
  （内部方法，业务脚本一般用不到）

# 延伸阅读

- 21-cmd-jsc_sys.md —— `sys`：这些助手函数底下的命令执行通道
- 101.dev/10-write-jsbin.md —— jsbin 编写规范中 `$wn` 的用法约定
- 源码：src/com/site0/walnut/impl/box/cmd/jsc/jsc_walnut.js
