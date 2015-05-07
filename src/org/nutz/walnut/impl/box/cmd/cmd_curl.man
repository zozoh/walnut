# 命令简介

	`curl` 下载文件，当前只支持http或https的链接

# 用法

	curl [-O] url
	当没有 -O 时，则会直接输出 url 的内容

# 示例

	显示 index.html 的内容:
	curl http://nutzam.com/index.html

	下载 index.html:
	curl -O http://nutzam.com/index.html
