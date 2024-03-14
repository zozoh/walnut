# 命令简介 

    `thing get` 获取某数据索引记录

# 用法

    thing [TsID] get ID 
       [-full]   # 表示获取 thing 的全部数据，包括 detail
       [-sort]   # 表示指定一个排序字段，如果有这个字段
                 #  - lm:asc  表示按照  lm 正序
                 #  - lm:desc 表示按照  lm 倒序
                 #  - lm 相当于是  lm:asc
                 # 那么输出的元数据包括 `th_next` 和  `th_prev` 两字段
                 # 当然如果查不到数据，这两个字段的值是 null
       [-check]  # 严格模式，如果不存在要输出错误  e.thing.noexists
       [-cqn]    # JSON 格式化输出

# 示例

    # 得到一个 thing 的全部元数据
    thing xxx get 45ad6823..
        
    # 打印某个 thing 的全部 JSON 信息
    thing xxx get 45ad6823.. -json 
    
    # 打印某个 thing 的全部元数据，如果不存在，抛错
    thing xxx get 45ad6823.. -check
        
    # 打印某个 thing 的名称和所属者，且不输出换行符
    thing xxx get 45ad6823.. -out '@{th_ow} belong to @{th_name}' -N
