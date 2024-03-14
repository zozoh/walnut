# 命令简介 

    `www add` 添加一个或多个新的域名映射

# 用法

    www add www.mysite.com [site2.com]  # 指定要添加的域名，可以是多个
        [-u userName]       # 指定的用户，如果不是自己，必须是 op/root 组成员才有权限 
        [-path ~/www]       # 域名所在的父目录，默认为 /home/$usrName/www
        [-o]                # 是否输出新添加的域名映射
        [-l]                # 如果仅仅添加一个域名，是否强制按照数组输出
                            # 仅在 -o 选项下有效      

# 示例

    # 添加一个新的域名映射，并输出添加后的记录信息
    demo@~$ www add www.mysite.com -o
    
    # 为某指定用户添加多个域名
    op@~$ www add aa.xiaobai.com bb.xiaobai.com -u xiaobai 
    
# 错误

    - e.cmd.www.add.exists : site2.com  # 要添加的域名已经存在
    - e.cmd.www.add.nopvg : xiaobai     # 没有权限为该用户添加域名
    
    
