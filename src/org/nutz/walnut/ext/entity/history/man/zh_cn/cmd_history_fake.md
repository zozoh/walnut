命令简介
======= 

`history fake` 可以伪造一条或者多条历史记录

为了能尽量模拟真实，命令支持你给入`uid`和`tid`的候选集合。然后从中随机挑选一些记录生成历史。

本命令的行为由输入的配置文件决定:

```js
{
  // 预先定义数据集合
  schema : {
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
  userId : {
    // 数据采用哪个 schema
    // 如果多个键采用相同的schema，则标识从对应对象上选取不同的 key
    schema : "accounts",
    // 对应数据集合的对象里面的哪个键
    key : "id"
  },
  userName : {
    schema : "accounts",
    key  : "nickname",
  },
  targetId : {
    schema : "videos",
    key : "id"
  },
  targetType : {
    value : "video"  // 直接采用一个静态值
  },
  operation : {
    cans : ["pay","view","like"]
  },

}
```


用法
=======

```bash
history {HistoryName} fake 
  [/path/to/conf]        # Fake 的配置文件路径
  [-date   now-1d]       # 模拟数据的起始时间，默认昨天
  [-span   7d]           # 时间跨度，向过去多久
  [-nb   100]            # 一共需要模拟多少条数据，默认 10
  [-seq  0]              # 伪记录的序号起始值，默认 0
                         # 配置了{gen:".."}的字段有效
  [-idpad 6]             # 伪需要的补零位，默认 6
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
# 添加一条历史记录
history fake ~/.myfake.json
```