# unzip压缩命令

	`unzip` 将一个zip包解压到指定目录

# 用法

	unzip [-f] <zipfile> <dir1>
	
	-f 文件已存在强制写入
	-l 查看压缩包文件内容
	-hidden 如果包内有隐藏文件，也写入
	-macosx 如果包内有 __MACOSX 文件，也写入
	-m 过滤器，WnMatch
	
# 示例

	// 查看usb.zip内容
	unzip -l usb.zip

	// 压缩usb.zip, 到当前文件夹 
	unzip usb.zip
	
	// 压缩usb.zip, 到指定目录
	unzip usb.zip ~/ztest/haha
	
	// 压缩usb.zip, 到指定目录, 存在的文件强制更新
	unzip -f usb.zip ~/ztest/haha
	
