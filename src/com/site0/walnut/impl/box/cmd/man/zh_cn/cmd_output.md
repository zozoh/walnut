# 命令简介 

`output` 和 `echo` 类似，也是输出内容，不过它支持延迟输出和随机输出，主要是用来测试

# 用法

	output [-delay ms] [-n number] [-te] ...
	
	-delay      延迟多少毫秒输出
	-n          输出多少条信息
	-interval   输出每条信息的时间间隔(毫秒)
	-e          将输出输入到错误输出
	-t          是否在每条信息前打上一个时间戳
	-i          是否输出每条信息的序号
	
# 示例

	// 直接输出一段文字
	output hello
	
	// 延迟  500ms 输出一段文件
	output -delay 500 hello
	
