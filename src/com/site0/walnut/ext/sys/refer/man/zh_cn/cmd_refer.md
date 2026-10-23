# 命令简介

`refer` 用来管理系统索引服务（SHA1 引用计数）。

# 用法

```bash
refer [挂载根目录] @处理器 [参数...]
  # 挂载根目录    # 不给则用系统默认的引用索引库
```

支持的处理器：

```bash
@add        # 增加一个引用
@remove     # 移除一个引用
@view       # 查看一个对象的引用情况
@count      # 统计一个对象的引用计数
@objsha1    # 修改某个系统对象的 SHA1 引用
```

# 示例

```bash
# 查看某 SHA1 的引用状态
refer @view 54af..9a2e
# 0) s15v0kefe2gc7om67ododn56a8
# 1) 5c5dltcr2ui02ormb7v6cdtd2f
# ...

# 查看某挂载目录的 SHA1 的引用状态
refer ~/attachments @view 54af..9a2e
```
