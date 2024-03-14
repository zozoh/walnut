# 过滤器简介

`@integer` 生成随机整数

# 用法

```bash
fake [N] @integer 
    [$MIN-$MAX]     # 数值范围，或者是最小值
    [$MAX]          # 最大值
```

# 示例

```bash
# 生成 0-100 的整数
demo> fake 3 @integer
40
68
71

# 生成 20-50 的整数
demo> fake 3 @integer 20-50
59
67
44

# 生成 0-8 的整数
demo> fake 3 @integer 8
0
2
5

# 生成 1-9 的整数
demo> fake 3 @integer 1 9
2
6
4
```