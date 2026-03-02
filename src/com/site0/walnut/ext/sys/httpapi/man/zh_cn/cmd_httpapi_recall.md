# 命令简介 

    `httpapi recall` 将会重新执行一个请求文件

# 用法

    httpapi recall
        [/path/api]      # API 的路径
        [~/path/to/req]  # 请求对象路径
    
# 示例
    
    # 执行指定 httpapi 请求对象
    demo@~$ httpapi recall /get/hello ~/.regapi/tmp/xxxx
