# 命令简介

`refer` 用来管理系统索引服务
    
# 用法

```
refer [~/path/to/mountRoot] [@add|@remove|@view|@count|@objsha1]
```

# 子命令列表

```bash
add         # 增加一个引用
remove      # 移除一个引用
view        # 查看一个对象的引用情况
count       # 统计一个对象的引用计数
objsha1     # 将会修改某个系统对象SHA1的引用
```

# 示例
    
```bash
# 查看某 SHA1 的引用状态
demo:> refer @view 54af..9a2e
0) s15v0kefe2gc7om67ododn56a8
1) 5c5dltcr2ui02ormb7v6cdtd2f
2) 4oh9u9p5gmgrpoguk7bkftiubh
3) rnih6c86dch2ho563e7b15tidu
4) q5r72v7unaj88qkk98uqiao85g
5) 0cnhdbir8sin3q88himugvf40h

# 查看某挂载目录的 SHA1 的引用状态
demo:> refer ~/attachments @view 54af..9a2e
0) s15v0kefe2gc7om67ododn56a8
1) 5c5dltcr2ui02ormb7v6cdtd2f
```