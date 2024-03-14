# 命令简介 

    `sshexec` 通过ssh远程执行一条命令
    
# 用法

	sshexec [-host nutz.cn] [-port 22] [-password 123456] [-connect_timeout 15] [-verify_timeout 15] [-exec_timeout 900] -command "ifconfig"
	
# 选项定义

	-host 主机名,默认127.0.0.1
	-port 端口,默认22
	-password 密码,默认为SEID:$session.id()
	-connect_timeout 连接超时设置,默认15秒
	-verify_timeout 密码验证超时设置,默认15秒
	-exec_timeout 执行超时设置,默认15分钟
	-command 需要执行的命令,必选
	
# 示例
	
	//执行ifconfig
	
	sshexec -host nutz.cn -password 123456 -command "ifconfig"
	
	// ps然后过滤
	
	sshexec -host nutz.cn -password 123456 -command "ps aux | grep java"
	
	