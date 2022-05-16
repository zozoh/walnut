# 命令简介 

    `cpobj` 命令,将文件/文件夹拷贝到指定路径

# 用法

```bash
cpobj [-rvApmd] src dst

src   # 源路径
dst   # 目标路径
-v    # 显示执行细节
-r    # 遇到路径递归
-A    # 即使是隐藏文件，也要操作
-d    # 如果目标存在，先删除再创建（仅限于文件） 
-mime # 复制的时候也要保证目标文件与元文件 tp/mime 相同
# 下面三个属性是指定如何复制元数据的策略
# 这三个开关是互斥，优先级为  -p > -m > -e
-p    # 复制标准属性以外所有元数据
-m    # 复制标准属性以外所有元数据以及这四个标准属性  `c|m|g|md`
-e    # 正则表达式指定copy特殊的元数据，支持 ! 语法

-Q    # 遇到错误，静默什么都不输出

-own xiaobai  # 修改目标文件的所属者，默认为当前操作账号
-grp xiaobai  # 修改目标文件的所属组，默认为当前操作账号
```
        
# 示例

```bash
# 拷贝单个文件
cp hi.jpg hi_again.jpg

# 拷贝目录
cp -r superdir supermandir

# 拷贝单个文件,且复制所有非标准元数据
cp -p hi.jpg hi_keepmode.jpg

# 拷贝单个文件,且复制所有非标准元数据以及所属权限
cp -m hi.jpg hi_keepmode.jpg
```
