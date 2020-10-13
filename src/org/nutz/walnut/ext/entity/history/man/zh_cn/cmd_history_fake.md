命令简介
======= 

`history fake` 可以伪造一条或者多条历史记录

为了能尽量模拟真实，命令支持你给入`uid`和`tid`的候选集合。然后从中随机挑选一些记录生成历史。

本命令的行为由输入的配置文件决定:

```js
{
  // 预先定义数据集合
  schama : {
    // 指向一个数据目录或者文件的路径，如果是目录则查找其内子对象
    // 如果是文件，则认为是一个 JSON 数据的文件
    "accounts" : {
      path : "~/accounts/index"
    }
    "videos"   : {
      path  : "~/videos/index",
      query : {},
      sort  : {ct:1},
      limit : 20,
      skip  : 0
    }

  },
  id : {
    // 这个是一个随机生成的模板，有一个占位符
    // ${seq} 为一个序号，从命令的输入获得
    // 当然如果你不声明 "id" 这个字段，则这个字段值为空
    // 那么 Hisotry API 会自动为其分配一个真正的 ID
    gen : "FAKE-${seq}"
  },
  uid : {
    // 数据采用哪个 schema
    // 如果多个键采用相同的schema，则标识从对应对象上选取不同的 key
    schama : "accounts",
    // 对应数据集合的对象里面的哪个键
    key : "id"
  },
  unm : {
    schama : "accounts",
    key  : "nickname",
  },
  tid : {
    schama : "videos",
    key : "id"
  },
  ttp : {
    value : "video"  // 直接采用一个静态值
  },
  opt : {
    cans : ["pay","view","like"]
  },

}
```


用法
=======

```bash
history {HistoryName} fake 
  [/path/to/conf]        # Fake 的配置文件路径
  [-dbegin 2020-09-21]   # 模拟数据的时间范围（起始日期时间）默认从to向前7天
  [-dend   now-1d]       # 模拟数据的时间范围（截止日期时间）默认昨天
  [-nb   100]            # 一共需要模拟多少条数据
  [-seq  0]              # 伪记录的序号起始值，配置了声明了id:{gen:".."}时有效
```

示例
=======

```bash
# 添加一条历史记录
history fake ~/.myfake.json
```