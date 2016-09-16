# 命令简介 

    `thing` 用来管理数据，当然你也可以用更底层一些的 `obj`
    很多时候用这个命令会更加方便

# 用法

    thing [TsID[/ThID]] 
           ACTION [options] 
           [-json "{..}] [-tmpl "TMPL"] [-N] [-Q]
    
    - TsID[/ThID] 「选」获取参考对象，默认为当前目录
                   ThingSetID 与 ThingID 用 / 分隔
                   没有后面的部分代表指定一个 ThingSet
                   全部声明，则表示一个 Thing
    - ACTION     可以是  get|init|create|detail|delete|update|query|comment|clean
                 默认为 "get"
    - options    根据不同的 ACTION 意义不同
    
    下面是所有子命令都支持的参数:
    
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
        
    > thing 命令会将第一个参数对应的 WnObj 临时设置为当前会话的 PWD
      就是说，如果不指定第一个参数，那么当前的目录就作为所在的 ThingSet 或者 Thing
    > 执行任何命令，默认是输出 JSON

# thing get
    
    #----------------------------------------------------
    # 命令格式
    thing [ID] [get]
    #----------------------------------------------------
    # 得到一个 thing 的详细信息 
    thing [ID]
    
    # 打印某个 thing 的全部 JSON 信息
    thing [ID] get -json 
    
    # 打印某个 thing 的名称和所属者，且不输出换行符
    thing [ID] get -out '@{th_ow} belong to @{th_name}' -N

# thing init

    #----------------------------------------------------
    # 命令格式
    thing [ID] init 
            [-Q] 不输出日志
            [-f] 强制重写 thing.json
    #----------------------------------------------------
    - 初始化一个 ThingSet，当前的目录必须是个 ThingSet，否则跑错
    - 命令会在目录下建立 data 等关键目录，以及 thing.json
   
# thing create

    #----------------------------------------------------
    # 命令格式
    thing [ID] create [th_nm] [th_brief] [-cate th_cate] [-fields "{..}"]
    #----------------------------------------------------
     - 当前对象可以是一个 thing 或者 ThingSet
     - 如果是一个 thing，相当于是它的 ThingSet
     
    # 创建一个名为 ABC 的 thing
    thing xxx create ABC
    
    # 创建一个名为 ABC 且有一个简要说明的 thing
    thing xxx create 'ABC' 'This is abc'
    # or
    thing xxx create ABC -fields "{th_brief:'This is abc'}"
    # or
    thing xxx create -fields "{th_nm:'ABC', th_brief:'This is abc'}"
    
    # 创建一个匿名的 Thing 并指明分类
    thing xxx create -cate xxx

# thing detail

    #----------------------------------------------------
    # 命令格式
    thing [ID] detail 
                [-content "xxxxx"] 
                [-tp "md|txt|html"] [-drop] [-quiet]
    #----------------------------------------------------
    - 内容，支持从管道读取
    - 默认 tp 为 txt
    
    # 显示 thing 的 detail，如果没有，则抛错
    thing xxx detail
    
    # 显示 thing 的 detail，如果没有，则什么都不输出
    thing xxx detail -quiet
    
    # 为 thing 修改详细内容
    thing xxx detail -content "哈哈哈"
    
    # 为 thing 修改详细内容 HTML
    thing xxx detail -content "<b>哈哈哈</b>" -tp "html"
    
    # 为 thing 删除详细内容
    thing xxx detail -drop
    
# thing media

    #----------------------------------------------------
    # 命令格式
    thing [ID] media 
                [-add xxx.jpg]
                [-overwrite]
                [-dupp '@{major}(@{nb})@{suffix}']
                [-read]
    #----------------------------------------------------
    # 列出一个 thing 所有的媒体文件，空的返回空数组 []
    thing xxx media
    
    # 添加一个空图片，如果已存在则抛错
    thing xxx media -add abc.jpg
    
    # 添加一个空图片，如果已存在则返回
    thing xxx media -add abc.jpg -overwrite
    
    # 添加一个空图片，如果已存在则根据模板创建新的
    thing xxx media -add abc.jpg -dupp
    
    # 添加一个图片，内容来自另外一个文件，如果已存在则抛错
    # 如果想不抛错，参见上面创建空文件的例子，根据需要添加
    # -overwrite 或者 -dupp 参数
    thing xxx media -add abc.jpg -read id:45vff..
    
    # 添加一个图片，内容来自标准输入，如果已存在则抛错
    cat abc.jpg | thing xxx media -add abc.jpg -read  

# thing attachment

    #----------------------------------------------------
    # 命令格式
    thing [ID] attachment 
                [-add xyz.zip]
                [-read]
    #----------------------------------------------------
    # 列出一个 thing 所有的附件，空的返回空数组 []
    thing xxx attachment
    
    # 添加一个空文件，如果已存在则抛错
    thing xxx attachment -add xyz.zip
    
    # 添加一个空文件，如果已存在则返回
    thing xxx attachment -add xyz.zip -overwrite
    
    # 添加一个空文件，如果已存在则根据模板创建新的
    thing xxx attachment -add xyz.zip -dupp
    
    # 添加一个附件，内容来自另外一个文件，如果已存在则抛错
    # 如果想不抛错，参见上面创建空文件的例子，根据需要添加
    # -overwrite 或者 -dupp 参数
    thing xxx attachment -add xyz.zip -read id:gf98..
    
    # 添加一个附件，内容来自标准输入，如果已存在则抛错
    cat my.zip | thing xxx attachment -add xyz.zip -read  
    
# thing delete
    
    #----------------------------------------------------
    # 命令格式
    thing [ID] delete [-quiet]
    #----------------------------------------------------
     - 当前对象必须是一个 thing，否则不能删除
     - 已经删除的，再次删除会抛错，除非 -quiet
     - 所谓删除其实就是标记 th_live = -1

# thing restore
    
    #----------------------------------------------------
    # 命令格式
    thing [ID] restore [-quiet]
    #----------------------------------------------------
     - 当前对象必须是一个 thing，否则不能恢复
     - 已经恢复的，再次恢复会抛错，除非 -quiet
     - 所谓恢复其实就是标记 th_live = 1

# thing clean

    #----------------------------------------------------
    # 命令格式
    thing [ID] clean [-limit 0]
    #----------------------------------------------------
    - 当前对象可以是一个 thing 或者 ThingSet
    - 如果是一个 thing，相当于是它的 ThingSet
    - 将真正执行 `rm`，所有的 th_live = -1 的都会被清除
    - limit 参数将限制清除的个数
    - 清除的顺序为最后修改时间从旧到新


# thing update

    #----------------------------------------------------
    # 命令格式
    thing [ID] update ["$th_nm"] 
                       [-brief "xxx"]
                       [-ow "xxx"]
                       [-cate CateID]
                       [-fields "{..}"]
    #----------------------------------------------------
     - 当前对象必须是一个 thing，否则不能更新
     - fields 里面的值，没有 -brief|ow|cate 优先
    
    # 改名
    thing xxx update "原力觉醒电影票"
    
    # 修改简介
    thing xxx update -brief "会员半价"
    
    # 修改更多的信息
    thing xxx update -fields "x:100,y:99"

     
# thing query

    #----------------------------------------------------
    # 命令格式
    thing [ThingSet ID] query 
                      [Condition Map]
                      [-t "c0,c1,c2.."]
                      [-pager]
                      [-limit 10]
                      [-skip 0]
                      [-sort "nm:1"]
    #----------------------------------------------------
     - 当前对象可以是一个 thing 或者 ThingSet
     - 如果是一个 thing，相当于是它的 ThingSet
     - 查询条件如果不包括 th_live，那么默认将设置为 th_live=1 表示所有可用的 thing
     - t 表示按照表格方式输出，是 query 的专有形式，内容就是半角逗号分隔的列名
     - pager  显示分页信息，如果是 JSON 输出，则将对象显示成 {list:[..],pager:{..}} 格式
        - 在 limit 小于等于 0 时，本参数依然无效
     - limit  限制输出的数量，默认 100
     - skip   跳过的对象数量，默认 0
     - sort 排序字段
     
# thing comment

    #----------------------------------------------------
    # 命令格式
    thing [ID] comment [-add xxx]
                       [-del xxx]
                       [-get xxx]
                       [-read xxx]
                       [-quick]
                       [commentID content]
                       [-tp txt|html|md]
                       [-maxsz 256]
                       [-minsz 5]
    #----------------------------------------------------
    - 注释内容，支持从管道读取
    - 支持 'sort|pager|limit|skip|json|out|t' 等参数
    - maxsz 默认 256 表示，超长的字符串，会被截取为 breif，完整内容将存在文件里
    - minsz 默认 5 表示评论最小长度
    
    # 添加注释，会自动修改 task.th_c_cmt 字段 
    thing xxx comment -add "搞定了，呼"
    
    # 添加 makrkdown 注释
    thing xxx comment -add "<b>哈哈</b>" -tp html
    
    # 修改注释
    thing xxx comment 20150721132134321 "修改一下注释"
    
    # 修改注释为 markdown
    thing xxx comment 20150721132134321 "修改一下注释" -tp md
    
    # 删除注释，会自动修改 task.th_c_cmt 字段 
    thing xxx comment -del 20150721132134321
    
    # 获取全部注释, 如果注释内容过长(有 breif 字段)，主动读取 content
    thing xxx comment
    
    # 获取全部注释, 如果注释内容过长也不主动读取
    thing xxx comment -quick
    
    # 获取最多 100 个注释，并显示翻页信息
    thing xxx comment -limit 100 -skip 0 -pager
    
    # 获取某个注释全部属性(自动确保读取 content)
    thing xxx comment -get 20150721132134321
    
    # 获取某个注释的内容文本（仅仅是内容文本，不是 breif)
    thing xxx comment -read 20150721132134321
    




