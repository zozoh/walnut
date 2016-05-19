# 命令简介 

    `myproject` 查看当前用户下的项目以及对应任务
    
    文件存放规约:
    
    ~/.project
    	项目A
    		任务1
    	项目B
    		任务2
    		任务3
    		任务4
    		
    		
    任务格式:
    {
    	level		: 5,						// 难度     1-5, 数字越高越难
    	priority	: 1,						// 优先级	   1-5
    	deadline	: "2015-06-30",				// 截止日期
    	progress	: 50,						// 当前进度  0-100
    	description	: "完成XXX"					// 项目说明
    }
    
    // 等task功能实现, 改命令将被撤销

# 用法

    myproject
	
	
	