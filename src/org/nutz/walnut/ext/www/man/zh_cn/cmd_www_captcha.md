命令简介
======= 

`www captcha` 用来生成验证码
    

用法
=======

```bash
www captcha
    [id:xxx]         # 【必须】站点主目录路径
    [$scene]         # 【必须】验证码针对的场景
    [$account]       # 【必须】验证码针对的账号 
    [-retry 3]       # 最大重试次数，默认3
    [-du 10]         # 有效期（分钟），默认10
    [-type digital]  # 验证码类型：digital|alphabet
    [-len 4]         # 验证码的位数，默认 4
    [-as json]       # 命令输出方式:
                     #  - json  : 直接输出  JSON(默认）
                     #  - sms   : 发送短信
                     #  - email : 发送邮件
                     #  - png   : 输出为PNG图像二进制码
    [-ajax]          # 除了图片方式，肯定要写JSON输出的
                     # 开启这个选项，则输出为 ajaxReturn 的包裹
    #-----------------------------------------------
    # json 模式下的特殊配置项
    [-cqn]           # 格式化方式
    #-----------------------------------------------
    # sms/email 模式下的特殊配置项
    [-cap 1423]      # 【必须】图形验证码，防机器人
    [-capscene robot]# 图形验证码的场景，默认 robot
    [-to 1842..]     # 接收通知的手机号或者邮箱，默认用账号
    [-by login]      # 短信模板文件的名称，默认用场景
    [-lang zh-cn]    # 模板语言，默认 zh-cn
    #-----------------------------------------------
    # png 模式下特殊选项
    [-http]          # 输出流用 HTTP 响应流的格式包裹
    [-size 100x50]   # 输出图片的尺寸，默认 100x50
    [-better]        # 输出较难辨认的验证码
```

示例
=======

```bash
# 输出一个四位数字的验证码的 JSON 形式
www captcha id:xxx auth zozoh

# 输出一个四位数字的验证码，并发送短信
www captcha id:xxx auth 18421964321 -cap 3421 -as sms

# 在 REGAPI 里，输出一个四位数字的验证码，并输出成一张图片
www captcha id:xxx robot 18421964321 -as png -http
```