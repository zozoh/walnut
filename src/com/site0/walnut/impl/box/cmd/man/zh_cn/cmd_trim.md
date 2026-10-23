# 命令简介

`trim` 去除管道输入文本的首尾空白

默认两端都去除；`-l` 只去左侧，`-r` 只去右侧。

# 用法

```bash
[上游命令] | trim [-lr]
  -l    # 只去左侧空白
  -r    # 只去右侧空白
```

# 示例

```bash
# 去除两端空白
echo "  hello   " | trim

# 只去左侧
echo "  hello   " | trim -l

# 只去右侧
echo "  hello   " | trim -r
```
