---
title: 简单命令
module: impl.box
keywords: [命令, 简单命令, JvmExecutor, cmd_cat, ZParams, WnSystem]
source:
  - impl/box/JvmExecutor.java
  - impl/box/cmd/cmd_cat.java
  - util/ZParams.java
related: [../10.concepts/04-command-model.md, custom-command, o, sqlx]
---

# 简单命令概述

**简单命令**直接继承 `JvmExecutor`，一个命令一个源文件，一次调用完成一件事情。
它是 Walnut 命令的基础形态：参数进来、结果出去，没有子命令，也没有跨调用的上下文。

系统内置的 73 个命令全部是简单命令；扩展命令中也有 21 个属于这种写法。

## 代码形态

一个最简单的命令长这样（以 `cmd_echo` 为例）：

```java
package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_echo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 用 ZParams 解析参数
        ZParams params = ZParams.parse(args, "^(n|l|e)$");
        // 向标准输出打印
        sys.out.println(params.val(0));
    }

}
```

### 接口与参数详解

**`exec(WnSystem sys, String[] args)`**

命令的入口方法，两个参数：

- **`sys`**：系统运行时句柄，是命令与 Walnut 交互的唯一通道。常用成员：
  - `sys.in` / `sys.out` / `sys.err`：标准输入输出流（详见 命令输入输出）
  - `sys.io`：IO 服务，读写对象（`sys.io.readText(o)`、`sys.io.getInputStream(o, 0)` 等）
  - `sys.session`：当前会话（环境变量、用户身份）
  - `sys.auth`：登录鉴权接口
  - `sys.jef`：命令工厂，可执行子命令（`sys.exec(...)` 系列方法）
  - `sys.cmdOriginal`：原始命令行文本
  - `sys.getMe()` / `sys.getHome()` / `sys.getCurrentObj()`：当前用户 / 主目录 / 当前目录对象
- **`args`**：参数数组。已经过 `prepareArgs` 预处理——默认实现会剥掉参数首尾成对的引号。
  如果希望保留引号（例如值本身可能是 `@xxx`），子类可以重载 `prepareArgs`。

**`ZParams.parse(args, "^(n|l|e)$")`**

参数解析统一用 `ZParams`：

- **`params.vals`**：位置参数数组（非 `-` 开头的部分）
- **`params.is("quiet")`**：开关参数是否存在（`-quiet` → true）
- **`params.getString("f")`**：带值参数（`-f xxx` → xxx）
- **第二个参数是开关正则**：匹配该正则的参数按布尔开关解析，不进 `vals`。
  注意 `params.is(...)` 的大小写敏感——按源码里 `params.is("noTop")` 的写法原样传

**其他可重载点**

- `prepareArgs(args)`：参数预处理（默认剥引号）
- `getManual()`：读取手册（基类已实现，按约定路径找 markdown，见下）

## 完整示例：`cmd_cat`

`cat` 输出一个或多个文件的全部内容，是典型简单命令：

```java
package com.site0.walnut.impl.box.cmd;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_cat extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        // 不能为空
        if (args.length == 0) {
            throw Er.create("e.cmd.noargs");
        }
        // 输出每个路径
        ZParams params = ZParams.parse(args, "^(quiet)$");
        boolean isQuiet = params.is("quiet");

        // 计算要列出的要处理的对象
        List<WnObj> list = null;

        // 尝试获取
        try {
            list = Cmds.evalCandidateObjsNoEmpty(sys, params.vals, 0);
        }
        catch (Throwable e) {
            // 静默模式
            if (isQuiet) {
                list = new LinkedList<>();
            }
            // 普通模式，抛错
            else {
                throw Er.wrap(e);
            }
        }

        // 没内容
        if (list.isEmpty()) {
            if (!isQuiet) {
                throw Er.create("e.io.obj.noexists", sys.cmdOriginal);
            }
        }
        // 输出内容
        for (WnObj o : list) {
            // 解开链接
            WnObj o2 = Wn.real(o, sys.io, new HashMap<>());
            // 目录不能输出
            if (o2.isDIR()) {
                sys.err.printlnf("e.io.readdir : %s", o2.path());
                continue;
            }
            // 输出内容
            InputStream ins = sys.io.getInputStream(o2, 0);
            try {
                sys.out.write(ins);
            }
            finally {
                Streams.safeClose(ins);
            }
        }
    }

}
```

要点解读：

- `Cmds.evalCandidateObjsNoEmpty(sys, params.vals, 0)`：把位置参数批量解析成 `WnObj`（支持路径与 `id:` 两种写法），这是内置命令处理对象参数的惯例
- `Wn.real(o, ...)`：解开链接，拿到真实对象
- `sys.err.printlnf(...)`：错误信息走标准错误流，不污染标准输出
- 抛错统一用 `Er.create("e.xxx.yyy", ...)`，错误码会被 `ajaxre` 等命令识别

## 使用方法

```bash
cat ~/abc.txt                                # 按路径输出
cat id:fqmnqpKVIvu91vYZgePoQ0                # 按 ID 输出
cat a.txt b.txt                              # 一次输出多个
cat -quiet ~/maybe-not-exists.txt            # 不存在时静默
```

运行时查看手册：`man cat`。

## 文档放在哪里

每个命令一篇中文手册，与源码同包，运行时 `man` 命令直接读取：

- 内置：`src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_<命令名>.md`
- 扩展：`src/com/site0/walnut/ext/**/man/zh_cn/cmd_<命令名>.md`

查找顺序为 `man/zh_cn/`（新）→ 包根 `.md`（旧）→ 包根 `.man`（更旧）。
文档格式约定见 write-doc.skill（简介 / 用法 / 示例三部分）。

## 现有的简单命令

全部命令（含类型与分类）的统一清单见 60-cmd-table.md，这里不再单独罗列。
内置 75 个命令全部是简单命令，扩展命令中也有 20 余个属于这种写法。
