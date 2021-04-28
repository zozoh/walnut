# 过滤器简介

`@value` 将上下文中的对象依次取值，并输出

# 用法

```bash
o @value
  [KEY1, KEY2...] # 指定取哪个值
  [-as str]      # 默认是输出一个字符串连接表
                  # json/str
  [-sep ,]        # 输出为 str 时，定制的分隔符，默认为空串
  [-cqn]          # 输出为 json 时，指定的格式化方式
```

# 示例

```bash
# 输出所有节点的 ID
o ~/abc @query '{tp:"txt"}' @value id 
```

