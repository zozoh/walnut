# 执行脚本(实验性质) 

`jsc` 将执行一段脚本或一个js文件,可以传入参数及调用其他命令

# 用法

```bash
jsc 
[path/to/js]            # 执行文件路径
[-debug]                # 调试模式
[-lsengine]             # 列出可用引擎
[-engine nashorn]       # 指定执行引擎，默认 nashorn
[-vars {...}]           # 指定脚本参数，
                        # 不指定则尝试从标准输入读取
[-merge]                # 表示 vars 的内容与标准输入融合
                        # 作为脚本的输入变量集
```
	
	
# 示例

```bash
最简单调用, 用到了call和sys实例. 注意, call方法必须带参数
jsc "sys.exec('ls /' + sys.me.nm);"

带调试信息
jsc -debug "var abc=1+1;sys.exec('touch /' + sys.me.nm + '/.bashrc');"

执行脚本文件
jsc -f /root/.js/clear_caches.js

列出当前系统支持的 engine
jsc -lsengine
```
	
	
	