# 命令简介 

 `lccp` 将本地的一个文件拷贝到 walnut 文件
     
 - 只有 root 组成员才能执行

# 用法

    lccp
         [/path/to/local]   # 本地目录，比如  c:/a.txt
         [/path/to/walnut]  # Walnut 的目录或者新文件路径
         -f                 # 如果存在同名文件，覆盖
         -v                 # 显示执行细节
    
    
# 示例

    # 将本地 c 盘某文件 copy 到当前目录
    lccp c:/a.txt
    
    # 将本地 c 盘某文件 copy 到当前目录 b.txt
    lccp c:/a.txt b.txt
    
    # 将本地 c 盘某文件 copy 到指定目录
    lccp c:/a.txt ~/fff/
    
    # 将本地 c 盘某文件 copy 到指定路径
    lccp c:/a.txt ~/fff/b.txt