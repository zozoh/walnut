# 命令简介 

    `sheet` 可以将JSON数组，转换成数据表格，或者将数据表格转换成JSON数组

---------------------------------------
# 用法

```
sheet /path/to/file      # 输入文件，如果为空，则从管道读取
     [-out /path/to/out] # 输入的文件路径，没有就会创建
     [-tpi csv]          # 输入的内容格式，如果是从管道读取，这个必须需要
                         # 如果没有设置，则看输入文件类型，从管道的话，默认 json
     [-tpo xls]          # 输出的内容格式，如果没指定输入文件，而是直接写到输出流里，这个必须需要
     [-flds ...]         # 字段映射内容
     [-mapping /path]    # 字段映射文件路径，比 -flds 优先
     [-ci {..}]          # 输入处理器需要的配置项
     [-co {..}]          # 输出处理器需要的配置项
     
       -out [输出] -flds [字段设定] -noheader -sep ";"
```

对于 `-tpi` 和 `-tpo` 支持下面的格式

- `json`
- `csv`
- `xls`

---------------------------------------
# 映射文件

映射文件 `-flds` 可以是一个文件路径 `file://~/path/to/mapping` 也可以直接就是映射内容

```
th_nm:名称
phone:电话
lm[$date%yyyy-MM-dd HH:mm:ss]:最后修改日期
lbls[$n]:标签
th_media_list[$n.id]:媒体
```

- 字段之间可以用半角逗号分隔，也可以用换行符分隔
- `$date` 表示日期，`%` 后面的表示日期格式，不写的话，默认 `yyyy-MM-dd HH:mm:ss`
- `$n` 表示数组，会用半角逗号连接
- `$n.id` 表示对象数组，输出的值为每个对象的 `id`，用半角逗号连接

Key 的全部写法为：

- 子对象   :  `a.b.c`
- 多重获取 : `key1||key2`
- 数组    : `key[$n]`
- 数组值   : `key[$n.name]`
- 日期    : `key[$date]`
- 日期    : `key[$date%yyyy-MM-dd HH:mm]`

---------------------------------------
# 处理器配置

## json 处理器

```
参见 JsonFormat
```

## csv 处理器

```
{
    sep : ",",          // 分隔符，默认 ","
    noheader  : false,  // 是否输出标题栏，默认 false
    emptyCell : "--"    // 空单元格输出的占位符
}
```

## xls 处理器

```
{
    sheetIndex : 0,        //「选」指明工作标签下标，默认0
    sheetName  : "xxx",    //「选」指明工作标签名称，比 sheetIndex 优先    
}
```

---------------------------------------
# 示例

```
# 将 JSON 数据变成一个 csv 并输出到标准输出
obj * -json | sheet -tpo csv -flds "key1:title1:dft1,key2:title2:dft2..."
```








    