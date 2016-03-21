# 命令简介 

    `appclist` 命令用来得到某个文件或文件类型，可以创建哪些子对象 
    
    命令会参考 `~/.ui/ftypes` 里面的声明，最终会输出一个详细的 JSON 
    
    [{
        tp   : 'folder',
        race : 'DIR',
        icon : '<i class="fa fa-folder"></i>',
        text : 'i18n:xxxx',
        tip  : 'i18n:xxx'
    }, {
        // .. 下一个文件类型
    }]
  
# 使用方法

    appclist [-cqn] [id:xxxx | /path/to/file | type:xxx]
    
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹 

# 实例
    
    # 读取某个对象应该可以建立什么子对象
    appclist id:xxxxx  
    
    # 读取某个文件类型应该可以建立什么子对象
    appclist type:folder