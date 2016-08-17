(function($z){
$z.declare([
    'zui', 
    'wn/util',
    'ui/pop/pop_browser'
], 
function(ZUI, Wn, PopBrowser){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <span code-id="obj" class="picker-obj">
        <i class="oicon"></i>
        <a target="_blank"></a>
    </span>
</div>
<div class="ui-arena picker opicker">
    <div class="picker-box"></div>
    <div class="picker-btn picker-choose">{{choose}}</div>
    <div class="picker-btn picker-clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.picker.opicker", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/picker/picker.css",
    //...............................................................
    events : {
        "click .picker-choose" : function(){
            var UI    = this;
            var conf  = UI.options.setup || {};
            new PopBrowser(_.extend({
                checkable : false,
                sidebar   : false,
                canOpen : function(o){
                    return o.race == 'DIR';
                },
                on_ok : function(objs){
                    if(objs && objs.length > 0){
                        UI._update(objs);
                    }
                }
            }, conf)).render();
        },
        "click .picker-clear" : function(){
            this._update();
        }
    },
    //...............................................................
    init : function(options) {
        $z.setUndefined(options, "setup", {});
        $z.setUndefined(options, "clearable", true);
        $z.setUndefined(options, "keeyAppend", true);
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        // 不用取消
        if(!opt.clearable){
            UI.arena.find(".picker-clear").remove();
        }
        // 标记多选
        if(opt.setup.checkable){
            UI.arena.addClass("picker-multi");
        }
        // 标记折行
        if(opt.wrapButton)
            UI.arena.attr("wrap-button", "yes");
    },
    //...............................................................
    setData : function(obj){
        console.log("opicker setData")
        this.ui_parse_data(obj, function(o){
            this._update(o, true);
        });
    },
    //...............................................................
    // 接受标准的 WnObj
    _update : function(o, quit){
        var UI  = this;
        var opt = UI.options;
        var jBox = UI.arena.find(".picker-box");
        // 清除数据
        if(!o){
            jBox.empty();
        }
        // 多选
        else if(opt.setup.checkable) {
            // 确保是数组
            if(!_.isArray(o)){
                o = [o];
            }
            // 是否保持原来的数据
            if(!opt.keeyAppend)
                jBox.empty();
            // 循环添加
            for(var i=0; i<o.length; i++){
                UI.__append_item(o[i], jBox);
            }
        }
        // 单选
        else{
            // 确保不是数组
            if(_.isArray(o)){
                o = o.length > 0 ? o[0] : undefined;
            }
            // 清除数据
            jBox.empty();
            
            // 添加
            UI.__append_item(o, jBox);
        }
        // 回调事件
        if(!quit)
            UI.__on_change();
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    __append_item : function(o, jBox){
        var UI = this;
        jBox = jBox || UI.arena.find(".picker-box")
        var jq = UI.ccode("obj").data("@OBJ", o);
        jq.find("i")
            .attr("otp", Wn.objTypeName(o));
        jq.find("a")
            .prop("href", "/a/open/wn.browser?ph=id:"+o.id)
            .text(Wn.objDisplayName(UI, o));
        jBox.append(jq);
    },
    //...............................................................
    getData : function(){
        return this.ui_format_data(function(opt){
            var re = [];
            this.arena.find(".picker-obj").each(function(){
                re.push($(this).data("@OBJ"));
            });
            if(!opt.setup.checkable){
                re = re.length > 0 ? re[0] : null;
            }
            return re;
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);