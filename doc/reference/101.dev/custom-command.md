---
title: 自定义命令开发
module: 101.dev
keywords: [自定义命令, JvmExecutor, JvmHdl, ZParams, man 手册, jvmbox-pkgs]
source: [impl/box/, ext/sys/mq/hdl/mq_secret.java]
related: [source-layout, jsexec-api, ../00.start-here/quick-start.md]
---

# 自定义命令开发

给 Walnut 加命令只需三步：写类 → 扫描注册 → 配手册。

## 两种形态

### 1. 单命令：继承 JvmExecutor

以 `cmd_hexbin`（`ext/util/hexbin/cmd_hexbin.java`）为模板：

```java
public class cmd_hello extends JvmExecutor {
    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "dn");   // 布尔开关声明
        String input = params.val(0);                  // 位置参数
        // 业务...
        sys.out.println("hello " + input);
    }
}
```

### 2. 带子命令：JvmHdlExecutor + JvmHdl

以 `mq_secret`（`ext/sys/mq/hdl/mq_secret.java`）为模板：

```java
// 入口：cmd_my.java
public class cmd_my extends JvmHdlExecutor {}

// 子命令：hdl/my_hello.java
@JvmHdlParamArgs("Q")                        // 参数模板：Q 为静默开关
public class my_hello implements JvmHdl {
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        int len = hc.params.getInt("len", 50);
        if (!hc.params.is("Q")) {
            sys.out.println(...);
        }
    }
}
```

子命令名 = 文件名去掉前缀（`my_hello` → `my hello`）。

## 注册：jvmbox-pkgs

`JvmExecutorFactory` 扫描类名匹配 `cmd_*` 的类，扫描包来自 `web.properties`：

```properties
jvmbox-pkgs:
com.site0.walnut
com.mycompany.mycmds          # ← 你的包加在这里
```

## 手册页

在包内建 `man/zh_cn/cmd_hello.md`（格式参考 `ext/**/man/zh_cn/`），用户 `man hello` 即可阅读。约定结构：命令简介 → 用法 → 参数说明 → 示例。

## 输入输出与错误

```java
// 输入：位置参数 / ZParams / 管道 stdin
String piped = sys.in.readAll();

// 输出
sys.out.println(...);        // 文本
sys.out.write(bytes);        // 二进制
sys.out.printJson(obj);      // JSON

// 错误
throw Er.create("e.cmd.hello.badInput", args);
```

错误码命名约定：`e.cmd.<命令名>.<场景>`。

## JS 快速覆盖

不想写 Java 时，同名 JS 文件优先于 JVM 命令执行（`~/bin` 或在 PATH 里的 `.js` 文件），适合原型验证，细节见 jsexec-api。

## 完整最小例子

```text
com.mycompany.mycmds/
|-- cmd_fortune.java              # extends JvmExecutor
|-- hdl/fortune_add.java          # extends JvmHdl（可选）
|-- man/zh_cn/
|   |-- cmd_fortune.md
|   |-- cmd_fortune_add.md
```

```java
public class cmd_fortune extends JvmExecutor {
    public void exec(WnSystem sys, String[] args) {
        String[] says = {"大吉", "中吉", "末吉"};
        sys.out.println(says[R.random(0, 2)]);
    }
}
```

```properties
# web.properties
jvmbox-pkgs:
com.site0.walnut
com.mycompany.mycmds
```

重启后 `fortune` 可用，`man fortune` 可读。

## 延伸阅读

- 源码包结构
- jsexec-api
