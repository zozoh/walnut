# 命令简介 

    `httpapi list` 将会列出当前域所有的 API

# 用法

    httpapi list 
        [-u xxxx]   # 指定一个用户。只有 root 和 op 组成员才能执行这个操作
    
# 示例
    
    # 列出本域所有的 httpapi
    demo@~$ httpapi list
    
    # 列出 xiaobai 域所有的 httpapi（需要 root 或者 op 组成员权限)
    demo@~$ httpapi list -u xiaobai
