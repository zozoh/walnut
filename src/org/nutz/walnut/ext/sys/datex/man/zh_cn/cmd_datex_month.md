# 命令简介 

`datex @month` 修改上下文月份

# 用法

```bash
datex @month
  [$month]     # 设置上下文的月份， 1 base
  [-i Offset]  # 偏移上下文的月份
```
# 示例

```bash
# 直接将上下的月份设置为 3 月
datex @month 3

# 直接将上下的月份向后偏倚 3 个月
datex @month -i 3
```