# 过滤器简介

`@gen_mapping` 根据表单或者表格的字段设定，生成映射配置。

> 关于映射配置，请参见 `WnBeanMapping`

# 用法

```bash
ti @gen_mapping [export|import] [-f 路径] [-get 键] [-as form|table] [-fldt 类型] [-dicts 路径] [-white a,b,c] [-black a,b,c] [-i18n 目录] [-lang 语言] [-cqn]
       # export|import    # 输出映射还是输入映射，默认 export
  -f        # 指明输入文件，如果没有则从标准输入读取内容
  -get      # 输入的表单字段不是数组，需要取得某个键的值（如 a.b）
  -as       # 输入内容是表单还是表格的字段设定，默认 form
  -fldt     # 强制字段类型
  -dicts    # 读取字段选项字典的方法；只有声明则尝试从标准输入读取
            # 字典的内容，格式为 config.dictionary 段的值
  -white    # 表单字段白名单
  -black    # 表单字段黑名单
  -i18n     # i18n 目录位置（其内第一层是各个语言目录），默认 /rs/ti/i18n/，多个路径用半角冒号分隔
  -lang     # 要输出的语言（会统一转换为 kebabCase，zhCn/zh_cn/zh-cn 等价），默认 zh-cn
  -cqn      # JSON 输出的格式化开关
```

# 示例

```bash
# 输出导出数据映射
ti @config | jsonx @get dictionary | ti @gen_mapping export -f meta-fields.json -dicts -qn

# 输出导入数据映射
ti @config | jsonx @get dictionary | ti @gen_mapping import -f meta-fields.json -dicts -qn
```
