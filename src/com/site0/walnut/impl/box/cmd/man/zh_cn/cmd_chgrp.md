# 命令简介

`chgrp` 修改对象的属组

# 用法

```bash
chgrp [-Rv] 组 路径...
  -R    # 递归处理目录下的所有子孙对象
  -v    # 每修改一个对象，输出其相对路径
```

# 示例

```bash
# 修改属组
chgrp guests ~/work

# 递归修改整个目录的属组
chgrp -R guests ~/work
```
