# 命令简介 

    `echo` 输出一段文本

# 用法

    echo [-en] 文本1 文本2 ...
    
# 示例

    # 显示普通文本
    demo@~$ echo hello
    hello
    
    # 显示普通文本，但是不输出换行
    demo@~$ echo -n hello
    hellodemo@~$ 
        
    # 显示文本，并将转移字符转移
    demo@~$ echo -e hel\nlo
    hel
    lo
    
    
