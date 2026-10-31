---
title: Java 库模式嵌入
module: 101.dev
keywords: [嵌入, Java API, WnIo, WnRun, IoC, 二次开发]
source: [util/Wn.java, util/WnRun.java, api/]
related: [90-source-layout, 50-custom-command.md, ../40.commands/20-cmd-jsc.md]
---

# Java 库模式嵌入概述

Walnut 不只能跑服务，也能作为库嵌入你的 Java 应用：拿它的 IoC 容器、直接编程 I/O、或以某用户身份执行命令。

## 拿 IoC 与服务

用 `ComboIocProvider` 加载 `conf/ioc/*.js`（与服务端同一套配置）：

```java
Ioc ioc = new NutIoc(new ComboIocLoader("*js", "conf/ioc"));
// 或者复用 Nutz MVC 的容器
```

服务工厂入口是 `Wn.Service`（`util/Wn.java`）：

```java
WnIo io      = Wn.Service.io(ioc);        // 对象 I/O
WnLoginApi auth = Wn.Service.auth(ioc);   // 登录/会话/权限
WnBoxService boxes = Wn.Service.boxes(ioc);      // 命令沙箱
WnSysCronApi crons = Wn.Service.crons(ioc);      // 定时任务
WnSysTaskApi tasks = Wn.Service.tasks(ioc);      // 任务栈
WnSysScheduleApi schedules = Wn.Service.schedules(ioc);
```

## 直接操作对象（WnIo）

```java
WnIo io = Wn.Service.io(ioc);

// 创建
WnObj o = io.create(null, "/data/report/2026-10.txt", WnRace.FILE);

// 读写
io.writeText(o, "hello walnut");
String text = io.readText(o);

// 查询
WnQuery q = Wn.Q.pid(io.fetch(null, "/data/report")).match("mime", "^text/");
List<WnObj> list = io.query(q);

// 元数据
o.setv("author", "zozoh");
io.appendMeta(o, "author");
```

> 路径必须是**标准绝对路径**：`~` 是 `HOME` 会话变量的简写，IO 层不认。
> 用 `Wn.normalizePath` 常规化后再交给 WnIo：
> 主要可选三个函数:
> 
> ```java
> package com.site0.walnut.util;
> public abstract class Wn {
>     public static String normalizePath(String ph, WnSystem sys) 
>     public static String normalizePath(String ph, WnSession se) 
>     public static String normalizePath(String ph, NutBean vars)
> }
> ```
> 譬如：
>
> ```java
> // vars 里通常有 HOME；域 demo 的 "~/.captcha/default.json" 会被
> // 常规化成 "/home/demo/.captcha/default.json"
> String aph = Wn.normalizePath("~/.captcha/default.json", vars);
> WnObj oConf = Wn.checkObj(io, aph);
> ```

## 以用户身份执行命令（WnRun）

```java
// WnRun 是 IoC bean（字段注入 serviceFactory/io/boxes/hooks...），直接取
WnRun run = ioc.get(WnRun.class);

// 以 zozoh 身份执行命令，返回标准输出
String out = run.exec("MYAPP", "zozoh", "o ~/docs -l | wc -l");

// 创建会话（longSession=true 长会话）
WnSession se = run.creatSession("zozoh", true);
```

`WnRun` 持有 services/auth/boxes/hooks/jef，等价于一个无 HTTP 的客户端。

## 何时用哪种

- **存取文件/元数据**：直接 `WnIo` 编程
- **复用 Walnut 命令生态（sheet/ooml/dsync...）**：`WnRun.exec`
- **复用定时任务调度**：`Wn.Service.crons/tasks`
- **全新业务能力**：自定义命令 或 xapi

## 代码风格约定

写 Walnut Java 代码时遵守的两条习惯（Web 模块服务类均已按此执行）：

- **中间结果具名，一行一件事**：不要把多个调用嵌在一行表达式里，
  每个中间结果先赋给具名局部变量再用——单步调试时可以逐个观察：

  ```java
  // 而不是 String otpErr = this._check_otp(this._get_otps(), se.getUser());
  WnOTPService otps = this._get_otps();
  WnUser user = se.getUser();
  String otpErr = this._check_otp(otps, user);
  ```

- **方法名前缀与可见性对应**：`protected` 方法用单下划线 + snake_case
  （如 `_check_otp`）；`private` 方法用双下划线（如 `__fail_json`）；
  特别常用、甚至可能升级为 `public` 的 `protected` 方法不加下划线，
  仅保持 snake_case。

## 注意事项

- 嵌入时 `conf/web.properties` 里的 mongo/redis 连接同样生效，注意与主服务共享存储时的锁实现（Redis）；
- `WnRun.exec` 会完整走权限与钩子链路，执行身份注意最小化；
- `Wn.WC()` 上下文（当前会话）在异步线程里要自行设置。

# 延伸阅读

- 源码包结构
- 自定义命令
- I/O 模型
