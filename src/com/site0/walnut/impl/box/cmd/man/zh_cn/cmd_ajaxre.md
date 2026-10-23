# 命令简介

`ajaxre` 将给定数据格式化成 AjaxReturn 格式输出

输出结构：成功为 `{ok:true, data:...}`；失败为 `{ok:false, errCode, msg, data}`。
输入若是 `e.xxx.yyy : 原因` 形式的错误串会自动识别为失败，并通过 i18n
服务补全多语言错误消息。

# 用法

```bash
ajaxre [-e] [-s] [-cqn] [-lang 语言] [数据]
  -e      # 强制将输入当做错误信息
  -s      # 数据按字符串处理（默认尝试解析成 JSON）
  -cqn    # JSON 格式开关：-c 紧凑、-q 键名加引号、-n 输出 null 值
  -lang   # 错误消息的语言（默认取当前会话语言）
数据      # 要输出的数据，缺省读管道；非 JSON 则按字符串处理
```

# 示例

```bash
# 输出格式化后的数组
ajaxre '[1,2,3]'

# 格式化错误信息
echo 'e.test.err : some text' | ajaxre

# 强制当做错误
ajaxre -e 'e.io.noexist : 文件不存在'
```
