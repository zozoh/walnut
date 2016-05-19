# 命令简介 

    `dusr` 命令用来管理一个域的用户数据
    

# 用法

    dusr [options...] [UserID] 
    
    参数表: 
    
    -create   JSON, 创建用户
    -login    JSON, 登录用户
    -reuse    登录时，复用已有的会话
    -logout   SEID, 登出一个会话
    -session  SEID|<nil>, 得到会话详情
    -passwd   新密码
    -get      JSON, 获取用户的查询字段
    -c        按json输出时，紧凑显示
    -n        按json输出时，如果有 null 值的键也不忽略
    -q        按json输出时，键值用双引号包裹
    
    -o        当修改密码后，是否要输出用户的信息
    
# 示例

    # 创建一个用户
    $:> dusr -create '{nm:"zozoh",passwd:"123456"}'
    
    # 登录
    $:> dusr -login '{nm:"zozoh",passwd:"123456"}'
    
    # 用 openid 登录
    $:> dusr -login '{openid:"xxxxxx"}'
    
    # 登出当前会话
    $:> dusr -logout
    
    # 登出指定会话会话
    $:> dusr -logout 6jeh35l080iojpipc5f1mrorki
    
    # 得到当前的会话信息
    $:> dusr -session
    
    # 得到指定的会话信息
    $:> dusr -session 6jeh35l080iojpipc5f1mrorki
    
    # 得到当前用户信息
    $:> dusr
    
    # 得到指定用户信息
    $:> dusr zozoh
    
    # 根据 ID 得到用户的信息
    $:> dusr -id t9obk3tai2hi7rcqio07kqthra
    
    # 得到手机号为 13998874213 的用户信息
    $:> dusr -get "phone:'13998874213'"
    
    # 得到手机号为 13998874213，并已验证通过的用户信息
    $:> dusr -get "phone:'13998874213', phone_checked:true"
    
    # 修改当前用户密码
    $:> dusr -passwd 123456
    
    # 修改指定用户密码
    $:> dusr zozoh -passwd 123456
    
        

    