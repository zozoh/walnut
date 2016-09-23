# 命令简介 

    `login` 命令将会在当前会话下创建一个任何用户的子会话。
     - root 组管理员能登录到除了 root 组管理员之外任何账户
     - 执行操作的用户必须为 root|op 组成员
     - 目标用户必须不能为 root|op 组成员
     
# 用法

    login 目标用户名 [-cnqH]
    
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹
    -H      按json输出时，也显示双下划线开头的隐藏字段
    
# 示例

    // 在当前会话下创建一个属于用户 xiaobai 的会话
    login xiaobai
     
    
