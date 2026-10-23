# 过滤器简介

`@vars` 设置生成上下文变量，供 `@gen` 的规则引用。

# 用法

```bash
val @vars [JSON] [=..] [=键] ...
  JSON    # 合并进上下文，如 '{prefix:"ID"}'
  =..     # 从管道读 JSON 并合并进上下文
  =键     # 从管道读 JSON 并用它整体替换上下文
```

# 示例

```bash
# 用 JSON 设置变量
val @vars '{prefix:"ID"}' @gen '${prefix}-${int:1,999}'

# 从管道合并变量
echo '{prefix:"ID"}' | val @vars =.. @gen '${prefix}-${int:1,999}'

# 用管道内容整体替换上下文
echo '{name:"xiaobai"}' | val @vars =name @gen '${name}'
```
