---
title: ZIO 的设计说明
author:zozoh
---

# 关于文件 ID

* 所有文件的 ID 一定是个 UUID
* open 返回的是一个字符串型的句柄

# 句柄管理器 `ZIoHandleManager`

句柄管理器 `ZIoHandleManager` 是接口，可以扩展，根据句柄获取文件操作的上下文。
句柄管理并不暴露所谓的 `ZIoHandle` 对象，他提供几个基本的操作

     - create(R|W|A)     // 创建句柄，同时会分配缓冲区
     - touch             // 更新句柄最后使用时间
     - updateReadPos     // 更新句柄的最后读取位置
     - destroy           // 销毁句柄
     

每个句柄的信息包括:

    {
        oid        : 对象的ID
        osz        : 对象内容的大小
        usr        : 谁打开的
        ct         : 什么时间打开的
        lm         : 最后操作的时间
        mode       : 打开模式
        swap       : 对应的交换区 ID
        swsz       : 交换区已写入的数据大小
        swapAppend : true | false 缓冲区是否是 A 模式打开的
        rpos       : 默认 0L，记录当前读操作的指向的位置
        wpos       : 默认 0L，记录当前写操作的指向的位置
    }

# 观察者管理器 `ZIoObserverManager`

向句柄注册观察者，将会记录一个句柄和一个观察者之间的关系，当 `ZIo` 执行 `write`, `flush`, `close` 操作的时候，会通知观察者管理器。而观察者的通知具体行为，由观察者接口(`ZIoObserver`)的实现来完成。 观察者管理器这个接口提供如下操作:

     - add           // 增加观察者
     - remove        // 移除观察者，会调用观察者的 depose 方法
     - tell          // 告知观察者


# 缓冲区管理器 `ZIoSwapManager`

句柄管理器缓冲区管理通过缓冲区管理器 `ZIoSwapManager` ，它提供

     
     - create      // 创建一个缓冲区
     - read        // 获得缓冲区某一位置开始的输入流
     - write       // 获得缓冲区输出流用以覆盖
     - deleteZ      // 删除一个缓冲区


# 句柄与缓冲区的映射管理器 `ZIoMapper`

     - join
     - detach
     - hasHandles
     - getHandles
     - getSwap


# 关于交换区

    打开文件句柄有以下几种模式:
     - R   : 只读
     - SR  : 读取文件以及共享区
     - W   : 写
     - A   : 追加
     - SW  : 共享写，flush 时覆盖
     - SA  : 共享追加，flush 合并

* 同一个 Obj 同一时刻可能有多个句柄
* 同一个 Obj 同一时刻只会有一个交换区

















