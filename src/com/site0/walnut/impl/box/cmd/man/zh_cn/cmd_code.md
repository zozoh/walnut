# 命令简介

`code` 将管道输入的内容按指定字符集编码后输出

常用于把内容转成指定编码再交给下游命令，例如导出 GBK 编码的 CSV 文件。

# 用法

```bash
[上游命令] | code 目标编码
```

# 示例

```bash
# 内容转为 GBK 编码后写入文件
cat ~/data.txt | code GBK > ~/data-gbk.txt

# 查询结果以 UTF-8 写出（上游默认编码不是 UTF-8 时）
sqlx @query mydao 'select * from t_pet' | code UTF-8
```
