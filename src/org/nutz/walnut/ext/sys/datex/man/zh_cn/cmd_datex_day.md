# 命令简介 

`datex @day` 修改上下文日期

# 用法

```bash
datex @day
  [$day]              # 设置上下文的日期， 1 base
                      # 如果设置为 31 ，且这个月没有 31 天，那么日期会顺延到下个月
                      # 除非开启 -inmonth 开怪
  [-i $day]           # 偏移上下文的日期， -1 表示最后一天
  [-inmonth]          # 开启这个开关，无论设置还是偏移，
                      # 都如果超过这个月，将会自动设置为最后一天，或者第一天
  [-mode w|d|wd]      # 偏移有下面几种模式：
                      #  - w: 指明偏移仅仅偏移工作日
                      #       如果上下文加载了节假日表，会更真实
                      #       否则将会仅仅跳过法定周末
                      #  - d: 仅仅偏移自然日
                      #  - dw: 偏移自然日，但是最后一天如果落入节假日，
                      #        则向后顺延到第一个遇到的工作日
                      #  - wd: 偏移自然日，但是最后一天如果落入节假日，
                      #        则向前提前到第一个遇到的工作日
                      #  - auto: 如果 offset小于0 则相当于 'wd'，
                      #          大于0 则相当于 'dw'【默认】
```
# 示例

```bash
# 直接将上下的月份设置为 3 月
datex @month 3

# 直接将上下的月份向后偏倚 3 个月
datex @month -i 3
```