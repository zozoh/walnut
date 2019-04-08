# zip压缩命令

    `zip` 命令将一些文件或目录压缩成一个zip文件

# 用法

    zip [options] <zipfile> <dir1> [dir2....]
    
    -f     如果文件已存在，强制覆盖
    -match 仅打包过滤后的文件，从dir中查找
    -list  仅显示要打包的文件，并不真正打包
    -r     遍历深层目录
    -hide  无视隐藏文件
    
# 示例

    // 压缩usb目录, 当usb.zip存在时,抛错
    zip usb.zip usb
    
    // 压缩usb目录, 覆盖usb.zip(如果存在)
    zip -f usb.zip usb
    
    // 压缩usb目录 以及子目录中所有内容
    zip -r usb.zip usb
    