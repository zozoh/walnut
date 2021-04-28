命令简介
======= 

`hmaker css` 给出指定的 CSS 文件界面上可用的类选择器。本命令会只挑选那些非复合的类选择器。
即，`.abc` 会被本命令列出，而 `.abc .xyz b` 这样的会被忽略。

    
用法
=======

```    
hmaker /path/to/site css 
    [CSSA CSSB ..]          # 要分析的 CSS 文件相对站点主目录的路径
                            # 如果你给的是一个绝对路径，那么可以不用给出站点路径
    [-sort selector:1]      # 是否要排序，1 表示 asc, -1 表示 desc
                            # 不给定 key，则默认认为是按照 selector 排序

命令输出的是一个 JSON 数组，如果没有可输出的，则输出的是空数组:

[{
    selector : "xyz",
    text     : "哈哈",  // 选择器的说明会看CSS文件前一行的注释，没有，则为 null
},{
    // 第二个选择器
}]
```

示例
=======

```
# 显示两个 css 文件全部的可以用类选择器
demo@~$ hmaker xxx css css/a.css css/b.css

# 显示两个 css 文件全部的可以用类选择器并倒序排列
demo@~$ hmaker xxx css css/a.css css/b.css -sort -1

# 显示两个 css 文件全部的可以用类选择器并按照文本正序排列
demo@~$ hmaker xxx css css/a.css css/b.css -sort text:1
```