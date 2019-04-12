# 命令简介 

    `thing restore` 恢复被标记删除的 Thing
    
# 用法

    thing [TsID] restore ID [-quiet]
    #----------------------------------------------------
    - 当前对象必须是一个 thing，否则不能恢复
    - 已经恢复的，再次恢复会抛错，除非 -quiet
    - 所谓恢复其实就是标记 th_live = 1
