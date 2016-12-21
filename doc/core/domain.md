---
title: 域名转发机制
author:zozoh
tags:
- 系统
- 扩展
- URL
---

# 域名转发规则映射

```
/domain
    45cd23..89acd    # 每个记录一个目录对象，元数据如下
    {
        dmn_grp  : "abc"           # 域名对应的组名
        dmn_host : "www.abc.com"   # 域名的网址
        dmn_expi : $ams            # 过期时间，绝对毫秒数
    }
    88rvfd..df12
    9vfg31..56fj

```

# WalnutFilter配置项

提供一个标准 *JSP/Servlet* 过滤器来预处理域名转发相关信息

```
<filter>
   <filter-name>walnutFilter</filter-name>
   <filter-class>org.nutz.walnut.jetty.WalnutFilter</filter-class>
   <init-param>
       <param-name>nutzFilterName</param-name>
       <param-value>nutz</param-value>
   </init-param>
   <init-param>
       <param-name>errorPage</param-name>
       <param-value>/WEB-INF/jsp/invalid_domain.jsp</param-value>
   </init-param>
   <init-param>
       <param-name>hostMap</param-name>
       <param-value>hostmap</param-value>
   </init-param>
</filter>

<filter-mapping>
   <filter-name>walnutFilter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>	
```

# hostmap 文件

```
#------------------------------------------------
# 只是通过
^/(gu|a|o|u|p)/(.*)$  => /${1}/${2}
#------------------------------------------------
# httpapi
^/api/(.*)$           => /api/${grp}/${1}
#------------------------------------------------
# www 模块的映射  
^/(.*)$               => /www/${grp}/${1}  
```
# web.properties 里面的配置

```
# 本项目仅当 web.xml 配置了 org.nutz.walnut.jetty.WalnutFilter 时生效
# 处理域名的正则表达式，符合这个表达式的域名，统统不路由
# 这个配置项，存放在 web.properties 里，
# 如果没有，则为 null  表示统统不路由
main-host=^(127.0.0.1|192.168.\d+.\d+|localhost|ngrok[.].+)$
```

# 请求的附加属性

经过过滤器的请求会被设置上如下属性

 Att Name         | Value                  | M | Comment
------------------|------------------------|---|----------
`wn_www_path_org` | `/index.html`          | - | 原始路径
`wn_www_host`     | `mysite.com`           | - | 请求域名
`wn_www_ip`       | `231.144.24.89`        | - | 对方 IP
`wn_www_port`     | `8080`                 | - | 端口
`wn_www_path_new` | `/www/demo/index.html` | Y | 新路径
`wn_www_grp`      | `mydomain`             | Y | 对应的域

# 自定义的登录界面

Walnut 的系统登录界面为了能实现多个看起来完全不同的应用均跑在一个系统上的要求。登陆界面也应该可以随意定制。你可以在系统的 `/etc/hosts.d/` 路径下定义任意的登录界面:

```
/etc/hosts.d
    mysite.com          # 某一个域
        pages           # 自定义的页面
            login.html  # 登录页面
            login.css
            login.js
```



