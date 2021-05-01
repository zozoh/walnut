# 命令简介 

    `sync` 数据同步
    
# 用法

	sync [-debug] [-add=true] [-del=true] [-force=false] <源URL> <目标URL>
	
# 选项定义

	-add 是否新增文件,默认为true
	-del 是否删除文件,默认为true
	-force 冲突是是否强制覆盖
	
# URL格式定义

	file:// 本地磁盘目录, 例如 file:///home/Users/wendal/walnut
	
	walnut:// walnut服务器, 例如 walnut://zozozh.walnut.org/home/zozoh/ngrok
	
	qiniu:// 七牛网盘,例如 qiniu:///nutzcn/ngrok
	
	若不带前缀,在walnut内代表内部路径
	
# 示例
	
	将/home/wendal/ngrok目录下的文件同步到本地磁盘的/opt/data目录
	
	sync /home/wendal/ngrok file:///opt/data
	
	
	