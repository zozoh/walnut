---
title: JS 脚本
module: jsexec
keywords: [jsc, JS, 脚本, nashorn, rhino, 挂载点, sys]
source:
  - jsexec/JsExec.java
  - impl/box/cmd/jsc/
  - util/JsExecContext.java
related: [command-pipeline, jsexec-api, hooks, cron-pipeline]
---

# JS 脚本

Walnut 用 JS 作为动态业务逻辑层：不用改 Java、不用重启，把 `.js` 文件放进域目录即可生效。默认引擎由 `web.properties` 的 `jsc-dft-engine` 指定（`nashorn` 或 `rhino`）。

## 快速上手

```bash
# 写一个脚本
echo 'print("hello " + (sys.me ? sys.me.name() : "world"))' > ~/bin/hello.js

# 执行
jsc ~/bin/hello.js
# → hello demo

# 传变量
jsc ~/bin/hello.js -vars 'name:"walnut"'
```

## 脚本上下文（全局对象）

- **`sys`** — 类型：WnSystem — 说明：**核心入口**：io/exec/me/se/envs/params
- **`obj`** — 类型：WnObj/Map — 说明：钩子场景下：当前操作的对象
- **`print(x)`** — 类型：函数 — 说明：输出到 stdout（等同 `sys.out.println`）

`sys` 常用成员：

```js
sys.me          // 当前用户 (WnUser)：id/name/homePath...
sys.se          // 当前会话 (WnSession)
sys.io          // WnIo：fetch/get/create/writeText/readText/setBy...
sys.exec("ls ~/docs")           // 执行命令，返回输出文本
sys.exec2("o ~/a.txt -cqn")   // 执行并返回退出码等（见 jsexec-api）
sys.envs        // 会话环境变量
sys.params      // 命令行参数
```

完整 API 见 [jsexec-api](../90.dev/jsexec-api.md)。

## 挂载点一览（JS 在哪些地方生效）

- **`jsc` 命令** — 触发时机：手动 — 上下文特点：完整 sys
- **钩子文件 `~/.hook/write/xxx.js`** — 触发时机：对象写入后 — 上下文特点：有全局 `obj`
- **全局钩子 `/sys/hook/*`** — 触发时机：系统级对象事件 — 上下文特点：root 身份或指定用户
- **cron 任务命令体** — 触发时机：定时触发 — 上下文特点：任务所属用户身份
- **MQ 消息 body** — 触发时机：消费时 — 上下文特点：消息 user 身份
- **regapi 命令模板** — 触发时机：API 调用时 — 上下文特点：请求对象为上下文

钩子示例（图片上传后自动处理）：

```js
// ~/.hook/write/make_webp.js
if (obj.mime && obj.mime.startsWith("image/")) {
    sys.exec("videoc id:" + obj.id + " -mode 'preview_image'");
}
```

## 脚本里执行命令

```js
// 简单取输出
var out = sys.exec("o /home/demo -cqn");
var list = JSON.parse(out);

// 带管道的复杂命令：用 sh 包装
sys.exec('sh -c "cat ~/a.txt | grep ERROR | wc -l"');

// 读写对象
var o = sys.io.fetch(null, "~/data.json");
if (o) {
    var text = sys.io.readText(o);
    sys.io.writeText(o, JSON.stringify({updated: Date.now()}));
}
```

## 多行脚本与库复用

- `jsc` 执行整个 `.js` 文件；域内公共函数库放 `~/bin/` 或专门目录，用 `// @import` 式的拼接（先 `cat lib.js my.js | jsc`）或 `DomainGlobalJsCache` 缓存的全局库。
- 输出约定：`print` 进 stdout；`throw` 异常会让命令失败，stderr 带原因——钩子里常用「写 stderr 中断操作」（见 [hooks](../60.sched-async/hooks.md)）。

## 注意事项

- JS 引擎是 JDK 自带（Nashorn 已在高版本 JDK 移除——项目内嵌了 nashorn 包裹层，详见 `jsexec/`；Rhino 是备选）。
- 脚本在会话身份下运行，越权操作会被 Io 层拒绝并抛异常。
- 死循环/慢脚本会占住 box 沙箱，长任务请改为 cron/task 异步执行。
