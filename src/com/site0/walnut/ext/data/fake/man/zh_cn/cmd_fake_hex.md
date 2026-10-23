# 过滤器简介

`@hex` 生成随机整数（16 进制）。

# 用法

```bash
fake [个数] @hex [范围|最小值] [最大值] [-upper]
  # 个数        # 生成多少个，默认 1
  # 范围        # 形如 8fff-ffff；一个值时表示最小值
  # 最大值      # 两个值时表示范围
  -upper      # 输出大写十六进制字符
```

# 示例

```bash
# 生成 0-FF 的整数
fake 3 @hex -upper
# A6
# D
# 4A

# 生成 8fff-ffff 的整数
fake 3 @hex 8fff-ffff
# aa45
# d0a7
# a01b
```
