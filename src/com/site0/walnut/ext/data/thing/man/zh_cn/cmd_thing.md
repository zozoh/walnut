# 命令简介 

`thing` 用来管理数据，当然你也可以用更底层一些的 `obj`
但是很多时候用这个命令会更加方便

-------------------------------------------------------------
# 用法

```bash
thing [ThingSetID] ACTION [options] 
        [-json "{..}"] [- tmpl "TMPL"] [-N] [-Q]
    
- ThingSetID 「选」指代 ThingSet 的ID，默认为当前目录
- ACTION     默认为 "get"，具体支持的 Action 看后续列表
- options    根据不同的 ACTION 意义不同
    
# 下面是所有子命令都支持的参数:
    
-e      输出对象字段的时候，符合正则表达式的字段才会被输出，如果以 ! 开头，表示取反
    
-Q      强制不输出，比如 -check 的时候，可以不输出
    
-t      按表格输出，这个参数指定了表格的列
-i      按表格输出时，显示序号，默认不显示序号
-ibase  按表格输出时，显示序号起始值，默认为 0
-h      按表格输出时，显示表头
-b      按表格输出时，显示表格边框
-s      按表格输出时，在表格后显示脚注

-json   输出为 JSON，后面是详细的 JSON 格式化信息
-c      按json输出时，紧凑显示
-n      按json输出时，如果有 null 值的键也不忽略
-q      按json输出时，键值用双引号包裹
-l      按json输出时，强制输出成列表。默认的，多个结果才显示成列表
-H      按json输出时，也显示双下划线开头的隐藏字段

-V      仅输出值
-sep    值之间用什么分隔，默认用空字符串，即指之间紧凑显示
-N      每个对象之间换行
    
-tmpl   表示每个对象按照一个模板输出，上下文为 obj 本身
        模板占位符为 `@{xxx}` 的格式
```
       
 - 执行任何命令，默认是输出 JSON
 
 -------------------------------------------------------------
 # 子命令列表
 
```bash
thing init          # 初始化数据集
thing get           # 获取数据完整信息
thing getone        # 查询某条数据完整信息
thing create        # 创建数据
thing detail        # 获取数据详情
thing delete        # 删除数据
thing update        # 更新数据
thing query         # 查询数据
thing comment       # 操作数据的评论
thing clean         # 清理数据集回收站
thing file          # 操作数据的关联文件
thing media         # 操作数据的媒体，是 thing file -dir media 的快捷形式
thing attachment    # 操作数据的附件，是 thing file -dir attachment 的快捷形式
thing tmpfile       # 在数据集创建一个临时文件
thing duplicate     # 复制一个数据记录
```
