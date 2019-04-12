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
        [-dis xxx]      # 指定特殊显示模式
                        #  param  : Map 形式的参数表，值都是空字符串
                        #  palist : Array 形式的参数表
                        #  anchor : 可被界面直接使用的数组
                        #  anlist : Array 形式的锚点表
        [-obj]          # 如果只有一个字段，强制为对象输出，否则会输出字符串
        [-cqn]          # c,q,n 为 JSON 的格式化信息
```

示例
=======

```
# 显示某个页面全部的锚点和动态参数
demo@~$ hmaker xxx com
[{
    id    : "rows_1",
    ctype : "rows",
    skin  : "skin-rows-normal",
    anchors : ["A0", "A1", "A2"],
    params : ["p1", "p2", "p3"]
}]

# 显示某个页面全部控件可以接受的动态参数(Map形态)
demo@~$ hmaker xxx com -dis param
{
    "p1" : "",
    "p2" : "",
    "p3" : ""
}

# 显示某个页面全部控件可以接受的动态参数
demo@~$ hmaker xxx com -dis palist
["p1", "p2", "p3"]

# 显示某个页面全部的锚点
demo@~$ hmaker xxx com -dis anlist
["A0", "A1", "A2"]

# 显示某个页面全部的锚点(可被界面直接使用的形式)
demo@~$ hmaker xxx com -dis anchor
[{
    id    : "text_1",
    ctype : "text",
    skin  : "skin-text-normal",
}, {
    id     : "text_1",
    ctype  : "text",
    anchor : "A0",
}, {
    id     : "text_1",
    ctype  : "text",
    anchor : "A1",
}]
```