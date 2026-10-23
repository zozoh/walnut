# 过滤器简介

`@i18n` 将输入的信息做多国语言翻译。

# 用法

```bash
ti @i18n [输入] [-lang 语言] [-i18n 目录] [-json] [-cqn]
  # 输入    # 文本、数组或对象；不给则从标准输入读取
  -lang    # 要输出的语言（统一转换为 kebabCase，zhCn/zh_cn/zh-cn 等价），默认 zh-cn
  -i18n    # i18n 目录位置，其内第一层是各语言目录；默认 /rs/ti/i18n/，多个路径用半角冒号分隔
  -json    # 强制采用 JSON 方式输出
  -cqn     # JSON 输出的格式化开关
```

# 示例

```bash
# 翻译一段文本
ti @i18n 'hello' -lang en-us

# 从管道翻译
echo '["hello","world"]' | ti @i18n -lang en-us
```
