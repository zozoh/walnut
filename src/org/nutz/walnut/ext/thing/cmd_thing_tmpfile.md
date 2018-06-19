# 命令简介 

    `thing tmpfile` 为数据集创建

# 用法

    thing [TsID] tmpfile [-create FNM] [-expi du] [-clean du]
    
    [-create FNM]   # 创建一个临时文件，如果不指定 FNM， 则会采用 `tmp_${id}` 作为文件名模板
                    # 如果想指定一个特别的文件类型，可以输入例如 `upload_${id}.xls`
    [-expi du]      # 表示一个时间，譬如 20m, 1h, 3d, 99s 等
                    # 配合上 Action，如果 create 表示临时文件的有效时间
    [-clean du]     # 表示清理所有的过期时间为未来一段时期的临时文件
                    #  du 与 -expi 的格式相同，默认为 0
                    # 如果是 0 则表示清除当前时间过期的临时文件
                    # 如果全清除，估计设置个 1000d 就能全清除了吧 ^_^!  

# 示例

    # 创建一个30分钟有效的临时文件
    thing xxx tmpfile -create -expi 30m
        
    # 创建一个指定文件名的临时文件，如果存在，则复用
    thing xxx tmpfile -create tmp_${id}.xls -expi 30m 
        
    # 清理所有过期文件
    thing xxx tmpfile -clean
    
    # 清理所有未来10分钟将过期的文件
    thing xxx tmpfile -clean 10m
    
    # 清理所有1天前过期的文件
    thing xxx tmpfile -clean -1d
