# 命令简介 

    `thing get` 获取某数据索引记录

# 用法

    thing [TsID] get ID 
       [-full]  表示获取 thing 的全部数据，包括 detail

# 示例

    # 得到一个 thing 的全部元数据
    thing xxx get 45ad6823..
        
    # 打印某个 thing 的全部 JSON 信息
    thing xxx get 45ad6823.. -json 
        
    # 打印某个 thing 的名称和所属者，且不输出换行符
    thing xxx get 45ad6823.. -out '@{th_ow} belong to @{th_name}' -N
