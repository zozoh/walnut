# 命令简介 

    `www query` 查询域名映射的信息列表

# 用法

    www query 
        [-ava]              # 表示返回所有可用的域名
        [-u userName]       # 指定的用户，如果不是自己，必须是 op/root 组成员才有权限 
        [-tmpl|-t|-json]    # 支持通用的 obj 格式化输出函数         

# 示例

    # 输出自己的域名映射信息
    demo@~$ www query
    
    # 输出某指定用户的域名映射信息
    op@~$ www query -u xiaobai
    
    # 按表格输出
    demo@~$ www query -t 'id,nm,www' -bish
    
# 错误

    - e.cmd.www.query.nopvg : xiaobai           # 没有权限查询该用户域名信息