# 命令简介 

    `httpparam` 命令一次性将某 HTTP 请求文件的参数解析，并输出成 JSON

# 用法

    httpparam -in id:xxx 
        [-cqn]            # JSON 格式化器
        [-out xxx]        # 输出模板
        [-map [key]]      # 是否将参数收缩到一个 Map 的 key 里，默认key为 params
        [-varray nm1:'sep',nm2:'sep']   # 将指定的参数根据分隔符拆分成数组
        [-vint   nm1,nm2,nm3..]         # 将指定的参数变成整数
        [-vbool  nm1,nm2,nm3..]         # 将指定的参数变成布尔
        [-vfloat nm1,nm2,nm3..]         # 将指定的参数变成浮点数
    
# 示例

    // 输出请求中的 id 字段
    httpparam -in id:xxx -out 'ID is @{id}' 
    
    // 将请求对象变成 JSON，紧凑格式输入
    httpparam -in id:xxx -c
    
    // 将请求对象变成 JSON 并将 price 字段变成浮点，d 和 a 字段变成布尔
    httpparam -in id:xxx -vfloot price -vbool 'd,a'
    
    // 将请求对象变成一个 {params:{请求参数集合}} 格式的 JSON
    httpparam -in id:xxx -map
    
    // 将请求对象变成一个 {REQ:{请求参数集合}} 格式的 JSON
    httpparam -in id:xxx -map REQ
    
