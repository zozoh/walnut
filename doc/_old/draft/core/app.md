---
title:应用
author:zozoh
tags:
- 系统
- 应用
---

----------------------------------------------------------
# 什么是应用

一个应用包括一套完整的前后台逻辑，它可以使用任何流行的前后端技术，它包括

- 后台逻辑
- 前台逻辑
- 资源文件

这些均存放再一个文件夹内，系统通过环境变量来查找该应用的文件夹。

```bash
# 系统当前会话环境变量声明了 APP 的查找路径
# 如果未声明，默认为 "/app"
APP_PATH="/rs/ti/app:/app"
```

> @see `org.nutz.walnut.web.module.AppModule#open`

一个应用文件夹的样子

```bash
%APP_ROOT_DIR%
  |-- pc_tmpl.html          # 静态界面模板文件
  |-- init_tmpl             # 动态界面渲染脚本【优先级更高】
  |-- init_context          # 动态初始化上下文脚本
  |-- xxx/                  # 其他资源文件或者目录均可由
                            # /a/load 加载

```

----------------------------------------------------------
# 界面渲染上下文

```js
{
  title    : "wn.mananger"   // 应用标题
  session  : {/*...*/},      // 会话信息
  rs       : "/gu/rs",       // 参见系统启动配置项， app-rs
  appName  : "wn.manager",   // 应用名称
  app      : {/*...*/},      // @see 下一节
  appClass : "wn_manager",   // 应用名称的 Snake 形式
  theme    : "light",        // 应用的界面主题

}
```

----------------------------------------------------------
# 上下文变量`app`

```js
{
   name: "wn.manager",
   session: {
      id  : "soncqrmdc6ilqr87mvs65qg6mf",
      me  : "demo",
      grp : "demo",
      du  : 3600000,
      envs": {
         PWD          : "/home/demo",
         MY_ID        : "o3j8p4kg58j38p109gjur8ecfk",
         MY_RACE      : "FILE",
         MY_NM        : "demo",
         APP_PATH     : "/rs/ti/app:/app",
         OPEN         : "wn.manager",
         PATH         : "/bin:/sbin:~/bin",
         UI_PATH      : "/etc/ui",
         MY_GRP       : "demo",
         HOME         : "/home/demo",
         MY_GRPS      : ["demo"],
         THEME        : "dark",
         VIEW_PATH    : "/mnt/demo/view/:/rs/ti/view/",
         SIDEBAR_PATH : "/rs/ti/view/sidebar.json"
      }
   }
}
```


