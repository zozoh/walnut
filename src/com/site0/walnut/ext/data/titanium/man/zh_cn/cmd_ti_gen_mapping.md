# 命令简介 

`ti gen_mapping` 根据表单或者表格的字段设定，生成映射配置。

> 关于映射配置，请参见 `WnBeanMapping`

# 用法
 
```bash
ti gen_mapping
  [export|import]     # [选] 是输出映射还是输入映射
                      # 默认为 `export`
  [-f /path/to]       # [选]指明输入文件，如果没有则从标准输入读取内容
  [-get a.b]          # 输入的表单字段不是数组，需要取得某个键的值
  [-as form|table]    # [选]输入内容是表单还是表格的字段设定
                      # 默认未 `form`
  [-dicts /path/to]   # 读取字段选项字典的方法，如果只有声明，则尝试从标准输入读取
                      # 字典的内容。字典格式为 config.dictionary 段的值
  [-white a,b,c]      # [选]表单字段，可以指定默认字段白名单
  [-black a,b,c]      # [选]表单字段，可以指定默认字段黑名单
  [-load /rs/ti/i18n] # i18n 目录的位置，其内第一层是各个语言目录，
                      # 名称如"zh-cn"，默认 "/rs/ti/i18n/"
                      # 多个路径用半角冒号分隔
  [-lang zh-cn]       # 要输出的语言（会统一转换为kebabCase）
                      # 因此 zhCn, zh_cn, zh-cn 是等价的
  [-cqn]              # [选]指定JSON输出格式
```
-------------------------------------------------------------
# 示例

```bash
# 输出导入数据映射部
demo:~$  ti config | jsonx @get dictionary | ti gen_mapping export -f meta-fields.json -dicts -qn

# 输出导出数据映射部
demo:~$  ti config | jsonx @get dictionary | ti gen_mapping import -f meta-fields.json -dicts -qn
```