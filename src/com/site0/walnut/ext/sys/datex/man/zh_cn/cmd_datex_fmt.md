# 命令简介 

`datex @fmt` 设置时间格式化方式

# 用法

```bash
datex @format
  [$Format]       # 默认就是 "yyyy-MM-dd HH:mm:ss"
                  # 如果是 "AMS" 则表示绝对毫秒数
```
# 示例

```bash
# 直接将上下当前时间格式设置为 'yy-MM-dd'
datex @fmt yy-MM-dd

# 直接将上下当前时间格式设置为绝对毫秒数
datex @fmt AMS
```