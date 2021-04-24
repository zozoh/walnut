# 过滤器简介

`@medias` 输出当前工作表关联的所有媒体

# 用法

```bash
o @medias
  [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
ooml demo.xlsx @xlsx @sheet @medias
[{
   fromColIndex: 4,
   fromRowIndex: 1,
   referId: "rId1",
   path: "xl/media/image1.png"
}, {
   fromColIndex: 4,
   fromRowIndex: 2,
   referId: "rId2",
   path: "xl/media/image2.png"
}]
```