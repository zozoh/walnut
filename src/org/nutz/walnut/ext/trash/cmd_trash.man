# 命令简介 

    `trash` 命令用来管理回收站
    

# 用法

    trash [mv|recover] path path2 path3 ....
    
# 初始化需要root账号执行

	# mkdir /trash
	# chmod +x /trash
	# chmod +r /trash

# 将文件放入回收站

	# trash mv /home/wendal/abc
	/home/wendal/abc -> /trash/wendal/abc
	
	// 若出现重名
	# trash mv /home/wendal/abc
	/home/wendal/abc -> /trash/wendal/abc_1
	
# 将文件还原到原路径

	# trash recover /trash/wendal/abc
	/trash/wendal/abc -> /home/wendal/abc
	
	若原路径存在,则抛错