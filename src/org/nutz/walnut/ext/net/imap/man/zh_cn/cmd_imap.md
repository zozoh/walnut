# 命令简介

`imap` 是采用`IMAP`协议收取邮件的命令
    

# 用法

```
imap [{ConfigName}] [FOLDER] [[@filter filter-args...]...]
```

它支持的`options`有

```bash
[{ConfigName}]     # 【必】配置名，用来读取 ~/.imap/{ConfigName}.json
[INBOX]            # 文件夹（默认为 INBOX）
```

它支持的过滤器有：

```bash
@summary    # 加载当前文件夹的摘要信息
@search     # 从服务器收取邮件
@flags      # 批量标记上下文中邮件状态
@save       # 将上下文中的邮件保存到一个 ThingSet 中
@json       # 将上下文的邮件采用 JSON 形式输出
@tab        # 将上下文的邮件采用表格形式输出
```
