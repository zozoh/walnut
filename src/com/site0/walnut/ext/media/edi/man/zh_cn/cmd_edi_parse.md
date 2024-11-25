# 过滤器简介

`@parse` 将报文文本解析为对象

# 用法

```bash
edi @parse 
[-tidy]        # 解析前按行整理，去掉空行和注释行
```

# 示例

```bash
edi @load ~/demo.edi.txt -render ~/vars.json @parse
```
