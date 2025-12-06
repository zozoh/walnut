`xo clear` 清理第三方平台对象存储中的对象

# 用法

```bash
xo clear \
  [key]           # 【必】对象的键名
```
# 示例

```bash
# 清理对象
xo s3:file @clear temp/
```