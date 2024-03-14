# 过滤器简介 

`@trans` 为上下文变量设置一个转换脚本。
该脚本假想的输入是上下文变量的`JSON`字符串
 

# 用法

```bash
@trans [/path/to/script.js]
```


# 示例

```bash
mailx @trans jsc /path/to/script.js -vars
```

