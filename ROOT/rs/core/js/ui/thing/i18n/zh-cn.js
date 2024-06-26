define({
    "thing" : {
        "create" : "创建..",
        "create_tip" : "请输入对象的名称",
        "create_tip2": "请输入{{text}}的名称",
        "create_do"  : "立即创建",
        "rm_tip" : "将选中的项目移入回收站",
        "refresh_tip" : "重新加载列表",
        "clean_do"    : "清空回收站",
        "clean_confirm"  : "清空回收站将导致所有回收站内数据不可恢复，你确定要继续吗？",
        "clean_restore" : "从回收站中恢复",
        "conf_setup" : "数据集配置...",
        "conf" : {
            "title" : "设置数据集「{{nm}}」",
            "icon_modify" : "修改数据集的图标",
            "viewsource"  : "查看源",
            "fnm" : {
                "id"      : 'ID',
                "thumb"   : '缩略图',
                "th_nm"   : '名称',
                "th_ow"   : '所有者',
                "lbls"    : '标签',
                "th_site" : '所属站点',
                "th_pub"  : '发布状态',
                "__media__"       : '多媒体',
                "__attachment__"  : '附件',
                "__brief_and_content__"  : '内容及摘要',
            },
            "ficon" : {
                "id"      : '<i class="zmdi zmdi-key"></i>',
                "thumb"   : '<i class="zmdi zmdi-photo-size-select-large"></i>',
                "th_nm"   : '<i class="zmdi zmdi-flag"></i>',
                "th_ow"   : '<i class="zmdi zmdi-account-o"></i>',
                "lbls"    : '<i class="zmdi zmdi-labels"></i>',
                "th_site" : '<i class="fa fa-sitemap"></i>',
                "th_pub"  : '<i class="zmdi zmdi-globe-alt"></i>',
                "th_enabled"  : '<i class="zmdi zmdi-assignment-check"></i>',
                "lm" : '<i class="zmdi zmdi-time-countdown"></i>',
                "ct" : '<i class="zmdi zmdi-time-restore"></i>',
                "__media__"       : '<i class="zmdi zmdi-camera-alt"></i>',
                "__attachment__"  : '<i class="zmdi zmdi-attachment-alt"></i>',
                "__brief_and_content__"  : '<i class="zmdi zmdi-collection-text"></i>',
            },
            "key" : {
                "key"    : "字段名",
                "title"  : "显示名",
                "type"   : "字段类型",
                "tip"    : "说明文字",
                "dft"    : "默认值",
                "editAs" : "显示控件",
                "uiConf" : "控件配置项",
                "uiConf_ph" : "JSON文本",
                "hide"   : "在列表中隐藏",
                "multi"  : "多选",
                "uiWidth" : "控件宽度",
            },
            "t" : {
                "string"   : "字符串",
                "int"      : "整数",
                "float"    : "浮点",
                "boolean"  : "布尔",
                "datetime" : "日期时间",
                "time"     : "时间",
                "object"   : "复杂对象",
            },
            "c" : {
                "input"        : "单行输入框",
                "text"         : "多行输入框",
                "label"        : "只读文字",
                "color"        : "颜色",
                "background"   : "背景",
                "switch"       : "切换按钮",
                "toggle"       : "开关",
                "droplist"     : "下拉列表",
                "checklist"    : "复选框列表",
                "radiolist"    : "单选列表",
                "pair"         : "名值对",
                "date_range"   : "日期范围",
                "number_range" : "数字范围",
                "opicker"      : "对象选择器",
                "datepicker"   : "日期选择器",
                "combotable"   : "组合列表",
            },
            "cicon" : {
                "input"        : '<i class="fa fa-square-o"></i>',
                "text"         : '<i class="zmdi zmdi-format-subject"></i>',
                "label"        : '<i class="zmdi zmdi-label-alt"></i>',
                "color"        : '<i class="zmdi zmdi-palette"></i>',
                "background"   : '<i class="zmdi zmdi-format-color-fill"></i>',
                "switch"       : '<i class="zmdi zmdi-view-column"></i>',
                "toggle"       : '<i class="fa fa-toggle-on"></i>',
                "droplist"     : '<i class="fa fa-toggle-down"></i>',
                "checklist"    : '<i class="zmdi zmdi-check-square"></i>',
                "radiolist"    : '<i class="zmdi zmdi-dot-circle"></i>',
                "pair"         : '<i class="zmdi zmdi-view-list"></i>',
                "date_range"   : '<i class="zmdi zmdi-time-interval"></i>',
                "number_range" : '<i class="zmdi zmdi-collection-item-9"></i>',
                "opicker"      : '<i class="zmdi zmdi-toys"></i>',
                "datepicker"   : '<i class="fa fa-calendar"></i>',
                "combotable"   : '<i class="zmdi zmdi-dns"></i>',
            },
            "blank"          : "请选择一个字段进行设置",
            "saveflds"       : "保存字段修改",
            "saving"         : "正在保存字段设置 ... ",
            "cancel"         : "放弃",
            "addfld"         : "添加字段",
            "addfld_tip"     : "请输入新字段的键值",
            "delfld"         : "删除字段",
            "mv_up"          : "上移",
            "mv_down"        : "下移",
            "e_no_thingjs"   : "没有找到数据集配置文件",
            "e_fld_nkey"     : "字段键值不能为空",
            "e_fld_invalid"  : "字段键值非法，只能是数字，小写字母，和下划线的组合",
            "e_fld_exists"   : "字段键值已存在",
            "tab_general"    : "设置",
            "tab_fields"     : "字段",
            "tab_import"     : "导入",
            "tab_export"     : "导出",
            "tab_commands"   : "命令",
            "general" : {
                "t_display" : "显示设定",
                "t_adv"     : "高级设定",
                "k_smfwh"   : "菜单收缩",
                "k_thIndex" : "显示索引",
                "k_thIndex_m" : "对象属性",
                "k_thIndex_d" : "详情内容",
                "k_thData"  : "显示数据",
                "thumbsz"   : "缩略图尺寸",
                "thumbsz_tip"   : "请输入诸如 `256x256`",
                "ukeys" : "唯一键约束",
                "ukeys_tip" : "一行一个，复合键用半角逗号分隔，以 * 开头的行，表示不能为空",
                "lnkeys" : "链接键"
            },
            "import" : {
                "enabled" : "允许导入",
                "accept"  : "文件格式",
                "accept_tip" : "可以导入哪些文件格式，请用半角逗号分隔，输入文件格式后缀，譬如 `.csv, .xls`",
                "processTmpl" : "命令输出模板",
                "processTmpl_tip" : "导入命令输出的模板，默认为 `${P}` 表示导入进度",
                "processTmpl_placeholder" :"${P} ${th_nm?-未知-} : ${phone?-未设定-}",
                "accept_placeholder" : ".csv, .xls",
                "unikey"  : "唯一键",
                "unikey_tip" : "导入时用这个字段来去重复，请输入数据表中的键值",
                "mapping" : "映射文件",
                "mapping_tip" : "请输入映射文件全路径，譬如 `~/.sheet/导入映射.txt`",
                "fixedForm" : "预设字段",
                "fixedForm_tip" : "导入前可以为所有数据预先设置一些字段值，请输入字段设置js文件全路径",
                "afterCommand" : "后续处理命令",
                "afterCommand_tip" : "每个导入的数据都会变成 JSON，经由管道传入被这个命令进行后续处理",
            },
            "export" : {
                "enabled" : "允许导出",
                "exportType" : "默认输出类型",
                "etp_xls" : "电子表格文件(.xls)",
                "etp_csv" : "通用数据文本(.csv)",
                "pageRange"  : "默认输出多页",
                "pageRange_tip"  : "默认将只会输出你看到的当前页的数据，如果你想输出多页数据，可以打开这个选项，并指定页码范围",
                "pageBegin"  : "默认起始页码",
                "pageEnd"    : "默认结束页码",
                "pageEnd_tip" : "如果输入小于等于 0 的整数，则表示最后一页",
                "audoDownload" : "默认自动下载",
                "audoDownload_tip" : "导出完毕后自动下载到本地",
                "mapping" : "映射文件",
                "mapping_tip" : "请输入映射文件全路径，譬如 `~/.sheet/导出映射.txt`",
                "processTmpl" : "命令输出模板",
                "processTmpl_tip" : "导出命令输出的模板，默认为 `${P}` 表示导出进度",
                "processTmpl_placeholder" :"${P} ${th_nm?-未知-} : ${phone?-未设定-}",
            },
            "filter" : {
                "data" : "数据",
                "recycle" : "回收站"
            }
        },  // end `thing.conf`
        "key" : {
            "id" : "ID",
            "lbls" : "标签",
            "lbls_tip" : "多个标签请用空格或逗号分隔",
            "ct" : "创建时间",
            "lm" : "最后修改",
            "icon"  : "图标",
            "thumb" : "缩略图",
            "media" : "多媒体",
            "attachment" : "附件", 
            "th_ow" : "所有者",
            "th_nm" : "名称",
            "brief" : "摘要",
            "th_site" : "所属站点",
            "th_pub" : "发布",
            "th_enabled" : "生效",
            "th_live" : "存活",
            "th_cate" : "分类",
            "th_c_cmt" : "评论数",
            "th_c_view" : "浏览数",
            "th_c_agree" : "赞同数",
        },
        "keytip" : {
            "thumb" : "只能上传 jpeg 或者 jpg 格式的图片",
        },
        "import" : {
            "title" : "导入数据",
            "step0" : "预设",
            "step1" : "选择文件",
            "step2" : "上传",
            "step3" : "执行导入",
            "step4" : "成功",
            "cf_tip1" : "将文件拖拽到这里，或者你可以",
            "cf_tip2" : "支持的文件类型",
            "cf_all"  : "任何格式",
            "e_fmulti" : "您只能选择一个文件导入",
            "e_fnone"  : "请选择一个文件导入，谢谢 -_-!",
            "e_accept" : "文件「{{fname}}」不能被接受，只能接受「{{accept}}」",
            "up_title"  : '上传文件',
            "up_ing"    : '已经写入了 {{loaded}} 字节',
            "up_finish" : '<i class="zmdi zmdi-settings zmdi-hc-spin"></i> 正在做数据导入前的准备工作...',
            "welcome" : "正在分析导入数据...",
            "done" : "导入数据完毕，请点击文件名下载",
            "viewlog" : "查看导入日志"
        },
        "export" : {
            "title" : "导出数据",
            "step1" : "导出设置",
            "step2" : "执行导出",
            "step3" : "完成",
            "exportType" : "输出类型",
            "pageRange"  : "输出多页",
            "pageBegin"  : "起始页码",
            "pageEnd"    : "结束页码",
            "audoDownload" : "自动下载",
            "welcome"  : "正在准备要导出的数据列表...",
            "viewlog" : "查看导出日志",
            "done" : "导出完毕"
        },
        "meta"   : "属性",
        "detail" : {
            "title"  : "内容",
            "brief"  : "请输入不超过 50 字的摘要",
            "save"   : "保存内容及摘要",
            "saving" : "正在保存...",
            "noobj"  : "还未读取对象",
            "genbreif" : "自动生成摘要",
        },
        "data" : {
            "media" : "多媒体",
            "attachment" : "附件",
            "overwrite_tip" : "文件 「{{nm}}」 已存在，您要覆盖它吗？",
            "drag_tip" : "松开鼠标，上传文件",
            "none_media_tip" : "请选择一个文件显示预览",
            "remove" : "删除选中文件",
            "upload" : "上传新文件",
            "download" : "打包下载...",
            "down_pickname"  : "下载包名称",
            "down_gen_zip"   : "生成压缩包",
            "down_show_down" : "完成下载",
            "down_begin" : "正在准备建立压缩包，这可能需要几十秒钟，请稍候...",
            "down_nm" : "下载包名称",
            "delnone" : "请先选择要删除的文件（支持按Shift键多选）",
            "asthumb" : "设置为当前对象的封面",
        },
        "err" : {
            "nothingjs"  : "没有找到 thing.js!",
            "remove_none"  : "您总得先选点啥再删除吧 -_-!",
            "restore_none" : "你没有选中任何可以被恢复的数据记录",
        },
        "blank" : '请选择数据以便查看详情',
    }
});