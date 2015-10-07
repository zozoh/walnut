# 命令简介 

    `httpc` 作为 http 客户端发送请求

# 用法

    httpc [GET|POST] url -body ""
    
# 示例

    根据一个文件，发送简单的 POST，发送的 ContentType 会根据文件的 mimeType
    httpc POST http:/xxx/xx/x -body id:$fileId
    
    
