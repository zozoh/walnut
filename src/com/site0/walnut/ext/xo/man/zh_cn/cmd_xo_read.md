`xo read` 读取第三方平台对象存储中的对象内容

# 用法

```bash
xo read \
  [key]           # 【必】对象的键名
```
# 示例

```bash
# 读取对象内容
xo s3:file @read hello.txt

# 读取对象内容并保存到本地文件
xo s3:file @read hello.txt > local.txt
```