# 命令简介 

    `me` 将显示当前账号信息，以及设置当前账号的环境变量

# 用法

    me [-set "PS1=\u@\h \W"] [-json] ..
    
# 示例

    显示自己所有的变量
    me
    
    为自己设置变量，之后每次登录都会有这个环境变量
    me -set x=100
    
    显示自己某几个变量
    me home pwd
    
    使用json格式显示，需要将-json放在最后
    me id nm -json
    
    
