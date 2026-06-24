# 过滤器简介

`@load` 重新加载上下文对象

# 用法

```bash
@load 
    [/path/to/file]        # 要加载的文件
    [-as json|tpll]        # 加载文件格式
    [-from /path/to/src]   # 【选】如果as=tpll
                           # 这个参数可以指定 tpll 文件的路径
                           # 如果不指定这个参数，那么就把标准输入
                           # 作为 tpll 文件的来源
    [-tz {timeZoneId}]     # 对于 tpll 中的数据，日期类型的会被转换为
                           # 标准的 Date 对象，这里指定转换时用到的时区
                           # 即，20050109 所在的时区是哪一个，
                           # 可用 -tz Australia/ACT 表示悉尼时间
                           # 可用 -tz UTC 表示标准时间
    [-raw numeric,dts20]   # 强制将指定类型解析为原始值
                           # - numeric,dts20,dcymd8
                           # 多个类型用半角逗号分隔
                           # 如果不指定，则自动根据类型解析值
                           # 如果指定 -raw all 则表示所有类型都采用原始值
    [-get {key}]           # 读取结果再次获取其中的一个键的值作为结果
                           # 支持 `a.b.c` 这样的键路径
    [-omit k1,k2..]        # 读取结果需要忽略的键
    [-pick k1,k2..]        # 读取结果需要保留的键
    [-put {key}]           # 指定这个参数，就不会替换上下文，而是将其计入
                           # 上下文的一个键中，支持 `a.b.c` 这样的键路径
                           # 当然上下文如果为空或者不是一个 Map 则无效


    
```

加载上下文对象，可以支持下面几种数据源

1. `as:"json"` 输入源就是一个标准的 JSON
2. `as:"tpll` 输入源是一个 tpll 文件，即 Tex-Parts-Lenght-Lines 文件
   每行一个对象，对象的字段是这个行中固定长度的一段

# 示例

```bash
# 加载 JSON 数据
cat ~/my.json | jsonx @load -as json 

# 加载 TLL 文件
cat ~/my.txt | json @load -as tpll ~/my.parse.json
```

