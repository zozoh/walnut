命令简介
======= 

`hmaker com` 列出某页面所有的组件信息
    
用法
=======

```    
hmaker /path/to/page com
        [-ctype xxx]    # 指定组件类型，^ 开头表示正则表达式
        [-id xxx]       # 指定组件ID，^ 开头表示正则表达式
        [-skin xxx]     # 指定组件皮肤，^ 开头表示正则表达式
        [-nolib]        # 不显示共享库里的组件
        [-obj]          # 如果只有一个字段，强制为对象输出，否则会输出字符串
        [-cqn]          # c,q,n 为 JSON 的格式化信息
```

示例
=======

```
# 显示全部可用链接，除了指定的目录
demo@~$ hmaker xxx com
[{
    id    : "rows_1",
    ctype : "rows",
    skin  : "skin-rows-normal",
}]
```