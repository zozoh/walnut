`xo list` 列出第三方平台对象存储中的对象

# 用法

```bash
xo list \
  [prefix]        # 可选的对象前缀过滤
  -D              # 不使用分隔符（默认使用分隔符）
  -limit 10       # 限制返回的对象数量（默认10）
```
# 示例

```bash
# 列出所有对象
xo s3:file @list

# 列出特定前缀的对象
xo s3:file @list images/

# 列出对象时不使用分隔符
xo s3:file @list -D

# 限制返回100个对象
xo s3:file @list -limit 100
```