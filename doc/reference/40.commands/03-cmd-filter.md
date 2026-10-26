---
title: 过滤命令
module: impl.box
keywords: [命令, 过滤命令, JvmFilterExecutor, JvmFilter, 过滤器, cmd_tmpl, cmd_o, 上下文]
source:
  - impl/box/JvmFilterExecutor.java
  - impl/box/JvmFilter.java
  - impl/box/JvmFilterContext.java
  - ext/data/tmpl/cmd_tmpl.java
related: [../10.concepts/04-command-model.md, o, sqlx, tmpl, custom-command]
---

# 过滤命令

**过滤命令**继承 `JvmFilterExecutor`，子命令称为**过滤器**（以 `@` 开头），
它们共享同一个**上下文对象**，按书写顺序依次对上下文加工，最后统一输出。
它适合「输入 → 多步加工 → 输出」的场景：每个过滤器只做一件事，任意搭配组合出复杂操作。

系统现有 28 个过滤命令，典型代表：`o`（对象瑞士军刀）、`sqlx`（SQL 网关）、`jsonx`（JSON 管道）、`tmpl`。

## 代码形态

### 主命令

以 `cmd_tmpl` 为例，主命令类只负责搭舞台：

```java
package com.site0.walnut.ext.data.tmpl;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

// 泛型参数：<上下文类型, 过滤器类型>
public class cmd_tmpl extends JvmFilterExecutor<TmplContext, TmplFilter> {

    public cmd_tmpl() {
        super(TmplContext.class, TmplFilter.class);
    }

    // 全局参数（@ 之前的部分）怎么解析
    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(showkeys)$");
    }

    // 创建上下文
    @Override
    protected TmplContext newContext() {
        return new TmplContext();
    }

    // 过滤器运行前的准备
    @Override
    protected void prepare(WnSystem sys, TmplContext fc) {
        fc.showKeys = fc.params.is("showkeys", false);
    }

    // 全部过滤器跑完后的统一输出
    @Override
    protected void output(WnSystem sys, TmplContext fc) {
        if (!fc.quiet) {
            WnTmplX t = WnTmplX.parse(null, fc.expert, fc.tmpl);
            sys.out.print(t.render(fc.vars, fc.showKeys));
        }
    }
}
```

构造器里传入的两个类决定了这个命令的全部能力：

- `TmplContext`：**上下文**，贯穿所有过滤器的共享数据（见下）
- `TmplFilter`：**过滤器基类**，`hdl/` 子包里每个 `tmpl_*` 类自动注册为 `@*` 过滤器（命名规则同复杂命令）

`JvmFilterExecutor` 还提供了几个可重载的钩子：

- `prepare(sys, fc)`：过滤器执行前（解析全局参数、初始化上下文）
- `output(sys, fc)`：过滤器全部执行后（统一渲染输出）
- `onFinished(sys, fc)`：`finally` 块中执行（清理资源）
- 过滤器执行中可通过 `fc.setBreakExec(true)` 中断后续过滤器

### 上下文

上下文继承 `JvmFilterContext`，保存三类数据：

```java
public class TmplContext extends JvmFilterContext {
    public NutMap vars;      // 本命令的共享数据：模板变量
    public String tmpl;      // 本命令的共享数据：模板内容
    public WnTmplTokenExpert expert;
    public boolean quiet;
    public boolean showKeys;
}
```

基类已提供：`sys`（运行时）、`params`（全局参数）、`jfmt`（JSON 格式）、
`getCurrentObj()`（当前目录对象）、`breakExec`（中断标记）。

### 过滤器

每个过滤器是一个继承命令过滤器基类的类，入口为 `process(sys, fc, params)`：

```java
public class tmpl_vars extends TmplFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(reset|json)$");
    }

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {
        // reset 开关或首次执行：重建上下文变量表
        if (params.is("reset") || fc.vars == null) {
            fc.vars = new NutMap();
        }
        // 参数 -f：从文件读变量；位置参数：直接给值；都没有：读管道
        if (params.hasString("f")) {
            WnObj o = Wn.checkObj(sys, params.getString("f"));
            setVar(fc, params.getString("name"), sys.io.readText(o), params.is("json"));
        }
        else if (params.vals.length > 0) {
            setVar(fc, params.getString("name"), params.val(0), params.is("json"));
        }
        else {
            setVar(fc, params.getString("name"), sys.in.readAll(), params.is("json"));
        }
    }
}
```

过滤器的经典三段式数据来源：**参数 → 文件（`-f`）→ 管道（`sys.in`）**，按优先级取值。

### 执行流程

```bash
用户输入:  tmpl @load -f ~/tpl.txt @vars '{name:"wendal"}' @render
              │
              ▼
JvmFilterExecutor.exec
  1. 按 @ 切分调用序列：[@load(-f ~/tpl.txt), @vars({name:...}), @render]
     @ 之前无内容 → 全局参数为空
  2. fc = newContext()，fc.params = 全局参数，fc.jfmt = JSON 格式
  3. prepare(sys, fc)
  4. 依次执行：tmpl_load.process(sys, fc, ...)   # 往 fc.tmpl 写
               tmpl_vars.process(sys, fc, ...)   # 往 fc.vars 写
               tmpl_render.process(sys, fc, ...) # 用 fc 数据渲染
     （某一步 setBreakExec(true) 则中断）
  5. onFinished(sys, fc)
  6. output(sys, fc)      # 统一输出
```

与复杂命令的关键区别：**过滤器的输入输出不是参数，而是共享上下文**——
`@load` 写入的 `fc.tmpl`，`@render` 直接拿来用；顺序不同，结果不同。

## 使用方法

```bash
# 模板来自参数
tmpl @load '你好 ${name}' @vars '{name:"wendal"}' @render

# 模板来自文件，变量来自管道
echo '{name:"wendal"}' | tmpl @load -f ~/tpl.txt @vars @render

# o：对象操作流水线（查询 → 更新 → 输出 JSON）
o ~/docs @query 'tp:"A"' @update 'star:1' @json

# jsonx：JSON 多步加工
echo '{a:{b:1}}' | jsonx @flatten @pick 'b'
```

## 文档放在哪里

- 主命令手册：`man/zh_cn/cmd_<命令名>.md` —— 总览 + 全部 `@过滤器` 列表（如 cmd_tmpl）
- 过滤器手册：`man/zh_cn/cmd_<命令名>_<过滤器名>.md` —— 与过滤器源码同名，格式与简单命令手册一致（简介 / 用法 / 示例）
- 过滤器较多的命令（如 `jsonx` 30+ 个过滤器），主手册只列清单，细节见各过滤器手册

## 现有的过滤命令

全部命令（含类型与分类）的统一清单见 60-cmd-table.md，这里不再单独罗列。
过滤命令共有 20 余个，典型代表：`o`（对象瑞士军刀）、`sqlx`（SQL 网关）、`jsonx`（JSON 管道）、`tmpl`。

> **类型边界说明**：三型命令按**写法（继承关系）**划分，复杂度是光谱而非绝对——
> `sqlx`、`o` 虽然属于过滤型（`@vars`/`@query` 共享 `SqlxContext`），
> 但内部同样把关键逻辑封装成服务类（如 `SqlProcessor`）供其他模块复用。
> 判断一个命令的类型，看它的基类最准确：`JvmExecutor` / `JvmHdlExecutor` / `JvmFilterExecutor`。
