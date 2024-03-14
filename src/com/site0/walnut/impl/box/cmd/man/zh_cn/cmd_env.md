# 命令简介 

	`env` 将显示和管理当前会话的环境变量

# 用法

	env [-u key] [key ...]
	
# 示例

	显示全部环境变量:
	env
	
	显示某几个环境变量
	env PWD PATH
	
	移除某几个环境变量
	env -u a,b,c
	
