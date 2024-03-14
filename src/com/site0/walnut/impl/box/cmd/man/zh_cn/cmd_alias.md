# 命令简介 

	`alias` 定义一个带参数的别名命令

# 用法

	alias [-p] [-r] [key=cmds..] [key1=cmds2..]
	
# 示例

	显示当前已经存在的别名命令
	alias -p
	
	移除之前定义的别名命令
	alias -r qq
	
	定义一个别名命令
	alias qq="ll -A"
	
	执行一个别名命令, 与普通方法无异,只是实际执行的命令为 ll -A /home,即展开式
	qq /home