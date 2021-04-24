# 过滤器简介

`@xlsx` 将上下文解析为 XLSX 的工作表，会设置上下文中的 `workbook` 字段。
这个命令，需要再 `@sheet|@workbook` 等子命令之前调用。

# 用法

```bash
o @xlsx
```

# 示例

```bash
ooml ~/abc.xlsx @xlsx
```

