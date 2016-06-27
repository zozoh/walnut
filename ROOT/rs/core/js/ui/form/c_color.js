(function($z){
$z.declare([
    'zui',
    'jquery-plugin/slidebar/slidebar'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-color">
    <div class="preview"><div></div></div>
    <input type="color" class="color">
    <input type="text"  class="hex">
    <input type="text"  class="alpha">
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_color", {
    css  : "theme/jqp/slidebar/slidebar.css",  
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .preview" : function(e){
            var jAlpha = this.arena.find(".alpha");
            if(!jAlpha.val())
                jAlpha.val("1.0");
            var color  = this.__get_color();
            var jColo  = this.arena.find(".color");
            if(color)
                jColo.val(color.HEX);
            jColo.click();
        },
        "change input.color" : function(e){
            this.arena.find(".hex").val($(e.currentTarget).val());
            this.__update(true);
        },
        "change input.hex" : function(){
            var jAlpha = this.arena.find(".alpha");
            if(!jAlpha.val()){
                jAlpha.val("1.0");
            }
            this.__update(true);
        },
        "change input.alpha" : function(e){
            var jAlpha = $(e.currentTarget);
            // 必须为 0-1 的数字
            var v = jAlpha.val() * 1;

            if(!_.isNumber(v) || !(v>=0 && v<=1)){
                jAlpha.val(1);
            }

            // 更新
            this.__update(true);
        },
        "click input.alpha" : function(e){
            var UI     = this;
            var jAlpha = $(e.currentTarget);
            if(jAlpha.val()){
                jAlpha.slidebar({
                    ruler : 4,
                    valueBy : function(v){
                        // console.log("valueBy", v, Math.round(v))
                        return Math.round(v);
                    },
                    change : function(v, pos){
                        jAlpha.val(v/100);
                        UI.__update(true);
                    }
                });
                jAlpha.slidebar("val", jAlpha.val()*100);
            }
        }
    },
    //...............................................................
    depose: function(){
        var UI = this;
        var jAlpha = UI.arena.find(".alpha");
        //console.log("I am depose", jAlpha.size())
        jAlpha.slidebar("destroy");
    },
    //...............................................................
    __get_color : function(){
        var UI    = this;
        var hex   = $.trim(UI.arena.find(".hex").val());
        var alpha = UI.arena.find(".alpha").val() * 1;
        if(!hex)
            return null;
        return $z.parseColor(hex, alpha);
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
    __update : function(val){
        var UI     = this;
        var color  = val === true 
                        ? UI.__get_color()
                        : val ? $z.parseColor(val) : null;
        var jHex   = UI.arena.find(".hex");
        var jAlpha = UI.arena.find(".alpha");
        var jPrew  = UI.arena.find(".preview>div");
        // 没颜色，清空
        if(!color){
            jHex.val("");
            jAlpha.val("");
            jPrew.css("background-color","");
        }
        else {
            //console.log(UI.arena.find(".color, .hex").size(), "["+color.HEX+"]")
            jHex.val(color.HEX);
            jAlpha.val(color.alpha == 1 ? "1.0" : color.alpha);
            jPrew.css("background-color",color.RGBA);
        }
        // true 表示要通知事件
        if(val === true){
            UI.__on_change();
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI.__get_color();
        });
    },
    //...............................................................
    setData : function(val, jso){
        //console.log(val)
        var UI = this;
        this.ui_parse_data(val, function(s){
            UI.__update(s)
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);