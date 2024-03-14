# 命令简介 

    `trim` 将管道上一级输出的文本截取空白

# 用法

    [some command] | trim [-lr]
    
# 示例

    # 截取左边的空白
    demo@~$ echo "  hello" | trim -l
    hello
    
    # 截取右边的空白
    demo@~$ echo "  hello   " | trim -l
       hello 
        
    # 截取两边的空白
    demo@~$ echo "  hello    " | trim
    hello
    
    
