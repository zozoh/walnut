# 命令简介 

    `adduser` 添加一个用户，并做好对应的初始化工作。然后输出用户的 JSON 数据
    
     - 通常来说这个命令需要 root 组管理员的权限。因为它需要读写 /sys/usr 下面的内容

# 用法

    adduser [-p PASSWD] [-setup PATH] [-m MODE] [-exists] [-json {..}] [-out ] [-cqnN] USR
    
    USR   指明创建的用户，可以是 email,手机,用户名
          也可以是 'oauth:github:profileId' 格式的 oauth 用户
          还可以是 'wxgh:weixinPnb:openid' 格式的微信公众号用户
          在不指定用户名的情况下，默认的用户名就是用户的 id
          
    -p       指明登录密码，如果不指明，直接 -p，则会为 123456，如果没有声明这个参数，则无密码
    -setup   指明用户创建后，需要执行的设置脚本目录
    -m       指明用户的初始化类型，这个参数也会影响 setup 的行为
             (参见 setup 命令的手册)
             同时，会为用户增加一个字段 "init"
    -exists  表示如果用户存在，就输出。没有这个参数，用户存在会抛错
       
    -json    表示需要输出用户的完整 JSON 信息，内容是 JsonFormat
    -cqn     快捷的表示 JSON 格式
    
    -out     用模版方式输出，占位符形式为 `@{xxx}`
    
    -N       打印输出的时候不输出结尾空行         
    
# 示例

    # 添加一个用户
    demo@~$ adduser xiaobai
    
    # 添加一个微信公号用户并初始化
    demo@~$ adduser wxgh:gh_7a167f4843b6:okyrt3iQ875fTF96BEtqLrZc345V -setup usr/create 
    
    # 添加一个用户，如果存在，就不创建，仅仅查询
    demo@~$ adduser -exists xiaobai