# 过滤器简介

`@map2list` 如果上下文对象是Map，则会被转成list。

即将 `{a:{x:1},b:{y:2}}` 变成 `[{key:a,x:1},{key:b,y:2}]`
    

# 用法

```bash
@map2list
  [-key key]         # 键值如果加到列表元素里，键名是什么，默认"key"
                     # 如果值为 `-nil-` 则表示不加入到列表元素里
  [-ignore k1,!k2]   #【选】指定某些键存在就忽略输出
                     # '!' 开头表示这个键不存在就忽略
                     # 半角逗号分隔，这些条件为 OR 的关系
```

