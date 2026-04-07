# 命令简介

`expl` 用来解释和执行表达式，支持多种表达式语法。

它主要调用 `WnExplain` 接口，通过 `WnExplains.parse()` 解析输入的表达式，然后通过 `explain()` 方法在给定上下文中执行并输出结果。

# 支持的表达式语法

## 字符串转义

```bash
# 直接输出转义后的字符串
\\"Hello World\\"
```

## 布尔值判断

```bash
# 判断变量是否存在或为真
==var_name

# 判断变量是否不存在或为假
!=var_name
```

## 获取值

```bash
# 获取变量值
=var_name

# 获取变量值，若不存在则使用默认值
=var_name || default_value

# 获取整个上下文
=..
```

## 调用表达式

```bash
# 调用 EL 表达式
=>expression
```

## 渲染模板

```bash
# 渲染模板
->template

# 条件渲染模板
->test_expression ? template
```

# 用法

```bash
expl [-showkeys] [@load | @vars] @render
#-------------------------------------\n
expl -showkeys @load "=var_name" @vars '{"var_name":"Hello"}' @render
```

它支持的过滤器有：

```bash
@load        # 读取表达式内容
@vars        # 设置上下文变量
@render      # 渲染表达式
```
