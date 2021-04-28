# 命令简介 

    `app editor` 得到某个编辑器的配置信息
      
# 使用方法

    app editor NAME [-cqn]
    -------------------------------------------
    NAME    # 编辑器的名称
    -c      # 按json输出时，紧凑显示
    -n      # 按json输出时，如果有 null 值的键也不忽略
    -q      # 按json输出时，键值用双引号包裹 

# 实例
    
    # 读取某编辑器的信息
    app editor edit_text  
    
    