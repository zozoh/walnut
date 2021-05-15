# 过滤器简介

`fake @str` 生成随机字符串

# 用法

```bash
fake [N] @str 
    [$MIN-$MAX]     # 字符范围，或最小字符数
    [$MAX]          # 最大字符数
```

# 示例

```bash
# 输出三个6-12长度的字符串
demo> fake 3 @str
tktMS7
ssrnJ4G
6MBr5u

# 输出三个20-40长度的字符串
demo> fake 3 @str 20 40
cwgbBJb23tGBqWR4hhka
VWh7Kxb2Jxuyt7ymhrArgzihzVZfFG
gHL3bUmVA4tJgQqutpqb3svuQ8
```
