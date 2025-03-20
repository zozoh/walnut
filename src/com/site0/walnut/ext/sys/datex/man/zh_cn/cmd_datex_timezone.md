# 命令简介 

`datex @timezone` 设置时区或者显示当前时区

# 用法

```bash
datex @format
  [timeZoneId]      # 【选】 设置输出时间字符串的时区
                    # 默认的，输出时间会用当前系统会话所在的时区。
                    # 如果不指定，那么相当于是要显示当前时区
  [-list]           # 列出所有可用时区
  [-n]              # 输出时，结尾不要带换行符号
  [-as id|name]     # 输出时区信息，输出的内容
                    #  - id: 仅仅是时区 ID
                    #  - name: 仅仅是时区的显示名称
                    #  - full: 时区完整的字符串描述
                    #  - json: 时区的JSON描述方式
                    # 如果不指定这个选项，则仅仅是向上下文里设置时区
```
# 示例

```bash
# 直接将上下当前时间格式设置为 'yy-MM-dd'
datex @fmt yy-MM-dd

# 直接将上下当前时间格式设置为绝对毫秒数
datex @fmt AMS

# 列出所有可用时区
datex @timezone -list

# 列出所有可用时区，用 JSON 方式打印
datex @timezone -list -as json
```