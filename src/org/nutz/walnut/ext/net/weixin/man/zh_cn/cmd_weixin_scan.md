# 命令简介 

`weixin scan` 用于处理扫描事件

# 用法

```bash
weixin {ConfName} scan [..参数]
```

# 示例

```bash
# 处理一个扫描事件,执行对应的命令文件 ~/.weixin/xxx/scene/12345678
demo@~$ weixin xxx scan -openid yyy -eventkey '12345678' -dft 'default'
```
    
    
     