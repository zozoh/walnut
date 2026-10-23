# 命令简介

`trash` 命令用来管理回收站：把文件移入回收站，或从回收站还原。

# 用法

```bash
trash @mv 路径...        # 将文件放入回收站
trash @recover 路径...    # 将文件从回收站还原到原路径
```

支持的处理器：

```bash
@mv       # 移入回收站（/trash/<用户>/ 下，重名自动加 _1 后缀）
@recover  # 还原到原路径；若原路径已存在则抛错
```

使用前需要 root 账号初始化回收站目录：

```bash
# mkdir /trash
# chmod +x /trash
# chmod +r /trash
```

# 示例

```bash
# 将文件放入回收站
trash @mv /home/wendal/abc
# /home/wendal/abc -> /trash/wendal/abc

# 重名时自动改名
trash @mv /home/wendal/abc
# /home/wendal/abc -> /trash/wendal/abc_1

# 将文件还原到原路径
trash @recover /trash/wendal/abc
# /trash/wendal/abc -> /home/wendal/abc
```
