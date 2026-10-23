# 命令简介

`md5sum` 计算 MD5 摘要

支持文件、字符串、管道三种输入来源，多个文件时逐个输出。

# 用法

```bash
md5sum [-nt] [-s 字符串] [文件...]
  -s    # 对给定字符串计算摘要
  -t    # 从管道读入字符串
  -n    # 输出后不追加换行
```

# 示例

```bash
# 计算文件摘要
md5sum ~/data.bin

# 计算字符串摘要
md5sum -s hello

# 从管道计算
cat ~/data.bin | md5sum

# 多个文件
md5sum ~/a.txt ~/b.txt
```
