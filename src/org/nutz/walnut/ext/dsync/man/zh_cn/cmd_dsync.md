# 命令简介

`dsync` 处理文件同步相关的操作

@see `f0-data-sync.md`

# 用法

```bash
# {ConfigName} 如果不声明，则表示 `dsync`
# 即，会根据配置文件 ~/.dsync/dsync.json 进行子命令操作
dsync {ConfigName} @xxx
```

它支持的子命令有：

```bash
dsync @archive       # 根据索引树建立归档包
dsync @as            # 输出上下文信息
dsync @pkgname       # 输出上下文对应的归档包名称
dsync @restore       # 根据上下文恢复域文件
dsync @tree          # 构建或加载对象的索引树
dsync @unpack        # 将归档包展开到缓存
```
