# 命令简介 

    `app-sidebar` 命令用来得到某个域的侧边栏项目 
    
    实际上，命令会读取一个 JSON 配置文件，格式如下 
    
    [{
        title : "i18n:my.sidebar.g0",
        items : [{
            ph      : '/path/to/file',
            icon    : '<i...>',          // 图标，默认用对象的设定
            text    : 'i18n:xxx',        // 文字，默认用对象的名称
            editor  : 'xxx'              // 编辑器，默认用对象关联的编辑器
            dynamic : false              // 是否为动态生成
        }, {
            // 动态执行: 命令的输出结果类型一组对象的 json 列表
            type  : "objs"            
            cmd   : "obj ~/* -l -json"
        }, {
            // 动态执行: 命令的输出结果是 item 本身
            type  : "items"
            cmd   : "xxx"
        }]
    }]
    
    配置文件的位置，默认被认为在 /etc/ui/sidebar.js ，用户可以通过环境变量 SIDEBAR 来指定位置
  
# 使用方法

    app-sidebar [-cq] [-html] [Path]
    
    Path    为配置文件的位置，如果不声明则看环境变量，还没有，就默认采用 /etc/sidebar
    
    -c      按json输出时，紧凑显示
    -q      按json输出时，键值用双引号包裹
    
    -html   是按照 HTML 输出，默认关闭。是按照 JSON 输出的 

# 实例
    
    # 输出本用户的侧边栏
    app-sidebar  
    
    # 指定某个侧边栏配置文件，并安 HTML 输出
    app-sidebar -html /path/to/file