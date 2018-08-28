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
-p    # 复制标准属性以外所有元数据
-m    # 复制标准属性以外所有元数据以及这四个标准属性  `c|m|g|md`
-d    # 如果目标存在，先删除再创建（仅限于文件） 
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
