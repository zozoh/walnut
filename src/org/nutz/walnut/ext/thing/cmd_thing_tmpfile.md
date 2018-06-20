# 命令简介 

    `thing tmpfile` 为数据集创建

# 用法

    thing [TsID] tmpfile [FNM] [-expi du] [-clean n]
    
    [FNM]           # 创建一个临时文件，如果不指定 FNM， 则会采用 `tmp_${id}` 作为文件名模板
                    # 如果想指定一个特别的文件类型，可以输入例如 `upload_${id}.xls`
    [-expi du]      # 表示一个时间，譬如 20m, 1h, 3d, 99s 等
                    # 配合上 Action，如果 create 表示临时文件的有效时间
    [-clean n]      # 表示清理所有的过期的文件，默认 n 为 1000 条，表示最大限制
                    # 0 或者负数表示全部清理  

# 示例

    # 创建一个30分钟有效的临时文件
    thing xxx tmpfile -expi 30m
        
    # 创建一个指定文件名的临时文件，如果存在，则复用
    thing xxx tmpfile tmp_${id}.xls -expi 30m 
        
    # 清理所有过期文件
    thing xxx tmpfile -clean
    
    # 最多清理 100 个过期的文件
    thing xxx tmpfile -clean 100
    
