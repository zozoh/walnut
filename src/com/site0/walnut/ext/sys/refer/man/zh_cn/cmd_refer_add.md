# 过滤器简介

`@add` 为某个目标增加一个引用。

# 用法

```bash
refer @add 引用者ID 被引用者ID...
       # 引用者ID      # 目标 ID（如 SHA1）
       # 被引用者ID    # 引用它的对象 ID，可多个
```

# 示例

```bash
# 为某自定义目标增加引用
refer @add $SHA1 $ID1 $ID2
```
