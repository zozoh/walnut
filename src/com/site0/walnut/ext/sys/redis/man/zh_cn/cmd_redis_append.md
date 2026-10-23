# 过滤器简介

`@append` 在键值的末尾追加内容（对应 Redis `APPEND`）。

# 用法

```bash
redis @append 键 内容

```

# 示例

```bash
# 追加日志片段
redis @append log:1 'hello'
