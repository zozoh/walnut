# 命令简介 

`qrcode` 执行二维码的生成和解析

# 用法

```bash
qrcode [-dn] [/path/to/obj] [options]

-d              		# 指明操作是解码，否则是编码，即生成二维码
-n              		# 解析二维码的时候，在内容后加上回车
-size   : 256   		# 生成二维码的大小
-fmt    : png   		# 生成二维码图片的格式
-margin : 1     		# 边距
-icon   : id:28e8s.. 	# icon图片的id或者path
-icsz   : 50            # 图标的大小,默认为 size 的 1/6
-c      : Q             # 容错级别,默认为 Q
                        # 支持 L,M,Q,H
```
	
	
# 示例

```bash
# 将一个字符串编码，并输出到标准输出
$:> echo 'I feel good' | qrcode
00 09 87 ...	

# 将一个字符串编码，并写到某个对象里
$:> echo 'I feel good' | qrcode > ~/myqrcode.jpg

# 从某对象解码，并将其内容写到标准输出
$:> qrcode -d ~/myqrcode.jpg
I feel food 

# 生成带有icon的二维码
$:> qrcode 哈哈 -icon id:xxxxxx
00 09 87 ...	
```