# 命令简介 

    `weixin tmpl` 生成模板响应

# 用法

    weixin [公众号id] tmpl [..参数]

    # 列出所有模板
    demo@~$ weixin xxx tmpl -get
    [{
        "template_id": "iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s",
        "title": "领取奖金提醒",
        "primary_industry": "IT科技",
        "deputy_industry": "互联网|电子商务",
        "content": "{{first.DATA}}\n\n..\n{{remark.DATA}}",
        "example": "您已提交领奖申请.."
    }, {
        ...
    }]
    
    # 获取行业信息
    demo@~$ weixin xxx tmpl -get industry
    {
        "primary_industry":{"first_class":"运输与仓储","second_class":"快递"},
        "secondary_industry":{"first_class":"IT科技","second_class":"互联网|电子商务"}
    }
    
    # 添加一个模板得到模板的 ID
    demo@~$ weixin xxx tmpl -add TM00015
    Doclyl5uP7Aciu-qZ7mJNPtWkbkYnWBWVja26EGbNyk
    
    # 删除一个模板
    demo@~$ weixin xxx tmpl -del iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s
    
    # 设置行业
    demo@~$ weixin xxx tmpl -industry 2,5
    
    # 发送模板消息
    demo@~$ weixin xxx tmpl -content "{..}" 
                -url http:xxx 
                -tid iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s
                -to  OPENID
    
     
