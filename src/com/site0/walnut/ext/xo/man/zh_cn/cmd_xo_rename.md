`xo rename` 重命名第三方平台对象存储中的对象

# 用法

```bash
xo rename \
  [oldKey]        # 【必】对象的旧键名
  [newKey]        # 【必】对象的新键名
```
# 示例

```bash
# 重命名对象
xo s3:file @rename hello.txt greeting.txt
```