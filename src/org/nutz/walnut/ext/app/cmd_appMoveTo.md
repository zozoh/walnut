# 命令简介 

    `app-move-to` 命令用来得到某个文件或文件类型，可以被移动到哪些类型的目录中 
    
    命令会参考 `~/.ui/ftypes/xxxx` 里面的声明，最终会输出一个详细的 JSON 
    
    {
        filter : ["^(folder)$",".."],  // 正则表达式列表过滤文件类型
        base   : "~/mybase",           // 起始的位置
    }
    
    因为可能输入的是多个文件对象，命令会做一个合并。filter 会是所有文件对象的集合
    base 会去所有文件对象推荐 base 共同的交集。 如果没有交集，则采用 "~"
  
# 使用方法

    app-move-to [-cqn] [id:xxxx | /path/to/file | type:xxx] ...
    
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹 

# 实例
    
    # 读取某个对象应该可以移动到什么目录内
    app-move-to id:xxxxx  
    
    # 读取某个文件类型应该可以移动到什么目录内
    app-move-to type:folder