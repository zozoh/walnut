# 命令简介

`dirm` 统计目录下符合条件的文件数量，并可批量 cp/mv 到输出目录

常用于把目录里散落的文件归档到统一目录。只处理文件，跳过子目录。

# 用法

```bash
dirm [目录] [-A] [-match '{条件}'] [-cp|-mv] [-out 目录] [-p]
  -A       # 包含隐藏对象
  -match   # 统计条件（JSON）
  -cp      # 统计后把匹配的文件拷贝到 -out 目录
  -mv      # 统计后把匹配的文件移动到 -out 目录
  -out     # cp/mv 的目标目录（默认 ~/dirm_<时间戳>）
  -p       # 拷贝时保留元数据（配合 -cp）
```

# 示例

```bash
# 统计当前目录（非隐藏）文件数量
dirm

# 统计指定目录
dirm ~/upload

# 按条件统计
dirm ~/upload -match '{tp:"jpg"}'

# 把 jpg 归档到指定目录
dirm ~/upload -match '{tp:"jpg"}' -mv -out ~/archive/jpg
```
