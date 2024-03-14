# 命令简介 

	`chmime` 用来设置对象的 MIME 类型 

# 用法

	chmime [-id] ($id0|/path/to/f0) $MIME
	
# 示例

	// 根据路径设置某一个文件的MIME
	chmime ~/abc.txt text/html
	
	// 根据 ID 设置某一个文件的MIME
	mime -id fqmnqpKVIvu91vYZgePoQ0 text/html
	
	
	
