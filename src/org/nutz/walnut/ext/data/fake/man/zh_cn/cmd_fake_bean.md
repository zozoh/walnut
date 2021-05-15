# 过滤器简介

`@bean` 生成一个或者多个模拟的对象

```js
{
  // 生成 UU32
  "key1" : "UU32",
  // 生成 0-100 的整数
  "key2" : "INT",
  // 生成 50-100 的整数
  "key3" : "INT:50-100",
  // 生成 随机的多段整数
  "key4" : "INTS:192.168.{100-200}.{0-255}",
  // 生成随机文本
  "key5" : "TEXT",
  // 生成随机文本，包括 5 至 10 个词
  "key5" : "TEXT:5-10",
  // 生成随机名称
  "key6" : "NAME",
  // 生成随机的毫秒数
  // today 表示今天
  // today+1d 表示明天
  // 第三段 5m 表示，五分钟一个随机时间
  "key7" : "AMS:today+1d:5m"
}
```

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