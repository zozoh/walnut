# 过滤器简介

`@inc` 对上下文的所有文件对象采用某个字段生成连续的序号

# 用法

```bash
o @inc
  [1]                # 增加的值，默认为 1
  [-key value]       # 采用的键，默认为`value`键
  [-pad 6]           # 是否对齐补 0，只有非 -quiet 模式才有效
  [-quiet]           # 不输出序号，否则将输出序号，而阻止默认输出
  [-json]            # 按照 JSON 输出, 否则将按行输出序号
  [-l]               # 按 JSON 输出时，只有一个对象时也强制输出为数组
  [-cqn]             # 按 JSON 输出时的格式化方式
```

# 示例

```bash
# 生成一个四位的序号
$demo> o @fetch -race FILE ~/tmp/seq1 @inc -pad 4
0001
```
