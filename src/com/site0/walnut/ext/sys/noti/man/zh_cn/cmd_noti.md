# 命令简介 

    `noti` 将用来提供系统通知

# 用法

    noti [参数]
    
# 示例

    # 添加微信模板通知
    demo@~$ noti add weixin -tmpl "ngqIp.."
                    -pnb "xxx"
                    -to "OPENID"
                    -url "xxxxx"
                    -content "{..}"
                    -lv 1
                    
    # 添加短信通知
    demo@~$ noti add sms -text "xxxx" -to "139.." -provider xxx -lv 1

    # 执行发送通知
    demo@~$ noti send [消息ID] [-u 用户] [-limit 1] [-timeout 10]
    
    # 清理已经完成的通知
    demo@~$ noti clean [-limit 100]
    
    # 显示通知 
    demo@~$ noti list [-u 用户] [-st -1]
    
    # 删除通知
    demo@~$ noti del [消息ID]
    
