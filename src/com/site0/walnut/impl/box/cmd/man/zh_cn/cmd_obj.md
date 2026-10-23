# 命令简介

`obj` 显示或修改对象的元数据

> **本命令已弃用，由 `o` 命令取代**。新代码请使用 `o`（上下文 + 过滤器
> 模式），本命令仅作维护兼容保留。

支持显示、创建（`-new`）、更新（`-u`/`-push`/`-pop`/`-set`）、
查询（`-match`）、树展开（`-tree`）、祖先链（`-an`）、分组计数
（`-GroupCount`）等操作。

# 用法

```bash
obj [id:xxx|路径] [-new JSON] [-u JSON] [-match {..}] [其他开关...]
  -new          # 创建对象（无 race 默认 FILE）
  -u            # 更新字段（JSON，可不包 {}；值 null 表示删除字段）
  -e 正则       # 只输出匹配的字段，! 开头取反
  -match {..}   # 按条件查询对象
  -sort '{..}'  # 排序
  -limit/-skip/-pager   # 分页
  -push/-pop/-set       # 数组字段追加/弹出/Map 融合
  -tree/-treeBy/-treeDepth/-treeFlat    # 树展开
  -an noself|noroot|nodes|full  # 祖先链模式
  -noexists null|ignore         # 对象不存在时不抛错
  -o/-Q         # 强制输出 / 强制不输出
  -t 列...      # 表格输出
  -cqn/l/H/V/N  # JSON 输出格式开关
  -tmpl 模板    # 模板输出（@{xxx} 占位符）
```

# 示例

```bash
# 显示对象
obj ~/tmp/my.txt
obj id:9rCTtaKaGX9eLinKeLD5k0

# 更新字段
obj ~/tmp/my.txt -u "width:100, height:99"

# 删除字段
obj ~/tmp/my.txt -u "width:null"

# 只显示匹配字段
obj ~/tmp/my.txt -e "^http-.*$"

# 创建对象
obj -new "nm:abc, race:FILE"

# 查询（-match 模式，可加 -sort/-limit/-skip/-pager）
obj -match "{tp:'jpg'}" -sort '{ct:-1}' -limit 10 -l
```
