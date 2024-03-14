# 命令简介

`sms` 用来处理和短信相关的操作。

默认读取当前用户下的发送配置进行邮件发送 `$HOME/.sms/config_${provider}`, 以云片网为例，格式如下:
    
    {
        apikey  : "abc1234567890",  // 提供商的apikey
        header  : "xxx"             // 强制添加头部，比如 【文尔科技】
        lang    : "zh-cn"           // 默认模板语言
        timeout : 5000              // 请求超时（毫秒），默认 5000  
    }
    
其中只有 "lang", "timeout" 为所有*provider*均支持的字段，其他字段，根据*provider*实现不同各不相同

    
# 用法

    sms send          // 发送短信
    sms query         // 查询短信发送状态
