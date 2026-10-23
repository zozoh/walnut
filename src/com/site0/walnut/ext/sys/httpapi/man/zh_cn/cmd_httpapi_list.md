# 过滤器简介

`@list` 列出当前域所有的 API。

# 用法

```bash
httpapi @list [-u 用户]
  -u    # 指定一个用户，列出该用户域的 API；只有 root 和 op 组成员才能执行
```

# 示例

```bash
# 列出本域所有的 httpapi
httpapi @list

# 列出 xiaobai 域的 httpapi（需要 root 或 op 组成员权限）
httpapi @list -u xiaobai
```
