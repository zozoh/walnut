# 命令简介 

	`cat` 命令一次性将某文件的全部内容输出到标准输出

# 用法

	cat (id:{$id}||/path/to/f0) ..
	[-quiet]    # 静默模式，如果对象不存在，什么也不输出
	
# 示例

	// 根据路径显示某一个文件的内容
	cat ~/abc.txt
	
	// 根据路径显示某几个文件的内容
	cat a.txt b.txt
	
	// 根据 ID 显示某一个文件的内容
	cat id:fqmnqpKVIvu91vYZgePoQ0
	
	// 根据 ID 显示某几个文件的内容
	cat id:fqmnqpKVIvu91vYZgePoQ0 6C_B-p_1IjaFWlJk_br_c1
	
