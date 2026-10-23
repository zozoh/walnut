# 过滤器简介

`@scss_import` 将目录下所有的 scss 文件引入主文件的桩子。所有不以 `_` 开头的文件都被认为是主文件。

所谓`桩子`就是在主 scss 文件中的注释：

```scss
@import "_utility.scss";
//-----------------------------------------------------
// App
@import "app/_wn-manager.scss";
//-----------------------------------------------------
// AUTO-INCLUDE-COM-STUB
//
//  {The dynamic import will be insert to here}
//
```

# 用法

```bash
ti @scss_import 工作目录 [控件目录...]
       # 工作目录    # scss 文件所在的目录
       # 控件目录    # 可多个，扫描这些目录下的控件样式
```

# 示例

```bash
# 自动引入控件样式
ti @scss_import theme com app
```
