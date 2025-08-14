# 命令简介 

`mailx` 命令用来处理电子邮件相关操作的过滤器型命令。可以支持：

- 收发邮件(SMTP/POP3/IMAP)
- 支持加密解密(S/MIME)
- 支持数字签名
- 富文本以及附件

-------------------------------------------------------------
# 用法


```bash
mailx 
  [ConfigName]                   # 配置名称
  [-lang zh-cn]                  # 语言，默认 zh-cn
  [-quiet]                       # 静默输出
  [-json]                        # 将结果输出为 JSON
  [-cqn]                         # 非静默输出的 JSON 格式
  [-ajax]                        # 非静默输出时，要 ajax 包裹
  [[@filter filter-args...]...]  # 过滤器
```

其中 `ConfigName` 表示配置文件名，对应为 `.mail/${ConfigName}.json`


它支持的过滤器有：

```bash
#-------------------------------------
# 收信
#-------------------------------------
@imap       # 采用 IMAP 方式收信
@aws_s3     # 采用 AWS SES S3 方式收信
@raw_msg    # 测试解析邮件消息的工具
#-------------------------------------
# SMTP 发送邮件
#-------------------------------------
# 设置头部
#-------------------------------------
@to         # 添加接收者
@cc         # 添加抄送者
@bcc        # 添加密送者
#-------------------------------------
@fto        # 根据文件或目录添加接收者
@fcc        # 根据文件或目录添加抄送者
@fbcc       # 根据文件或目录添加密送者
#-------------------------------------
@sub        # 邮件标题
#-------------------------------------
# 设置内容
#-------------------------------------
@content    # 邮件正文
@tmpl       # 邮件模板
@at         # 邮件附件
#-------------------------------------
# 模板相关
#-------------------------------------
@set        # 根据一个指定文件元数据或内容设置邮件字段
@vars       # 添加上下文变量
@meta       # 通过文件元数据向上下文增加一组变量
@json       # 通过文件内容向上下文增加一组变量
@trans      # 为邮件的上下文变量添加一个转换脚本
```

-------------------------------------------------------------
# 示例

```bash 
# 发送邮件
mailx @config ics @sub xxx @attchment ~/a.edi @send


# 发送邮件
mailx @to a@b.c b@8.com @sub xxx @at ~/a.edi \
      @sign -type smime -store_passwd xxx  -alias xxx -priv_passwd xxx ~/path.store.pfx \
      @encrypt -type smime ~/some.cer

# 发送邮件
mailx @from xx@xx.com @to a@b.c @sub xxx @at ~/a.edi \
      @sign -conf ~/sign.json \
      @encrypt -conf ~/encrypt.json \
      @save ~/outbox/index

# 接受邮件
office365 token xxx.json | mailx @imap INBOX -conf imap.json -passwd 
```
