# 命令简介 

`xapi send` 向第三方平台发送请求，并输出返回结果

# 用法

```bash
xapi send 
  [apiName]        # 应用平台名称
  [account]        # 平台的账号名
  [reqName]        # 平台的请求对象名称（具体参见文档 f0-xapi-weixin.md）
  [-vars {..}]     # 参数表的上下文，如果没有，从标准输入读取
```
# 示例

```bash
# 获取微信公众号用户的信息
demo@~$ xapi send wxgh demo user/info -vars 'openid:"xxx"'
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