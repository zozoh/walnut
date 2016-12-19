# 命令简介 

    `app actions` 读取指定的 action 文件内容，并将内容输出成一个数组
      
# 使用方法

    app actions ACTION1 ACTION2 ...
    -------------------------------------------
    ACTION    # 动作名称
    -c        # 按json输出时，紧凑显示
    -n        # 按json输出时，如果有 null 值的键也不忽略
    -q        # 按json输出时，键值用双引号包裹
    -------------------------------------------
    输出的格式为:
    ["..ACTION文件 A 内容..","..ACTION文件 B 内容.."]
    

# 实例
    
    # 展开动作信息
    app actions new tree search properties  
    
    