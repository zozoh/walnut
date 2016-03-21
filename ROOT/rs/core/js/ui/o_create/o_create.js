(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="nodir" class="ocreate-warn">
        <i class="fa fa-warning"></i> <b>{{ocreate.nodir}}</b>
    </div>
    <div code-id="noitem" class="ocreate-warn">
        <i class="fa fa-warning"></i> <b>{{ocreate.noitem}}</b>
    </div>
    <div code-id="empty" class="ocreate-warn">
        <i class="fa fa-warning"></i> <b>{{ocreate.empty}}</b>
    </div>
    <div code-id="invalid" class="ocreate-warn">
        <i class="fa fa-warning"></i> <b>{{ocreate.invalid}}</b>
    </div>
    <li code-id="li"><i class="oicon"></i><b></b></li>
    <div code-id="body" class="ui-oicon-16">
        <div class="ocreate-list"><div>
            <header>{{ocreate.title}}</header>
            <ul></ul>
        </div></div>
        <div class="ocreate-view">
            <div class="ocreate-thumb"><img></div>
            <div class="ocreate-text"></div>
            <div class="ocreate-input"><input placeholder="{{ocreate.newtip}}"></div>
            <div class="ocreate-tip"></div>
        </div>
        <div class="ocreate-actions"><b md="ok">{{ok}}</b><b md="cancel">{{cancel}}</b></div>
    </div>
</div>
<div class="ui-arena ocreate" ui-fitparent="yes"></div>
*/};
//==============================================
return ZUI.def("ui.o_create", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/o_create/o_create.css",
    i18n : "ui/o_create/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        $z.setUndefined(opt, "validate", function(nm){
            if(/['",\/\\%&*!~`\^?;|#]/.test(nm)){
                return false;
            }
            return true;
        });
    },
    //...............................................................
    events : {
        "click .ocreate-list li" : function(e){
            var UI   = this;
            var jLi  = $(e.currentTarget);
            var tp   = jLi.find(".oicon").attr("otp");
            var race = jLi.attr("race");
            var text = jLi.find("b").text();
            var tip  = jLi.attr("tip");

            // 取消其他的高亮 & 将自己高亮
            UI.arena.find(".ocreate-list li").removeClass("highlight");
            jLi.addClass("highlight");

            // 显示缩略图
            UI.arena.find(".ocreate-thumb img").prop("src", "/o/thumbnail/type:"+tp+"?sh=128");

            // 显示文字
            UI.arena.find(".ocreate-text").html(text + '<em>(' + tp + ')</em>');

            // 显示提示信息
            UI.arena.find(".ocreate-tip").html(tip);
        },
        'click .ocreate-actions b[md="ok"]' : function(){
            this.__do_create();
        },
        'click .ocreate-actions b[md="cancel"]' : function(){
            var UI   = this;
            var opt  = UI.options;
            $z.invoke(opt, "on_cancel", [], UI);
        },
        'change .ocreate-input input' : function(){
            this.__do_create();
        }     
    },
    __do_create : function(on_error){
        var UI   = this;
        var opt  = UI.options;
        var jTip = UI.arena.find(".ocreate-tip");

        // 正在创建，等等
        if(UI.arena.attr("ing"))
            return;

        // 标识
        UI.arena.attr("ing", "yes");

        // 得到输入的名称
        var nm = $.trim(UI.arena.find(".ocreate-input input").val());

        // 显示错误
        if(!nm) {
            UI.ccode("empty").appendTo(jTip.empty());
            UI.arena.removeAttr("ing");
            return;
        }

        // 校验
        if(!opt.validate(nm)) {
            UI.ccode("invalid").appendTo(jTip.empty());
            UI.arena.removeAttr("ing");
            return;
        }

        // 执行并返回
        var jLi  = UI.arena.find(".ocreate-list li.highlight");
        var obj = {
            pid  : UI.$el.attr("pid"),
            tp   : jLi.find(".oicon").attr("otp"),
            race : jLi.attr("race"),
            nm   : nm
        };
        var cmdText = $z.tmpl("obj -o -new \'<%=obj%>\'")($z.toJson(obj));
        // console.log(cmdText)
        Wn.exec(cmdText, function(re){
            UI.arena.removeAttr("ing");
            // 如果出错了
            if(/^e./.test(re)){
                UI.ccode("invalid").appendTo(jTip.empty()).find("b").text(re);
            }
            // 成功搞定，调用回调
            else {
                var o2 = $z.fromJson(re);
                $z.invoke(opt, "on_ok", [o2], UI);
            }
        });
    },
    //...............................................................
    __draw_body : function(clist, o) {
        var UI  = this;
        var opt = UI.options;

        // 显示主体
        UI.ccode("body").appendTo(UI.arena);

        // 首先显示可以创建的列表
        var jUl = UI.arena.find(".ocreate-list ul");
        for(var i=0; i<clist.length; i++){
            var cl  = clist[i];
            var jLi = UI.ccode("li").appendTo(jUl);
            jLi.find(".oicon").attr("otp", cl.tp);
            jLi.find("b").text(UI.str(cl.text, cl.tp));
            jLi.attr("race", cl.race);
            if(cl.tip)
                jLi.attr("tip", UI.str(cl.tip));
        }

        // 模拟点击第一项
        jUl.children().first().click();

    },
    //...............................................................
    update : function(o) {
        var UI  = this;
        var opt = UI.options;
        
        // 只能是目录
        if('DIR' != o.race) {
            UI.ccode("nodir").appendTo(UI.arena);
            return;
        }

        // 记录 pid
        UI.$el.attr("pid", o.id);

        // 查询一下对象可以被创建的列表
        var cmdText = o.tp ? "appclist type:"+o.tp
                           : "appclist id:"+o.id;
        Wn.exec(cmdText, function(re){
            var clist = re ? $z.fromJson(re) : [];
            if(clist.length == 0){
                UI.ccode("noitem").appendTo(UI.arena);
            }
            // 绘制主体
            else {
                UI.__draw_body(clist, o);
            }
        });


    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);