命令简介
======= 

`hexbin` 二进制数据与HEX字符串的互转

用法
=======

```
hexbin [-d] [data]
```

示例
=======

```bash
# 将abc.bin的数据编码为HEX字符串
cat abc.bin | hexbin

# 把hex.txt中的数据按HEX格式解码为数据
cat hex.txt | hexbin -d
```

例子

```
hexbin 123
# 输出, '1' -> 0x31, '2' -> 0x32, '3' -> 0x33
313233
```
