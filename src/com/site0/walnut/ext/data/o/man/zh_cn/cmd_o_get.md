# 过滤器简介

`@get` 根据 ID 获取一个或几个对象


# 用法

```bash
o @get
  [ID ...]           # 指定对象ID，可以是多个
  [-ignore]          # 指明这个参数，表示如果不存在就忽略
  [-fallback]        # 开启这个选项，仅获取第一个存在的对象
  [-reset]           # 表示将重置上下文的内容
```

# 示例

```bash
o @get ID1 ID2 ID3 ...
```
