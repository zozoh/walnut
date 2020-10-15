命令简介
======= 

`statistics aggregate` 将一组数据表中的记录，聚合到另外一张数据表中

```js
{
  // 原始来自哪个表
  dao   : "default",      // 默认为 default
  tableName : "t_history",
  // 将结果存放到哪个表
  targetDao   : "default",   // 默认与 srcDao 相同
  targetTableName : "t_agg_date",
  // 选择哪些数据
  query : {},
  // 原始字段中，哪个代表日期时间
  // 根据日期时间，会拆分出如下的 key
  //  - @key (根据 groupBy 生成的 key)
  //  - @year  (2009)
  //  - @month (0base)
  //  - @date  (1base)
  //  - @week  (1base)
  //  - @day  (0-Sun. 1-6)
  //  - @hour (0-23)
  //  - @minute (0-59)
  //  - @second (0-59)
  //  - @ams (绝对毫秒数)
  //  - @format (根据 dateFormat 生成的格式化字符串)
  //  - @value (累计值)
  // 默认 ct
  dateTimeBy : "ct",
  // 日期时间的格式化字符串，默认 "yyyy-MM-dd"
  dateFormat : "yyyy-MM-dd",
  // 字段如何分组，根据这个模板算出一个键，就用它分组
  // 所有匹配的值，将会聚集在一起计数
  // 形成一个聚集值 @value 并加入上下文
  groupBy : "${tid}_${ttp}_${opt}_${@year}_${@month}_${@date}"
  // 目标记录是怎样的映射
  mapping : {
    tid   : "=tid",
    tp    : "->${ttp}-${opt}",
    val   : "=@value",
    tams  : "=@ams",
    year  : "=@year",
    month : "=@month",
    date  : "=@date"
  },
  // 用一个 JSON Map 来存储标志
  // {"20200921":true, "20200922":true ...}
  // 标识对应的时间段已经处理过了
  // 也就是说，如果删掉这个文件，那么一定会重新处理
  markBy : "~/.domain/statistics/web.mark.json",
  // 在给定的时间周期里，按照什么时间单位来设置标记
  // 现在支持 year | month | week | day | hour
  // 并且执行命令会根据这个时间周期对齐时间范围
  //  - year  : 开始年1月1日 00:00:00 至 结尾年的下一年1月1日 00:00:00
  //  - month : 开始月1日 00:00:00 至 结尾月的下一月1日 00:00:00
  //  - week0 : 开始周日 00:00:00 至 结尾周下一周日 00:00:00
  //  - week1 : 开始周一 00:00:00 至 结尾周下一周一 00:00:00
  //  - day   : 开始日 00:00:00 至 结尾日下一天的 00:00:00
  //  - hour  : 开始小时的 00:00 至结尾小时下一小时的 00:00
  // 默认为 day
  markUnit : "day"
  // 标记文件太大也会影响效率，所以每次存储前都会预先剪裁
  // 这里限制最多从今天向前存储 300天， 支持
  //  - 10s 表示10秒 
  //  - 20m 表示20分钟 
  //  - 1h 表示1小时 
  //  - 1d 表示一天 
  //  - 1w 表示一周 
  //  - 100 表示 100毫秒】
  // 默认为 366d 
  markRemain : "366d"
}
```


用法
=======

```bash
statistics aggregate
  [/path/to/conf]          # 配置文件路径
  [-date   now-1d]         # 起始时间，默认昨天
  [-span   7d]             # 时间跨度，向过去多久
  [-force]                 # 强制重新归纳
```

示例
=======

```bash
statistics web
```