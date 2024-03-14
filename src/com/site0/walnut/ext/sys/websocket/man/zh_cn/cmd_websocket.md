# 命令简介 

    `websocket` 用来处理WebSocket相关的操作

# 用法

    websocket [操作] [wsid|id:$id] [参数]
    
# 发送一个文本信息
    
    demo@~$ websocket text 4tqpgk7u0ch3jovd9fhvg0cg5i  "{method:'snapshot'}"
    
# 发送一个文本信息给监听某个文件的全部websocket
    
    demo@~$ websocket text id:xxx  "{method:'snapshot'}"
    
# 发送一个事件,事实上是文本信息的封装
    
    demo@~$ websocket event 4tqpgk7u0ch3jovd9fhvg0cg5i publish
    
# 发送一个流
    
    demo@~$ cat abc | websocket binary 4tqpgk7u0ch3jovd9fhvg0cg5i
    
# WebSocket工作流程

```
// 客户端发起连接
// 服务器端接受连接,下发文本欢迎信息 {event:"hi",wsid:'$wsid'}
// 客户端接受到event=hi
// 客户端发送监视请求, 类型为文本信息 {method:'watch', user:'$user', match:{box_macid:'AABBCCDDEEFF'}}

// 客户端定期发送ping信息,保持连接

// 服务器端使用websocket命令在任意时刻,指定wsid发送信息
```