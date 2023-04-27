# 命令简介 

`dsync @ids` 输出上下文加载的树的 ID 映射。

它会遍历指定的树，并加载树对象对应的标准元数据，则可以获得一个原始 ID 与路径的列表。
通过 `-as id` 参数可以输出原始ID与新ID的映射。

# 用法

```bash
dsync @ids
  [FILE|DIR]      # 指定限制寻找文件还是目录
  [^.*.pdf$]      # 一个正则表达式表示过滤路径
  [~/my/folder]   # ~ 开头表示是一个路径前缀
  [-tree ^(name1|name2)$]    # 指定树名称，如果不指定则为全部的树
                             # 支持正则表达式
  [-limit 0]      # 最多输出多少记录，0 表示全部
  [-skip 0]       # 跳过多少记录
  [-as id|path]   # `-as id` 表示输出的映射值为新对象ID，默认相当于 -as path
  [-cqn]          # JSON 格式化输出
```
# 示例

```bash
dsync @tree 61ea7d15c314dc1a386a41a65378f9288300e99d @ids -skip 0 -limit 20000 -tree site -as id -qn > ~/.tmp/ar_idmap2.json
```