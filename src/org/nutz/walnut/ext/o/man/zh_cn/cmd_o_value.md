# 过滤器简介

`@value` 将上下文中的对象依次取值，并输出

# 用法

```bash
o @ancestors 
  [-key id]    # 指定取哪个值
  [-as json]   # 默认是输出一个 JSON 数组
               # json/str/lines
  [-sep ,]     # 输出为 str 时，定制的分隔符，默认`,`
  [-cqn]       # 输出为 json 时，指定的格式化方式
```

# 示例

```bash
# 输出所有节点的 ID
o ~/abc @query '{tp:"txt"}' @value id 
```

