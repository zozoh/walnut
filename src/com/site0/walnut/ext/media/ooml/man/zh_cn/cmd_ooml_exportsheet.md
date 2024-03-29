# 过滤器简介

`@exportsheet` 将当前工作表数据对象输出到一个目录里。一行一个对象。

本处理会根据上下文中的映射设定，来处理输出工作。

# 用法

```bash
ooml @exportsheet 
  [/path/to]         # 输出路径，默认是当前目录下的 "sheet" 目录
  [-head 0]          # 指定标题行，默认为 0, 表示第一行
                     # 标题行声明了对象的每个字段的键值
  [-name ${..}]      # 对象名称模板，如果不指定，则会一定创建一个新的对象
                     # 且名称与对象 ID 相同
  [-uniq "phone"]    # 指定一个唯一字段（映射后），
                     # 如果设置，添加的时候如果发现已经存在，则更新
  [-skip 0]          # 跳过多少数据
  [-limit 0]         # 最多导出多少数据，<=0 表示全部数据
  [-quiet]           # 静默输入
```

# 示例

```bash
ooml demo.xlsx @xlsx @sheet @exportsheet ~/myoutput
```