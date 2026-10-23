# 过滤器简介

`@sentence` 生成随机的句子。

# 用法

```bash
fake [个数] @sentence [词数范围|最小词数] [最大词数] [-lang 语言]
  # 个数          # 生成多少个，默认 1
  # 词数范围      # 形如 3-5；一个值时表示固定词数
  # 最大词数      # 两个值时表示词数范围
  -lang          # 语言种类，默认 zh_cn
```

# 示例

```bash
# 输出三个 3-5 个单词的英文句子
fake 3 @sentence 3 5 -lang en_us
# Jophiel kant hmos crosshead.
# Sodalites gomar laparogastrotomy.
# Eb samsun chrysophyllum neufchtel.
```
