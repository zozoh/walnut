# unzip压缩命令

`unzip` 将一个zip包解压到指定目录

# 用法

```bash
unzip [-f] <zipfile> <dir1>

	-f       # 文件已存在强制写入
	-l       # 查看压缩包文件内容
	-quiet   # 静默输出。当然 read 模式下，即使静默，还是要输出内容的
	-hidden  # 如果包内有隐藏文件，也写入
	-macosx  # 如果包内有 __MACOSX 文件，也写入
	-m       # 只解压匹配的项目，WnMatch
	-read    # 仅仅将包内文件项目输出到标准输出
```
	
# 示例

```bash
# 查看usb.zip内容
unzip -l usb.zip

# 压缩usb.zip, 到当前文件夹 
unzip usb.zip

# 压缩usb.zip, 到指定目录
unzip usb.zip ~/ztest/haha

# 压缩usb.zip, 到指定目录, 存在的文件强制更新
unzip -f usb.zip ~/ztest/haha
```