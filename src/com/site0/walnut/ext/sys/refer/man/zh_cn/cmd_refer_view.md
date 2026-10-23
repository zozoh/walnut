# 过滤器简介

`@view` 查看某个指定对象的引用状况。

# 用法

```bash
refer @view 引用者ID
       # 引用者ID    # 目标 ID（如 SHA1）
```

# 示例

```bash
# 查看某 SHA1 的引用列表
refer @view 54af..9a2e
# 0) s15v0kefe2gc7om67ododn56a8
# 1) 5c5dltcr2ui02ormb7v6cdtd2f
# 2) 4oh9u9p5gmgrpoguk7bkftiubh
# ...
```
