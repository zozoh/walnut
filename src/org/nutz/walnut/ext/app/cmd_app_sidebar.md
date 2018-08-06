# 命令简介 

`app sidebar` 命令用来得到某个域的侧边栏项目 
    
 实际上，命令会读取一个 JSON 配置文件，格式如下 

```js    
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
```
    
 配置文件的位置的查找顺序为：
 
 1. 命令参数 `[0]`
 2. 环境变量 `SIDEBAR`
 3. 命令参数 `-dft`
 4. 系统默认 `/etc/ui/sidebar.js`
  
# 使用方法

    app sidebar [-cq] [-html] [/path/to/sidebar.js] [-dft /path/to/sidebar.js]
    -------------------------------------------
    Path    指明配置文件的位置
    
    -c      按json输出时，紧凑显示
    -q      按json输出时，键值用双引号包裹
    -dft    指明默认配置文件路径
    
    -html   是按照 HTML 输出，默认关闭。是按照 JSON 输出的 

# 实例
    
    # 输出本用户的侧边栏的  JSON 格式
    app sidebar  
    
    # 指定某个侧边栏配置文件，并安 HTML 输出
    app sidebar -html /path/to/file
    