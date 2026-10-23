# 过滤器简介

`@unpack` 将归档包展开到缓存。

# 用法

```bash
dsync @unpack [SHA1] [-q] [-f] [-L] [-l]
       # SHA1    # 指定包的指纹，根据配置信息以及指纹能确定唯一的包名；
                 # 不指定则操作 HEAD 索引树，否则从缓存目录加载对应的索引树
  -q       # 静默输出
  -f       # 强制覆盖，否则缓存目录中存在的项目就不再写入
  -L       # 将解开的归档包内容加载到上下文中
  -l       # 仅列出包的内容，不写入缓存
```

# 示例

```bash
# 列出归档包内容
dsync @unpack -l
# F: site.tree : -1bytes
# F: meta/site.json : -1bytes
# F: data/951b2d27dd61fe410eb5f38c32cd185a1db193a9 : -1bytes
# ...
# Done for unpack ... in Total: 2781ms

# 解开归档包并加载上下文
dsync @tree 575b..e964a @unpack -L
```
