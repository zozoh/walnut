命令简介
======= 

`statistics agg` 将一组数据表中的记录，聚合到另外一张数据表中

```js
{
  // 原始来自哪个表
  srcDao   : "default",      // 默认为 default
  srcTableName : "t_history",
  // 将结果存放到哪个表
  targetDao   : "default",   // 默认与 srcDao 相同
  targetTableName : "t_agg_day",
  // 选择哪些数据
  query : {},
  // 原始字段中，哪个代表日期时间
  // 根据日期时间，会拆分出如下的 key
  //  - @groupKey (根据 groupBy 生成的 key)
  //  - @begin (时间区间开始时间，区间参见 timeUnit)
  //  - @end (时间区间开始时间，区间参见 timeUnit)
  //  - @year  (2009)
  //  - @month (0base)
  //  - @date  (1base)
  //  - @week  (1base)
  //  - @day  (0-Sun. 1-6)
  //  - @hour (0-23)
  //  - @minute (0-59)
  //  - @second (0-59)
  //  - @ams (记录的绝对毫秒数)
  //  - @format (根据 dateFormat 生成的格式化字符串)
  //  - @value (累计值)
  // 默认 ct
  srcTimeBy : "ct",
  // 日期时间的格式化字符串，默认 "yyyy-MM-dd"
  dateFormat : "yyyy-MM-dd",
  // 字段如何分组，根据这个模板算出一个键，就用它分组
  // 所有匹配的值，将会聚集在一起计数
  // 形成一个聚集值 @value 并加入上下文
  // 这个 key 则作为 @groupKey 也加入上下文
  groupBy : "${tid}_${ttp}_${opt}_${@year}_${@month}_${@date}"
  // 每次查询出来的历史记录，该如何计算值呢？
  // 如果历史记录的 mor 字段存放的就是一个数字，那么可以用 "=mor" 来获取
  // 如果 mor 里面是个复杂的 JSON 字符串，则可以用
  // "=mor.path.to.number" 来获取
  // 默认这个值，相当于 1
  sumBy : 1,
  // 目标记录是怎样的映射
  mapping : {
    tid   : "=tid",
    opt   : "->${ttp}-${opt}",
    val   : "=@value",
    tams  : "=@ams",
    year  : "=@year",
    month : "=@month",
    date  : "=@date"
  },
  // 这个用来指明生成的目标记录，哪个是标识时间的字段
  // 默认 tams
  targetTimeBy : "tams",
  // 用一个 JSON Map 来存储标志
  // {"20200921":true, "20200922":true ...}
  // 标识对应的时间段已经处理过了
  // 也就是说，如果删掉这个文件，那么一定会重新处理
  markBy : "~/.domain/statistics/agg/web.mark.json",
  // 在给定的时间周期里，按照什么时间单位来对齐时间
  // 现在支持 year | month | week | day | hour
  // 并且执行命令会根据这个时间周期对齐时间范围
  //  - year  : 开始年1月1日 00:00:00 至 结尾年的下一年1月1日 00:00:00
  //  - month : 开始月1日 00:00:00 至 结尾月的下一月1日 00:00:00
  //  - week0 : 开始周日 00:00:00 至 结尾周下一周日 00:00:00
  //  - week1 : 开始周一 00:00:00 至 结尾周下一周一 00:00:00
  //  - day   : 开始日 00:00:00 至 结尾日下一天的 00:00:00
  //  - hour  : 开始小时的 00:00 至结尾小时下一小时的 00:00
  // 默认为 day
  timeUnit : "day"
  // 标记文件太大也会影响效率，所以每次存储前都会预先剪裁
  // 这里限制最多从今天向前存储 300天， 支持
  //  - 10s 表示10秒 
  //  - 20m 表示20分钟 
  //  - 1h 表示1小时 
  //  - 1d 表示一天 
  //  - 1w 表示一周 
  //  - 100 表示 100毫秒】
  // 默认为 1100d， 即近 3年 
  markRemain : "1100d"
}
```


用法
=======

```bash
statistics agg
  [/path/to/conf]        # 配置文件路径
  [-date   now-1d]       # 起始时间，默认昨天
  [-span   7d]           # 时间跨度，向过去多久
  [-force]               # 强制重新归纳
  [-test]                # 并不真的执行数据的插入，只是预览
  [-quiet]               # 静默输出
  [-tmpl "@{xx}"]        # 输出为模板
  [-t 'id,uid']          # 输出为表格
  [-json]                # 输出为 JSON
  [-cqn ]                # 输出为 JSON 时的格式化
  [-bish]                # 输出位 -t 时的格式化
```

示例
=======

```bash
# 汇总7日数据，并作为 JSON 数据
statistics agg ~/.domain/statistics/agg/web.json-json -cqn

# 汇总7日数据（强制计算），并作为表格数据输出
statistics agg ~/.domain/statistics/agg/web.json -t 'tid,tp,year,month,date,tams,dt,val,tnm' -bish -force
```