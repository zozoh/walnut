# 命令简介 

`datex @set` 设置上下文当前的日期时间

# 用法

```bash
datex @set
  [$DateTime]      # 设置上下文的日期时间，支持时间宏
  [-tz {TimeZone}] # 如果是用时间字符串设置，如果还可以指定时区
                   # 如果输入值里面本来就带有时区信息(譬如， Z 或者 +8等) 
                   # 那么本选项无效, 默认为当前会话时区
```
# 示例

```bash
# 直接将上下当前时间设置为 '2023-09-21 00:00:00.000'
datex @set 2023-09-21

# 直接将上下当前时间设置为当前时间之后的1天
datex @set 'now+1d'

# 直接将指定当前时间的绝对毫秒数
datex @set 1694368557435
```