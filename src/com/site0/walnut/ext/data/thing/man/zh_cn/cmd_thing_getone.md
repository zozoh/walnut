# 命令简介 

    `thing getone` 查询一条数据

# 用法

    thing [TsID] getone 
        [Condition Map]  # 查询条件
        [-content]       # 输出内容
        [-obj]           # 如果查询不到对象，也要输出个 null
        [-sort "nm:1"]   # 排序
        [-cqn]           # JSON 格式化输出
    ///////////////////////////////////
    // 参数说明:
    ///////////////////////////////////
    - [TsId] 当前对象可以是一个 `thing` 或者 `ThingSet`
             如果是一个 `thing`，相当于是它的 `ThingSet`
    - [Consition Map] 查询条件如果不包括 `th_live`，
                      那么默认将设置为 `th_live=1` 表示所有可用的 `thing`
    - content 表示同时同时输出数据的内容，记做 `content` 字段
    - sort 排序字段
    


