# 过滤器简介

`@pkgname` 输出上下文对应的归档包名称。

# 用法

```bash
dsync @pkgname [SHA1]
       # SHA1    # 指定包的指纹，根据配置信息以及指纹能确定唯一的包名；
                 # 不指定则操作 HEAD 索引树，否则从缓存目录加载对应的索引树
```

# 示例

```bash
# 输出对应的归档包名称
dsync @pkgname
# dsync-6e3b41d6e5e1fb29caa3dc45a8abdac718d5d8ac.zip
```
