命令简介
======= 

`hmaker read` 用来读取一个站点下的资源文件。如果是 skin.css 则动态编译
    
用法
=======

```
hmaker [site] read         # 如果不指定 [site] 会认为当前目录为 site
   [path]                  # 指定站点资源路径，必须为相对路径，不能包含 id:xxx 形式的路径
   [-etag xxx]             # 输出为标准 HTTP 响应，同时根据 etag 判断是否输出 304
   [-range bytes=0-7183]   # 当输出 HTTP 响应时，分段下载表示，符合 Http头的 Range 规范
   [-UserAgent xxx]        # 当输出 HTTP 响应时，指定下载客户端的信息
```

示例
=======
```
# 读取一张图
hmaker id:xxx read image/bg.jpg

# 读取皮肤定义
hmaker id:xxx read skin.css   
```
