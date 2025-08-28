`xo get` 查看第三方平台对象存储中对象的元数据

# 用法

```bash
xo get \
  [key]           # 【必】对象的键名
```
# 示例

```bash
# 查看对象元数据
xo s3:file @get hello.txt
```