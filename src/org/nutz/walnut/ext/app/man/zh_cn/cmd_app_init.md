# 命令简介 

    `app init` 命令用来根据一个模板目录，创建用户的域文件结构和环境变量 
    ----------------------------------------------------------------
    模板目录的结构如下:
    ----------------------------------------------------------------
    /path/to/dir
        _files          # 文件目录结构，优先执行
        _script         # 要执行的脚本，最后执行
        abc.png         # 在 _files 里面指定的文件内容
        xyz.txt         # 就是个纯文本
    ----------------------------------------------------------------
    _files 文件指明了域的目录结构，通常的，用 "#" 开头的行作为注释被无视
    剩下的内容分段，每段开始就意味着下一段结束。段的结构为
    
    @FILE a/b/c.txt            # 文件或者目录的路径
    {                          # { 意味着元数据开始
        元数据的 JSON 描述
    }                          # } 意味着元数据段结束
    %COPY> xyz.txt              # % 描述了内容区。COPY | TMPL | TEXT
    ----------------------------------------------------------------
    内容区的描述规范:
    - COPY 就是简单 copy 内容
    - TMPL 则是做文本占位符替换
    - 后面跟的目标模板文件相对路径，如果不跟路径则
    - 采用文件/目录相同的路径
    ----------------------------------------------------------------
    # copy 一个文件的内容
    %COPY> xyz.txt
    
    # 读取文件内容，进行转换，并写入目标
    %TMPL> xyz.txt
    
    # 直接声明了文本内容，遇到文件结尾结束，或者 %END% 行为结尾
    # 内容会被 trim
    %COPY: hello world
    this is second line
    %END%
    
    # 直接声明了文本内容，就这一行
    %TMPL% hello ${name}
    
    
# 使用方法

    app init DIR [DEST] [-c JSON] [-u xxx]
    
    DIR   # 模板目录的路径
    DEST  # 目标目录路径，默认为 ~
    -c    # JSON，模板文件的占位符填充的上下文，如果为空从 pipe 里读
    -u    # 指定用户，生成的文件所属为改用户, 需root权限
    
# 实例
    
    # 根据模板设置自己的主目录
    app init /etc/myapp/init  
    
    # 根据模板设置某个目录
    app init /etc/myapp/init ~/abc
    
    # 根据模板设置自己的主目录，并指定了占位符
    app init /etc/myapp/init -c  'pnb:"gh_4b091ca219a2",host:"www.mysite.com"'
    
    # root用户为用户xiaobai初始化
    app init /etc/myapp/init /home/xiaobai -u xiaobai
    