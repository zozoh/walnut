# 过滤器简介

`fake @text` 生成随机字符串文本

# 用法

```bash
fake [N] @text 
    [$MIN-$MAX]     # 词数范围，或最小词数
    [$N]            # 固定词数
    [-lang zh_cn]   # 语言种类，默认 zh_cn
```

# 示例

```bash
# 输出三个30-50长度的文本
demo> fake 3 @text 30 50 -lang en_us
governmentalism kainogenesis lipohemia danava
unprogressively uterovesical linville tand
collogen dusa puritano peroratorical emi 
```
