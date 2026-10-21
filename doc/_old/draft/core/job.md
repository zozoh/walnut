---
title:后台任务
author:wendal
tags:
- 系统
- 后台任务
---

# 应用场景

耗时任务,阻塞任务,需要在后台运行,计划任务

# 任务注册文件

所有任务都放在 `/sys/job/eacc.../` 一类的文件夹中,内容包括了执行任务所需要的全部信息

	```
	/sys/job/eacc...
	            -- cmd  // 命令文本
	            -- env  // 环境变量
	            -- xxx  // 其他job add时指定添加的资源
	```
	
任务的元数据,用于存储任务的调度信息

	```
	{
		job_nm    : "视频转换_wendal_xxxx", //任务名称
		job_ct_usr  : "root",  // Job的创建者
		job_usr   : "root",  // 运行时用户名
		job_grp   : "wendal",// 运行时组
		job_ava   : 0,       // 任务起效时间,单位毫秒,为绝对时间
		job_expi   : -1,      // 任务过期时间,单位毫秒,为绝对时间,默认为-1,永不过期
		job_st    : "new",   // 任务状态: new,run,done
		job_begin : 1234213, // 任务开始的时间
		job_end   : 2134234, // 任务完成的时间
		job_cron  : "0 * * *"// cron表达式
	}
	```
	
# 执行机制

任务排序优先级 按 job_ava 进行排序
	
# 执行流程

分2种, 带cron和不带cron

## 不带cron

	```
	// 将job_state设置为run, job_begin设置为当前毫秒数
	// 执行cmd
	// 将job_state设置为done, job_end设置为当前毫秒数
	```
	
## 带cron

	```
	// 首先,将job_ava += 一天
	// 按cron, 计算出当前还需要执行的时间点
	// 将原任务拷贝N分,分别设置job_ava的值,并移除cron标示,并修改job_name += 时间
	```
	
# 任务清理机制

任务清理本身也是一个job