# 命令简介 

	`session` 命令用来查看和管理会话的相关信息

# 用法

	session [-clear]
	
# 示例

	显示当前会话所有信息
	session
	
	清除所有过期会话(已禁用)
	session -clear
	
	创建会话,仅root和opt组的用户可以操作
	session -create wxlogin:xxxx:sdfsdfsfd
		
