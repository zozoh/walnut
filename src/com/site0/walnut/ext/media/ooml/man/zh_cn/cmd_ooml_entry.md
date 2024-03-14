# 过滤器简介

`@entry` 输出包中的实体列表

# 用法

```bash
ooml @entry
  [-list]      # 打印实体列表模式。如果不开启此选项，也没有 -t
               # 则按 JSON 模式输出
  [-t race..]  # 打印实例列表的表格模式, 可以指定显示字段和顺序
               # 半角逗号分隔: "race,type,size ,content,path"
               # 默认是全部字段
  [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
#
# JSON 模式输出压缩包里面的实体信息
#
ooml demo.xlsx @entry
[{
   race: "FILE",
   type: "xml",
   path: "[Content_Types].xml",
   size: 1454,
   ...
#
# 按表格的方式输入压缩包里的实体信息
#
ooml demo.xlsx @xlsx @entry -t -bish
#  | race | type | size  | content | path
---+------+------+-------+---------+----------------------------------------
0  | FILE | xml  | 1454  | 1454    | [Content_Types].xml
1  | FILE | rels | 588   | 588     | _rels/.rels
2  | FILE | xml  | 1586  | 1586    | xl/workbook.xml
3  | FILE | rels | 698   | 698     | xl/_rels/workbook.xml.rels
...
15 | FILE | xml  | 771   | 771     | docProps/app.xml
---+------+------+-------+---------+----------------------------------------
total 16 items
#
# 指定输入表格字段
#
ooml demo.xlsx @xlsx @entry -t type,path -bish
#  | type | path
---+------+----------------------------------------
0  | xml  | [Content_Types].xml
1  | rels | _rels/.rels
2  | xml  | xl/workbook.xml
...
15 | xml  | docProps/app.xml
---+------+----------------------------------------
total 16 items
#
# 按列表模式输入实体信息
#
ooml demo.xlsx @xlsx @entry -list
0.F:xml:1454:[Content_Types].xml
1.F:rels:588:_rels/.rels
2.F:xml:1586:xl/workbook.xml
...
15.F:xml:771:docProps/app.xml
```

