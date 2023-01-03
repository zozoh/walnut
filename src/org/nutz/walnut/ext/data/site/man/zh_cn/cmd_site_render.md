# 命令简介 

`site render` 将指定数据渲染为静态网页，以便部署到CDN加速

本命令由于行为需要较高的定制性，所以配置信息封装在了一个配置文件里：

```json5
{
  // 站点的原始路径，将根据这个路径下的内容进行站点发布
  "home" :"~/website",
  // 默认输出目录
  "target": "~/www/website",
  // 全局上下文变量
  "vars":{
    "rs" : "/"
  },
  // 渲染哪些归档
  "archives": [
    {
      // 归档集合的名称
      "name" :"ar",
      // 从哪里加载归档，也同时会根据这个路径自动生成 rph
      "base" : "~/site/",
      // 输出的目标路径， 默认就是 rph， 渲染器会保证路径以.html结束
      "dist" : "${lang}/${rph}",
      // 一个当前归档元数据为上下文的解释集合，作为渲染 wnml 的上下文
      // 真正渲染时，与全局的  vars 融合
      "vars":{
        "id" : "=id"
      },
      // 符合条件的对象会被渲染
      "filter":{
        "race" :"DIR",
        "tp":"^(category|article)",
        "published":true
      },
      // 排序
      "sort":{
        "sort":1
      },
      // 符合条件的对象会被递归
      "recur":{
        "tp":"category"
      },
      // 为了防止意外，要规定一个单层查询最大数据限制，默认2000,
      "limit":2000,
      // 渲染模板
      "html": "pub.wnml"
    }
  ],
  // 还需要额外渲染哪些静态网页
  "pages": [
    {
      "name" : "site",
      // 相对路径采用站点源码目录
      "base" : "page",
      "dist" : "${lang}/${rph}",
      "filter":{
        "race" :"FILE",
        "nm":  [
          "page/ytplayer.json",
          "page/subscrib.json",
          "page/subscrib_ok.json",
        ]
      },
      "limit":2000,
      "html": "pub.wnml"
    }
  ]
  

  // 需要复制哪些静态资源
  "copyFiles":[
    "css",
    "dist",
    "img"
  ],
  // 渲染的语言，如果不声明，则虚渲染时将没有 ${lang}
  // 多个语言将导致一个归档杯渲染多次
  "langs": ["en_uk","zh_cn","zh_hk"],
  // 输出的的html路径模板
  "path": "${lang}/${rph}.html",
  // 采用哪个模板输出
  "html" : "pub.wnml"
}
```

-------------------------------------------------------------
# 用法
 
```bash
site render
  [ID, ID ..]               # 可以指定多个归档的 ID
  [-conf conf.json]         # 渲染的配置文件路径
  [-target ~/wwww/website]  # 指明目标输出目录，将覆盖 conf.target
  [-langs en_uk,zh_cn]      # 指明输出语言，将覆盖 conf.langs
  [-name ar]                # 指定了归档集合的名称,表示仅仅会渲染这个集合
  [-copy]                   # 强制执行复制。如果声明了-name默认是不复制文件的,
                            # 即会无视 copyFiles 和 page 段的声明 
  [-page]                   # 强制渲染指定接页面。如果声明了-name默认是不复制文件的,
                            # 即会无视 copyFiles 和 page 段的声明 
  [-json]                   #  JSON 模式将不输出日志，而是最后汇总一个json
                            # 结果集合 {arID:[path1,path2,paht3]
  [-cqn]                    # json模式下输出json的格式
```

-------------------------------------------------------------
# 示例

```bash
# 根据对象自动选择一个视图
demo:~$ site ~/mysite @render -conf ~/pub.conf
```