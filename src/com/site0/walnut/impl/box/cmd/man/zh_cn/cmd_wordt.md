# 命令简介

`wordt` 替换 Word 文档（.doc）中的占位符，输出新文档

按段落扫描 `${xxx}` 占位符并用变量集渲染。变量集来自 `-vars` 参数，
缺省读管道。

# 用法

```bash
wordt 模板.doc 输出.doc [-c] [-vars '{名值...}'] [-debug]
  -c       # 输出文件不存在时自动创建（否则抛错）
  -vars    # 占位符变量集；缺省或 -vars true 时读管道
  -debug   # 显示替换过程
```

# 示例

```bash
# 变量从管道读
echo '{name:"xiaobai",date:"2026-10-23"}' | wordt tmpl.doc out.doc -c

# 直接指定变量
wordt tmpl.doc out.doc -vars '{name:"xiaobai"}' -c

# debug 模式
wordt tmpl.doc out.doc -vars '{name:"xiaobai"}' -debug
```
