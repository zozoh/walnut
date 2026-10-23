# 过滤器简介

`@count` 查看某个指定对象的引用数量。

# 用法

```bash
refer @count 引用者ID
       # 引用者ID    # 目标 ID（如 SHA1）
```

# 示例

```bash
# 查看某 SHA1 的引用数量
refer @count 54af..9a2e
# 18
```
