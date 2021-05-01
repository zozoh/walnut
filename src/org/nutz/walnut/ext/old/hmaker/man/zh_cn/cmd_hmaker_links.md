命令简介
======= 

`hmaker links` 列出站内所有可用的页面链接，包括编辑页面和 html
    
用法
=======

```    
hmaker /path/to/page links
        [-site]      # 强制输出相对于站点的相对链接，默认是相对于指定页面的
        [-ignore]    # 搜索时忽略的顶级目录，不声明这个选项，一定是忽略 css,js,lib,image 这几个目录的
        [-key id,nm..]    # 指定输出的文件元数据字段，这里 `rph` 是特殊元数据，表示资源相对于给定参考页面的相对路径
        [-obj]            # 如果只有一个字段，强制为对象输出，否则会输出字符串
        [-cqn]            # c,q,n 为 JSON 的格式化信息
```

示例
=======

```
# 显示全部可用链接，除了指定的目录
demo@~$ hmaker xxx links -ignore "css,js,lib,image,tmp" -key "rph"

```