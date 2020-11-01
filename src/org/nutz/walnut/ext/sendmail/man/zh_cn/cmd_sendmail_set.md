过滤器简介
======= 

`@set` 通过一个文件或者标准输入的内容设置邮件
 

用法
=======

```bash
@fbcc 
  [~/path/to]        # 文件路径，如果不指定，则试图从标准输入读取 JSON 内容
  [-mapping {..}]    # 映射方式，默认 {name, email}
  [-match {..}]      # 过滤方式，仅仅对路径为目录的有效，
                     # 会在其内再获取第一个符合条件的文件对象
                     # 如果目录为 ThingSet，则采用 ThingSet 的查找方式
  [-content]         # 表示读取文件内容，并作为 "content" 字段
  [-read]            # 仅对文件有效，与 -content 互斥（更低优先级）
                     # 表示将内容读取为 JSON 并设置邮件
```


示例
=======

```bash
sendmail @set ~/emails/index/signup -mapping 'name:"=nickname",account:"=email"'
```

