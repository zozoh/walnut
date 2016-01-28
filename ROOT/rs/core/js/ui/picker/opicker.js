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
        <b></b>
    </span>
</div>
<div class="ui-arena picker opicker">
    <div class="picker-box"></div>
    <div class="picker-btns"><b
        class="picker-choose">{{choose}}</b><b
        class="picker-clear">{{clear}}</b></div>
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
    },
    //...............................................................
    setData : function(obj){
        var UI   = this;
        var opt  = UI.options;
        var context = opt.context || UI;
        // 同步
        if(_.isFunction(opt.parseData)){
            var o = opt.parseData.call(context, obj);
            UI._update(o);
        }
        // 异步 
        else if(_.isFunction(opt.asyncParseData)){
            opt.asyncParseData.call(context, obj, function(o){
                UI._update(o);
            });
        }
        // 直接使用
        else{
            UI._update(obj);
        }
    },
    //...............................................................
    // 接受标准的 WnObj
    _update : function(o){
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
    },
    //...............................................................
    __append_item : function(o, jBox){
        jBox = jBox || this.arena.find(".picker-box")
        var jq = this.ccode("obj").data("@OBJ", o);
        jq.find("i").attr("otp", Wn.objTypeName(o));
        jq.find("b").text(Wn.objDisplayName(o));
        jBox.append(jq);
    },
    //...............................................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var re = [];
        UI.arena.find(".picker-obj").each(function(){
            re.push($(this).data("@OBJ"));
        });
        if(!opt.setup.checkable){
            re = re.length > 0 ? re[0] : null;
        }
        if(_.isFunction(opt.formatData)){
            return opt.formatData.call(context, re);
        }
        return re;
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jBtn = UI.arena.find(".picker-btns");
        var jBox = UI.arena.find(".picker-box");
        jBox.css("padding-right", jBtn.outerWidth(true)+"px");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);