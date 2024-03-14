# 命令简介 

    `captcha` 用来生成验证码

# 用法

    captcha [-w 120] [-h 30] [-text 答案] [-textlen 4] [-out json] [-bg] [-noise] [-border] [-fisheye]
	
# 示例
	
	生成验证码,json输出
	captcha
	
	```json
	{
   		"text": "2899",
   		"png": "iVB...ASNDED"
	}
	```
	
	生成验证码,指定长宽及答案,json输出
	captcha -w 100 -h 40 -text 1234
	
	生成验证码,指定长宽及答案,直接输出png
	captcha -w 100 -h 40 -text 1234 -out png