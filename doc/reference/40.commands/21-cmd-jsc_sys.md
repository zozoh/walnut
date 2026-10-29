---
title: sys 注入对象
module: 40.commands
keywords: [jsc, sys, JsExecContext, JvmJsExecContext, exec2, 命令执行, 脚本注入]
source:
  - util/JvmJsExecContext.java
  - util/JsExecContext.java
related:
  - 20-cmd-jsc.md
  - 22-cmd-jsc_walnut.md
  - 23-cmd-jsc_log.md
---

# sys 注入对象概述

每个 jsbin 脚本都会注入一个 `sys` 对象。注意：**它不是 `WnSystem`**，
而是它的一个裁剪封装 `JvmJsExecContext`（实现接口 `JsExecContext`）。
这样设计有两个原因：

- 脚本引擎是多线程共享的，注入对象的接口越小，暴露给脚本的面就越可控
- `jsc` 的执行环境不一定来自命令沙箱（例如 regapi、钩子也会调用 `JsExec`），
  用一个统一的瘦身接口，比直接塞一个完整的 `WnSystem` 更稳

所以脚本里能用的就是本篇列出的这些成员；没列出的能力（会话管理、
环境变量等），请通过 `sys.exec(...)` 执行相应的命令完成。

## 命令执行

`sys` 的主通道是执行 Walnut 命令，按返回值形态分三组：

```js
// 执行命令，输出直接落到脚本的标准输出（可被管道继续消费）
sys.exec("o 'id:3h..4x' @update 'tp:media'");

// 执行命令，返回标准输出的字符串
var re = sys.exec2("me -json -cqn");

// 执行命令，返回标准输出的字符串，并可传入标准输入
var re = sys.exec2("jsonx @schema", JSON.stringify({nm:"demo"}));

// 带格式参数的版本
sys.execf("o %s @json", "~/data/pet.json");
var re = sys.exec2f("o %s @json -cqn", "~/data/pet.json");
```

- `exec2` 返回字符串；命令执行失败时返回 `e.xxx:原因` 形式的错误串
  （不会抛异常），判断与解析见 22-cmd-jsc_walnut.md 的 `checkExecReturn`
- `exec2map` / `exec2list`：`exec2` 的结果直接解析为 Map / Map 列表，
  解析失败返回 `null`

## 对象读写

```js
// 按路径找对象（支持 ~ 与环境变量），不存在返回 null / 抛错
var o  = sys.fetch("~/data/a.json");
var o2 = sys.check("~/data/a.json");

// 低阶 IO 接口（WnIo），对象级操作走它
var io = sys.io();

// 文本读写（路径不存在会抛错）
sys.writeText("~/result.txt", "hello");
var txt = sys.readText("~/data/a.json");

// 读二进制片段
var bytes = sys.readBytes("~/big.bin", 0, 1024);
var part  = sys.readTextAt("~/big.txt", 100, 50);

// 路径展开：把 ~/xxx 与环境变量展开为绝对路径
var aph = sys.path("~/jsbin/_g.js");   // => /home/demo/jsbin/_g.js
```

## 输出与输入

`out` / `err` / `in` 是公开字段，脚本里直接访问：

```js
sys.out.printlnf("hello %s", "world");
sys.err.println("something wrong");
var input = sys.in.readAll();   // 读标准输入（管道传入的内容）
```

- `out` 的内容就是 `jsc` 命令的标准输出，会进入命令管道
- 写 `err` 的内容进入错误输出，API 场景下不影响响应体

## 其他实用成员

- `sys.safe(val)` —— 抹除值里的单双引号、换行、`;`、`$`，
  用于把外部值拼进命令字符串时防注入
- `sys.json(obj)` —— 对象转紧凑 JSON 字符串
- `sys.uu32()` —— 生成一个 UU32 随机 ID
- `sys.getAccountName()` / `sys.getSessionTicket()` —— 当前用户名 / 会话 ticket
- `sys.nlog()` —— 取底层 Java Log 对象（日志会落文件系统），一般用 `$log` 即可
- `sys.markLogOff()` / `sys.markLogOn()` —— 关闭 / 恢复当前线程的命令日志
  （批量操作时压日志量用）

# 延伸阅读

- 20-cmd-jsc.md —— jsc 命令全貌：引擎、注入对象总表、缓存机制
- 22-cmd-jsc_walnut.md —— `$wn`：基于 `sys.exec2` 封装的助手函数
- 23-cmd-jsc_log.md —— `$log`：带 trace ID 的日志输出
- 源码：src/com/site0/walnut/util/JvmJsExecContext.java
