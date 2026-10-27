# 过滤器简介

`@append` 把值追加到键已有字符串的末尾（对应 Redis `APPEND`），返回追加后的总长度。

键不存在时等价于 `@set`。因为整个追加是原子的，适合并发下拼接日志、流水。

# 用法

```bash
redis @append 键 追加内容
```

# 示例

```bash
# 追加一行日志
redis @append log:20261027 '12:00 user login\n'
redis @append log:20261027 '12:01 user logout\n'
redis @strlen log:20261027

# 管道喂入文件内容
o ~/tmp/note.txt -cqn | redis @append doc:note
```
