命令简介
=======

`str` 命令用来对输入的字符串参数进行转换

用法
=======

```bash
str
    [-decodeURI UTF-8]  # 【选】对字符串解码，默认 UTF-8
    [-trim]             # 【选】去掉字符串左右空白
    [-replace]          # 【选】替换模式，读取两个路径参数
                        #  1. -模式-
                        #  2. -替换- 如果没有就是空字符串
```
  
示例
=======

```bash
# 去掉空白
demo:$ echo ' Hello ' | str -trim
Hello

# URI解码(UTF-8)
demo:$ echo '"a=%2012"' | str -decodeURI
a= 12

# 替换字符串
demo:$ echo '"a=%2012"' | str -replace % '&#37;'
a=&#37;2012
```
    
    
