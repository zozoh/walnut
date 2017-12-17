命令简介
======= 

`hmaker lib` 用来管理一个站点的共享库

用法
=======

```
hmaker /path/to/site lib
       [-list obj|..]    # 列出所有可用的库文件名称, 值可以是
                         #  - obj  : 对象的 JSON 表示，格式参考 c,q,n
                         #  - name : 仅仅是对象名，一行一个
                         #  默认是一个 JSON 数据组，元素是库文件名 
       [-read libName]   # 读取某个库文件内容
       [-write libName]  # 写入某个库文件，其内容根据 -content 或者 -file 参数决定，
                         # 都没有声明的话则从标准输出读取
       [-content xxx]    # 写入时，库文件的内容。
       [-file /path/to]  # 写入时，库文件的内容拷贝自哪个文件
       [-del libName]    # 删除某个库文件，如果 -o 表示还要输出其元数据
       [-get libName]    # 输出某个库文件的元数据
       [-rename libName newName]  # 将某个库改名，同时涉及到的页面也会被修改
       [-pages libName]  # 列出所有引用某个库的页面列表
       [-ocqn]           # 输出库文件元数据据 c,q,n 为 JSON 的格式化信息 
```
    
