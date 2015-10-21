(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="flow" class="oform-flow">
        <div class="oform-actions"></div>
        <div class="oform-flow-main"></div>
    </div>
    <div code-id="flowGroupHead" class="oform-flow-ghead"></div>
    <div code-id="tabs" class="oform-tabs">
        <ul class="oform-tabs-bar"></ul>
        <div class="oform-tabs-viewport">
            <div class="oform-tabs-main"></div>
        </div>
        <div class="oform-actions"></div>
    </div>
</div>
<div class="ui-arena" ui-fitparent="yes"></div>
*/};
//===================================================================
return ZUI.def("ui.oform", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/oform/oform.css",
    //...............................................................
    init : function(options){
        var UI = this;
        $z.evalFunctionField(options);
        $z.setUndefined(options, "mergeData", true);
        $z.setUndefined(options, "mode", "flow");
        $z.setUndefined(options, "hideGroupTitleWhenSingle", true);
        $z.setUndefined(options, "actions", []);
        $z.setUndefined(options, "fields", [{
            others : true,
            title  : "i18n:others"
        }]);
        $z.setUndefined(options, "idKey", "id");

        // 整理 fields 字段
        var grpList = [];
        var grp;
        options.fields.forEach(function(fld){
            // 是组字段，则添加
            if(!_.isString(fld.key)){
                // 将之前的收集字段用组添加进列表
                if(grp){
                    grpList.push(grp);
                }
                // 当前组
                grp = _.extend({
                    type   : "string",
                    editAs : "input"
                }, fld);

                // 解析自身的多国语言
                if(grp.title)
                    grp.title = UI.text(grp.title);

                // 解析自己所有的字段的多国语言标题
                // 确保自身的类型，有 items 就是普通组
                if(grp.items){
                    grp.group = true;
                    grp.items.forEach(function(fld){
                        UI.normalize_field(fld, grp);
                    });
                }
                // 动态组
                else{
                    fld.others = true;
                    if(_.isString(fld.filter)){
                        fld.filter = new RegExp(fld.filter, "g");
                    }
                }

                // 添加自身
                grpList.push(grp);
                grp = undefined;
            }
            // 普通字段，归纳到组里
            else{
                // 初始化归纳组
                if(!grp) {
                    grp = {
                        group  : true,
                        type   : "string",
                        editAs : "input",
                        items  : []
                    };
                }
                // 整理
                UI.normalize_field(fld, grp);

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

        // 最后等重绘完毕模拟点击
        UI.on("ui:redraw", function(){
            UI.arena.find(".oform-tabs-li").first().click();
        });
    },
    //...............................................................
    // 调试用 dump 函数
    _dump_groups : function(noShowFieldDetail, grps){
        var UI = this;
        var str = "";
        if(grps) {
            if(!_.isArray(grps)){
                grps = [grps];
            }
        }else{
            grps = UI.groups;
        }
        grps.forEach(function(grp){
            str += (_.template("{{_gmode}}[{{title}}] type:'{{type}}'"
                              + " editAs:'{{editAs}}'"
                              + " {{items.length}} fields"))(_.extend({
                title : "NoTitle", type:"?", editAs:"?", 
                items : [], _gmode : grp.group ? "G" : "?"
            }, grp));
            // 简单显示字段名
            if(noShowFieldDetail) {
                str += " {";
                grp.items.forEach(function(fld){
                    str += '[' + fld.key + "]";
                });
                str += "}";
            }
            // 显示所有组的字段的细节
            else {
                if(grp.items && grp.items.length>0)
                    grp.items.forEach(function(fld){
                        str += '\n  @' + UI._dump_field(fld);
                    });
                else
                    str += "\n    ...";
            }
            str += "\n";
        });
        return str;
    },
    _dump_field : function(fld){
        return (_.template('"{{key}}" : {{type}} >>> {{editAs}} #{{title}}'))(_.extend({
            type : "?", editAs : "?"
        }, fld));
    },
    //...............................................................
    events : {
        "click .oform-tabs-li" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            if(jq.attr("highlight"))
                return;
            // 改变标签的显示样式
            jq.parents(".oform-tabs-bar").children().removeAttr("highlight");
            jq.attr("highlight", "true");
            
            // 显示当前的组
            var index = jq.attr("grp-index") * 1;
            UI.arena.find(".oform-tabs-main").children()
                .hide()
                    .eq(index).show();
        },
        "click .oform-abtn" : function(e){
            var UI = this;
            var jBtn = $(e.currentTarget);
            var aa = jBtn.data("@ACTION");
            var o = UI.getData();
            //console.log(o)
            var context = aa.context || UI;
            // 回调函数
            if(aa.handler){
                aa.handler.call(context, o);
            }
            // URL
            else if(aa.url){
                $.ajax(_.extend({
                    url    : url,
                    method : "POST",
                    contentType : "application/json",
                    dataType : "json",
                    data : $z.toJson(o)
                }, aa.ajax));
            }
            // 执行命令
            else if(aa.cmd && UI.exec){
                // 计算命令模板的上下文 
                var oJson = _.extend({}, o);
                if(oJson[UI.options.idKey])
                    delete oJson[UI.options.idKey];
                var d = {
                    o : o, 
                    json : $z.toJson(oJson).replace("'", "\\'")
                };
                // 用户要求自定义这个上下文
                if(_.isFunction(aa.cmd.data)){
                    d = aa.cmd.data(d);
                }
                // 生成命令字符串
                var str = (_.template(aa.cmd.command))(d);
                //console.log(str)
                // 执行命令
                UI.exec(str, {
                    context  : context,
                    done     : aa.cmd.done,
                    fail     : aa.cmd.fail,
                    complete : aa.cmd.complete
                });
            }
            // 不知道
            else{
                throw "Dont know how to run btn : " + jBtn.text() + ":\n" + $z.toJson(aa);
            }

        },
        "click .oform-e-bool-switch li" : function(e){
            var jq = $(e.currentTarget);
            jq.parents(".oform-e-bool-switch").find("li").removeClass("checked");
            jq.addClass("checked");
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        UI.arena.empty();

        // 得到控制器
        var _C = UI[UI.options.mode];

        // 设置主区域
        _C.setupArena(UI);

        // 最后绘制动作按钮
        if(UI.options.actions.length > 0)
            UI.__draw_actions();
        else
            UI.arena.find(".oform-actions").remove();
    },
    //...............................................................
    __draw_actions : function(){
        var UI = this;
        var jActions = UI.arena.find(".oform-actions");
        UI.options.actions.forEach(function(a){
            var jBtn = $('<div class="oform-abtn">');
            if(a.icon)
                jBtn.html(a.icon);
            if(a.text)
                $('<span>').text(UI.text(a.text)).appendTo(jBtn);
            jBtn.data("@ACTION", a);
            jBtn.appendTo(jActions);
        });
    },
    //...............................................................
    __draw_group_fields : function(grp, jq){
        var UI = this;
        grp.$el = $('<table class="oform-grp">').appendTo(jq);
        // 仅绘制普通组的字段，动态组的字段是不固定的，只有设置了数据才能绘制
        if(grp.group && grp.items && grp.items.length>0){
            grp.items.forEach(function(fld){
                UI.__append_field(grp,fld);
            });    
        }
    },
    __append_field : function(grp, fld){
        fld.$el  = $('<tr class="oform-fld">').appendTo(grp.$el);
        fld.$nm  = $('<td class="oform-fldnm">').appendTo(fld.$el)
        fld.$val = $('<td class="oform-fldval">').appendTo(fld.$el)

        if(fld.icon)
            $(fld.icon).attr("tp","icon").appendTo(fld.$nm);
        $('<span tp="title">' + (fld.title||fld.key) + '</span>').appendTo(fld.$nm);
    },
    //...............................................................
    flow : {
        setupArena : function(UI){
            var jq = UI.ccode("flow").appendTo(UI.arena);
            var jMain = jq.find(".oform-flow-main");

            // 循环绘制每个组
            UI.groups.forEach(function(grp){
                // 绘制组的标题
                if(UI.groups.length>1 || !UI.options.hideGroupTitleWhenSingle){
                   UI.ccode("flowGroupHead").text(grp.title || "NoTitle").appendTo(jMain);
                }
                // 绘制组内每个字段
                UI.__draw_group_fields(grp, jMain);
            });
        },
        resize : function(UI){
            var jMain = UI.arena.find(".oform-flow-main");
            var jActions = UI.arena.find(".oform-actions");
            var H = UI.arena.height();
            jMain.css("height", H - jActions.outerHeight());
        }
    },
    //...............................................................
    tabs : {
        setupArena : function(UI){
            var jq = UI.ccode("tabs").appendTo(UI.arena);
            var jBar  = jq.find(".oform-tabs-bar");
            var jMain = jq.find(".oform-tabs-main").attr("grp-nb", UI.groups.length);

            // 如果没必要显示标签，删除 
            if(UI.groups.length<=1 && UI.options.hideGroupTitleWhenSingle){
                jBar.remove(); 
            }
            
            // 循环绘制
            UI.groups.forEach(function(grp, index){
                // 每个组的标题
                if(jBar.size()>0){
                    var jq = $('<li class="oform-tabs-li">').appendTo(jBar);
                    jq.attr("grp-index", index).text(grp.title || "Group"+index);
                }
            
                // 绘制组内每个字段
                UI.__draw_group_fields(grp, jMain);
            });

        },
        resize : function(UI){
            var jBar = UI.arena.find(".oform-tabs-bar");
            var jViewPort = UI.arena.find(".oform-tabs-viewport");
            var jActions = UI.arena.find(".oform-actions");
            var H = UI.arena.height();
            jViewPort.css("height", H - jBar.outerHeight() - jActions.outerHeight());
        }
    },
    //...............................................................
    setData : function(o){
        //console.log(this._dump_groups());
        var UI = this;
        var dynamics = [];
        var keys = _.extend({}, o);
        // 保存原始对象
        UI.$el.data("@DATA", o);
        // 对每个组循环显示
        UI.groups.forEach(function(grp){
            // 普通组
            if(grp.group){
                grp.items.forEach(function(fld){
                    // 设置字段的编辑控件 
                    UI.edit_set(fld, o);
                    // 移除已经处理过的字段
                    if(keys[fld.key])
                        delete keys[fld.key];
                });
            }
            // 动态组
            else{
                dynamics.push(grp);
                // 清除自己的动态字段
                if(grp.items && grp.items.length > 0){
                    grp.items.forEach(function(fld){
                        fld.$el.remove();
                    });
                }
                grp.items = [];
            }
        });
        // 最后用动态组再消化一遍没处理过的字段
        dynamics.forEach(function(grp){
            for(var key in keys){
                if(!grp.filter || new RegExp(grp.filter).test(key)){
                    var v = o[key];
                    var vType = typeof v;
                    // 创建一个字段 
                    var fld = {
                        key    : key,
                        title  : key,
                        type   : vType,
                        editAs : getEditByType(UI, key, vType)
                    };
                    // 绘制这个字段
                    UI.__append_field(grp, fld);
                    grp.items.push(fld);
                    // 设置值
                    UI.edit_set(fld, o);
                    // 删除记录
                    delete keys[key];
                }
            }
        });
    },
    //...............................................................
    getData : function(){
        var UI = this;
        var re = UI.options.mergeData ? _.extend({}, UI.$el.data("@DATA")) : {};
        UI.groups.forEach(function(grp){
            if(grp.items.length > 0){
                grp.items.forEach(function(fld){
                    var v = UI.edit_get(fld);
                    // 如果返回 undefined，表示控件不想修改值
                    if(_.isUndefined(v)){
                        return;
                    }
                    // 更新数据的值
                    $z.setValue(re, fld.key, v);
                });
            }
        });
        console.log(re)
        return re;
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