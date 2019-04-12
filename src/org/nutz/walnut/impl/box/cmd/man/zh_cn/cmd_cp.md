# 命令简介 

	`cp` 命令,将文件/文件夹拷贝到指定路径

# 用法

	cp [-r] [-p] src dst

# 示例

	// 拷贝单个文件
	cp hi.jpg hi_again.jpg

	// 拷贝目录
	cp -r superdir supermandir

	// 拷贝单个文件,且复制元数据
	cp -p hi.jpg hi_keepmode.jpg

