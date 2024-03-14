# 执行脚本(实验性质) 

	`jsc` 将执行一段脚本或一个js文件,可以传入参数及调用其他命令

# 用法

	jsc [-debug|lsengine] [-engine 引擎名称] [-vars json字符串] [-f 文件路径] [js代码]
	
# 可用对象及方法

	sys :  WnSystem实例, // 对应的就是调用jsc时的WnSystem实例
	
	vars内的变量将直接放入上下文
	
# 示例

	最简单调用, 用到了call和sys实例. 注意, call方法必须带参数
	jsc "sys.exec('ls /' + sys.me.nm);"
	
	带调试信息
	jsc -debug "var abc=1+1;sys.exec('touch /' + sys.me.nm + '/.bashrc');"
	
	执行脚本文件
	jsc -f /root/.js/clear_caches.js
	
	列出当前系统支持的 engine
	jsc -lsengine
	
	
	