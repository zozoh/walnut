# 过滤器简介

`@ajax` 将当前上下文按照 AJAX 的方式输出

# 用法

```bash
o @ajax 
  # 指定输出的键,是一个 AutoMatch 的字符串
  # 同时也支持快捷名: 
  #  - %EXT     : 过滤所有标准字段
  #  - %EXT-NM  : 过滤所有除了'nm'的标准字段
  #  - %EXT-TP  : 过滤所有除了'nm|tp'的标准字段
  #  - %EXT-TC  : 过滤所有除了'nm|tp'以及时间相关的标准字段
  #  - %EXT-THC : 过滤所有除了'nm|tp'时间以及Thing相关的标准字段
  [-keys KeyMatch]
  #--------------------------------------
  [-path]    # 确保对象加载了路径
  [-cqn]     # 指定的格式化方式
```

# 示例

```bash
o @get ID1 ID2 ID3 @ajax -cqn
```

