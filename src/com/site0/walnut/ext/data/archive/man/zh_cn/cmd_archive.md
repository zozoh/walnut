# 命令简介

`archive` 用来处理归档文件（如 zip、tar、tgz 等）

# 用法

```bash
archive <归档文件路径> \
  [@unzip]        # 解压归档文件
  [-charset UTF-8] # 指定字符集（默认 UTF-8）
  [-hidden]       # 包含隐藏文件
  [-macosx]       # 处理 macOS 特殊文件
  [-quiet]        # 静默模式，输出 JSON 格式结果
```

# 示例

```bash
# 查看归档文件内容
archive ~/downloads/sample.zip

# 使用静默模式查看归档文件内容
archive ~/downloads/sample.zip -quiet

# 解压归档文件到当前目录
archive ~/downloads/sample.zip @unzip

# 解压归档文件到指定目录
archive ~/downloads/sample.zip @unzip ./output/

# 指定字符集解压归档文件
archive ~/downloads/sample.zip @unzip -charset GBK
```