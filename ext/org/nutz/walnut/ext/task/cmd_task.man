# 创建一个任务
#  - 如果 `task add xxx` 则表示创建顶级任务
#  - 如果 `task 45cd.. add xxx` 表示创建子任务
#  - 如果创建子任务，没有指定 -prev|-next 的时候，默认附加在末尾
#  - 当然，如果 task add xxx -prev cde3.. ，创建的任务和 `cde3..`平级
task [3acd..] add '任务标题' 
            [-prev 'fd21..']     # 新任务应该插在什么任务之前
            [-next '996e..']     # 新任务应该插在什么任务之后
            [-lbls "标签A,标签B"]
            [-id 'ddc0..']       # 为新任务指定 ID
            [-ow      "zozoh"]   # 指定任务分配账号

# 将任务移到某任务后面
#                  [X]
#   prev [A]       [Y]
#        [B] -----> after Y 
#   next [C]       [Z]
#
# 改动:
#    A.next = B.next
#    C.prev = B.prev
#    B.prev = Y
#    B.next = Y.next
#    Y.next = B
#    Z.prev = B
#
task 3acd.. after ee13..

# 将任务移到某任务前面
#
#   prev [A]       [X]
#        [B] -----> before Y 
#   next [C]       [Y]
#                  [Z]
# 改动:
#    A.next = B.next
#    C.prev = B.prev
#    B.prev = Y.prev
#    B.next = Y
#    X.next = B
#    Y.prev = B
#
task 3acd.. before ee13..

# 删除一个任务
#  - 如果不注明 -r，那么如果任务还有子任务，则会被拒绝删除
task rm -r '任务AID' '任务BID'

# 修改一个任务
#  - 至少要有一项
#  - 否则抛错
task 3acd.. update '任务标题'
        -lbls    "标签A,标签B" 
        -status  "DONE|NEW" 
        -ow      "zozoh"
        -d_start "2015-07-21 13:33:32"
        -d_stop  "2015-07-21 14:33:32"
        -du      3600000
        -done    "2015..004"
        -verify  "2015..781"

# 检索当前组的所有根任务
task query

# 检索子任务
#  - 检索出的任务会自动根据前置关系排队
task query -pid '父任务ID' -order logic

# 自由检索任务
#  - 标题关键字，如果以 ^ 开头，则表示正则表达式
#  - 如果声明了 pid，则排序一定是 logic 的
#  - 如果是列表， `|`分隔表示`或`， `,`分隔表示`与`
task query '标题关键字'
        -json    # 按json输出，便于解析，默认按行输出
        -G       # 任务所在组，即 others/$grp，默认为空表示查询 mine 下的任务
        -pid     "父任务ID"
        -lbls    "标签A,标签B" 
        -status  "DONE,NEW" 
        -lv      0
        -ow      "zozoh,xiaobai"
        -c       "zozoh"
        -ct      "(2014-09-21, 2015-07-21 13:33:32)"
        -lm      "(2014-09-21, 2015-07-21 13:33:32]"
        -d_start "[,2015-07-21 13:33:32)"
        -d_stop  "[2014-09-21,]"
        -du      "[,80000]"
        -done    true
        -verify  false
        -order   "asc:tt,desc:ct"
        -limit   20 | 0
        -skip    100

# 列出某任务下所有的子任务，成为一个任务树
task 3acd.. tree

# 添加任务注释，会自动修改 task.cmtnb 字段 
task 3acd.. comment add "搞定了，呼" 

# 修改任务注释
task 3acd.. comment 20150721132134321 "修改一下注释"

# 删除任务注释，会自动修改 task.cmtnb 字段 
task 3acd.. comment del 20150721132134321