# 命令简介 

	`uuid` 用于生成指定格式的uuid字符串

# 用法

	uuid [-F (STR|uu32|uu16|uu64)] [-c 100] [-f tmp.txt] 
	
# 示例

	// 生成一个uuid
	uuid
	
	// 生成100个uuid, 注意,最多100个
	uuid -c 100
	
	// 生成10个uu16格式的uuid
	uuid -F uu16 -c 10
	
	// 生成一个linux标准uuid格式的uuid到tmp.txt文件
	
	uuid -F STR -f tmp.txt
	
