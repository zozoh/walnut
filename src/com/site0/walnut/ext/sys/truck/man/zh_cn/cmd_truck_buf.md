# 过滤器简介

`@buf` 设置缓冲区大小。缓冲区用来做桶转移时的读写缓冲。

# 用法

```bash
truck @buf [字节数]
       # 缓冲区大小，默认 8192
```

# 示例

```bash
# 加大缓冲区再迁移
truck @buf 65536 @from ~/data -bm mongo @to
```
