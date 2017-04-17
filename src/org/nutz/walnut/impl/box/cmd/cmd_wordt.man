# wordt命令

	`wordt` 命令将word文档中占位符进行替换输出新的文档

# 用法

	wordt <tmpl.doc> <out.doc> [-var] [-c] [-debug]
	
# 示例

	// -c表示out.doc不存在时自动创建，否者抛错
	wordt tmpl.doc out.doc -c
	
	// 指定占位替换内容，否者从管道中读取
	wordt tmpl.doc out.doc -var "{...}"
	
	// debug模式，显示替换内容
	wordt tmpl.doc out.doc -var "{...}" -debug

