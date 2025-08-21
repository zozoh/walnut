# 过滤器简介

`@aws_s3` 从 AWS 的 S3 服务直接读取邮件的 `MIME Text` 并生成邮件对象

```json5
"aws_s3": {
  
}
```

# 用法

```bash
@imap
# 指定一个收件箱文件夹名称，如果不指定
# 则会采用全局默认配置
[INBOX]

#--------------------------------------------------
# 下面的配置项目与 mailx @imap 意义相同
#--------------------------------------------------
[-to ~/path/to/${msg_id}]            # 邮件对象存储路径
[-at ~/path/${msg_id}/${file_name}]  # 附件存储路径
[-meta '{...}']   # 固定元数据 
[-decrypt]        # 自动解密
[-content xxx]    # 正文的 ContentType
[-header]         # 调试模式下打印消息邮件头
[-debug]          # 调试模式
[-json]           # 采用 json 的方式输出存储对象，
[-cqn]            # JSON模式的格式化
[-after xxx]      # 写入邮件完毕后的回调命令
```

# 示例

```bash
# 收取最近的邮件，如果是加密邮件自动解密，并存入一个指定数据集
mailx @aws_s3 -decrypt -to ~/mymail

# 收取所有未读的邮件，不自动解密，并存入一个指定的数据集
mailx @aws_s3  -flags !SEEN -to ~/mymail

# 收取所有最近邮件，并在控制台打印邮件头
mailx @aws_s3 -json -cqn

# 读取所有未读邮件，并自动解密（根据 test.secuy的设置），并存入指定数据集
mailx @load ~/tmp/test.secuy.json @aws_s3 -decrypt -content 'Application/EDIFACT' -to ~/myemails
```
