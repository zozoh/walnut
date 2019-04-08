# 命令简介 

    `weixin text` 这是weixin out text的快捷版本
    
# 用法

	weixin [公众号id] text [-text "这是你的信息"]
	
	# 指定文本
	>> weixin xxx text -text 起床了
	
	# 从管道读取
	
	>> echo "碎觉碎觉" | weixin xxx text