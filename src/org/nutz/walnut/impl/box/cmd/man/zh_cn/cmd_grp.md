# 命令简介 

    `grp` 管理用户组

# 用法

    grp [grp] [-a usr] [-role 1] [-d usr] [-get usr] [-json] [-quiet]
    
    [grp]  表示组名，如果为空则表示当前组。
    
    -a     添加用户到一个组, 执行者必须是组管理员
    -role  如果指定了一个组，那么默认的角色是什么，可以是 1|10|100|-1,
            role:1 管理员
            role:10 组员
            role:100 预备组员，等待管理员审批，等待期间，和非组员权限一样
            role:-1 黑名单，这个权限的人对这个组里所有的数据都是不可见的
           默认是 10
    
    -d     从一个组删除用户, 执行者必须是组管理员
    
    -get   指明要查询某个用户的所在组，如果没有值，则表示当前用户
    -json  表示输出的是一个 JSON 数组
    
    -quiet 尽量不输出错误
    
    
# 示例

    # 显示当前用户所在的组
    demo@~$ grp
    root xiaobai
    
    # 显示某特殊用户所在的组。如果不是自己，则需要 root 权限
    demo@~$ grp -get xiaobai -json
    ["root", "xiaobai"]
    
    # 将自己添加到某组的组员（必须需要对应组的管理员权限）
    demo@~$ grp nutz -a
    
    # 将自己添加到某组的管理员（必须需要对应组的管理员权限）
    demo@~$ grp nutz -a -role 1
    
    # 将指定用户添加到某组黑名单（必须需要对应组的管理员权限）
    demo@~$ grp nutz -a xiaobai -role -1
    
    # 将自己从某组删除（必须需要对应组的管理员权限）
    demo@~$ grp nutz -d
    
    # 将指定用户从某组删除（必须需要对应组的管理员权限）
    demo@~$ grp nutz -d xiaobai
    
    # 显示某用户在某组内的权限
    demo@~$ grp nutz -get xiaobai
    1 ADMIN
    
    # 显示某用户在某组内的权限 (JSON)
    demo@~$ grp nutz -get xiaobai -json
    {"grp":"nutz","usr":"xiaobai","role":10,"roleName":"MEMBER"}
    

    