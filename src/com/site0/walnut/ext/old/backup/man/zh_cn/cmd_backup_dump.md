# 命令简介 

    `backup dump` 备份一个或多个文件夹

# 用法

    backup dump [-debug|v|trace|dry] [-conf XXX.conf] [-keepTemp false] [-base /home/wendal/] dirA dirB dirC
    
参数:
debug 调试日志,开关型参数
v     调试日志,开关型参数
trace 非常详细的日志,开关型参数
keepTemp  是否保留临时文件,默认是不保留
dry   仅输出日志,不生成最终的备份文件
base  基准路径,普通用户默认/home/$user, root用户默认为/
includePatterns 需要匹配的模式,可以是多个,逗号分隔
excludePatterns 需要排除的模式,可以是多个,逗号分隔
excludes 需要排除的路径,可以是多个,逗号分隔
outputFormat 输出格式,当前仅支持zip,默认值也是zip
dst 输出路径,默认会生成一个路径
prevs 增量备份时需要参考的压缩包,可以是多个,逗号分隔

# 示例

// 备份乐少年的全部内容,使用乐少年账号
>: backup dump /home/leshaonian

// 仅备份sites和things
>: backup dump sites things
