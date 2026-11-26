# 命令简介

`pebble` 用 Pebble 模板引擎渲染 HTML 模板：支持**模板继承**、**包含**、**宏**，并且**默认对输出做 HTML 转义**。

它和 `tmpl` 的分工：

```bash
tmpl     # 字符串级替换：拼一行文本、改个路径、生成一段 SQL
pebble   # 页面级模板：整页 HTML，页头页脚只写一份，字段自动转义
```

`pebble` 的模板语法是 Twig/Jinja 那一套，会 HTML 就能上手。

## 自动转义（安全边界）

`{{ x }}` 输出的值会**自动转义** `< > & " '`，所以标题里带 `&`、引号都不会破坏页面。

正文 HTML 必须显式 `|raw`：

```bash
{{ data.body }}          # 当文本输出，HTML 标签会变成可见字符
{{ data.body|raw }}      # 原样输出，用于正文
```

`|raw` 是唯一的转义出口，用错会引入 XSS。本命令只读模板、不执行代码，风险面就在这一个过滤器上。

## 路径规则

模板文件就是 Walnut 对象树里的文件，**`@load` 用绝对路径**：

```bash
~/templates/views/detail-goods.html      # 域主目录下的模板
/home/hurom/templates/layout.html        # 完整路径也认
```

模板内部引用其它模板（`extends` / `include` / `import`）的路径解析规则：

```bash
"~/templates/layout.html"        # 以 ~ 或 / 开头 → 按绝对路径
"partials/header.html"           # 其它 → 相对"当前模板所在目录"
"../partials/header.html"        # 相对路径支持 ..，跟文件系统直觉一致
```

举例：`~/templates/views/detail-goods.html` 里写

```bash
{% extends "~/templates/layout.html" %}
{% include "partials/breadcrumb.html" %}   {# → ~/templates/views/partials/breadcrumb.html #}
{% include "../partials/footer.html" %}    {# → ~/templates/partials/footer.html #}
```

命令只读模板文件，不写、不删。能读到什么，取决于执行者的 Walnut 权限，跟 `cat` 一致。

# 安全边界

模板是**受信输入**：本命令不做模板沙箱，能读什么由执行者的 Walnut 权限决定，等价 `cat`。
所以有三条硬约束：

1. **不要编译外部输入**。`@load '字符串'` 与管道形式只用于调试和拼接小片段；
   正式发布一律 `@load -f <文件>`，模板文件只放在自己人能写的位置。
   （把用户输入当模板编译就是 SSTI；旧版 Pebble 的 include 还能借此读任意本地文件，
   本工程已升到修复版 4.1.0，但「不编译外部输入」这条仍要守。）
2. **不要把它挂成 HTTP 接口**（regapi / xapi）。错误信息里带模板路径和行号，
   只应该回给 box 调用者，不该出现在网页上。
3. **谁能改模板，谁就能改整站输出**。如果有人能登录后台改模板，等价于能给站点写任意 HTML。

`|raw` 是唯一的转义出口，只用于正文 `body`；其余字段一律默认转义。

# 用法

```bash
pebble @load [来源] @vars [变量] @render [-strict] [-maxsize 8388608]
```

命令本身没有选项，两个开关都属于 `@render`：

```bash
@render [-strict] [-maxsize 8388608]
  -strict        # 变量未定义时报错（默认关，未定义渲染为空串）
                 # 模板一多，变量名拼错会静默变空，发布前建议开一次
  -maxsize <n>   # 单次渲染输出上限（UTF-8 字节），默认 8388608
                 # 渲染过程中超限即刻中止，不会先渲染完再检查，也不会输出半截内容
```

不提供模板缓存开关：Walnut 的 IO 层自带读缓存，模板每次按需读取，改完立刻生效。

支持的过滤器：

```bash
@load      # 读取模板内容（参数、-f 文件或管道）
@vars      # 设置上下文变量（参数、-f 文件或管道）
@render    # 渲染并输出到标准输出，带 -strict / -maxsize
```

渲染结果写磁盘用 shell 重定向，目标不存在会自动创建：

```bash
pebble @load -f ~/templates/views/goods.html @vars -f /tmp/vars.json @render > ~/www/website/goods/vm200.html
```

# 模板语法

## 输出与过滤

```bash
{{ name }}                    # 自动转义
{{ body|raw }}                # 原样输出（正文用）
{{ name|default("匿名") }}     # 缺省值
{{ dt|date("yyyy-MM-dd") }}   # 日期格式化
{{ txt|trim|upper }}          # 链式过滤
{{ a ~ b }}                   # 字符串拼接（注意不是 +）
{# 这是注释，不输出 #}
```

常用过滤器：`raw` `escape` `default` `upper` `lower` `trim` `replace` `slice`
`join` `length` `first` `last` `sort` `reverse` `date` `numberformat` `urlencode`
`capitalize` `title` `abbreviate`

## 判断

```bash
{% if g.show and g.order > 100 %}
  ...
{% elseif g.cate == "A" %}
  ...
{% else %}
  ...
{% endif %}
```

## 循环

```bash
{% for g in list %}
  {{ loop.index }}      {# 从 1 开始 #}
  {{ loop.index0 }}     {# 从 0 开始 #}
  {{ loop.first }}      {# 是否第一个 #}
  {{ loop.last }}       {# 是否最后一个 #}
  {{ loop.length }}     {# 总数 #}
{% endfor %}

{# 空集合走 else 分支 #}
{% for g in list %}
  ...
{% else %}
  暂无内容
{% endfor %}
```

## 继承与包含

```bash
{# ~/templates/views/detail-goods.html #}
{% extends "~/templates/layout.html" %}

{% block content %}
  <h1>{{ data.nm }}</h1>
  {% include "../partials/breadcrumb.html" %}   {# → ~/templates/partials/breadcrumb.html #}
  {{ data.body|raw }}
{% endblock %}
```

```bash
{# ~/templates/layout.html #}
<!DOCTYPE html>
<html lang="{{ site.lang }}">
<head>{% include "partials/head.html" %}</head>
<body>
  {% include "partials/header.html" %}
  <main>{% block content %}{% endblock %}</main>
  {% include "partials/footer.html" %}
</body>
</html>
```

## 宏

```bash
{# ~/templates/partials/pager.html #}
{% macro pager(p) %}
  {% if p.pgnb > 1 %}
  <nav>
    {% if p.prev %}<a href="{{ p.prev }}">上一页</a>{% endif %}
    {% for u in p.urls %}<a href="{{ u.href }}">{{ u.pn }}</a>{% endfor %}
    {% if p.next %}<a href="{{ p.next }}">下一页</a>{% endif %}
  </nav>
  {% endif %}
{% endmacro %}
```

```bash
{% import "partials/pager.html" as ui %}
{{ ui.pager(pager) }}
```

# 错误

```bash
e.cmd.pebble.tpl.noexists : <模板路径>
e.cmd.pebble.vars.noexists : <变量文件路径>
e.cmd.pebble.render : <模板路径> (line <n>) - <原因>
e.cmd.pebble.too.large : <字节数>
```

模板语法错、变量缺失（`-strict`）都会带上文件名和行号，便于在几百个页面里定位。

# 示例

```bash
# 1. 模板来自参数
pebble @load '你好 {{ name }}' @vars '{name:"wendal"}' @render

# 2. 模板和变量都来自文件
pebble @load -f ~/templates/views/detail-goods.html @vars -f ~/tmp/vars.json @render

# 3. 变量从管道进来（jsc 里最常用）
echo '{"name":"wendal"}' | pebble @load -f ~/templates/views/x.html @vars @render

# 4. 渲染整页并落盘
pebble @load -f ~/templates/views/home.html @vars -f ~/tmp/home.json @render \
  > ~/www/website/index.html

# 5. 发布前用严格模式跑一遍，抓变量名拼错
pebble @load -f ~/templates/views/home.html @vars -f ~/tmp/home.json @render -strict
```
