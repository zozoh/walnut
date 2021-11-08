# 过滤器简介

`@sheet` 为上下文当前工作簿的指定当前工作表

# 用法

```bash
ooml @sheet
    [Index]      # 指定工作表的下标（0BASE）
                 # 如果没有声明，且也没有 -name/-id 参数
                 # 默认选择第一个工作表    
    [-name xxx]  # 【选】指定一个工作簿的名称
    [-id 1]      # 【选】指定一个工作簿的 ID，最优先
```

# 示例

```bash
# 将当前工作簿的第一个工作表设为当前工作表
ooml ~/abc.xlsx @xlsx @sheet

# 将当前工作簿的第三个工作表设为当前工作表
ooml ~/abc.xlsx @xlsx @sheet 2

# 按名称指定工作簿的当前工作表
ooml ~/abc.xlsx @xlsx @sheet -name Sheet1

# 按ID指定工作簿的当前工作表
ooml ~/abc.xlsx @xlsx @sheet -id 1
```

