# 命令简介 

	`role` 命令获取某个用户在某个组所扮演的权限

# 用法

	role [[user:]group] [-nm] [-role ROLE]
	
	-nm     参数表示除了输出权限值，是否还要输出一个说明文字
	-role   声明了这个参数，则表示修改角色的设定
	
# 示例

	// 显示当前账号在组abc的权限
	role abc
	
	// 显示小白账号在组abc的权限
	role xiaobai:abc

	// 修改小白账号在组abc的权限为管理员 
	role xiaobai:abc -role 1	
