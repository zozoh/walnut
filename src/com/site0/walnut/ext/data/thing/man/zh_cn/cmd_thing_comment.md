# 命令简介 

    `thing comment` 管理注释

# 用法

    thing [TsID] comment ID
                      [-add xxx]
                      [-del xxx]
                      [-get xxx]
                      [-read xxx]
                      [-quick]
                      [commentID content]
                      [-tp txt|html|md]
                      [-maxsz 256]
                      [-minsz 5]
    #----------------------------------------------------
    - 注释内容，支持从管道读取
    - 支持 'sort|pager|limit|skip|json|out|t' 等参数
    - maxsz 默认 256 表示，超长的字符串，会被截取为 breif，完整内容将存在文件里
    - minsz 默认 5 表示评论最小长度

# 示例

    # 添加注释，会自动修改 task.th_c_cmt 字段 
    thing xxx comment 45ad6823.. -add "搞定了，呼"
        
    # 添加 makrkdown 注释
    thing xxx comment 45ad6823.. -add "<b>哈哈</b>" -tp html
        
    # 修改注释
    thing xxx comment 45ad6823.. 20150721132134321 "修改一下注释"
        
    # 修改注释为 markdown
    thing xxx comment 20150721132134321 "修改一下注释" -tp md
        
    # 删除注释，会自动修改 task.th_c_cmt 字段 
    thing xxx comment 45ad6823.. -del 20150721132134321
        
    # 获取全部注释, 如果注释内容过长(有 breif 字段)，主动读取 content
    thing xxx comment 45ad6823..
        
    # 获取全部注释, 如果注释内容过长也不主动读取
    thing xxx comment 45ad6823.. -quick
        
    # 获取最多 100 个注释，并显示翻页信息
    thing xxx comment 45ad6823.. -limit 100 -skip 0 -pager
        
    # 获取某个注释全部属性(自动确保读取 content)
    thing xxx comment 45ad6823.. -get 20150721132134321
        
    # 获取某个注释的内容文本（仅仅是内容文本，不是 breif)
    thing xxx comment 45ad6823.. -read 20150721132134321




