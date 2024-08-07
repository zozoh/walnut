# 命令简介 

`ajaxre` 命令将给定数据格式化成 Nutz 的 AjaxReturn 格式输出，即

```
成功的格式:
----------------------------------------------
{
    ok : true,     // 标识成功
    data : ..      // 数据可以是任何 Json 数据 
}
----------------------------------------------
失败的格式:
----------------------------------------------
{
    ok : false,         // 标识失败
    errCode : "xxx",    // 错误码
    msg  : "xxx",       // 错误消息
    data : ..           // 错误原因 
}
----------------------------------------------
```

# 用法

    ajaxre [-e] [-s] [-cqn] [-lang zh-cn] Data 
    
    -s       「选」表示数据会被当做字符串，否则会被解析成 JSON 对象
    -e       「选」强制将输入当做错误信息
    -cqn     「选」输出的 AJAX JSON 对象的格式 -c:紧凑  -q:用引号包裹键 -n:忽略空值
    -lang    「选」采用哪种语言格式化错误信息，默认用 sys.getLang()   
    Data     「选」要输出的数据，如果没有则从标准输入里读取，会尝试解析成JSON，如果不是 JSON 就当做字符串
    
# 示例

    # 输出一格式化后的数组对象
    demo@~$ ajaxre '[1,2,3]'
    {
        ok : true,
        data : [1,2,3]
    }
    
    # 格式化错误信息 
    demo@~$ echo 'e.test.err : some text' | ajaxre
    {
        ok : false,
        errCode : "e.test.err",
        msg : "测试错误 : some text",
        data : "some text"
    }