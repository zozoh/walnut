# 过滤器简介

`@translate` 用来转换上下文对象的值
    
# 用法

```bash
jsonx @translate
  [-f /path/to.json]    # 指定一个转换的映射文件
                        # 如果未指定，则从标准输入读取
  [-mapping]            # 表示这是一个BeanMapping的高级转换
                        # 否则只是简单字段映射
  [-by "key"]           # 映射文件的某个键下面的内容才是映射信息
                        # 只有 -mapping 模式下才有效
  [-only]               # 不在映射集合内的对象，将会被无视
```

# 示例

```bash
# 按照正则表达式过滤
jsonx @remove '^(a|b|c)$'

# 直接过滤键名
jsonx @remove key1 key2 key3

# 混合模式
jsonx @remove key1 "^(a|b|c)" key3
```