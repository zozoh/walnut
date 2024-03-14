# 命令简介 

`app init` 命令用来根据一个模板目录，创建用户的域文件结构和环境变量。
如果想看详细描述，请阅读文档 [域应用初始化](core-l2/c2-app-init.md)
   
# 使用方法

```bash
app init 
    [/path/to/home]    # 【选】模板目录的路径
    [-dir /path/to]    # 【选】目标目录路径，默认为 ~
    [-vars {...}]      # 【选】占位符填充的上下文
    [-by _files]       # 【选】初始化模板文件名称，默认为 "_files"
    [-script]          # 【选】执行的脚本，默认为 "_script"
                       # 如果设置为 "off" 则表示不要执行脚本
```

> 为了找到模板的源，会依次尝试：

1. 命令第一个参数所指向的路径 `args[0]`
2. `~/.domain/init/_files`
3. `/mnt/project/${domain}/init/domain/_files`
4. `/mnt/project/${domain}/*/init/domain/_files`

> 为了找到上下文变量，会依次尝试:

1. 命令参数 `-vars` 所指向的 JSON
2. 标准输入流 `[STDIN]`
3. `~/.domain/vars.json`
    
# 实例

```bash
# 根据模板设置自己的主目录
app init /etc/myapp/init  

# 根据模板设置某个目录
app init /etc/myapp/init -dir ~/abc

# 根据模板设置自己的主目录，并指定了占位符
app init /etc/myapp/init -vars 'pnb:"gh_4b091ca219a2",host:"www.mysite.com"'

# 执行域初始化，但是不要执行脚本
app init -script off
```    
