define({
    "zcron" : {
        "title" : "编辑循环表达式",
        "tab" : {
            "by_week"  : "周",
            "by_month" : "月",
            "by_adv"   : "高级"
        },
        "pick_month_mo"  : "请选择月份",
        "pick_month_da"  : "请选择月中的日期",
        "pick_month_W"   : "所有的工作日",
        "pick_week"  : "请选择周中日期",
        "pick_time"  : "请选择每天的时间点",
        "pick_month" : "选择月份",
        "pick_date"  : "选择日期",
        "adv_invalid": "您的表达式格式错误",
        "adv_tip"    : '这里你可以自由的输入你的 Quartz 表达式， 关于 Quartz 表达式的语法'
                       + '请参见 <a target="_blank" href="http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger">官方语法说明</a>',
        "exp" : {
            "start" : "从?开始",
            "to" : "至",
            "L1" : "最后一",
            "Ln" : "倒数第?",
            "N" : "第?个",
            "INV" : "",
            "EXC" : "（不包含）",
            "dates" : {
                "full"     : "yyyy年M月d日",
                "same"     : "M月d日",
                "region"   : "${from}${ieF}至${to}${ieT}，",
                "no_from"  : "截止至${to}${ieT}，",
                "no_to"    : "从${from}${ieF}开始，",
            },
            "times" : {
                "region" : "${from}${ieF}到${to}${ieT}，",
                "off" : "经过?后开始，",
                "h" : "?小时",
                "m" : "?分钟",
                "s" : "?秒钟",
                "step" : "每隔?；",
                "pad" : "起始时间按?全天对齐，"
            },
            "year" : {
                "unit" : "年",
                "ANY" : "每年",
                "span" : "每隔?年",
                "tmpl" : "?年",
                "suffix" : "的"
            },
            "month" : {
                "unit" : "月",
                "span" : "每?个月",
                "ANY" : "每月",
                "dict" : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" ],
                "suffix" : "的"
            },
            "day" : {
                "unit" : "日",
                "span" : "每?天",
                "ANY" : "每天",
                "tmpl" : "?号",
                "suffix" : "的",
                "W" : "最近的工作日",
                "Wonly" : "所有工作日"
            },
            "week" : {
                "unit" : "周",
                "span" : "每隔?周",
                "ANY" : "每周",
                "dict" : [ "周日", "周一", "周二", "周三", "周四", "周五", "周六" ],
                "suffix" : "的每天"
            },
            "hour" : {
                "span" : "每?小时",
                "ANY" : "每小时的",
                "tmpl" : "?点",
                "suffix" : ""
            },
            "minute" : {
                "span" : "每?分钟",
                "ANY" : "每分钟的",
                "tmpl" : "?分",
                "suffix" : ""
            },
            "second" : {
                "span" : "每?秒钟",
                "ANY" : "",
                "tmpl" : "?秒"
            }
        } // ~ end of "exp"
    }
});