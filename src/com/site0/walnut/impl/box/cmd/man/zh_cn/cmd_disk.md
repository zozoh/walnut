# 命令简介 

	`disk` 将列出某目录下的子对象obj。如果没有给入参数，则将 "." 作为默认参数

# 用法

	disk [-A] [-tp txt] [/path/to/dir]..
	
# 示例

	显示当前目录所有非隐藏对象obj
	disk
	
	显示上一级目录:
	disk ..
	
	显示当前目录下，所有以 a 开头的对象
	disk a*
	
	显示当前目录下所有对象，包括隐藏对象
	disk -A
	
	显示txt类型的文件
	disk .. -tp txt
	
	
