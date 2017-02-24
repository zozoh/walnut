# 命令简介 

    `thing create` 创建记录

# 用法

    thing [TsID] create [th_nm] [th_brief] [-cate th_cate] [-fields "{..}"]
    #----------------------------------------------------
    - 当前对象可以是一个 thing 或者 ThingSet
    - 如果是一个 thing，相当于是它的 ThingSet

# 示例 

    # 创建一个名为 ABC 的 thing
    thing xxx create ABC
        
    # 创建一个名为 ABC 且有一个简要说明的 thing
    thing xxx create 'ABC' 'This is abc'
    # or
    thing xxx create ABC -fields "{th_brief:'This is abc'}"
    # or
    thing xxx create -fields "{th_nm:'ABC', th_brief:'This is abc'}"
        
    # 创建一个匿名的 Thing 并指明分类
    thing xxx create -cate xxx
