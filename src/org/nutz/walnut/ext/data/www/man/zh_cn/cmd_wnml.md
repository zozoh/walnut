# 命令简介 

    `wnml` 用来将一个对象转换成可访问的网页形式，并将网页代码输出到标准输出
    

# 用法

    wnml /path/to/file 
        [-home /xxx]     # 指定所在站点名称，没有指定的话，默认用文件所在目录作为 SITE_HOME
        [-c {..}]        # 转换上下文，没声明内容，从管道读取
    
# 示例

    # 转换一个对象 
    $:> wnml ~/mysite/page/abc.wnml -home ~/mysite
    
    # 从一个管线转换对象上下文
    $> cat ~/abc.json | wnml ~/mysite/abc.wnml -c 
        

    