---
title: 如何建立回收站
author: wendal
---

设计动机
=========================================

有些文件比较鸡肋, 例如某些过期的数据文件, 但删掉又怕出事,那就放着一个可以"反悔"的地方咯, 回收站

为了能够"反悔", 回收站内的文件需要知道原文件的路径信息,例如pid或者完整path

创建回收站功能
=========================================

使用 ~/.Trash作为回收站的目录, 移入该文件夹时,自动添加*old_pid/old_path/old_nm*

所有, 通过定义一个hook来实现比较合适, 对应的事件是move, 目标路径是~/.Trash

其文本内容如下:

```
obj id:${id} -u 'old_pid:"${pid}",old_path:"${path}",old_nm:${nm},nm:`uuid`'
```

含义是设置4个属性
* old_pid 原本的pid
* old_path 原本的路径
* old_nm 原本的名称
* nm 设置为新的唯一名称

然后,这个hook的作用域: 移入目录是~/.Trash, 且原始路径不在~/.Trash

因为hook的匹配模式尚不支持~/XXX,所以需要变通为 /home/(\w)+/.Trash

```
{
	hook_by : [
		{
			_mv_dest : "^/home/(\w)+/.Trash",
			path : "!^/home/(\w)+/.Trash"
		}
	]
}
```

结合上面的描述, 最终的命令是

```
echo 'obj id:${id} -u \'old_pid:"${pid}",old_path:"${path}"\'' > ~/.hook/move/0_move_to_trash
obj ~/.hook/move/0_move_to_trash -u 'hook_by:[{_mv_dest:"^/home/(\w)+/.Trash",path:"!^/home/(\w)+/.Trash"}]'
```
