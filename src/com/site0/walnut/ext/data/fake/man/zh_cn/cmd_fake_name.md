# 过滤器简介

`@name` 生成随机姓名

# 用法

```bash
fake [N] @name 
    [FULL]          # 模式：
                    #  - FULL(默认)
                    #  - FULL_WITH_MIDDLE
                    #  - FULL_NO_MIDDLE
                    #  - FIRST
                    #  - MIDDLE
                    #  - FAMILY
    [-lang zh_cn]   # 语言种类，默认 zh_cn
```

# 示例

```bash
# 随机输出三个中文姓名
demo> fake 3 @name
夏娇
黎爱芬
汪艳红

# 随机输出三个英文姓名
demo> fake 3 @name -lang en_us
Bit Rose
Lorina Hunter
Sheron Fox
```
