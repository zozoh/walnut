# 命令简介 

`thing file` 获取/管理一条记录的关联文件


# 用法

```bash
thing [TsID] file 
  [ID]            # Thing 对象的 ID
  [-dir media]    # 操作目录，在data目录下的路径，如果为空，则表示直接放置于数据目录
  [-add xxx.jpg]  # 添加文件
  [-dupp '@{major}(@{nb})@{suffix}']  # 重名时如何修改文件名
  [-overwrite]            # 重名时覆盖
  [-read /path/to/src]    # 指定写入文件的内容，如果没给参数从标准输入读取内容
  #----------------------------------------------------------------------------
  # 指明 -read 读取的文件流是HTTP表单上传流，可包括多个文件
  # 本参数必须包括 "boundary=..." 部分，用来标识上传流的字段边界符
  # 否则形当于没有设置
  # 因此 -add 参数就变成了过滤器， * 或者空则表示全部导入
  # 而 ukey 则表示被 -add 参数匹配的第一个文件
  [-upload "multipart/form-data; boundary=------------949109308418366359677655"]
  # 这个在表单上传时，指明采用哪个表单字段
  # 如果不指明，则每个表单的文件项，都会被写入，采用的的文件名是原始文件名
  # 如果表单只有一个文件，这个可以结合 -add 参数为文件改名
  [-upfield "file"]
  #----------------------------------------------------------------------------
  [-del xxx.jpg]          # 删除指定文件
  [-get xxx.jpg]          # 获取指定文件的元数据
  [-ufc]                  # 重新计算 Thing 的附件数量和元数据
  [-ukey thumb]           # 将当前文件的 ID 设置到 Thing 对象指定字段
  [-http abc.txt]          # 输出指定文件的内容为 HTTP 响应
  [-etag xxx]             # 输出为标准 HTTP 响应，同时根据 etag 判断是否输出 304
  [-range bytes=0-7183]   # 当输出 HTTP 响应时，分段下载表示，符合 Http头的 Range 规范
  [-UserAgent xxx]        # 当输出 HTTP 响应时，指定下载客户端的信息
  [-download false]       # 当输出 HTTP 响应时，开启下载模式，即指定 Content-Disposition
  [-cat abc.txt]          # 直接输出指定文件内容
  [-quiet]                # -http/get/cat 如果文件不存在是否静默
```

# 示例

```bash
# 列出一个 thing 所有的媒体文件，空的返回空数组 []
thing xxx file xxx -dir media
    
# 添加一个空图片，如果已存在则抛错
thing xxx file xxx -dir media -add abc.jpg
    
# 添加一个空图片，如果已存在则返回
thing xxx file xxx -dir media -add abc.jpg -overwrite
    
# 添加一个空图片，如果已存在则根据模板创建新的
thing xxx file xxx -dir media -add abc.jpg -dupp
    
# 添加一个图片，内容来自另外一个文件，如果已存在则抛错
# 如果想不抛错，参见上面创建空文件的例子，根据需要添加
# -overwrite 或者 -dupp 参数
thing xxx file xxx -dir media -add abc.jpg -read id:45vff..

# 输出图片内容，将直接输出图片的二进制内容
thing xxx file xxx -dir media -cat abc.jpg

# 同理，如果想输出一个附件内容，可以是
# 如果附件是文件会按照文本输出，否则输出二进制 
thing xxx file -dir attachment xxx -cat xyz.txt
    
# 添加一个图片，内容来自标准输入，如果已存在则抛错
cat abc.jpg | thing xxx file xxx -dir media -add abc.jpg -read  

# 删除某个媒体，不存在抛错
thing xxx file xxx -dir media -del abc.jpg

# 删除多个媒体，不存在抛错
thing xxx file xxx -dir media -del abc.jpg xyz.jpg ufo.png

# 删除某个媒体，不存在抛错也不报错
thing xxx file xxx -dir media -del abc.jpg -quiet

# 删除多个媒体，不存在抛错也不报错
thing xxx file xxx -dir media -del abc.jpg xyz.jpg ufo.png -quiet
```