`xo write` 写入对象到第三方平台的对象存储

# 用法

```bash
xo write \
  [key]           # 【必】对象的键名
  [text]          # 对象的文本内容（可选，如果不提供则从标准输入读取）
  -meta {..}      # 对象的元数据
```
# 示例

```bash
# 写入文本内容到对象
xo s3:file @write hello.txt "Hello, World!"

# 从标准输入读取内容写入对象
cat local.txt | xo s3:file @write hello.txt

# 写入对象并指定元数据
xo s3:file @write hello.txt "Hello, World!" -meta '{"content-type":"text/plain","author":"demo"}'
```