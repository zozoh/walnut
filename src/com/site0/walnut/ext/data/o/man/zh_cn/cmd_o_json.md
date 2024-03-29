# 过滤器简介

`@json` 将当前上下文按照 JSON  的方式输出

# 用法

```bash
o @json 
  # 指定输出的键,是一个 AutoMatch 的字符串
  # 同时也支持快捷名: 
  #  - %EXT     : 过滤所有标准字段
  #  - %EXT-NM  : 过滤所有除了'nm'的标准字段
  #  - %EXT-TP  : 过滤所有除了'nm|tp'的标准字段
  #  - %EXT-TC  : 过滤所有除了'nm|tp'以及时间相关的标准字段
  #  - %EXT-THC : 过滤所有除了'nm|tp'时间以及Thing相关的标准字段
  [KeyMatch]
  #--------------------------------------
  [-path]    # 确保对象加载了路径
  [-cqn]     # 指定的格式化方式  
  #--------------------------------------
  [-mapping /path]  # 将结果做转换，指向一个BeanMapping映射文件
  [-by "key"]       # 映射文件的某个键下面的内容才是映射信息
  [-only]           # 当映射时不在映射集合内的对象，将会被无视
  [-explain {..}]   # 【选2】将结果（mapping之后）再次转换成新值
  [-explainBy /path] # 【选2】比explain更优先，用一个JSON 文件
                     # 提供更加复杂的转换行为
```

# 示例

```bash
o @get ID1 ID2 ID3 @json -cqn
```

