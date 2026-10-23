# 命令简介

`chown` 修改对象的属主（可同时修改属组）

用户必须是已存在的系统账户，否则报错。属组可选，用 `用户:组` 的形式一并修改。

# 用法

```bash
chown [-Rv] 用户[:组] 路径...
  -R    # 递归处理目录下的所有子孙对象
  -v    # 每修改一个对象，输出其相对路径
```

# 示例

```bash
# 修改属主
chown xiaobai ~/work/report.txt

# 同时修改属主和属组
chown xiaobai:guests ~/work

# 递归移交整个目录
chown -R xiaobai ~/work
```
