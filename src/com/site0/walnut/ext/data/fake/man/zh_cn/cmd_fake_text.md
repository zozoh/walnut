# 过滤器简介

`@text` 生成随机字符串文本。

# 用法

```bash
fake [个数] @text [词数范围|最小词数] [最大词数] [-lang 语言]
  # 个数          # 生成多少个，默认 1
  # 词数范围      # 形如 30-50；一个值时表示固定词数
  # 最大词数      # 两个值时表示词数范围
  -lang          # 语言种类，默认 zh_cn
```

# 示例

```bash
# 输出三个 30-50 个词的英文文本
fake 3 @text 30 50 -lang en_us
# governmentalism kainogenesis lipohemia danava
# unprogressively uterovesical linville tand
# collogen dusa puritano peroratorical emi
```
