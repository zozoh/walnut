# 过滤器简介

`@imap` 向 IMAP 服务器收取邮件。他会读取 `mailx`的配置`imap`段内容作为配置：

```json5
"imap": {
  // IMAP 服务地址
  "host":"outlook.office365.com",
  // -1 表示采用默认端口
  "port":-1,
  // 邮箱账户
  "account":"demo@abc.com",
  // 邮箱密码
  "password":"xxxxxxx",
  // 值如果没有指定 authProvider， 则采用一个默认的认证实现
  // 除了 office365 以外更多的 Provider 实现正在赶来的路上
  "provider": {
    "name": "office365",
    "setup": {
      "sslEnable": true,
      "authMechanisms": "XOAUTH2",
      "authority": "https://login.microsoftonli....",
      "clientId": "9b...06",
      // 如果不指定，则相当于获取下面所有权限
      "scope": [
        "offline_access",
        "email",
        "https://outlook.office.com/IMAP.AccessAsUser.All",
        //"https://outlook.office.com/POP.AccessAsUser.All",
        //"https://outlook.office.com/SMTP.Send"
      ],
      // 指明 access_token 的缓存路径
      // 可以是绝对路径，如果是相对路径，则表示与 config 文件
      // 相同的目录。
      "cachePath": "office356_access_token"
    }
  },
  // 收件箱文件夹名称，默认为 INBOX
  // 如果收信时不特别指定文件夹，则默认就采用这个文件夹
  "inboxName":"INBOX"
}
```

# 用法

```bash
@imap
[INBOX]           # 指定一个收件箱文件夹名称，如果不指定
                  # 则会采用全局默认配置
[-to ~/path/to]   # 指定一个要存储的数据集
                  # 如果不指定，则仅仅是打印邮件头
                  # 因为保存邮件的正文和附件，有可能导致邮件被标志已读
                  # 不指定存储目标的方式，可以用来调试输出
[-decrypt]        # 如果遇到加密邮件，会自动解密
                  # 前提是必须指定存储目标，以及上下文中加载了加密证书
[-flags SEEN]     # 指定了搜索的条件，多个条件采用半角逗号分隔，大小写不敏感
                  # 标记之间的关系为 AND
                  # 支持的过滤标记为:
                  # - ANSWERED : 已回复
                  # - DELETED : 已删除
                  # - DRAFT : 草稿
                  # - FLAGGED : 星标邮件
                  # - RECENT : 最近
                  # - SEEN : 已读
                  # - USER : 用户自定义（暂不知道这个是怎么用的）
                  # 如果前面有一个 "!" 表示取反
                  # 譬如 "!SEEN" 表示所有未标记已读的邮件，即未读邮件
                  # 不指定任何 flags 相当于是 RECENT
[-content xxx]    # 指明某种特殊的邮件段内容，也要当作正文来加载
                  # 譬如 `Application/EDIFACT` 
                  # 大小写不敏感
[-or]             # 多个 Flags 之间是一个 OR 的关系
[-header]         # 调试输出模式（未指定输出数据集时）下，也要详细打印每个消息的邮件头
[-debug]          # 即使是输出到数据集也要打印调试信息，默认是不打印
[-json]           # 输出数据集后，也将输出的数据控制台输出，采用 json 的方式
[-cqn]            # 如果是控制台输出，JSON模式的格式化
```

# 示例

```bash
# 收取最近的邮件，如果是加密邮件自动解密，并存入一个指定数据集
mailx @imap -decrypt abc -to ~/mymail

# 收取所有未读的邮件，不自动解密，并存入一个指定的数据集
mailx @imap  -flags !SEEN -to ~/mymail

# 收取所有最近邮件，并在控制台打印邮件头
mailx @imap -json -cqn

# 读取所有未读邮件，并自动解密（根据 test.secuy的设置），并存入指定数据集
mailx @load ~/tmp/test.secuy.json @imap -flags !SEEN -decrypt -content 'Application/EDIFACT' -to ~/myemails
```
