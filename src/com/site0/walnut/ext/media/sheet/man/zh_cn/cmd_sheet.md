# 命令简介

`sheet` 可以在 JSON 数组与数据表格之间互转：把 JSON 数组渲染成 csv/xls，或把 csv/xls 解析成 JSON 数组。

转换时可用字段映射（`-flds`/`-mapping`）挑选、重命名、格式化字段。

# 用法

```bash
sheet [输入文件] [-out 输出文件] [-tpi 格式] [-tpo 格式] [-flds 映射] [-mapping 映射文件] [-ci JSON] [-co JSON] [-process 模板] [-keys AutoMatch] [-names AutoMatch] [-pick AutoMatch] [-omit AutoMatch] [-skip N] [-limit N]
  # 输入文件    # 不给则从管道读取
  -out        # 输出文件路径；不给则输出到标准输出
  -tpi        # 输入内容格式：json|csv|xls；从管道读取时默认 json
  -tpo        # 输出内容格式：json|csv|xls；默认取输出文件的类型
  -flds       # 字段映射内容，字段用半角逗号或换行分隔
  -mapping    # 字段映射文件路径，比 -flds 优先；文件是 json 时采用 BeanMapping 映射
  -ci         # 输入处理器的配置项 JSON
  -co         # 输出处理器的配置项 JSON（如 noheader、sep 等）
  -process    # 进度模式：输出到文件时，向标准输出打印每条的处理进度
              # 模板占位符 ${xxx} 取当前记录字段，${P} 为进度串 %[10/982]，${I} 为记录下标（1base）
              # 特殊值 "<auto>" 表示输出 "${P}: 前三个字段"
  -keys       # 只有匹配的键才被保留（对应 fld.key）
  -names      # 只有匹配的输出字段名才被保留（对应 fld.name）
  -pick       # 仅保留匹配的记录
  -omit       # 过滤掉匹配的记录
  -skip       # 跳过输入列表的前 N 条，默认 0
  -limit      # 最多输出多少条，默认 0 表示不限
```

# 映射写法

```bash
th_nm:名称
phone:电话
lm[$date%yyyy-MM-dd HH:mm:ss]:最后修改日期
lbls[$n]:标签
sex[${1=男;2=女}]:性别
th_enabled[$boolean->Yes/No]:生效
phone[$str]:电话
th_price[$int]:价格
```

- 字段之间用半角逗号或换行分隔
- `$date` 日期，`%` 后是格式，默认 `yyyy-MM-dd HH:mm:ss`
- `$n` 数组，用半角逗号连接；`$n.id` 对象数组取每个的 `id`
- `${1=男;2=女}` 值映射表
- 所有 `$xxx` 都可写成 `@xxx`（避免 `$` 被其他程序处理）
- Key 的完整写法：`a.b.c` 子对象、`key1||key2` 多重获取、`key[$n]` 数组、`key[$date]` 日期、`key[$boolean->Yes/No]` 布尔转字符串、`key[$int]` 整数

# 处理器配置

csv 输出（`-co`）：

```bash
{
    sep : ",",          # 分隔符，默认 ","
    noheader  : false,  # 不输出标题栏，默认 false
    emptyCell : "--"    # 空单元格的占位符
}
```

xls（`-ci`/`-co`）：

```bash
{
    sheetIndex : 0,        # 工作标签下标，默认 0
    sheetName  : "xxx",    # 工作标签名称，比 sheetIndex 优先
    noheader   : false,    # 不输出标题栏，默认 false
    rowOffset  : 0,        # 起始行
    colOffset  : 0,        # 起始列
    images     : "/xx/xx", # 导出图片数据
    addRowIndex: 123,      # 添加行号
}
```

# 示例

```bash
# 将 JSON 数组渲染成 csv 输出到标准输出
echo '[{nm:"wendal",age:18},{nm:"zozoh",age:20}]' | sheet -tpo csv

# 指定字段映射和标题
echo '[{nm:"wendal"}]' | sheet -tpo csv -flds 'nm:名称,age:年龄'

# 解析 xls 成 JSON
sheet ~/data/users.xls -tpi xls -tpo json

# 转换并输出到文件，同时打印进度
sheet ~/data/users.xls -out ~/data/users.csv -process '<auto>'
```
