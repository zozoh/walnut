# 命令简介 

    `obj` 将显示或修改对象的元数据

# 用法

    obj [id:ea81..|/path/to/obj]
        [-new $JSON] 
        [-u $JSON] 
        [-e $REGEX] 
        [-t fld0,fld1..]
        [-iocnqhbslVNPH]
        [-ibase 1 or 0]
        [-push "key:value"]
        [-push_uniq false]
        [-pager]
        [-limit 1]
        [-skip 0]
        [-match {..}]
        [-mine]
        [-GrpupCount $Tmpl]
        [-noexists null|ignore]
        [-ExtendBy {..}]
        [-ExtendFilter {..}]
        [-ExtendDeeply]
        [-tree {...}]
        [-treeBy {..}]
        [-treeDepth 1]
        [-an noself|noroot|nodes]
        [-anuntil {..}]
        [-hide]
    
    -new    创建一个新的对象，内容是一个 JSON 字段，可以不用 "{}" 包裹，其中:
             - 即使里面包括 "id,ct,lm" 等标准字段也是无效的
             - 如果没有 "nm" 字段，将用 "${id}" 来代替
             - 你可以添加自己任意复杂的自定义字段
             - 如果指定了路径，那么 "pid" 会被无视
             - 如果没指定路径，也没有 "pid" 那么，则在当前路径下创建
             - 可以和 "-u" 参数联用，表示创建后再更新
             - 如果没有指明 "race" 则默认认为是 "FILE"
             
    -IfNoExists  仅仅当 -new 模式下有用，表示如果不存在才创建，大小写敏感
    
    -u      用一个 JSON 表示要更新对象的哪些字段，JSON 字符串可以不用 "{}" 包裹
    
    -e      输出对象字段的时候，符合正则表达式的字段才会被输出，如果以 ! 开头，表示取反
    
    -o      强制输出，默认 -u 和 -new 的时候不输出，默认的输出方式为 json，除非指定了 -t
    -Q      强制不输出，比如 -check 的时候，可以不输出
    -P      每个对象都强制获得全路径，默认只有一个对象时才会这样
    -A      强制输出对象所有的祖先，这个只有当一个对象，或者 -P 的时候才会生效
    
    -noexists  如果根据路径指定的对象不存在，默认会抛出异常，声明这个参数则表示
               不要抛出异常，null 表示用 null 来代替， ignore 表示忽略
    
    -ExtendFilter   一个Map，匹配上的DIR项目，将会被展开子，展开子将遵守 -sort 设定，但是会无视 -limit 和 -skip
    -ExtendBy       展开子的条件，默认是全部子
    -ExtendDeeply   展开子的时候是否递归
    
    -tree           一个Map，匹配上的DIR项目，将会被展开子，展开子将遵守 -sort 设定，但是会无视 -limit 和 -skip
    -treeBy         展开子的条件，默认是全部子
    -treeDepth      展开深度，默认为1，如果为 0 表示无限展开
    -treeFlat       将会将 tree 项目抹平为一个一级数组 
    
    -an         找到的一个数组顺序为从根至自己: [根] [节点1] [节点2] [自己]
                 - noself 则表示返回: [根] [节点1] [节点2]
                 - noroot 则表示返回: [节点1] [节点2] [自己]
                 - nodes  则表示返回: [节点1] [节点2]
                 - full   则表示返回: [根] [节点1] [节点2] [自己]
                                                默认为 noself
    -anuntil    值为一个 Map，表示符合这个条件的节点就是根了。否则会一直寻找到 /xxx 目录
    
    
    -limit  一页显示多少数据，默认全部数据
    -skip   分页显示时，跳过多少数据
    -pager  显示分页信息，如果是 JSON 输出，则将对象显示成 {list:[..],pager:{..}} 格式
            在指定路径模式下，本参数无效
            在 limit 小于等于 0 时，本参数依然无效
            
    -match  对输出的对象过滤, 在指定路径模式下，相当于为本参数增加了 pid 选项
    
    -mine   在 -match 条件下，为条件添加 d0:"home", d1:"主组" 两条约束
    
    -push   将名值对中的内容追加到对象对应字段中，确保会生成数组形式的值
    -push_uniq  当进行 push 操作的时候，是否确保已有的内容就不追加了, 默认 true
    
    -pop    将名值对中的内容从对象对应字段中删除，如果最后成为空数组，会置 null
            格式类似  key1:"val1", key2:"val2"
            val 的格式是
             - "i:3"  表示 0 base下标，即第四个
             - "i:-1" 表示最后一个
             - "i:-2" 表示倒数第二个
             - "n:3"  表示从后面弹出最多三个
             - "n:-1" 表示从开始处弹出最多一个
             - "v:xyz"  表示弹出内容为 'xyz' 的项目
             - "!v:xyz" 表示弹出内容不为 'xyz' 的项目
             - "l:a,b"  表示弹出半角逗号分隔的列表里的值
             - "!l:a,b" 表示弹出不在半角逗号分隔的列表里的值
             - "e:^a.*" 表示弹出被正则表达式匹配的项目
             - "!e:^a.*" 表示弹出没有被正则表达式匹配的项目
             - ""    表示删除全部空数据项目
             - null  表示清空全部数据

    -set    为指定字段设置新值。如果字段是一个Map，则会融合，否则替换
            格式类似 key1:{...}, key2:{...}
            给入的文档，如果值为 null 则表示移除
    
             
    -GroupCount 参数是一个字符串模板，根据这个模板生成的键，并计入结果 Map，值就是计数
                GroupCount是一个实验性参数，它就是在查询的基础上做的进一步归纳，所以会比较慢
                一旦声明了这个参数，所有对输出的过滤都不生效了，会直接打印出 JSON 
                当然，对于 JSON 的格式控制还是生效的
    
    -c      按json输出时，紧凑显示
    -n      按json输出时，如果有 null 值的键也不忽略
    -q      按json输出时，键值用双引号包裹
    -l      按json输出时，强制输出成列表。默认的，多个结果才显示成列表
    -H      按json输出时，也显示双下划线开头的隐藏字段
    
    -t      按表格输出，这个参数指定了表格的列
    -i      按表格输出时，显示序号，默认不显示序号
    -ibase  按表格输出时，显示序号起始值，默认为 0
    -h      按表格输出时，显示表头
    -b      按表格输出时，显示表格边框
    -s      按表格输出时，在表格后显示脚注
    
    -V      仅输出值
    -sep    值之间用什么分隔，默认用空字符串，即指之间紧凑显示
    -N      每个对象之间换行
        
    -tmpl   表示每个对象按照一个模板输出，上下文为 obj 本身
            模板占位符为 `@{xxx}` 的格式
    
    -hide   表示不输出所有的隐藏文件
    
# 示例

    显示当前对象 (PWD对应的)
    demo@~$ obj .
    
    根据ID显示对象信息
    demo@~$ obj id:9rCTtaKaGX9eLinKeLD5k0
    
    根据路径显示对象信息
    demo@~$ obj tmp/my.txt
    
    更新某个对象的某几个值
    demo@~$ obj tmp/my.txt  -u "width:100, height:99"
    
    根据ID更新某个对象的某几个值
    demo@~$ obj -id 9rCTtaKaGX9eLinKeLD5k0 -u "{width:100, height:99}"
    
    删除一个对象的属性
    demo@~$ obj tmp/my.txt -u "width:null"
    
    显示对象的某几个属性。 如果只有一个对象的一个属性被显示，则仅输出属性值
    demo@~$ obj tmp/my.txt -e "^http-.*$"
    
    设置日期属性
    demo@~$ obj abc.txt -u "someday:'$date:2015-09-21 12:34:24.444'"
    
    设置日期属性到当前时间
    demo@~$ obj abc.txt -u "someday:'$date:now'"
    
    设置绝对毫秒值
    demo@~$ obj abc.txt -u "lm:'$ms:1436888941391'"
    
    设置绝对毫秒值到当前时间
    demo@~$ obj abc.txt -u "lm:'$ms:now'"
    
    根据创建者和所属组归纳
    demo@~$ obj * -GroupCount "${c}_${g}"
    {
        demo_demo : 89,
        usr1_demo : 33,
        usr2_demo : 4
    }
    
    为对象增加数组字段的值
    demo@~$ obj abc.txt -push "x:100"; obj abc.txt -push "x:100" -o
    {
        ..
        x: [100]
        ..
    }
    
    为对象增加数组字段的值即使重复也追加
    demo@~$ obj abc.txt -push "x:100"; obj abc.txt -push "x:100" -push_uniq false -o
    {
        ..
        x: [100, 100]
        ..
    }
    
    弹出对象的某个字段的某个值
    demo@~$ obj abc.txt -push "x:['A','B','C']"; obj abc.txt -pop "x:'n:1'" -o
    {
        ..
        x: ['A','B']
        ..
    }
    
    设置某个字段某个内置Map的值
    demo@~$ obj abc.txt -u "pos:{x:10}"; obj abc.txt -set "pos:{y:88}" -o
    {
        ..
        pos : {x:10, y:88}
        ..
    }
    
    删除某个字段某个内置Map的值
    demo@~$ obj abc.txt -u "pos:{x:10,y:88}"; obj abc.txt -set "pos:{y:null}" -o
    {
        ..
        pos : {x:10}
        ..
    }
    
    
    
    
