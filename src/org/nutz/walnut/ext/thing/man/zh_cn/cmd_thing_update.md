# 命令简介 

    `thing update` 更新 Thing 的元数据

# 用法

    thing [TsID] update ID ["$th_nm"] 
                      [-brief "xxx"]
                      [-ow "xxx"]
                      [-cate CateID]
                      [-fields "{..}"]
    #----------------------------------------------------
    - 当前对象必须是一个 thing，否则不能更新
    - fields 里面的值，没有 -brief|ow|cate 优先

# 示例
    
    # 改名
    thing xxx update 45ad6823.. "原力觉醒电影票"
        
    # 修改简介
    thing xxx update 45ad6823.. -brief "会员半价"
        
    # 修改更多的信息
    thing xxx update 45ad6823.. -fields "x:100,y:99"
