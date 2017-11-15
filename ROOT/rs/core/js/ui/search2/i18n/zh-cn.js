define({
    "search" : {
        "filter": {
            "tip"   : "请输入查询条件"
        },
        "pager" : {
            "tip"   : '页 {{pn}}/{{pgnb}} - 记录数 {{nb}}/{{sum}}',
            "first" : '首页',
            "prev"  : '上页',
            "next"  : '下页',
            "last"  : '尾页',
            "modify_pgnb" : "修改页大小",
            "modify_tip" : '当前每页有 {{pgsz}} 条记录，你想修改成多少？ 请输入一个大于 0  的整数:'
        },
        "e" : {
            "o_no_uiForm" : "对象没有设置表单配置字段 uiForm",
            "noactived" : "你必须在列表里先高亮一个对象才能继续操作",
            "nochecked" : "你必须在列表里先勾选至少一个对象才能继续操作",
            "pgsz_must_int" : "页大小必须是一个整数",
            "pgsz_less_then_zero" : "页大小必须大于 0",
            "pgsz_is_big" : "一页超过 1000 条数据，可能会导致加载时间较长，你确定吗？",
            "pgsz_too_big" : "一页超过 10000 条数据! 天啊，您还是稍微设少一点吧 -_-!",
        }
    }
});