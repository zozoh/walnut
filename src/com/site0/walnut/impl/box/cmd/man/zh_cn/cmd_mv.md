# 命令简介

`mv` 移动对象到新位置

目标已存在且是目录时，源对象移动到其下；目标不存在时相当于改名。
目标已存在且是文件则报错。

# 用法

```bash
mv [-TScnqlov] 源 目标
  -T     # 保持原来的类型不变
  -S     # 不触发源和目标目录 syncTime 的修改
  -v     # 显示移动细节（优先于 -o）
  -o     # 将移动的对象输出为 JSON（多个则为列表）
  -cqn   # JSON 格式开关；l 强制输出列表
```

# 示例

```bash
# 移动到已存在的目录
mv ./test.txt ~/myfolder

# 文件改名
mv abc.txt xyz.txt

# 显示移动细节
mv -v abc.txt ~/myfolder
```
