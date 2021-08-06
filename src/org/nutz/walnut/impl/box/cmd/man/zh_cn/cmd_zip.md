# zip压缩命令

`zip` 命令将一些文件或目录压缩成一个zip文件

# 用法

```bash
zip [options] <zipfile> <dir1> [dir2....]

-m      一个 AutoMatch 的过滤条件
-quiet  静默模式，不输出
-hide   包括隐藏文件
```
    
# 示例

```bash
// 压缩usb目录, 当usb.zip存在时,抛错
zip usb.zip usb

// 压缩usb目录, 甚至包括隐藏文件
zip -hide usb.zip usb

// 压缩usb目录中文件名以 abc 开头的文件
zip -m '{nm:"^abc.+$"}' usb.zip usb
```