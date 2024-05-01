# 过滤器简介

`@hex` 生成随机整数(16进制)

# 用法

```bash
fake [N] @hex 
    [$MIN-$MAX]     # 数值范围，或者是最小值
    [-upper]        # 输出的为大写十六进制字符
```

# 示例

```bash
# 生成 0-FF 的整数
demo> fake 3 @hex -upper
A6
D
4A

# 生成 8fff-ffff 的整数
demo> fake 3 @hex 8fff-ffff
aa45
d0a7
a01b
```