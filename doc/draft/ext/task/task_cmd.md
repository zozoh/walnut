---
title:task命令
author:zozoh
tags:
- 系统
- 扩展
- task
---

# task 命令概述

系统提供 *task* 命令，针对下面的场景

1. 用户的 TODO List
2. 机器人自动执行任务（单次或者周期性）
3. 多人协同
4. 工作流
5. OKR 管理
6. 缺陷管理
7. 测试用例管理（配合机器人可以做到自动测试）

*task* 命令的策略是

1. 数据存放在每个用户自己的组
2. 如果需要协同，采用链接的方式
3. 每个任务就是一个文件夹，里面可以存放任务相关的数据
4. 任务的子任务等，是通过元数据逻辑关联的，在树上不体现
5. 因此逻辑上，一个任务可以是多个任务的子
6. *task* 命令将负责对这些数据进行操作

# task 的数据模型

## task 目录结构

```
~/.task                     # 每个组一个专有的 task 目录
    tags                    # 存放所有的可用标签
        紧急                 # 标签文件根据元数据可以是普通标签
        注册模块              # 也可以表示一个线程
        第一版上线            # 或者一个里程碑
    root                    # 所有任务的根任务
        7ca9..              # 每个任务就是一个目录，mine 下的是根任务
            detail          # 任务更多详细的说明
                detail.md   # 可以用一份 markdown 文件描述细节
                history.txt # 任务相关的操作历史，一行一个，比如
                            # 20150712152213 zozoh  assign  wendal
                            # 20150712152419 wendal comment 20150712..
                            # 20150712152419 wendal rmcmt   20150712..
                            # 20150712182950 wendal detail  ca3d4..
            attachment      # 附件存放的目录
                45c..89.png # 用户可以上传任何文件作为附件
            comments        # 保存本任务所有的评论
                20150721132134321  # 一个精确到毫秒的时间戳排列任务的注释
                ...
            6bc1..          # 每个子任务也是一个目录
            c9da..          # 子任务顺序，但是会根据任务的前置顺序排列
        c82e..              # 下一个根任务
        45ca.. -> d89c..    # 可以是一个链接，链接到其他组的任务
        cdae.. -> a341..    # 如果对方元数据更新，本对象也会随着更新，以便查找
    
```

## task 的元数据

```
{
    id       : "4cda..",    # 任务的ID
    pid      : "89ca..",    # 父任务或所属分类的ID
    nm       : "$id",       # 任务的名称与 ID 相同
    tp       : "task",      # 任务是特殊类型的目录
    ow       : "xiaobai",   # 任务的所有者
    lv       : 0,           # 任务等级, 0:当前要处理 1:稍后处理 2:归档封存
    lbls     : [..],        # 任务的标签
    title    : "xxxxx",     # 任务的标题
    tzone    : "GMT+8:00",  # 任务所在时区
    d_start  : 1432..,      # 任务被接受的时间（绝对毫秒）
    d_stop   : 1432..,      # 任务处理完成的时间（绝对毫秒）
    du       : 43253,       # 根据 history.txt，任务总共消耗的有效工作时间(毫秒)   
    status   : "NEW",       # 任务的状态: NEW|ACCEPT|ING|PAUSE|DONE|REOPEN
    done     : "2015..004", # 任务被完成的注释，没有这个字段的 DONE 任务，表示无需完成
    verify   : "2015..312", # 任务被审核的注释，有这个字段的任务才表示审核通过
    cmtnb    : 0,           # 任务的注释数量
    #......................................................................
    # 任务的顺序，由 next 和 prev 表示的一个双向链表决定
    # 显然，prev 为空的任务为第一个子任务，next 为空的为最后一个子任务
    prev     : "ec21..",    # 指向本任务前面的任务，空表示本任务为第一个子任务
    next     : "ccda.."     # 指向本任务后面的任务，空表示本任务为最后一个子任务
}
```

## tag 的元数据

```
{
    nm : "Bug",     # 就是文件名
    tp : "tag"      # 类型可以是 tag | milestone | thread
}
```

* tag 的元数据文件，的内容是这个元数据的详细介绍

# task 的视图

有几种经典的视图对应 *task* 的不同场景

* 检索视图，*task* 呈列表排列
* 日期视图，在日历上显示相关的 *task*
* 线程视图，为 *task* 设置线程属性，一个任务可以属于多个线程
* 里程碑视图， 为 *task* 设置里程碑属性

*task* 命令会提供根据这几种视图对应的检索参数

# task 命令

```
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

```













