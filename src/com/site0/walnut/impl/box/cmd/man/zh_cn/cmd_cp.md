# 命令简介

`cp` 拷贝文件/目录到指定路径

拷贝目录需要 `-r`。默认不复制元数据，`-p` 保留。

# 用法

```bash
cp [-rpv] 源 目标
  -r    # 递归拷贝目录
  -p    # 复制时保留元数据
  -v    # 显示执行细节
```

# 示例

```bash
# 拷贝单个文件
cp hi.jpg hi_again.jpg

# 拷贝目录
cp -r superdir supermandir

# 拷贝并保留元数据
cp -p hi.jpg hi_keepmode.jpg
```
