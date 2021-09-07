# 过滤器简介

`@rows` 输出当前工作表的行数据

# 用法

```bash
ooml @rows
  [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
ooml demo.xlsx @xlsx @sheet @rows
[{
  rowIndex: 1,
   cells: [{
      reference: "A1",
      styleIndex: 1,
      dataType: "SharedString",
      value: "Number"
   }]
}]
```