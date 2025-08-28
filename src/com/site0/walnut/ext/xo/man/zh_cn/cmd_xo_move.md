`xo move` 移动第三方平台对象存储中的对象到新的路径

# 用法

```bash
xo move \
  [oldKey]        # 【必】对象的旧键名
  [newKey]        # 【必】对象的新键名
```
# 示例

```bash
# 移动对象
xo s3:file @move hello.txt docs/greeting.txt
```