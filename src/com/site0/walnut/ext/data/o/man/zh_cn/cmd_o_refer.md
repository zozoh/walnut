# 过滤器简介

`@refer` 读取上下文对象关联的对象详情

# 用法

```bash
o @refer 
  [key]          # 对象的键名，这个键是对另外一个对象的引用
                 # 支持半角逗号分隔，表示多个键
                 # 如果 key 采用 "xxx=id" 这种形式，表示这个键实际上是个 ID
                 # 譬如 "thumb,video_cover=id"
  [name]         # 将目标对象读取出来后，存放到对象的哪个键里
                 # 支持模板字符串，默认为 `${key}_obj`
  [-get keyPath] # 声明了这个属性，表示是针对上下文对象某个属性
                 # 对应的子对象列表执行上下文关联对象详情的读取操作
                 # 支持 a.b.c这种形式
  [-keys ...]    # 一个字段过滤器，表示挑选目标对象哪些键来存储
                 # 支持 '%EXT|NM|TP...' 等快捷键
                 # 默认的，会采用 '%SHA1'
```

# 示例

```bash
o @refer thumb thumb_obj -keys '^(id|nm|title|sha1|len|mime|tp|width|height)$'
```

