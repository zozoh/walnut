# 过滤器简介

`@entry` 输出包（如 xlsx）中的实体列表。

# 用法

```bash
ooml 包文件 @entry [-list] [-t 字段,...] [-bish] [-cqn]
  -list    # 列表模式输出；不开启且无 -t 时按 JSON 模式输出
  -t       # 表格模式，可指定显示字段和顺序，半角逗号分隔
           # 如 'race,type,size,content,path'，默认全部字段
  -bish    # 表格输出的格式化开关
  -cqn     # JSON 输出的格式化开关
```

# 示例

```bash
# JSON 模式输出压缩包里面的实体信息
ooml demo.xlsx @entry
# [{
#    race: "FILE",
#    type: "xml",
#    path: "[Content_Types].xml",
#    size: 1454,
#    ...

# 按表格的方式输出压缩包里的实体信息
ooml demo.xlsx @xlsx @entry -t -bish
#  | race | type | size  | content | path
# 0  | FILE | xml  | 1454  | 1454    | [Content_Types].xml
# ...
# total 16 items

# 指定表格字段
ooml demo.xlsx @xlsx @entry -t type,path -bish

# 列表模式输出实体信息
ooml demo.xlsx @xlsx @entry -list
# 0.F:xml:1454:[Content_Types].xml
# 1.F:rels:588:_rels/.rels
# ...
```
