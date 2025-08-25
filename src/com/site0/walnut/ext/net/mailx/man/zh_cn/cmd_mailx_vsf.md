# 过滤器简介

`@vsf` 从一个映射文件夹直接读取邮件的 `MIME Text` 并生成邮件对象

```json5
{
  
}
```

# 用法

```bash
@vsf
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
```

# 示例

```bash
# 收取最近的邮件，如果是加密邮件自动解密，并存入一个指定数据集
mailx @vsf -decrypt -to ~/mymail

# 收取所有最近邮件，并在控制台打印邮件头
mailx @vsf -json -cqn

# 读取所有未读邮件，并自动解密（根据 test.secuy的设置），并存入指定数据集
mailx @load ~/tmp/test.secuy.json @vsf -decrypt -content 'Application/EDIFACT' -to ~/myemails
```
