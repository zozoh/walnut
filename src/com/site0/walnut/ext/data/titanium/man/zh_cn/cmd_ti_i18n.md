# 命令简介 

`ti i18n` 将输入的信息做多国语言翻译

-------------------------------------------------------------
# 用法
 
```bash
ti i18n
  [Text|Array|Object]   # 输入，如果没有输入，将从标准输入读取
  [-lang zh-cn]         # 要输出的语言（会统一转换为kebabCase）
                        # 因此 zhCn, zh_cn, zh-cn 是等价的
  [-load /rs/ti/i18n]   # i18n 目录的位置，其内第一层是各个语言目录，
                        # 名称如"zh-cn"，默认 "/rs/ti/i18n/"
                        # 多个路径用半角冒号分隔
  [-json]               # 强制采用 JSON 的方式输出
  [-cqn]                # [选]如果要输出 json 格式化方式
```

-------------------------------------------------------------
# 示例

```bash
# 根据对象得到可以创建的子对象类型列表
demo:~$ ti config
```