# 命令简介 

	`touch` 更新文件的最后修改时间

# 用法

	touch [-c] [-r] [-o] <file|dir> [file2|dir2]
	
# 示例

	// 创建一个文件
	touch abc
	
	// 仅修改已有文件的时间戳
	touch -c abc
	
	// 如果是目录,循环修改文件的时间戳
	touch -r pictures
	
	// 确保文件存在，并输出文件元数据
    touch -o abc
	
	
