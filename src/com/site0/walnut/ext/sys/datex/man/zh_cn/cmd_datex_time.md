# 命令简介 

`datex @time` 修改上下文时间

# 用法

```bash
datex @time 
  [$time]     # 设置上下文的时间
              # 支持正负整数，表示一天的绝对毫秒数
```
# 示例

```bash
# 直接将时间设置为 00:00:00
datex @set 2023-09-21 @time 0

# 直接将时间设置为 23:59:59.999
datex @set 2023-09-21 @time -1

# 直接将时间设置为 23:59:59.000
datex @set 2023-09-21 @time -1000

# TODO 暂不支持
# 直接将时间设置为 00:10:00
datex @set 2023-09-21 @time 00:10:00
```