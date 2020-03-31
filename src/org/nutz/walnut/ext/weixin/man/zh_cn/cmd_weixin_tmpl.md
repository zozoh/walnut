# 命令简介 

`weixin tmpl` 处理微信模板消息

# 用法

```bash
weixin {ConfName} tmpl 
    [-get]           # 获取微信服务器所有的模板消息
    [-add {TmplID}]  # 添加一个模板消息（输出模板ID）
    [-del {TmplID}]  # 删除一个模板消息
    [-send {..}]     # 发送一个模板消息
        [-tid {TmplID}]  # 微信模板ID
        [-to OpenID]     # 接收者的 OpenID
        [-url URL]       # 【选】消息的链接

```

# 示例

```bash
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
demo@~$ weixin xxx tmpl -send '{ta:"三个字儿",tb:"Hello!"}' 
            -tid hii5UTOT6P7YnJjGN-9FiSCf2qmSjaZsKrTsjF-7ej8
            -to  okyjlsv00EwLDRHnf_vWoq2iKJaE
            -url http://nutz.cn

# 从管道读取内容并发送
demo@~$ echo '{ta:"三个字儿",tb:"Hello!"}' | weixin xxx tmpl -send
    -tid iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s
            -tid hii5UTOT6P7YnJjGN-9FiSCf2qmSjaZsKrTsjF-7ej8
            -to  okyjlsv00EwLDRHnf_vWoq2iKJaE
            -url http://nutz.cn
```
     
