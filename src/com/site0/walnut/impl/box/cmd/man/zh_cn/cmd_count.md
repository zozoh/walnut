# 命令简介 

	`count` 将列出某目录下的子对象数量。如果没有给入参数，则将 "." 作为默认参数

# 用法

	count [-A] [-tp txt] [-match '{...}'][/path/to/dir]..
	
# 示例

	显示当前目录所非隐藏对象数量
	count
	
	显示上一级目录的对象数量
	count ..
	
	计算当前目录下，所有以 a 开头的对象对象数量
	count 
	
	计算当前目录下所有对象数量，包括隐藏对象
	count -A
	
	计算txt类型的对象数量
	count .. -tp txt
	
	计算符合match的对象数量
	count -match 'tp:"txt"'
	
