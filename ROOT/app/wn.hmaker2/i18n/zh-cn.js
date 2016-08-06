define({
    "hmaker" : {
        "res" : {
            "title" : '资源库',
        },
        "prop" : {
            "title" : '属性',
            "tab_block" : '<b>外观</b>',
            "tab_com"   : '<b>控件</b>',
            "padding"   : "边距",
            "border"    : "边框",
            "borderRadius" : "圆角",
            "background": "背景",
            "color"     : "前景色",
            "boxShadow" : "阴影",
            "img_src"   : "图片源",
            "href"      : "超链接",
        },
        "pos" : {
            "abs"    : "绝对定位: 用鼠标在页面上任意定位本块",
            "v_lt"   : "左上顶点定位",
            "v_rt"   : "右上顶点定位",
            "v_lb"   : "左下顶点定位",
            "v_rb"   : "右下顶点定位",
            "left"   : "左",
            "right"  : "右",
            "top"    : "上",
            "bottom" : "下",
            "width"  : "宽",
            "height" : "高",            
        },
        "page" : {
            "show_prop" : "显示页面属性"
        },
        "com" : {
            "columns" : {
                "name"  : "垂直分栏",
                "tip"   : "对于所在区域进行垂直分隔，可以容纳更多控件",
                "icon"  : '<i class="zmdi zmdi-view-column"></i>',
            },
            "text" : {
                "name"  : "文本",
                "tip"   : "直接编辑你想要的文字内容",
                "icon"  : '<i class="fa fa-text-width"></i>',
            },
            "image" : {
                "name"  : "单张图片",
                "tip"   : "不解释你懂的",
                "icon"  : '<i class="fa fa-image"></i>',
            },
            "slider" : {
                "name"  : "图片幻灯",
                "tip"   : "选择一组图片或者一个文件夹，进行图片播放",
                "icon"  : '<i class="zmdi zmdi-slideshow"></i>',
            },
            "thingset" : {
                "name"  : "动态数据集合",
                "tip"   : "为你的数据集合设置列表模式的显示版式。支持翻页，海量数据也没问题哦",
                "icon"  : '<i class="fa fa-cubes"></i>',
                "tt_ds"     : "控件数据",
                "tt_flt"    : "过滤器",
                "tt_pager"  : "翻页器",
                "ds_id"     : "数据源",
                "ds_query"  : "数据过滤条件",
                "template"  : "显示模板",
                "flt_enabled": "支持过滤",
                "flt_cnd"   : "过滤条件",
                "flt_keywd" : "关键字",
                "flt_region": "范围",
                "flt_val"   : "值",
                "flt_add"   : "添加过滤字段",
                "flt_sort"  : "排序",
                "flt_sort_add" : "添加排序字段",
                "pg_enabled" : "支持翻页",
                "pg_size"    : "每页记录数",
                "redata"     : "更换数据源",
                "retemplate" : "更换显示模板",
                "mode" : {
                    "none"      : "未设置数据源", 
                    "gone"      : "数据源不存在",
                    "invalid"   : "数据源格式非法",
                    "lackdef"   : "数据源缺少数据定义文件",
                    "wrongdef"  : "数据源字段定义文件错误",
                    "tmplnone"  : "未设定显示模板",
                    "tmplgone"  : "显示模板不存在",
                    "tmplnodom" : "显示模板缺少 DOM 文件",
                    "tmplnocss" : "显示模板缺少样式文件",
                    "tmpldom_E" : "显示模板DOM文件内容为空",
                    "tmplcss_E" : "显示模板样式文件格式错误或者内容为空",
                },
                "flt" : {
                    "enabled"  : "显示过滤器",
                    "disabled" : "隐藏过滤器",
                },
                "pg" : {
                    "enabled"  : "显示翻页条",
                    "disabled" : "隐藏翻页条",
                }

            },
            "thingobj" : {
                "name"  : "动态数据对象",
                "tip"   : "详细的定制了某个数据的详细显示方式",
                "icon"  : '<i class="fa fa-cube"></i>',
            },
        }
    }
});