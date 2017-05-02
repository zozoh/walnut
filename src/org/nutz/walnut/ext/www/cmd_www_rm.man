# 命令简介 

    `www rm` 删除一个新的域名映射

# 用法

    www rm www.mysite.com [site2.com]  # 要删除的域名，可以是多个
        [-u userName]       # 指定的用户，如果不是自己，必须是 root 组成员或者 op 组管理员才有权限 

# 示例

    # 删除一个的域名映射，并输出添加后的记录信息
    demo@~$ www rm www.mysite.com -o
    
    # 为某指定用户删除多个域名
    op@~$ www rm aa.xiaobai.com bb.xiaobai.com -u xiaobai 
    
# 错误

    - e.cmd.www.rm.noexists : site2.com  # 域名并不存在
    - e.cmd.www.rm.nopvg : xiaobai       # 没有权限删除该用户的域名
    
    
