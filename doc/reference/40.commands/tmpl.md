---
title: tmpl 命令：文本模板渲染
module: 40.commands
keywords: [tmpl, 模板, 占位符, 循环, 分支, WnTmpl]
source: [ext/data/tmpl/, util/tmpl/]
related: [sqlx, extend-regapi, 03_cmd_filter]
---

# tmpl 命令：文本模板渲染

`tmpl` 把「文本模板 + 变量上下文」渲染成最终文本。它是 Walnut 的**通用模板引擎**：API 响应组装、SQL 语句生成（sqlx）、钩子命令、邮件正文，底层都是它（`util/tmpl/WnTmpl`）。

- 源码：`src/com/site0/walnut/ext/data/tmpl/`（命令），`src/com/site0/walnut/util/tmpl/`（引擎）
- 手册：`man tmpl`

## 设计思路

1. **占位符即数据绑定**：`${key}` 从上下文取值，支持键路径（`a.b.c`）、默认值（`${key?默认值}`）、备用键（`${key?@otherKey}`）；
2. **结构化控制**：`${#loop}` 循环、`${#if}/${#else-if}/${#else}` 分支，模板能表达列表渲染和条件输出；
3. **类型转换器**：占位符可带转换指令（格式化的日期、补零数字等，见下）；
4. **到处复用**：sqlx 的 SQL 模板、regapi 的 `http-header-*` 值、钩子的命令模板，都是 WnTmpl 语法。

## 适用场景

- **API 响应组装**：regapi 命令里拼 JSON/文本响应
- **SQL 生成**：sqlx 模板（见 [sqlx](sqlx.md)）
- **批量命令生成**：`o ... @tmpl '...' | run` 生成命令再执行
- **邮件/通知正文**：mailx 模板、短信文案
- **配置文件生成**：按环境变量渲染配置

## 语法

### 占位符

```bash
# 基本取值
${name}

# 默认值：name 缺失时输出 "游客"
${name?游客}

# 备用键：优先 title，没有则用 nm
${title?@nm}

# 类型转换器（引擎支持的转换指令）
${lm<date:yyyy-MM-dd HH:mm>}     # 日期格式化
${seq<int:%06d>}                 # 数字格式化（补零）
```

### 循环

```bash
${#loop it,index : orders}
  ${index}. ${it.nm} — ¥${it.fee}
${#end}
```

- `it` 是当前元素（名字自定），`index` 是下标（可选）；
- `orders` 是上下文里的集合/数组。

### 分支

```bash
${#if "status":"OK"}
  支付成功
${#else-if "status":"WAIT"}
  等待支付
${#else}
  状态异常: ${status}
${#end}

# 多条件为 AND；not 取反
${#if "race":"FILE", "fee":"100..999"}$${fee}${#end}
${#if not "email":"[BLANK]"}${email}${#end}
```

条件写法：`"键":AutoMatch`（引号键 + 匹配表达式，逗号 AND，`not` 取反）。

## 命令用法

```bash
tmpl [-showkeys] [@load | @vars] @render
#  -showkeys   占位符无值时保留 ${key} 原文（调试用）
```

```bash
# 从文件加载模板 + 变量渲染
tmpl @load ~/tmpl/hello.txt @vars '{name:"zozoh", orders:[{nm:"A",fee:12},{nm:"B",fee:30}]}' @render

# 管道喂模板
cat ~/tmpl/hello.txt | tmpl @vars '{name:"zozoh"}' @render
```

## 与其他命令的配合

```bash
# o 查询结果渲染（@tmpl 内部就是 tmpl 引擎）
o ~/orders @query -l @tmpl '订单 ${nm} 金额 ${fee}\n'

# 批量生成命令交给 run
o ~/users @query -l @tmpl 'mailx default @to ${email} @sub "您好 ${nm}";\n' | run

# regapi 里拼响应
echo '{"msg":"你好 ${http-qs-name?访客}"}'
```

## 注意事项

- 嵌套结构用键路径 `${a.b.c}`，上下文是 JSON Map；
- 分支条件的键要加引号：`"键":表达式`；
- 大模板建议 `@load` 文件而不是命令行内联（转义省心）；
- `-showkeys` 是排查「变量没注入」的第一步。

## 延伸阅读

- [sqlx](sqlx.md) — 模板在 SQL 上的扩展（`${@vars=where}`）
- [o 命令](o.md) — `@tmpl` 输出
- [扩展 API](../90.dev/extend-regapi.md) — 模板渲染实战
