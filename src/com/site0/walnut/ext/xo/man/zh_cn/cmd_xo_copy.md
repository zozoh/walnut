`xo copy` 复制第三方平台对象存储中的对象

# 用法

```bash
xo copy \
  [sourceKey]     # 【必】源对象的键名
  [targetKey]     # 【必】目标对象的键名
```
# 示例

```bash
# 复制对象
xo s3:file @copy hello.txt hello_backup.txt
```