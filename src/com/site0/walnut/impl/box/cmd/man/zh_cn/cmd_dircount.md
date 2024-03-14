# 命令简介 

	`dircount` 将列出某目录下的子对象数量, 并跟根据deep值进行深度遍历。
	
	返回结果为json格式。
	
	{
	   "id": "XXXXXXX",
	   "name": "XXXXX",
	   "path": "/home/pw/test",
	   "count": 11
	}

# 用法

	count [-A] [-tp txt] [-match '{...}'] [-deep 0] [/path/to/dir]..
	
# 示例

	显示当前目录所非隐藏对象数量
	dircount
	
	计算当前目录下所有对象数量，包括隐藏对象
	dircount -A
	
	计算txt类型的对象数量
	dircount -tp txt
	
	计算符合match的对象数量
	dircount -match 'tp:"txt"'
	
	计算2层深度目录
	dircount -deep 2
	
