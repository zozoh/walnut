# 命令简介 

    `email` 用来发送邮件
    
    默认读取当前用户下的发送配置进行邮件发送 $HOME/.email/config_default, 格式如下:
    
    {
		host 	: "smtp.qq.com",
		port 	: 25,
		account : 'root@walnut.com',
		from    : "nutzcn论坛",         
		alias	: 'root',
		password: '123456',
		lang    : "zh-cn",
		ssl     : true
    }

# 用法

    email [-c /path/to/conf]  # 「选」指定发送配置文件，默认用 ~/.email/config_default
          [-r xxx]            # 接收者 xxx@xx.com 半角逗号分隔
          [-cc xxx]           # 抄送者 xxx@xx.com 半角逗号分隔
          [-s xxx]            # 指定标题。如果是 i18n:xxx 格式，
                              # 则从 ~/.email/i18n/$lang/_subjects 读取标题
                              # 标题也会被 -vars 执行
          [-m xxxx]           # 发送内容，如果开启了 -tmpl 则被无视
          [-tmpl tmplName]    # 模板文件名，通常在  ~/.email 目录下
                              # 绝对路径的话，就可以不在 ~/.email 目录下
                              # 如果是 i18n:xxxx 则表示在 ~/.email/i18n/$lang/ 目录下
          [-lang zh-cn]       # 指定模板语言，默认为从 conf 里读取，还是没有的话用 zh-cn
          [-vars {}]          # 占位符变量，提供给 -tmpl 和 -s 用的
                              # 如果没声明，则从标准输入读取 
          [-attach 单个附件]   # 附件路径或附件详情
          [-attachs 多个附件]  # 附件详情数组 
          [-debug]            # 开启调试模式
                    [-vars map变量json字符串] 
          [list|clear] 
          [limit]
	
# 示例
	
	发送一封简单的邮件
	email -r xiaobai@163.com -s 你好小白 -m 这是一封测试邮件
	
	同时发给多个接收者并指定名称
	email -r xiaobai@163.com=小白,xiaohong@qq.com=小红 -s 你好,小白小红 -m 这是另一封测试邮件
	
	发送模板邮件
	email -r xiaobai@163.com -s 标题你好 -tmpl /root/3g_mail.tmpl -vars "{count:1024,mac:'AABBCCDDEEFF'}"
	
	发送多份内部邮件, 注意-r的参数需要时一个合法的内部用户名
	email -r wendal,peter -s 出去郊游 -m 收到请回复 -local
	
	列出20封本地邮件,默认10封
	email list 20
	
	清除30封本地邮件,默认10封
	email clear 30
	
	debug为调试开关
	email -r xiaobai@163.com -s 你好小白 -m 这是一封测试邮件 -debug
	
	异步发送(未完成)
	email -async -r xiaobai@163.com -s 你好小白 -m 这是一封测试邮件
	
	单一附件发送
	email -r vt400@qq.com -s 附件测试 -m 带附件的邮件 -attach ~/report/abc.xls
	
	单一附件带详情
	email -r vt400@qq.com -s 附件测试 -m 带附件的邮件 -attach '{path:"~/report/abc.xls",name:"年报.xls",desc:"详情ABC"}'
	
	多附件发送
	email -r vt400@qq.com -s 附件测试 -m 带附件的邮件 -attachs '[{path:"~/report/abc.xls",name:"年报.xls"},{path:"~/report/测试报告.xls"}]'

