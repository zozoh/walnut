命令简介
======= 

`hmaker rs` 列出某站点可用的 css/js 资源
    
用法
=======

```    
hmaker /path/to/site rs
        [-path css,js]    # 指定搜索路径，如果没有，则整站查找 
        [-match ^css|js]  # 正则表达式匹配资源相对路径，如果没有，则会列出全部文件        
        [-key id,nm..]    # 指定输出的文件元数据字段，这里 `rph` 是特殊元数据，表示对应站点的相对路径
        [-obj]            # 如果只有一个字段，强制为对象输出，否则会输出字符串
        [-cqn]            # 输出库文件元数据据 c,q,n 为 JSON 的格式化信息
```

