# 命令简介 

`avatar` 根据名字生成头像图片

# 用法

	avatar $name [options]
	
	-s   : 256   # 生成图像大小
	-fc     : #FFF  # fontColor
	-bg     : #000  # bgColor
	-font   : 'Microsoft Sans Serif' # 字体名称

	
# 示例

    # 将名字生成头像，并输出到标准输出
    $:> avatar 胖五
    00 09 87 ...	
	
    # 将名字生成头像(指定大小，颜色)，并写到某个对象里
    $:> avatar 胖五 -s 128 -fc '#080' -bg '#F00' > ~/myavatar.png
	
	