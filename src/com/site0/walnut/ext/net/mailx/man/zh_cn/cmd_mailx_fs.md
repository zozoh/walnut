# 过滤器简介

`@fs` 从一个文件夹直接读取邮件的 `MIME Text` 并生成邮件对象。
对于 AWS 的 `SES` 这样的邮件接口，它实际上是将邮件放到 `S3` 里的。
系统可以将 `S3` 的对应存储桶映射到自己的某个目录里，以便抽象处理。

它会读取 `mailx`的配置`fs`段内容作为配置：

```json5
{
  "fs":{
    // 指定映射 S3 存储桶的目录
    "home": "~/tmp/s3",
    // 指明收件箱的文件夹名称，默认为 `INBOX`
    "inbox": "INBOX",
    // 指明归档的路径前缀，它会根据文件的 lastModified
    // 进行日期时间格式化
    "archivePrefix": "'ARCHIVE'/yyyy/MM/dd" 
  }
}
```

# 用法

```bash
@fs
#--------------------------------------------------
# 下面的配置项目与 mailx @imap 意义相同
#--------------------------------------------------
[INBOX]                              # 收件箱前缀
[-to ~/path/to/${msg_id}]            # 邮件对象存储路径
[-at ~/path/${msg_id}/${file_name}]  # 附件存储路径
[-meta '{...}']   # 固定元数据 
[-decrypt]        # 自动解密
[-content xxx]    # 正文的 ContentType
[-header]         # 调试模式下打印消息邮件头
[-debug]          # 调试模式
[-json]           # 采用 json 的方式输出存储对象，
[-cqn]            # JSON模式的格式化
[-after xxx]      # 写入邮件完毕后的回调命令模板
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
