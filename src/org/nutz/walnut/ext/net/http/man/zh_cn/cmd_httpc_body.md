# 过滤器简介

`@params` 向上下文内容设置请求体的内容
 

# 用法

```bash
httpc {URL} @body 
  ['xxxx']        # 直接就是 Body 的内容
  [-f /path/to]   # 采用一个文件路径作为 body 的内容

```



# 示例

```bash
# 直接POST一个 body
httpc http://demo.com/path/to @body xxx

# 从文件读取 body 的内容
httpc http://demo.com/path/to @body -f ~/xxx

# 从标准输入读取 body 的内容
cat ~/xxx | httpc http://demo.com/path/to @body
```
