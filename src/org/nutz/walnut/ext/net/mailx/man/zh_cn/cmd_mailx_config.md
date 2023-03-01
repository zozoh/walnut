# 过滤器简介

`@config` 为上下文邮件设置邮件相关信息。

一个配置文件可以包括如下信息:

```json5
{
  "to": "abc@xy.com; xxx@xx.com",
  "subject": "Some Text",
  "contentType": "text|html",
  "contentPath": "~/path/to.html",
  "content": "xxxxx",
  "attachments":[
    "~/path/to/a.zip",
    "~/path/to/b.zip"
  ],
  "security": {
    "type" : "SMIME",
    "sign":{
      "storePath":"~/demo.pfx",
      "storePassword":"xxxxxxx",
      "keyAlias":"55da4...4198b9",
      "keyPassword":""
    },
    "encryptCertFile":"~/path/to.cer"
  }
}
```

# 用法

```bash
@config path/to.json
```


示例
=======

```bash
mailx @config demo.json
```

