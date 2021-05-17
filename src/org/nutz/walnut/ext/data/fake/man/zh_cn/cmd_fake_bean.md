# 过滤器简介

`@bean` 生成一个或者多个模拟的对象

```js
{
  // 生成 UU32
  "key1" : "{FAKER}",
  // 嵌套对象
  "key2" : {...}
  // 模拟枚举
  "key3" : [...]
}
```
 其中 `{FAKER}` 是一个字符串，可能的值为：

 - `UU32` : 生成 UUID 32 字符串
 - `INT` : 生成 0-100 的整数
 - `INT:50-100`: 生成 50-100 的整数
 - `INTS:192.168.{100-200}.{0-255}`: 生成 随机的多段整数
 - `STR`: 生成随机的字符串
 - `STR:5-10`: 生成随机的字符串，长度为 5 至 10 个字符不等
 - `SENTENCE`: 生成一个随机句子
 - `SENTENCE:5-10`: 生成一个随机句子，内容为 5 至 10 个词不等
 - `TEXT`: 生成一段随机文本
 - `TEXT:20-50`: 生成一段随机文本，长度为 20 至 50 个字符不等
 - `NAME`: 生成一个随机人名
 - `AMS`: 生成一个今天的随机毫秒数
 - `AMS=today:5m`: 生成一个今天的00:00:00开始5分钟内的毫秒数
 - `AMS=today:1h:yyyy-MM-dd HH:mm`: 生成一个今天的00:00:00开始1一小时内的时间字符串
 - `:{FAKER}XXX`: 采用 `:` 开头，表示一个字符串模板, `{..}` 内还可以是一个 `FAKER`
 - `?A,B,C`: 采用 `?` 开头，是一个半角逗号分隔的枚举型随机内容
 - 其他情况，统统当作一个固定字符串

 其中 `AMS` 的语法为：

 ```
 [AMS][=+][{Start}][:{Duration}][:{Format}]
 ```

- `[+=]`: 表示生成随机数的方式：
   + `+` 表示从 `{Start}` 开始，以 `{Duration}` 为段，在段内生成随机时间，这个特别适合模拟日志等按时间索引的随机数据
   + `=` 表示从 `{Start}` 开始，以 `{Duration}` 为长度的区间内，随机模拟一个时间
- `{Start}`: 开始时间
   + `today` 表示今天
   + `today+1d` 表示明天
   + `2020-12-09` 则表示一个绝对的日期字符串
- `{Duration}`: 表示一个时间长度
   + `1d` 表示一天
   + `1m` 表示一分钟
   + `1s` 表示一秒钟
   + `500` 表示 500 毫秒
- `{Format}`: 是以个日期时间格式化字符串，如果没有指定则输出毫秒数

 

# 用法

```bash
fake [N] @bean
    [{..}]          # 【选】模拟对象的设置，如果没有指定
                    # 则试图从标准输入读取
    [-lang zh_cn]   # 语言种类，默认 zh_cn
    [-to /path/to]  # 一个目标目录，将模拟的对象输出到对应目录
    [-v]            # 对于 -to 模式，逐行打印输出对象的调试信息
```

# 示例

```bash
# 根据配置生成两个随机对象
demo>  cat bean.json | fake 2 @bean -lang en_us
[{
   nm: "Um5Cub2yX",
   nickname: "Mahanta Lloyd",
   title: "pamperedly schmitz enne",
   brief: "Earthy numerably abulia globus rosinweed. ",
   client: "192.168.12.211",
   ct: "2021-05-13T00:04:02"
}, {
   nm: "Z2zPpAVtBy",
   nickname: "Galante Davis",
   title: "nonapparentness violet",
   brief: "Dollarfish before-recited unspasmed birdlet bulldoggedness coulommiers. ",
   client: "192.168.24.250",
   ct: "2021-05-13T00:06:38"
}]
```