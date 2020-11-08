# 命令简介 

`sendmail` 命令用来处理电子邮件相关操作的过滤器型命令

-------------------------------------------------------------
# 用法


```bash
sendmail 
  [ConfigName]                   # 配置名称
  [-lang zh-cn]                  # 语言，默认 zh-cn
  [-base http://...]             # HTML 邮件的 base URL
  [-quiet]                       # 静默输出
  [-json]                        # 将结果输出为 JSON
  [-cqn]                         # 非静默输出的 JSON 格式
  [-ajax]                        # 非静默输出时，要 ajax 包裹
  [[@filter filter-args...]...]  # 过滤器
```

其中 `ConfigName` 表示配置文件名，对应为 `.mail/${ConfigName}.json`


它支持的过滤器有：

```bash
@to         # 添加接收者
@cc         # 添加抄送者
@bcc        # 添加密送者
@fto        # 根据文件或目录添加接收者
@fcc        # 根据文件或目录添加抄送者
@fbcc       # 根据文件或目录添加密送者
@set        # 根据一个指定文件元数据或内容设置邮件字段
@sub        # 邮件标题
@msg        # 邮件正文
@tmpl       # 邮件模板
@at         # 邮件附件
@vars       # 添加上下文变量
@meta       # 通过文件元数据向上下文增加一组变量
@json       # 通过文件内容向上下文增加一组变量
@trans      # 为邮件的上下文变量添加一个转换脚本
```
