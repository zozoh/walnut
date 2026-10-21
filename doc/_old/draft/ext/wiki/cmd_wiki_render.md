# 命令简介 

    `wiki render` 命令用来渲染一个单个文档，或者一组文档集合

# 用法

    wiki render 
            [/path/to]              # 指定一个源目录或者源文件
                                    # 如果不指定，那么就会从管道读取。表示渲染一个单个文件
                                    # 当从管道读取的时候，默认当前路径作为这个页面的基准路径
            [-base /path/to]        # 重新制定基准路径。所谓「基准路径」就是表示正在渲染的这个
                                    # wiki 内容假想的路径。默认的，如果你用 -src 指定了一个
                                    # 文档，那么这个文档所在的目录（如果是文档集合，自然是递归查）
                                    # 就是基准路径。你可以用本参数重新指定。
                                    # 当然本参数最有用的是，当从管道读取的时候，
                                    # 通常要指定一个基准路径
            [-dst /path/to]         # 指定输出的目标，可以是目录或者文件
                                    # 如果是文件，那么输入也必须是文件
                                    # 如果不指定，渲染文件就直接输出到控制台，如果是目录会抛错
            [-linkbase /path/]      # 指定了链接的根目录，相对链接当然无所谓了，对于 id:xxx 的也会
                                    # 相对 -base 来找到文件，但是对于 /xx/xx 形式的链接，会从 
                                    # linkbase 下面找。默认这个值与 base 相同。
            [-tmpl /path/to]        # 渲染后输出的 HTML 模板
            [-tree tree.xml]        # 如果是目录，指定文档集的 tree 列表所在文件
            [-treeName tree.html]   # 指定输出的名字，默认是改变 -tree 文件的主名字
                                    # 如果是目录自然结构，输出的是 tree.html

# 示例 

    # 渲染一个目录
    wiki render /path/to/dir/ -dst /path/to/target
        
    # 渲染一个文件，并将输出打印到控制台
    wiki render /path/to/some_wiki.md
    
    # 从管道渲染，基准路径为当前目录
    cat ~/abc.md | wiki render
        
    # 从管道渲染，指定基准路径和输出目录 
    cat ~/abc.md | wiki render -base /path/to/ -dst /path/to/dst/
