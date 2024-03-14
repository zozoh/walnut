# 命令简介 

    `wup` 用来提供通用更新服务

# 用法

    wup [操作] [子操作?] [..参数]
    
# wup服务初始化

```
# 初始化
root@~$ wup init
```
 
# 节点操作

```
# node初始化
root@~$ wup node init -macid AABBCCDDEEFF -godkey 123456 -type gbox

# node获取节点信息
root@~$ wup node get -macid AABBCCDDEEFF
{
	...
}

# 获取node列表
root@~$ wup node query
[
	{
		...
	},
	{
		...
	}
]

#设置软件版本
root@~$ wup node set -macid AABBCCDDEEFF -pkg walnut -version $version
```
	
# 更新包操作

	# 构建更新包,包括strato
	root@~$ wup pkg build all

	# 构建更新包,仅零站
	root@~$ wup pkg build all-site0

    # 添加更新包
    root@~$ wup pkg add ~/dw
    
    # 添加更新包并更新几个服务器配置, 例如零站(腾讯云), 零站(爬虫), cherry(德纳的strato)
    root@~$ wup pkg add ~/dw site0_qcloud site0_mediax cherry
    
    # 获取更新包信息
    root@~$ wup pkg info -name jdk -version 8u112 -macid AABBCCDDEEFF -key adw..dsfasd
    
    
    # 获取更新包,将输出更新包的二进制数据
    wendal@~$ wup pkg get -name jdk -version 8u112 -macid AABBCCDDEEFF -key adw..dsfasd
