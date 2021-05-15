# 过滤器简介

`fake @sentence` 生成随机的句子

# 用法

```bash
fake [N] @sentence 
    [$MIN-$MAX]     # 词数范围，或最小词数
    [$MAX]          # 最大词数
    [-lang zh_cn]   # 语言种类，默认 zh_cn
```

# 示例

```bash
# 输出三个3-5个单词的英文句子
demo> fake 3 @sentence 3 5 -lang en_us
Jophiel kant hmos crosshead. 
Sodalites gomar laparogastrotomy. 
Eb samsun chrysophyllum neufchtel. 
```
