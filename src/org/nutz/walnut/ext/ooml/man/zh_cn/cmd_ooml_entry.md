# 过滤器简介

`@entry` 输出包中的实体列表

# 用法

```bash
o @entry
  [-list]      # 打印实体列表模式。如果不开启此选项，则按 JSON 模式输出
  [-t race..]  # 打印实例列表的表格模式
  [-cqn]       # 指定的格式化方式  
```

# 示例

```bash
ooml ~/abc.xlsx @entry -list
```

