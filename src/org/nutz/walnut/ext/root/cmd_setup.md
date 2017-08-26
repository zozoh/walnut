# 命令简介 

 `setup` 根据给定到路径和模式，执行对指定用户执行一系列脚本
     
 - 只有 root 组成员才能指定用户
 - 普通用户只能 setup 自己

# 用法

    setup [-u USR] [-m|-om MODE] path
    
     path  如果为相对路径，则用 /sys/setup 补全
     -u    指定用户
     -m    先执行父目录中初始化，再执行特殊初始化模式
     -om   跳过父目录脚本，只执行特殊初始化模式

# 脚本目录的存放规则

    ########################################### 
    # 用户创建的初始化脚本
    ###########################################
    /sys/setup/usr/create     # 当用户创建成功后，可以指定其初始化脚本
        01_xxx                # 任何用户都要执行的脚本    
        02_xxx                # 按照名称排序
        abc                   # 初始化类型为 "abc" @see user 的 "init" 字段
            01_init_folders      # 具体的一个脚本内容
            02_setup_weixin.js   # 如果是 js 文件，则交给 jsc 来执行
    
    ########################################### 
    # 用户登录的初始化脚本
    ###########################################
    /sys/setup/usr/login      # 当用户登录成功后，可以指定其初始化脚本
        01_xxx                # 任何用户都要执行的脚本    
        02_xxx                # 按照名称排序
        abc                   # 初始化类型为 "abc" @see user 的 "init" 字段
            01_store_log         # 具体的一个脚本内容
            02_check_finger.js   # 如果是 js 文件，则交给 jsc 来执行
            03_call_local        # 当然，你可以在脚本里再调用脚本
                                 # 比如 cat ~/.profile | run
   
# 示例

    // 当用户创建时，调用初始化设置
    setup -u xiaobai －m abc usr/create
    
    // 当用户登录时，调用初始化设置
    setup -u xiaobai －m abc usr/login
    
    // 指定的初始化目录
    setup -u xiaobai /home/xiaobai/setup
    