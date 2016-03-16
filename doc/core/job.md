---
title:后台任务
author:wendal
tags:
- 系统
- 会话
---

# 应用场景

耗时任务,阻塞任务,需要在后台运行,计划任务

# 任务注册文件

所有任务都放在 `/sys/job` 下,内容包括了执行任务所需要的全部信息

	```
	{
		user   : "wendal", // 执行任务的用户的用户名,非创建任务的用户名哦
		cmd  : "videoc 东方风神.avi", // 需要执行的命令,若为不存在,代表只需要执行回调
		callback : "需要执行的回调",    // 需要执行的回调
		env  : {"PWD":'/home/wendal/xxx/xxx'}
	}
	```
	
任务的元数据,用于存储任务的调度信息

	```
	{
		job_name : "视频转换_wendal_xxxx", //任务名称
		job_user : "root",   // 创建任务的用户用户名,一般情况下只有root可以为其他用户创建任务
		job_priority : 100,  // 任务优先级
		job_expires : -1,    // 任务过期时间,单位毫秒,为绝对时间,默认为-1,永不过期
		job_state : "wait",  // 任务状态: wait,run,ok,fail,expired
		job_begin : 1234213, // 任务开始的时间
		job_end   : 21342334 // 任务完成的时间
	}
	```
	
# 执行机制

单机版基本上就是通过一个线程池来调度,而集群版需要一个主服务器协调,或通过mongodb的upset做锁

	```
	任务排序优先级:
	job_priority > job_expires > create_time
	```
	
# 执行流程

	```
	// 将job_state设置为run, job_begin设置为当前毫秒数
	// 执行cmd的值,如果存的话
	// 执行callback的值,如果存在的话
	// 将job_state设置为ok或fail, job_end设置为当前毫秒数
	```
	
# 带过期时间的任务,若已经过期

	```
	// job已经过期
	// 将job_state设置为expired, job_start/job_end均设置为当前毫秒数
	```
	
# 任务清理机制

符合以下要求的任务将会删除

过期/已完成/已失败,一天以上