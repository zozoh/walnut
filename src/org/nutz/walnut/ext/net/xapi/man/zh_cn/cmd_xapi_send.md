# 命令简介 

`xapi send` 向第三方平台发送请求，并输出返回结果

# 用法

```bash
xapi send 
  [apiName]           # 应用平台名称
  [account]           # 【必】平台的账号名
  [reqName]           # 【必】平台的请求对象名称（具体参见文档 f0-xapi-weixin.md）
  [-vars {..}]        # 参数表的上下文，如果没有，从标准输入读取
  [-proxy /path/to]   # 一个指向 proxy配置文件的路径
                      # 如果该文件存在就启用代理
                      # 文件内容为 {type:"http",host,port} 
                      # 如果没有指定，会自动寻找 ~/.domain/proxy.json
                      # 除非指明 "none"
  [-timeout 60:10]    # 请求过期（秒）, 可以是`60:10` 或者 `60`
                      #  - 60 秒 : 连接 Timeout
                      #  - 10 秒 : 握手 Timeout
```
# 示例

```bash
# 获取微信公众号用户的信息
demo@~$ xapi send wxgh demo gh_user_info -vars 'openid:"xxx"'
{
    "subscribe": 1, 
    "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M", 
    "nickname": "Band", 
    "sex": 1, 
    "language": "zh_CN", 
    "city": "广州", 
    "province": "广东", 
    "country": "中国", 
    "headimgurl":"http://thirdwx.qlo...xCfHe/0",
    "subscribe_time": 1382694957,
    "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
    "remark": "",
    "groupid": 0,
    "tagid_list":[128,2],
    "subscribe_scene": "ADD_SCENE_QR_CODE",
    "qr_scene": 98765,
    "qr_scene_str": ""
}
```