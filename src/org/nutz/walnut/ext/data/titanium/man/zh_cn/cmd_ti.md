# 命令简介 

`cmd_ti` 用来处理和 `Titanium` 框架相关的服务器操作。 主要是界面层的东东。

-------------------------------------------------------------
# 子命令列表
 
```bash
ti views           # 获取某个对象的视图信息
ti metas           # 获取某个对象的元数据信息
ti sidebar         # 获取当前会话的侧边栏
ti config          # 获取当前会话的特殊 Ti.Config 设置      
ti coms            # 获取所有可编辑控件的定义信息  
ti build           # 根据配置信息，将控件库或者核心库打包
ti scss_import     # 将目录下所有的 scss 文件引入主文件的桩子
ti i18n            # 将输入的信息做多国语言翻译
ti i18n_tidy       # 整理 i18n 的文件，固定排序等
ti i18n_compare    # 比较不同语言版本的 i18n，看看有什么疏漏
ti i18n_zh_ct      # 【未实现】将简体中文自动转换为繁体中文
ti webdeps         # 解析 web 的 deps 依赖文件，生成一个引用列表
ti gen_mapping     # 根据表单或者表格的字段设定，生成映射配置
ti sidebar_actions # 根据 sidebar 的json内容，生成action文件列表

ti www             # 【废弃】发布一个站点
```
