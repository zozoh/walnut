# 命令简介 

    `weixin media` 获取临时素材的方法

# 用法

    weixin [公众号id] media [-mid 素材id] [-out 输出的目标]

    # 读取临时素材到标准输出
    demo@~$ weixin xxx media -mid xyzabc
    
    # 读取临时素材到abc.mp4
    demo@~$ weixin xxx media -mid xyzabc -out abc.mp4