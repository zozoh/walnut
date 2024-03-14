# 命令简介 

    `backup restore` 备份一个或多个文件夹

# 用法

    backup restore [-debug|dry|ignore_sha1_miss] [-base /home/wendal/] [-target XXX] [-base YYY] .dump/XXXX.zip
    
参数:
debug 调试日志,开关型参数
dry   仅输出日志,不生成最终的备份文件
base  基准路径,普通用户默认/home/$user, root用户默认为/
force_id 恢复原id,非常重要
prevs 供参考的增量备份包
target 目标根文件夹

# 示例

// 使用备份文件v55f2vrfogigjoc52p608sfvd6.zip还原数据,并强制使用原始id
>: backup restore -v .dump/v55f2vrfogigjoc52p608sfvd6.zip -target /home/leshaonian/ -base /home/leshaonian/ -force_id
