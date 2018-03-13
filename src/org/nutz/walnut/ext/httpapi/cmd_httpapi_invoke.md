# 命令简介 

    `httpapi invoke` 将会调用某一个指定的 api

# 用法

    httpapi invoke path/to/api               # api 路径（相对于 "~/.regapi/api")     
                   [-req /path/to/request]   # 请求文件，没有会自动创建一个
                   [-cookie String]          # 请求的 Cookie 内容
                   [-get JSON|@pipe]         # 请求的QueryString
                   [-post JSON|@pipe]        # 表单提交参数，会自动将请求对象设置为 POST
                   [-file /path/to/file]     # 将一个文件的内容 copy 成请求文件
                   [-body String]            # 自由的 Body 内容，如果没有内容，仅声明
                                             # 则从 pipe 里读取
                   [-u xxxx]                 # 指定一个用户。只有 root 和 op 组成员才能执行这个操作
    
**注意**

- `get|post|body` 都可以从 pipe 里读取内容，但是只能一个有效，即，一个读完了 pipe 其他的就读不到了
- 优先级为 `get > post > body`

# 示例
    
    # 用 GET 方式执行某个 api
    demo@~$ httpapi invoke demo/get -get "id:'12345'"
    
    # 用 POST 方式执行某个 api
    demo@~$ httpapi invoke demo/get -post "id:'12345'"
    
    # 从某个请求文件里读参数，并执行 api
    demo@~$ httpparam id:xxx | httpapi invoke demo/get -get @pipe
