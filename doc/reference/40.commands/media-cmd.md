---
title: 媒体处理命令
module: 40.commands
keywords: [imagic, videoc, videoi, qrcode, sheet, subtitle, ooml, dom, edi, mediax]
source: [ext/media/]
related: [command-model, data-cmd, ../70.integrations/alipay-aliyun.md]
---

# 媒体处理命令

Walnut 把一批媒体处理能力做成了内置命令：图像、视频、二维码、电子表格、字幕、Office 文档渲染、HTML 操作等。源码集中在 `src/com/site0/walnut/ext/media/`。

> 每个命令的完整参数请以系统内 `man <命令>` 为准（本文只讲机制和常见用法）。`pdf`、`tpassport` 两个命令目前没有手册页，用法请直接读源码。

## 命令一览

| 命令 | 用途 | 手册 |
|---|---|---|
| `imagic` | 图像处理（缩放/裁剪/水印/旋转等，基于 java.awt） | ✅ |
| `videoc` | 视频转码（封装 ffmpeg） | ✅ |
| `videoi` | 读取视频元信息（时长/码率/分辨率等） | ✅ |
| `qrcode` | 生成二维码图片 | ✅ |
| `sheet` | 电子表格读写（csv/xls/xlsx 互转、取行） | ✅ |
| `subtitle` | 字幕格式转换（srt） | ✅ |
| `ooml` | Office 模板渲染（docx/xlsx 占位符与循环） | ✅（10+ 子页） |
| `avatar` | 生成头像图（文字/颜色） | ✅ |
| `dom` | jsoup 风格的 HTML/XML  DOM 操作 | ✅（11 子页） |
| `edi` | EDI 电子数据交换（加载/解析/渲染报文） | ✅ |
| `mediax` | 媒体抓取与下载（crawl/download/post） | ✅ |
| `pdf` | PDF 处理（JVM Handler 结构） | ❌ |
| `tpassport` | 证件照排版绘制 | ❌ |

## imagic：图像处理

输入输出都走 Walnut 文件对象或标准流，处理步骤用 `-filter` 串成管道：

```bash
# 缩小到 1/2，再以封面模式缩放为 1920x1080，品质 0.6
imagic abc.jpg -filter 'scale(0.5) cover(1920,1080)' -qa 0.6 -out xyz.jpg

# 裁剪（起点 0,0，区域 120x180）
imagic abc.jpg -filter 'clip(0,0,120,180)' -qa 0.8 -out xyz.jpg

# 镜像与自动旋转
imagic abc.jpg -filter 'mirror(0) autoexif(false)' -out xyz.jpg
```

可用过滤器：`scale / rotate / clip / flip|mirror / autoexif / cover / contains`（参数细节见 `man imagic`）。源文件支持 http:// 地址；`-out ~self~` 表示原图替换。

## videoc / videoi：视频转码与信息

`videoc` 转码后会把产物写进 `.<文件名>.videoc/` 目录，并回写源文件元数据（`videoc_dir / video_preview / thumb`）：

```bash
# 转码（输出主视频 + 预览视频 + 预览图）
videoc ~/in.avi -bv 3000 -ba 64 -vcodec libx264 -preset ultrafast -fps 24

# 只生成预览视频和预览图
videoc ~/in.avi -mode '^preview'

# 只输出主视频
videoc ~/in.avi -mode main
```

`videoi` 读取视频元信息（时长/码率/分辨率等），输出 JSON。

## qrcode：二维码

```bash
# 把一段文本变成二维码图片（写入对象）
echo 'https://example.com/p/123' | qrcode > ~/qr.jpg

# 带 icon 的二维码（-icon 给图片 id 或路径，-size 尺寸，-c 容错级别）
qrcode 'https://example.com/p/123' -icon id:28e8s.. -size 300 -c H > ~/qr.jpg

# 解码二维码图片
qrcode -d ~/qr.jpg
```

## sheet：电子表格

核心是「JSON 数组 ⇄ 表格文件（csv/xls）」双向转换，并带字段映射能力：

```bash
# xlsx 转 csv，便于脚本处理
sheet ~/report.xls -out ~/report.csv

# JSON 数组（管道）导出成 xls
echo '[{name:"A",age:18},{name:"B",age:20}]' | json -c | sheet -tpi json -tpo xls -out ~/out.xls

# 字段映射：键 名称 一一对应，支持类型转换与默认值
sheet ~/in.csv -out ~/out.xls -flds 'th_nm:名称, phone:电话, lm[$date%yyyy-MM-dd]:最后修改日期'
```

`-flds` / `-mapping` 的映射语法很强大：`[$int]`、`[$date%fmt]`、`[$boolean->Yes/No]`、`[${1=男;2=女}]` 等（完整列表见 `man sheet`）。

## ooml：Office 模板渲染（重点）

ooml 是 Walnut 的「Open Office Markup Language」处理引擎，适合解析和批量生成 Word/Excel 文档（合同、证书、成绩单）。它采用命令管道风格：`ooml [Path] [[@filter filter-args...]...]`。

常用过滤器：

```bash
# 解析 xlsx，查看包内实体列表
ooml ~/tmpl/report.xlsx @xlsx @entry

# 查看模板条目内容（理解占位符结构）
ooml ~/tmpl/report.xlsx @view

# 设置映射表后导出工作表数据对象
ooml ~/tmpl/report.xlsx @xlsx @mapping ~/map.txt @rows

# 用变量集展开模板并打包成新文档
ooml ~/tmpl/cert.docx @checkout word/document.xml @explain -vars ~/data/cert.json @pack ~/out/cert.docx
```

模板占位符语法（机制层面，细节见 `man ooml explain`）：

- `${name}` — 普通字段占位，从数据上下文取值；
- `#loop-begin` / `#loop-end` — 行循环（表格一行数据重复展开）；
- 图片占位 — 在图片 alt 文本中写占位符，渲染时替换为实际图片。

子页齐全：`@beans / @checkout / @entry / @explain / @exportsheet / @mapping / @medias / @pack / @rows / @sheet / @view / @workbook / @xlsx`（`man ooml explain` 等），覆盖「查看模板结构 → 映射数据 → 打包输出」全流程。

## dom：HTML DOM 操作

基于 jsoup 的 HTML DOM 过滤型命令，同样采用 `@过滤器` 管道风格，上下文维护一棵 DOM 树（document/current 节点）：

```bash
# 加载 HTML 并查找符合选择器的元素
cat ~/page.html | dom @find 'a.menu'

# 格式化输出 / 导出 docx / 转 JSON
cat ~/page.html | dom @format
cat ~/page.html | dom @docx ~/out.docx
cat ~/page.html | dom @json
```

过滤器：`@as / @docx / @find / @format / @html / @json / @mutate / @refimg / @reset / @transattr`（`man dom find` 等）。

## edi：EDI 报文

`edi` 处理电子数据交换报文（加载 `load`、解析 `parse`、渲染 `render`、查看 `view`、打包 `pack`、消息 `msg`），常用于对接物流/海关/企业 ERP 的固定格式报文。

## 典型场景

- **批量出合同**：docx 模板 + `ooml` + 查询出来的客户数据；
- **用户上传头像后裁正方形**：`.regapi` 上传钩子 + `imagic -scale`；
- **报表导出**：查询结果 → `sheet` 写成 xlsx → API 下载；
- **二维码门票**：`qrcode` 生成 + `ooml` 排版打印。

## 注意事项

- `videoc` 依赖服务器安装 ffmpeg，容器部署时要确认；
- 大图片/大视频处理耗内存，建议在异步任务（cron/task）里跑；
- `ooml` 模板制作建议先 `ooml view` 看结构、`ooml explain` 理解占位符，再灌数据。

## 延伸阅读

- [命令模型](command-model.md) — 命令执行机制
- [数据命令](data-cmd.md) — jsonx、fake 等数据处理
- [集成：支付/微信/阿里云](../70.integrations/payment.md)
