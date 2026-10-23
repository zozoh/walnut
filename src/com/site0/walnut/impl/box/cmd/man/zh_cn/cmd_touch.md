# 命令简介

`touch` 创建文件，或更新已有文件的最后修改时间

文件不存在则创建（FILE），存在则刷新 `lm` 时间戳。

# 用法

```bash
touch [-o] 路径...
  -o    # 输出创建/更新的对象（JSON）
```

# 示例

```bash
# 创建一个文件
touch abc

# 更新已存在文件的时间戳
touch abc

# 创建并输出对象信息
touch -o abc
```
