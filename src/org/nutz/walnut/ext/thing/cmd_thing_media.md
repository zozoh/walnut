# 命令简介 

    `thing media` 管理 Thing 的媒体目录

# 用法

    thing [TsID] media ID
               [-add xxx.jpg]
               [-dupp '@{major}(@{nb})@{suffix}']
               [-overwrite]
               [-read /path/to/src]    # 指定写入文件的内容，如果没给参数从标准输入读取内容
               [-del xxx.jpg]
               [-get xxx.jpg]          # 获取指定文件的元数据
               [-ufc]                  # 重新计算 Thing 的附件数量和元数据
               [-cat abc.txt]          # 输出指定文件的内容（根据文件的mime类型决定是按照文本还是二进制输出）
               [-httpresponse]         # 当 -cat 模式时，输出为标准 HTTP 响应
               [-http-etag]            # 当输出标准 HTTP 响应的时候，是否需要根据 etag 判断输出 304
               [-quiet]

# 示例

    # 列出一个 thing 所有的媒体文件，空的返回空数组 []
    thing xxx media xxx
        
    # 添加一个空图片，如果已存在则抛错
    thing xxx media xxx -add abc.jpg
        
    # 添加一个空图片，如果已存在则返回
    thing xxx media xxx -add abc.jpg -overwrite
        
    # 添加一个空图片，如果已存在则根据模板创建新的
    thing xxx media xxx -add abc.jpg -dupp
        
    # 添加一个图片，内容来自另外一个文件，如果已存在则抛错
    # 如果想不抛错，参见上面创建空文件的例子，根据需要添加
    # -overwrite 或者 -dupp 参数
    thing xxx media xxx -add abc.jpg -read id:45vff..
    
    # 输出图片内容，将直接输出图片的二进制内容
    thing xxx media xxx -cat abc.jpg
    
    # 同理，如果想输出一个附件内容，可以是
    # 如果附件是文件会按照文本输出，否则输出二进制 
    thing xxx attachment xxx -cat xyz.txt
        
    # 添加一个图片，内容来自标准输入，如果已存在则抛错
    cat abc.jpg | thing xxx media xxx -add abc.jpg -read  
    
    # 删除某个媒体，不存在抛错
    thing xxx media xxx -del abc.jpg
    
    # 删除多个媒体，不存在抛错
    thing xxx media xxx -del abc.jpg xyz.jpg ufo.png
    
    # 删除某个媒体，不存在抛错也不报错
    thing xxx media xxx -del abc.jpg -quiet
    
    # 删除多个媒体，不存在抛错也不报错
    thing xxx media xxx -del abc.jpg xyz.jpg ufo.png -quiet
