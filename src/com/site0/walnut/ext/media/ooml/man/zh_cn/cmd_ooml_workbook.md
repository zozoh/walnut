# 过滤器简介

`@workbook` 输出上下文中workbook的全局信息，之前需要调用过 `@xlsx` 解析工作簿。

# 用法

```bash
ooml @workbook
    [xml ...]    # 指定要输出的方式，支持
                 #  - xml  : 工作簿的 XML 内容
                 #  - tree : 工作簿的 XML 按 tree 的形式展现
                 #  - rels : 工作簿的资源映射表
                 #  - strs : 工作簿的共享字符串表
                 # 和支持多个一起输出，主要就是为了查看工作簿的解析数据
    [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
#-------------------------------------------------
# 打印工作簿的 XML
ooml demo.xlsx @xlsx @workbook xml
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x15 xr xr6 xr10 xr2" ...
...
#-------------------------------------------------
# 打印工作簿的 tree
ooml demo.xlsx @xlsx @workbook tree
[0]workbook(0):  @xmlns=htt...
|   |-- [0]fileVersion(0):  @appName=xl @lastEdited=7 @lowestEdited=7 @rupBuild=23901
|   |-- [1]workbookPr(1):  @defaultThemeVersion=166925
|   |-- [2]mc:AlternateContent(2):  @xmlns:mc=htt...
|   |   |-- [0]mc:Choice(0):  @Requires=x15
|   |   |   |-- [0]x15ac:absPath(0):  @url=D:\worksp...
|   |-- [3]xr:revisionPtr(3):  @revIDLastS...
|   |-- [4]bookViews(4): 
|   |   |-- [0]workbookView(0):  @xWindow=-120 ...
|   |-- [5]sheets(5): 
|   |   |-- [0]sheet(0):  @name=...
|   |-- [6]calcPr(6):  @calcId=0
#-------------------------------------------------
# 打印工作簿的关系映射表
ooml demo.xlsx @xlsx @workbook rels
{
   path: "xl/workbook.xml",
   rels: {
      rId1: {
         id: "rId1",
         target: "worksheets/sheet1.xml",
         type: "WORKSHEET"
      },
      rId2: {
         id: "rId2",
         target: "theme/theme1.xml"
      },
      rId3: {
         id: "rId3",
         target: "styles.xml",
         type: "STYLES"
      },
      rId4: {
         id: "rId4",
         target: "sharedStrings.xml",
         type: "SHARED_STRING"
      }
   }
}
#-------------------------------------------------
# 打印工作簿的共享字符串
ooml demo.xlsx @xlsx @workbook strs
0.Number
1.Name
...
#-------------------------------------------------
```

