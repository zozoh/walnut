# 过滤器简介

`@beans` 输出当前工作表数据对象列表

# 用法

```bash
ooml @beans
  [-head 0]    # 指定标题行，默认为 0, 表示第一行
               # 标题行声明了对象的每个字段的键值
  [-limit 0]   # 最多输出多少行，0表示不限
  [-skip  0]   # 跳过多少行（从标题下第一行开始）
  [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
ooml demo.xlsx @xlsx @sheet @beans -q
[{
   "Number": "1",
   "Name": "xxxx",
   "Picture": {
      "fromColIndex": 4,
      "fromRowIndex": 1,
      "referId": "rId1",
      "path": "xl/media/image1.png"
   },
   "Type" : "45"
}]
```