# 命令简介 

`mode` 对象权限码分析工具攻击将显示或修改对象的元数据

# 用法

```
mode 
    [input]      # 权限码的输入
    [-octal]     # 八进制模式
    [-f]         # 文件模式。输入将不是权限吧，而是一个文件的路径
    [-real]      # 文件模式下默认了，它会获取自定义权限码。
                 # 如果未声明自定义权限且开启了这个选项，
                 # 它会获取文件的原生权限码
```
    
# 示例

```
$> mode 777 -octal
mod: rwxrwxrwx
oct: 777
int: 511

$> mode 488
mod: rwxr-x---
oct: 750
int: 488

$> mode 6
mod: rw-rw-rw-
oct: 666
int: 438

$> mode rwxr-x---
mod: rwxr-x---
oct: 750
int: 488

$> mode rw-
mod: rw-rw-rw-
oct: 666
int: 438
```

