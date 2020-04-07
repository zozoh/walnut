命令简介
======= 

`base64` 针对base64的操作

用法
=======

```
base64 [-d] [data]
```

示例
=======

```bash
# 将abc.bin的数据编码为base64
cat abc.bin | base64

# 把base.txt中的数据按base64解码为数据
cat base.txt | base64 -d
```
