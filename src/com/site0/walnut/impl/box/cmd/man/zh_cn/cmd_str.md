# 命令简介

`str` 对管道输入的字符串做转换

支持 URI 解码、去空白、替换三种模式。

# 用法

```bash
[上游命令] | str [-decodeURI [编码]] [-trim] [-replace 模式 替换]
  -decodeURI    # URI 解码（默认 UTF-8）
  -trim         # 去掉左右空白
  -replace      # 替换：第一个参数是模式，第二个是替换串（缺省为空串）
```

# 示例

```bash
# 去掉空白
echo ' Hello ' | str -trim

# URI 解码
echo 'a=%2012' | str -decodeURI

# 替换字符串
echo 'a=%2012' | str -replace % '&#37;'
```
