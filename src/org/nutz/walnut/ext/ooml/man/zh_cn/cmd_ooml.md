# 命令简介

`ooml` 用来处理`Open Office Markup Language`相关的文件
    
# 用法

```
o [Path] [[@filter filter-args...]...]
```

# 过滤器列表

```bash
#
# 解析
#
@xlsx       # 将上下文解析为 XLSX 的工作表
#
# 设置上下文
#
@mapping    # 设置上下文映射表
#
# 输出
#
@entry      # 输出包中的实体列表
@workbook   # 输出上下文中workbook的全局信息
@sheet      # 输出上下文的某一个工作表
@rows       # 输出当前工作表的行数据
@medias     # 输出当前工作表关联的所有媒体
@beans      # 输出当前工作表数据对象列表
@export     # 将当前工作表数据对象输出到一个目录里
```