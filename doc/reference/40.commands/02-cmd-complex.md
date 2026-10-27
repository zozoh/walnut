---
title: 复杂命令
module: impl.box
keywords: [命令, 复杂命令, JvmHdlExecutor, JvmHdl, cmd_xapi, 子命令, 服务类]
source:
  - impl/box/JvmHdlExecutor.java
  - impl/box/JvmHdl.java
  - impl/box/JvmHdlContext.java
  - ext/net/xapi/cmd_xapi.java
related: [../10.concepts/04-command-model.md, xapi, sqlx, custom-command]
---

# 复杂命令概述

**复杂命令**继承 `JvmHdlExecutor`，子命令是 `hdl/` 目录下一个个独立的 `JvmHdl` 类。
它适合「一个领域、多种操作」的场景：主命令只是入口壳，按第一个参数把请求分发给子命令处理器；
领域里的关键逻辑还会封装成**服务类**，供其他模块复用——命令本身只是这些服务的接线员。

系统现有 30 余个复杂命令（全部是扩展命令），典型代表：`xapi`、`cron`、`task`、`pay`、`ti`。

## 代码形态

### 主命令

以 `cmd_xapi` 为例，主命令类非常薄：

```java
package com.site0.walnut.ext.net.xapi;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

public class cmd_xapi extends JvmHdlExecutor {

    // 公共辅助逻辑也可以以静态方法的形式挂在主命令上，供各 hdl 复用
    public static NutBean loadVars(WnSystem sys, JvmHdlContext hc) {
        NutBean vars = sys.session.getEnv();
        String str = Cmds.getParamOrPipe(sys, hc.params, "vars", true);
        if (!Strings.isBlank(str)) {
            NutMap v2 = Wlang.map(str);
            vars.putAll(v2);
        }
        return vars;
    }

}
```

主命令甚至不需要写任何方法——`JvmHdlExecutor` 的构造器会自动扫描**本包 `hdl/` 子包**，
把所有 `cmd_xapi_*` 类注册为子命令（类名去掉 `cmd_xapi_` 前缀、转小写）：

```bash
src/com/site0/walnut/ext/net/xapi/
|-- cmd_xapi.java          # 主命令（入口壳）
|-- XApi.java              # 服务接口：第三方 API 调用
|-- impl/WnXApi.java       # 服务实现
|-- XApiConfigManager.java # 服务：专家配置管理
|-- XApiExpertManager.java # 服务：专家对象缓存
|-- hdl/
|   |-- xapi_send.java     # → 子命令 send
|   |-- xapi_req.java      # → 子命令 req
|   |-- xapi_info.java     # → 子命令 info
|   |-- xapi_ak.java       # → 子命令 ak
|   |-- xapi_wxjssdk.java  # → 子命令 wxjssdk
|-- man/zh_cn/
|   |-- cmd_xapi.md        # 主命令手册（总览）
|   |-- cmd_xapi_send.md   # 子命令手册
|   |-- ...
```

### 子命令处理器

每个子命令是一个实现 `JvmHdl` 接口的类，入口为 `invoke(WnSystem sys, JvmHdlContext hc)`：

```java
@JvmHdlParamArgs(value = "cnqN", regex = "^(url|force)$")
public class xapi_req implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取变量（复用主命令的静态辅助方法）
        NutBean vars = cmd_xapi.loadVars(sys, hc);

        // 位置参数
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path    = hc.params.val_check(2);

        // 调用服务类完成真正的工作
        XApi api = new WnXApi(sys);
        XApiRequest req = api.prepare(apiName, account, path, vars, force);

        // 输出结果
        sys.out.println(Json.toJson(req, hc.jfmt));
    }

}
```

### `JvmHdlContext`：子命令的运行上下文

`invoke` 收到的第二个参数 `hc` 是本次调用的上下文（继承自 `NutMap`，可随意放键值）：

- `hc.sys`：系统运行时（同简单命令的 `sys`）
- `hc.ioc`：IoC 容器，可直接取全局 Bean
- `hc.hdlName` / `hc.hdl`：当前子命令名与处理器实例
- `hc.params`：`ZParams`，子命令自己的参数（`@JvmHdlParamArgs` 注解声明开关，如 `value="cnqN"` 表示 `-c -n -q -N` 都是布尔开关，`regex` 补充更多开关）
- `hc.jfmt`：JSON 输出格式（由 `-c -q -n` 等全局开关生成）
- `hc.oRefer`：关联对象（需要「主目录」语义的命令用，如 `ftp`、`alipay` 的账户目录）
- `hc.pager`：分页器（`-limit`/`-skip` 等）
- `hc.setv(key, val)` / `hc.get(key)`：在上下文里传数据，`doOtherHandler` 可转交其他子命令处理

### 执行流程

```bash
用户输入:  xapi req weixin pay /order/create -force
              │
              ▼
JvmHdlExecutor.exec
  1. hdlName = "req"（第一个参数），其余作为 hdl 参数
  2. hc.hdl = hdls.get("req") → xapi_req 实例
  3. hc.parseParams → hc.params / hc.jfmt
  4. _before_invoke（子类可重载：如加载配置文件）
  5. hdl.invoke(sys, hc)   ← 真正的业务逻辑
  6. _before_quit（子类可重载：如输出汇总）
```

## 为什么需要服务类

复杂命令与过滤命令最大的区别不在「有没有子命令」，而在**逻辑归属**：

- `xapi` 的核心是 `XApi`/`WnXApi`（请求签名、密钥管理、缓存），`cmd_xapi` 只是入口
- `sqlx` 的核心是 `SqlProcessor`（模板 SQL 执行），被 SqlIndexer、SqlBM、登录会话存储复用
- `cron` 的核心是 `WnCronApi`，被调度器与 regapi 复用

这样做的好处：HTTP API（regapi）、定时任务、cron 管线、其他命令，都能以 Java 方式直接调用
同一套服务，命令行只是其中一种触发方式。

## 使用方法

```bash
xapi                          # 不带参数：显示用法（由各命令手册约定）
xapi info                     # 子命令：列出全部可用 API
xapi req weixin pay /order/create        # 子命令 + 位置参数
xapi req weixin pay /order/create -force # 子命令 + 开关
man xapi req                  # 查看子命令手册
```

## 文档放在哪里

- 主命令手册：`man/zh_cn/cmd_<命令名>.md` —— 整体介绍命令解决什么问题、列出全部子命令
- 子命令手册：`man/zh_cn/cmd_<命令名>_<子命令>.md` —— 与子命令源码同名，格式与简单命令手册一致
- 主命令类若提供静态辅助方法，也应在主命令手册中说明

## 现有的复杂命令

全部命令（含类型与分类）的统一清单见 60-cmd-table.md，这里不再单独罗列。
复杂命令全部是扩展命令，共有 30 余个，典型代表：`xapi`、`cron`、`task`、`pay`、`ti`。

