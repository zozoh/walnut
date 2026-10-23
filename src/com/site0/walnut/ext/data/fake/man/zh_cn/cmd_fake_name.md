# 过滤器简介

`@name` 生成随机姓名。

# 用法

```bash
fake [个数] @name [模式] [-lang 语言]
  # 个数        # 生成多少个，默认 1
  # 模式        # FULL（默认）| FULL_WITH_MIDDLE | FULL_NO_MIDDLE | FIRST | MIDDLE | FAMILY
  -lang        # 语言种类，默认 zh_cn
```

# 示例

```bash
# 随机输出三个中文姓名
fake 3 @name
# 夏娇
# 黎爱芬
# 汪艳红

# 随机输出三个英文姓名
fake 3 @name -lang en_us
# Bit Rose
# Lorina Hunter
# Sheron Fox
```
