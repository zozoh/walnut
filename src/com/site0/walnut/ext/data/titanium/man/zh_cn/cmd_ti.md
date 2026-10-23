# 命令简介

`ti` 用来处理和 `Titanium` 框架相关的服务器操作，主要是界面层的东东。

# 用法

```bash
ti @处理器 [参数...]
```

支持的处理器：

```bash
@views            # 获取某个对象的视图信息
@metas            # 获取某个对象的元数据信息
@sidebar          # 获取当前会话的侧边栏
@config           # 获取当前会话的特殊 Ti.Config 设置
@coms             # 获取所有可编辑控件的定义信息
@build            # 根据配置信息，将控件库或者核心库打包
@scss_import      # 将目录下所有的 scss 文件引入主文件的桩子
@i18n             # 将输入的信息做多国语言翻译
@i18n_tidy        # 整理 i18n 的文件，固定排序等
@i18n_compare     # 比较不同语言版本的 i18n，看看有什么疏漏
@gen_mapping      # 根据表单或者表格的字段设定，生成映射配置
@sidebar_actions  # 根据 sidebar 的 json 内容，生成 action 文件列表
```

# 示例

```bash
# 获取当前会话的侧边栏
ti @sidebar

# 获取控件定义
ti @coms

# 获取对象的视图
ti @views id:xxx
```
