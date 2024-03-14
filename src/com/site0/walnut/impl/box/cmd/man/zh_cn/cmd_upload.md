# 命令简介 

	`upload` 将会显示一个文件上传对话框

# 用法

	upload [-id 40jps.. | /path/to/upload] [-single] [-name ..] [-type ..] [-mime ..]
	
	-single : 是否只上传一个文件，默认为 false 
    -name   : 名称的正则表达式校验，默认不检查
    -type   : 类型的校验，默认不检查
    -mime   : 内容类型的校验，默认不检查
	
# 示例

	# 上传多个文件到 ~/mypics 目录
	upload ~/mypics 
	
	# 上传多个文件到指定 ID 的目录
	upload -id 40jps88ieqj06olvlnkdabj8pc
	
