---
title: 表格与 CSV 输出（sheet）
module: 70.functionate
keywords: [sheet, csv, xls, json, 表格, 导出]
source: [ext/media/sheet/]
related: [README, 30-office-ooml.md]
---

# 表格与 CSV 输出（sheet）

业务系统最常见的导出需求就是 CSV。
`sheet` 命令在 **JSON 数组** 和 **csv / xls** 之间互转，
支持字段挑选、重命名和格式化——通常一条命令就能完成导出。

## JSON → CSV

```bash
# 最简单的导出
echo '[{nm:"wendal",age:18},{nm:"zozoh",age:20}]' | sheet -tpo csv

# 指定字段和中文表头
echo '[{nm:"wendal",age:18}]' | sheet -tpo csv -flds 'nm:名称,age:年龄'
```

配合 `sqlx` 就是数据库导出：

```bash
sqlx 'SELECT nm,age FROM t_user' | sheet -tpo csv -out ~/users.csv -process '<auto>'
```

`-process '<auto>'` 会在输出过程中打印进度，数据量大时有用。

## CSV / XLS → JSON

```bash
# 读 xls 转 JSON
sheet ~/data/users.xls -tpi xls -tpo json

# 读 xls 写成 csv
sheet ~/data/users.xls -out ~/data/users.csv
```

## 格式控制

- `-tpi` / `-tpo`：输入 / 输出格式标识，`json | csv | xls`
- `-flds`：字段映射，`原名:新名` 逗号分隔，控制输出哪些列、列名是什么
- `-co '{"sep":";","noheader":true}'`：csv 输出选项（分隔符、是否带表头）
- `-ci/-co` 也控制 xls 的 sheetIndex、rowOffset 等读入选项

字段格式化（`$date`、`$n`、`$int`、`$boolean`、值映射表）在 man 文档
`cmd_sheet.md` 里有完整说明。
