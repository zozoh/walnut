# 命令简介 

    `wiki tree` 命令用来解析wiki文档结构

# 用法

    wiki tree 
            [/path/to]              # 指定一个源目录或者一个tree文件
            [-json]                 # 输出json格式
            [-xml]                  # 输出xml格式

# 示例 

    # 解析一个目录,先查找tree.xml,然后找tree.md,都没有,就遍历目录树作为tree
    wiki tree /path/to/dir/
    
    # 直接指定一个tree定义文件
    wiki tree /path/to/wiki/index.xml
