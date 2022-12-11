# 命令简介 

`site render` 将指定数据渲染为静态网页，以便部署到CDN加速

本命令由于行为需要较高的定制性，所以配置信息封装在了一个配置文件里：

```json5
{
  // 默认输出目录
  "target": "~/www/website",
  // 渲染哪些归档
  "archives": [
    {
      //会根据这个路径自动生成 rph
      "homePath" : "~/site/",
      // 符合条件的对象会被渲染
      "filter":{
        "race" :"DIR",
        "tp":"^(category|article)",
        "publishsed":true
      },
      // 符合条件的对象会被递归
      "recur":{
        "tp":"category"
      }
    }
  ],
  // 还需要额外渲染哪些静态网页
  "pages": [
    "page/ytplayer.json",
    "page/subscrib.json",
    "page/subscrib_ok.json",
  ],
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
  [~/wwww/website]       # 指明目标输出目录，将覆盖 conf.target
  [-conf conf.json]      # 渲染的配置文件路径
  [-langs en_uk,zh_cn]   # 指明输出语言，将覆盖 conf.langs
```

-------------------------------------------------------------
# 示例

```bash
# 根据对象自动选择一个视图
demo:~$ site ~/mysite @render -conf ~/pub.conf
```