# 命令简介 

`json` 将输入的 JSON 文本进行格式后再输出

# 用法

    cat xxx | json [-cqnr] [-e $REGEX+] [-d yyyyMMdd] [-u '{xx:xxx}'] [-out Tmpl]
    
    -c            # 紧凑格式输出
    -q            # 不用双引号包裹键值
    -n            # 忽略 null 值
    -e            # 正则表达式表示指定输出的字段，! 开始表示取反
    -d            # 对日期时间格式的字段格式化
    -prefix       # 对 JSON 字段增加前缀
    -prefix_key   # 一个正则表达式说明特殊的字段才增加前缀
    -r            # 遇到对象，的值也递归修改前缀
    
    -type         # 指名输入的类型，默认会调用 JSON 解析，如果输入的是数字或者字符串
                  # 可以用这个来指明，支持的值为：
                  # String|Integer|Floot|Long|Double|Boolean|Object
    
    -err          # 如果遇到输入是 "e.xxx" 这样的错误信息，直接从标准输出打印出来
                  # 不声明这个选项，则会抛 Json 解析失败的错误
    
    -key          # 如果输入是一个 Map，那么被这个值描述到键会被保留，其他到会被删掉
                  # 指明保留到 :  ^(a|b|c)$
                  # 指明不保留 :  !^(a|b|c)$
    
    -mapping      # 映射字段。即改变字段的键值
                  # 譬如 -mapping '{a:"AA", b:"BB"}'
                  # 那么会把输入的键 a 变成 AA, 键b变成BB
                  # 在映射中可以合并值，如果 a:"AA:@{a}-@{b}" 实际上表示一个模板替换
                  # 将对象 {a:"pet",b:"good"} 变成 {AA:"pet-good"}
                  # 不会递归的映射，第一层对象，或者第一层容器里面所有的对象才会被其影响
                  # 它会作用在 -prefix,-get,-put,-u 这些处理操作之前
                  # 在 -key 操作之后
    -mapping_only # 指明，只有在声明了 mapping 的字段才会保留
                  # 只有声明了 mapping 选项，本选项才起作用
    
    -get          # 从一个 JSON 文本中取值，键值支持 . 的路径操作
                  # 如果是列表，则参数是下标

    -keys         # 从当前对象（可以是 -get 处理之后）获取对象的键数组
                  # 当然，如果对象不是 Map，则返回空数组
    
    -val          # 从JSON对象里取值，如果对象是个数组，则生成一个值的数组
                  
    -put          # 将这段 JSON 加入到一个新到 map，并指定 key
                  
    -u            # 更新 map(深层递归)，
    -a            # 更新 map(深层递归)，没有的键才加上
    
    -out          # 采用模板方式输出，模板占位符格式化为 "@{xxx}"
    -str          # 直接作为字符串方式输出，否则会转 JSON 输出 
    
    
# 示例

    # 格式化输出
    $:> echo '{x:100,y:80}' | json
    {
       "x" : 100,
       "y" : 80
    }
    
    # 增加前缀
    $:> echo '{x:100,y:80}' | json -prefix "pos_"
    {
       "pos_x" : 100,
       "pos_y" : 80
    }
    
    # 为指定字段增加前缀
    $:> echo '{x:100,y:80}' | json -prefix "pos_" -prefix_key "^x$"
    {
       "pos_x" : 100,
       "y" : 80
    }
    
    $:> echo '{x:100,y:80}' | json -prefix "pos_" -prefix_key "!^x$"
    {
       "x" : 100,
       "pos_y" : 80
    }
    
    # 形成新到 map
    $:> echo '{x:100,y:80}' | json -put "pos"
    {
        pos : {
           "x" : 100,
           "y" : 80
        }
    }
    
    # 取出某值到新 map
    $:> echo '{pos:{x:100,y:80}}' | json -get "pos" -put "position"
    {
        position : {
           "x" : 100,
           "y" : 80
        }
    }
    
    # 除了某几个 key 剩下的都输出
    $:> echo '{x:100,y:80,z:66}' | json -key "!^(x|y)$"
    {
        "z" : 66
    }
    
    # 除了某几个 key 剩下的都不输出
    $:> echo '{x:100,y:80,z:66}' | json -key "^(x|y)$"
    {
        "x" : 100
        "y" : 80
    }
    
    # 从复杂对象里取值
    $:> echo '{pos : {x:100,y:80}}' | json -get "pos.x"
    100
    
    # 从复杂列表里取值
    $:> echo '[{pos : {x:100,y:80}},{}]' | json -get "0.pos.x"
    100
    
    # 从复杂列表里取值
    $:> echo '{obj: [{pos : {x:100,y:80}},{}]}' | json -get "obj[0].pos.x"
    100
    
    # 将对象某个字段值变成一个数组
    $:> echo '[{x:1,y:2},{x:9,y:8}]' | json -val x
    [1,9]
    