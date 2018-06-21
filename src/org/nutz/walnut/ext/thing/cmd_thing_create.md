# 命令简介 

    `thing create` 创建记录

# 用法

    thing [TsID] create 
            [th_nm]              # 填充 th_nm 字段
            [-tp "xxx"'          # 填充 tp 字段
            [-brief "xxx"]       # 填充 brief 字段
            [-ow "xxx"]          # 填充 th_ow 字段
            [-cate "xxx"]        # 填充 th_cate 字段 
            [-fields "{..}"]     # 其他字段，上述参数如果有值，会将对应字段覆盖
                                 # 如果是数组，则创建多个对象
            [-unique "phone"]    # 指定一个唯一字段，如果设置，添加的时候如果发现已经存在
                                 # 则改成更新
    #----------------------------------------------------
    - 当前对象可以是一个 thing 或者 ThingSet
    - 如果是一个 thing，相当于是它的 ThingSet

# 示例 

    # 创建一个名为 ABC 的 thing
    thing xxx create ABC
        
    # 创建一个名为 ABC 且有一个简要说明的 thing
    thing xxx create 'ABC' -brief 'This is abc'
    # or
    thing xxx create ABC -fields "{th_brief:'This is abc'}"
    # or
    thing xxx create -fields "{th_nm:'ABC', th_brief:'This is abc'}"
        
    # 创建一个匿名的 Thing 并指明分类
    thing xxx create -cate xxx
