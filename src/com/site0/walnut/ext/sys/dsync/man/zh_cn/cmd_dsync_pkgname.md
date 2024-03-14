# 命令简介 

`dsync @pkgname` 输出上下文对应的归档包名称

# 用法

```bash
dsync @pkgname
  [{SHA1}]    # 【选】指定包的指纹，根据配置信息以及指纹能确定唯一的包名 
              # 如果没有指定，则会操作HEAD索引树，否则会从缓存目录里加载
              # 对应的索引树
```
# 示例

```bash
# 输出对于的归档包名称
demo@~$ dsync @pkgname
dsync-6e3b41d6e5e1fb29caa3dc45a8abdac718d5d8ac.zip
```