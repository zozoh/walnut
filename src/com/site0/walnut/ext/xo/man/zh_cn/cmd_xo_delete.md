`xo delete` 删除第三方平台对象存储中的对象

# 用法

```bash
xo delete \
  [key]           # 【必】对象的键名
```
# 示例

```bash
# 删除对象
xo s3:file @delete hello.txt
```