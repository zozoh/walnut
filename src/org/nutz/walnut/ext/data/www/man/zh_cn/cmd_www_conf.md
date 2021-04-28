# 命令简介 

    `www conf` 得到域名映射方面的配置文件信息(JSON)。
    这个配置文件路径为 "~/.www/www.conf"，内容格式为:
    
    {
        // 声明域名 DNS 的 A 记录，应该指向的地址
        // 通常这个地址是系统所在的服务器地址
        "dns_r_A" : "127.0.0.1",
    
        // WWWModule 下几个入口函数需要的配置信息
        "login_ok"   : "/",                // dusr 登录成功后调整的 URL
        "login_fail" : "/login_fail.wnml", // dusr 登录失败后跳转的 URL
        "logout"     : "/",                // dusr 登出后跳转的 URL
    }

# 用法

    www conf 
        [-cqn]    # 通用的 JSON 格式化命令 @see cmd_obj

# 示例

    # 输出配置文件内容
    demo@~$ www conf
    {
        "login_ok" : "/",
        "login_fail" : "/login_fail.wnml",
        "logout" : "/",
    }
    