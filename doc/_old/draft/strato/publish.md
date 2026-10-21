---
title: 发布
author: wendal
---

# 设计原则

发布, 是带转换的一种输出操作

1. 快速    -- 总能在短时间内完成/失败, 也代表了发布过程中不进行视频转换等耗时工作
2. 可重复 -- 相同参数和环境下,发布得到的目录是逻辑等价的
3. 原子性 -- 多个发布可以同时进行, 总是在完成之后迁移到目标文件夹

# 发布参数

	C  #需要发布的obj的id
	trans  #转换器,默认为box_profile, 可以是box, screen等
	dstdir #目标目录, 这取决于特定的转换器, 除box_profile外, 大部分转换器需要指定,当然,大部分转换器都是被box_profile调用的
	tmpdir #临时目录, 默认是 /home/$user/.publish/.tmp/$PID
	dummy  #是否真正移动到目标目录,测试用

# 命令描述

	典型命令: publish $obj
	<!--  -->
# 目录结构

	```
	/home
		-- wendal
		-- zozoh
			-- 屏幕
				-- 八层翻转
			-- 素材
				-- 刷屏图片
				-- 视频素材
			-- .publish
				-- .tmp   #发布时的临时目录
					-- 8989
				-- AA
					-- BBCCDDEEFF
					-- 77EECCEEAA
				-- E0
					-- CCDDEEDDEE
					-- CCDD999911      #按机器的macId划分文件夹
						-- obj.tgz     #本目录下所有obj的压缩包
						-- pub_history #发布历史
						-- 播放器
							-- 已安装
								-- wendal测试机
						-- 屏幕
							-- 八层翻转
							-- 八层翻转_pobj
								-- 1
								-- 2
						-- 素材
							-- 刷屏图片
								-- 1.jpg
								-- 2.jpg
							-- 视频
								-- 东方红.result
									-- 1_1.mp4
									-- 1_2.mp4
	```

上述结构保证了:

1. 每个box的发布是独立的,即使他们引用了完全一样的播放计划和播放屏幕, 解决了关联发布的问题
2. 不同box中相同的素材均指向相同的文件, 发布的过程中不需要涉及文件拷贝的问题,同时指纹也是预先生成好了

发布目录下的Obj,需要额外添加一个属性, originId代表原Obj的id

# 基本流程

发布的流程, 其实就是传入参数,然后调用一个转换器的过程, 期间又调用其他转换器.


	```
	wsh --> publish xxx --> box_profile_transfer.call(/*各转换参数*/)
									-- screen_transfer.call(/*各转换参数*/)
										-- video_transfer.call(/*各转换参数*/)
										-- qml_transfer.call(/*各转换参数*/)
	```

# 关于 obj.tgz

这是box_profile转换器特有的输出, 含有:
1. 本次发布得到的所有obj对象的集合
2. box_profile, 播放计划及相关的所有播放屏幕

# 关于下载

通过 .obj.tgz 文件的指纹来表示当前box的最新发布版本

	```
	GET /$dmn/publish/$mac[:2]/$mac[2:]/obj.tgz?sha1=$sha1

	return isSame($sha1) ? 304 : data;

	```

盒子根据obj.tgz的内容即可分析出需要下载的文件的指纹

	```
	GET /file/$sha1

	return file;
	```
