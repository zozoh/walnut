# 命令简介

`pdf` PDF 处理

# 用法

```bash
pdf @pdf2jpg 源PDF 目标目录
```

支持的处理器：

```bash
@pdf2jpg    # PDF 每页转成一张 JPG（output_000.jpg 起）
```

# 示例

```bash
# 把 PDF 逐页转成图片
pdf @pdf2jpg ~/doc/report.pdf ~/doc/report_pages
```
