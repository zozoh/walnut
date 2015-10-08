(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="flow" class="oform-flow">
        <div class="oform-flow-main"></div>
    </div>
    <div code-id="flowGroupHead" class="oform-flow-ghead">
    </div>
    <div code-id="tabs" class="oform-tabs">
        <ul class="oform-tabs-bar"></ul>
        <div class="oform-tabs-viewport">
            <div class="oform-tabs-main"></div>
        </div>
    </div>
</div>
<div class="ui-arena"></div>
*/};
//===================================================================
return ZUI.def("ui.oform", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/oform/oform.css",
    //...............................................................
    init : function(options){
        var UI = this;
        $z.setUndefined(options, "mergeData", true);
        $z.setUndefined(options, "mode", "flow");
        $z.setUndefined(options, "hideGroupTitleWhenSingle", true);
        $z.setUndefined(options, "actions", []);
        $z.setUndefined(options, "field_i18n", {"zh-cn":{}});
        $z.setUndefined(options, "fields", [{
            others : true,
            title  : "i18n:others"
        }]);
        
        // 首先分析消息字符串，将其扩展到 UI 本身的字符串集合里
        var mm = options.field_i18n[UI.lang];
        var i18nKeys = Object.keys(options.field_i18n);
        if(!mm && i18nKeys.length > 0)
            mm = options.field_i18n[i18nKeys[0]];
        _.extend(UI._msg_map, mm);

        // 整理 fields 字段
        var grpList = [];
        var grp;
        options.fields.forEach(function(fld){
            // 是组字段，则添加
            if(!_.isString(fld.key)){
                // 将之前的收集字段用组添加进列表
                if(grp){
                    grpList.push(grp)
                    grp = undefined;
                }
                // 解析自身的多国语言
                if(fld.title)
                    fld.title = UI.text(fld.title);

                // 解析自己所有的字段的多国语言标题
                // 确保自身的类型，有 items 就是普通组
                if(fld.items){
                    fld.groups = true;
                    fld.items.forEach(function(fld){
                        // 必须有 key
                        if(!_.isString(fld.key)){
                            throw "!!! oform said : fld noKey : " + $z.toJson(fld);
                        }
                        if(fld.title)
                            fld.title = UI.text(fld.title);
                    });
                }
                // 动态组
                else{
                    fld.others = true;
                }

                // 添加自身
                grpList.push(_.extend({
                    type   : "String",
                    editAs : "input"
                }, fld));
            }
            // 普通字段，归纳到组里
            else{
                // 初始化归纳组
                if(!grp)
                    grp = {
                        group  : true,
                        type   : "String",
                        editAs : "input",
                        items  : []
                    };
                // 解析自身的多国语言
                if(fld.title)
                    fld.title = UI.text(fld.title);
                // 记录到归纳组里
                grp.items.push(fld);
            }
        });
        // 最后确保最后一段被添加了
        if(grp){
            grpList.push(grp);
        }
        // 嗯，那么现在所有的顶层对象都是组了
        UI.groups = grpList;
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        UI.arena.empty();

        // 如果只有一个组，那么看看是否全部展示
        if(UI.groups.length==1 && UI.options.hideGroupTitleWhenSingle){
            UI.__draw_group_fields(UI.groups[0], UI.arena);
        }
        // 根据显示模式绘制字段组
        else{
            // 得到控制器
            var _C = UI[UI.options.mode];

            // 设置主区域
            _C.setupArena(UI);
        }
    },
    //...............................................................
    __draw_group_fields : function(grp, jq){
        var UI = this;
        grp.$el = $('<table class="oform-grp">').appendTo(jq);
        console.log("FLD  ", grp)
        grp.items.forEach(function(fld){
            fld.$el  = $('<tr class="oform-fld">').appendTo(grp.$el);
            fld.$nm  = $('<td class="oform-fldnm">').appendTo(fld.$el)
            fld.$val = $('<td class="oform-fldval">').appendTo(fld.$el)

            if(fld.icon)
                $(fld.icon).attr("tp","icon").appendTo(fld.$nm);
            $('<span tp="title">' + (fld.title||fld.key) + '</span>').appendTo(fld.$nm);
        });
    },
    //...............................................................
    flow : {
        setupArena : function(UI){
            var jq = UI.ccode("flow").appendTo(UI.arena);
            var jMain = jq.find(".oform-flow-main");

            // 循环绘制每个组
            UI.groups.forEach(function(grp){
                // 绘制组的标题
                if(grp.title){
                    UI.ccode("flowGroupHead").text(grp.title).appendTo(jMain);
                }
                // 绘制组内每个字段
                UI.__draw_group_fields(grp, jMain);
            });
        },
        resize : function(UI){

        }
    },
    //...............................................................
    tabs : {
        setupArena : function(UI){
            var jq = UI.ccode("tabs").appendTo(UI.arena);
            var jBar  = jq.find(".oform-tabs-bar");
            var jMain = jq.find(".oform-tabs-main");
            
            // 循环绘制
            UI.groups.forEach(function(grp, index){
                // 每个组的标题
                var jq = $('<li class="oform-tabs-li">').appendTo(jBar);
                jq.attr("grp-index", index).text(grp.title || "Group"+index);
            
                // 绘制组内每个字段
                UI.__draw_group_fields(grp, jMain);
            });
        },
        resize : function(UI){

        }
    },
    //...............................................................
    events : {
        "click .ui-mask-closer" : function(e){
            this.close();
        }
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var _C = UI[UI.options.mode];
        _C.resize(UI);
    }
});
//===================================================================
});
})(window.NutzUtil);