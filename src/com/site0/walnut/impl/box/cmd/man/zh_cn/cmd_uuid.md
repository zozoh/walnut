# 命令简介

`uuid` 生成指定格式的 UUID 字符串

# 用法

```bash
uuid [-F STR|uu32|uu16|uu64] [-c 数量] [-f 输出文件]
  -F    # 格式：STR 标准 UUID / uu32 / uu16 / uu64（默认 uu32）
  -c    # 生成数量，最多 100
  -f    # 同时写入文件
```

# 示例

```bash
# 生成一个 uuid
uuid

# 生成 100 个
uuid -c 100

# 生成 10 个 uu16 格式
uuid -F uu16 -c 10

# 生成标准格式并写入文件
uuid -F STR -f tmp.txt
```
