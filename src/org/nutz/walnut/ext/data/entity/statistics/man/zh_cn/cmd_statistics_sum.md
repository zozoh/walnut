命令简介
======= 

`statistics sum` 将一组数据表中的记录，按照某种规律汇总

```js
{
  // 原始来自哪个表
  srcDao   : "default",      // 默认为 default
  srcTableName : "t_agg_day",
  // 选择哪些数据
  query : {
    "opt" : "video-view"
  },
  // 原始字段中，哪个代表日期时间
  // 根据日期时间，会拆分出如下的 key
  //  - @groupKey (根据 groupBy 生成的 key)
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
  srcTimeBy : "tams",
  // 日期时间的格式化字符串，默认 "yyyy-MM-dd"
  dateFormat : "yyyy-MM-dd",
  // 字段如何分组，根据这个模板算出一个键，就用它分组并求和
  // 最后的汇总值作为 @value 加入上下文
  // 这个 key 则作为 @groupKey 也加入上下文
  groupBy : "${tid}_${opt}",
  // 要加总的值字段
  sumBy : "val",
  // 目标记录是怎样的映射
  mapping : {
    name  : "=tnm",
    value : "=@value"
  },
  // 映射后的数据，哪个字段表示名称
  nameBy  : "name",
  // 映射后的数据，哪个字段表示值
  valueBy : "value",
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
  timeUnit : "day",
  // 为了效率，求和计算结果会缓存到一个文件夹里
  // 这里指明文件夹路径
  cacheDir : "~/.domain/statistics/sum_cache/",
  // 在缓存中的名称，接受上下文占位符
  //  - @dateBegin : "20200921"
  //  - @dateEnd   : "20201001"
  // 这两个值是从 `-date` 参数和 `-span` 参数得到的时间范围
  // 时间字符串的格式为 cacheDateFormat 定义的格式，默认为 "yyyyMMdd"
  cacheName : "web-sum-video-view-${@dateBegin}-${@dateEnd}.json",
  cacheDateFormat : "yyyyMMdd"
}
```


用法
=======

```bash
statistics sum
  [/path/to/conf]           # 配置文件路径
  [-agg /path/to/aggconf]   # 聚合配置文件路径，如果声明将会尝试先聚合
  [-agg-force]              # 强制清空聚集结果的缓存
  [-date   now-1d]          # 起始时间，默认昨天
  [-span   7d]              # 时间跨度，向过去多久
  [-sort '{value:1}']       # 输出结果需要按照什么方式排序
                            # 1 从小到大， -1 从大到小
                            # 只有第一个项目是有效的，即不能组合排序
  [-top 10]                 # 指定只输出前多少条记录（在-sort启用时才有效）
  [-others Others]          # 是否将其余数据合并到【其他】项，以及指定这个项的名称
                            # 只有 -top 生效时才有效
  [-force]                  # 强制重新归纳
  [-test]                   # 并不真的执行数据的插入，只是预览
  [-tmpl "@{xx}"]           # 输出为模板
  [-t 'id,uid']             # 输出为表格
  [-json]                   # 输出为 JSON
  [-cqn ]                   # 输出为 JSON 时的格式化
  [-bish]                   # 输出位 -t 时的格式化
```

示例
=======

```bash
# 汇总7日数据，并作为 JSON 数据输出，并不记录缓存
statistics sum ~/.domain/statistics/sum/video-view.json -test -json
```