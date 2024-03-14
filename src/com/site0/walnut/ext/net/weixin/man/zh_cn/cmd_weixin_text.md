# 命令简介 

`weixin text` 这是weixin out text的快捷版本
    
# 用法

```bash
weixin {ConfName} text [-text "这是你的信息"]
```
	
# 示例

```bash
# 指定文本
demo@~$ weixin xxx text -text 起床了

# 从管道读取

demo@~$ echo "碎觉碎觉" | weixin xxx text
```