# 命令简介 

`datex @timezone` 设置时区或者显示当前时区

# 用法

```bash
datex @timezone
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
# 输出当前时间字符串 (GMT+8) 时区
datex @timezone GMT+8
> 2025-09-25 23:40:46

# 输出当前时间字符串 UTC 时区
datex @timezone UTC
> 2025-09-25 15:41:59

# 列出所有可用时区
datex @timezone -list

001) [GMT+0 ]                      Africa/Abidjan :                      Greenwich Mean Time : 格林尼治标准时间
002) [GMT+0 ]                        Africa/Accra :                      Greenwich Mean Time : 格林尼治标准时间
003) [GMT+3 ]                  Africa/Addis_Ababa :                         East Africa Time : 东部非洲时间
004) [GMT+1 ]                      Africa/Algiers :           Central European Standard Time : 中欧标准时间
...

# 列出所有可用时区，用 JSON 方式打印
datex @timezone -list -as json
[{
   id: "Africa/Abidjan",
   offset: 0,
   text_cn: "格林尼治标准时间",
   text_en: "Greenwich Mean Time"
}, {
   id: "Africa/Accra",
   offset: 0,
   text_cn: "格林尼治标准时间",
   text_en: "Greenwich Mean Time"
}, {
...
```