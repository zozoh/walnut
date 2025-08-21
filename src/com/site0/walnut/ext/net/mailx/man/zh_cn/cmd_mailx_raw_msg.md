# 过滤器简介

`@raw_msg` 测试解析邮件消息的工具
 

# 用法

```bash
@raw_msg 
[/path/to/mail ..] # 邮件文件的路径，可以是多个
#--------------------------------------------------
# 下面的配置项目与 mailx @imap 意义相同
#--------------------------------------------------
[-meta '{...}']   # 固定元数据 
[-decrypt]        # 自动解密
[-content xxx]    # 正文的 ContentType
[-header]         # 调试模式下打印消息邮件头
[-json]           # 采用 json 的方式输出存储对象，
[-cqn]            # JSON模式的格式化
```


# 示例

```bash
# 加载两个附件对象
mailx @at ~/a.jpg ~/b.zip

# 从标准输入加载附件
cat ~/test.zip | mail @at -name tt.zip -mime application/zip
```

