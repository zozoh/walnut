# 命令简介 

`dsync @unpack` 将归档包展开到缓存

# 用法

```bash
dsync @unpack
  [{SHA1}]       # 【选】指定包的指纹，根据配置信息以及指纹能确定唯一的包名 
                 # 如果没有指定，则会操作HEAD索引树，否则会从缓存目录里加载
                 # 对应的索引树
  [-q -quiet]    # 静默输出
  [-f -force]    # 强制覆盖，否则缓存目录中存在的项目就不再写入了
  [-L -load]     # 将解开的归档包内容加载上下文中
  [-l -list]     # 仅仅是列出包的内容，并不写入到缓存
```
# 示例

```bash
# 输出对于的归档包名称
demo@~$ dsync @unpack
F: site.tree : -1bytes
F: meta/site.json : -1bytes
F: data/951b2d27dd61fe410eb5f38c32cd185a1db193a9 : -1bytes
F: data/1eb40e0bcd73ae8fbdafd75a8d3a4c80455e45f0 : -1bytes
F: data/1779b59783da1c361cd1aca04c11c0f8c363eb22 : -1bytes
...
F: data/ec93622d298be54714e24292ea34ad7d0965bd01 : -1bytes
F: data/9d281c667f2bba5f9f1108380604162771b88790 : -1bytes
F: data/c7b92516d17ef99821ceeb771e0991c666b41c0e : -1bytes
F: medias.tree : -1bytes
F: meta/medias.json : -1bytes
Done for unpack /home/demo/.dsync/pkg/dsync-042db63e9e19bec75b2fff9af8c8e97888c0273b.zip;ID(4rtmlegdsmhl9rhg5t9ie5qlj8) in Total: 2781ms : [21-03-31 00:32:53.694]=>[21-03-31 00:32:56.475]
item_count: 129
dir_count: 0
file_count: 0
size_count: 0Bytes
```