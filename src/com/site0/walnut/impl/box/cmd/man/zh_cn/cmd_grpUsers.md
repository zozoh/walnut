# 命令简介 

    `grp-users` 查询一个组有哪些用户

# 用法

    grp-users [grp] 
              [-e $REGEX] 
              [-t fld0,fld1..]
              [-iocnqhbslVNPH]
              [-ibase 1 or 0]
              [-pager]
              [-match {..}]
    
    [grp]  表示组名，如果没有输入，表示当前用户的主组。
    
    -limit  一页显示多少数据，默认全部数据
    -skip   分页显示时，跳过多少数据
    -pager  显示分页信息，如果是 JSON 输出，则将对象显示成 {list:[..],pager:{..}} 格式
            在指定路径模式下，本参数无效
            在 limit 小于等于 0 时，本参数依然无效
            
    -match  对输出的过滤
    
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹
    
    -t      按表格输出，这个参数指定了表格的列
    -i      按表格输出时，显示序号，默认不显示序号
    -ibase  按表格输出时，显示序号起始值，默认为 0
    -h      按表格输出时，显示表头
    -b      按表格输出时，显示表格边框
    -s      按表格输出时，在表格后显示脚注
    
    -V      仅输出值
    -sep    值之间用什么分隔，默认用空字符串，即指之间紧凑显示
    -N      每个对象之间是否换行
    
    -H      输出的时候，也显示双下划线开头的隐藏字段，仅对 JSON 有效
    
    
# 示例

    # 显示当前组所有的用户
    demo@~$ grp-users -t nm
    root
    xiaobai
    
    # 显示指定组所有的用户
    demo@~$ grp-users xiaobai -t nm
    xiaobai
    
    
    