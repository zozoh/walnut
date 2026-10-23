# 命令简介

`cat` 输出一个或多个文件的全部内容

支持路径或 `id:` 两种方式指定对象。目录对象会报错跳过。

# 用法

```bash
cat [-quiet] 对象...
  -quiet    # 静默模式：对象不存在时不输出任何内容
```

# 示例

```bash
# 按路径显示文件内容
cat ~/abc.txt

# 一次显示多个文件
cat a.txt b.txt

# 按 ID 显示
cat id:fqmnqpKVIvu91vYZgePoQ0

# 多个 ID
cat id:fqmnqpKVIvu91vYZgePoQ0 6C_B-p_1IjaFWlJk_br_c1
```
