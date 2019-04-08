# 命令简介 

	`usermod` 用于修改用户属性

# 用法

	usermod [-G root,wendal] [-E '{ackey:'xxxyyyzzz'}'] wendal
	
# 示例

	// 修改wendal所在的组为 root
	usermod -G root wendal
	
	// 修改zozoh用户的theme属性为dark-collorized
	usermod -E "{theme:'dark-collorized'}" zozoh
	
	// 快捷修改open属性
	usermod -s wn.browser
	
