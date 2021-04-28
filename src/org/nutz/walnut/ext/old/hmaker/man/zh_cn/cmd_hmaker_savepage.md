命令简介
======= 

`hmaker savepage` 保存页面并更新元数据
    

用法
=======

```    
hmaker savepage /path/to/page
        [-content xxx]    # 写入时，库文件的内容。
        [-file /path/to]  # 写入时，库文件的内容拷贝自哪个文件
```

 * 如果不给定 -content 或者 -file，则从标准输入读取内容

