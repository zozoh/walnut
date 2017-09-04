# 命令简介 

    `thing detail` 管理数据详情

# 用法

    thing [TsID] detail ID
               [-drop]
               [-content "xxxxx"]
               [-brief "xxx"]
               [-tp "md|txt|html"]
               
    #----------------------------------------------------
    - 内容，支持从管道读取
    - 默认 tp 为 txt

# 示例
    
    # 显示 thing 的 detail
    thing xxx detail 45ad6823..
       
    # 为 thing 修改详细内容，并自动生成摘要
    thing xxx detail 45ad6823.. -content "哈哈哈"
    
    # 为 thing 修改详细内容，并手动设置摘要
    thing xxx detail 45ad6823.. -content "哈哈哈" -brief "haha"
    
    # 从文件里读取一段文字，作为 thing 的详细内容
    cat ~/abc.txt | thing xxx detail 45ad6823.. -content  
        
    # 为 thing 修改详细内容 HTML
    thing xxx detail 45ad6823.. -content "<b>哈哈哈</b>" -tp "html"
        
    # 为 thing 删除详细内容
    thing xxx detail 45ad6823.. -drop
