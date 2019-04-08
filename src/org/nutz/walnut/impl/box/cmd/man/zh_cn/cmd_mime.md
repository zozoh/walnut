# 命令简介 

	`mime` 用来查看对象的 MIME 类型 

# 用法

	mime [-id] ($id0|/path/to/f0) [($id1|/path/to/f1)]..
	
# 示例

	// 根据路径显示某一个文件的MIME
	mime ~/abc.txt
	
	// 根据路径显示某几个文件的MIME
	mime a.txt b.txt
	
	// 根据 ID 显示某一个文件的MIME
	mime -id fqmnqpKVIvu91vYZgePoQ0
	
	// 根据 ID 显示某几个文件的MIME
	mime -id fqmnqpKVIvu91vYZgePoQ0 6C_B-p_1IjaFWlJk_br_c1
	
	
