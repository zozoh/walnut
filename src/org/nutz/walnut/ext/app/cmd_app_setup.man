# 命令简介 

    `app setup` 命令用来得到某对象的编辑器和菜单配置 
    
    最终会输出一个详细的 JSON 
    
    {
        actions : ["@::save_text"],   // 当前文件对象支持的菜单项
        editors : [ "edit_json" ]     // 当前文件对象支持的编辑器
    }
    
    命令会对 Action 字符串进行过滤，一个 Action 字符串的格式为
    
     模式     权限                                动作 
    "[@Ee] : [rwx]|[ADMIN(root);MEMBER(root)] : new"
    
    
  
# 使用方法

    app setup [/path/to/file]   # 可选路径，默认为当前路径
    -------------------------------------------
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹 

# 实例
    
    # 读取当前路径的应用设置
    app setup  
    
    # 读取某个指定路径的应用设置
    app setup ~/mydir/myfile