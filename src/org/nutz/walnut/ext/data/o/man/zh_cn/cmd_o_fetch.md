# 过滤器简介

`@fetch` 根据路径获取一个或几个对象

# 用法

```bash
o @fetch
  [~/path/to/file ...]   # 指定对象路径，可以是多个
  [-race FILE]           # 指定对象族类，如果不存在，则会自动创建
                         # 如果不声明这个属性，则文件必须存在
  [-ignore]              # 指明这个参数，表示如果存在就忽略
  [-fallback]            # 开启这个选项，仅获取第一个存在的对象
                         # 它会直接让 -race/ignore 失效
```

# 示例

```bash
o @fetch a.txt b.txt c.txt
```

