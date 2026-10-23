# 过滤器简介

`@pdf2jpg` 把 PDF 文件的每一页转换成一张 JPG 图片，存入目标目录。

输出图片按页码命名：`output_000.jpg`、`output_001.jpg` ……

依赖服务器上的 ImageMagick（`/usr/bin/convert`），不存在时命令输出该路径并返回。

# 用法

```bash
pdf @pdf2jpg 源PDF 目标目录
```

# 示例

```bash
# 把 PDF 逐页转成图片
pdf @pdf2jpg ~/doc/report.pdf ~/doc/report_pages
```
