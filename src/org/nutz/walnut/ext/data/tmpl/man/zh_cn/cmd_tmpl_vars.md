# 过滤器简介

`@vars` 设置上下文变量


# 用法

```bash
tmpl @vars  
  ["{x:100,y:90}"]   # 变量值来自参数,可以是多个
  [-f /path/to]      # 变量值来自文件
  [-name abc]        # 指明本次设置是向上下文增加一个变量
                     # 那么 变量参数只会读一个
  [-json]            # -name 模式下，指明参数的值是 JSON ,需要解析
  [-reset]           # 开启这个开关将会重置上下文变量
                     # 默认的要与上下文融合
```

# 示例

```bash
# 从文件里获得
tmpl @vars -f ~/myvars.json
# 从参数里获得
tmpl @vars '{x:100,y:90}'
# 从管道获得
cat ~/myvars.json | tmpl @vars
```
