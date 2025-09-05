`archive unzip` 解压归档文件

# 用法

```bash
archive unzip \
  [目标目录]       # 可选的解压目标目录
  [-type zip|tar|tar.gz|tgz] # 指定归档类型
  [-charset UTF-8] # 指定字符集
  [-quiet]        # 静默模式
```

# 示例

```bash
# 解压归档文件到当前目录
archive ~/downloads/sample.zip @unzip

# 解压归档文件到指定目录
archive ~/downloads/sample.zip @unzip ./output/

# 明确指定归档类型并解压
archive ~/downloads/samplefile @unzip -type zip

# 指定字符集解压归档文件
archive ~/downloads/sample.zip @unzip -charset GBK

# 静默模式解压
archive ~/downloads/sample.zip @unzip -quiet
```