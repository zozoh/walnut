# 过滤器简介

`@setrange` 从指定偏移覆写字符串的一部分（对应 Redis `SETRANGE`），返回修改后的总长度。

- 偏移从 0 开始；中间空隙用 `\0` 填充
- 键不存在时相当于创建新字符串

# 用法

```bash
redis @setrange 键 偏移 内容
```

# 示例

```bash
redis @set key1 'Hello World'
redis @setrange key1 6 'Redis'   # 得到 "Hello Redis"

# 从文件内容偏移写入
redis @setrange doc:body 0 '第一行\n第二行'
```
