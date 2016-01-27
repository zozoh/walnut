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
    <div class="picker-btn">
        <b class="picker-choose">{{choose}}</b>
    </div>
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
                checkable :false,
                canOpen : function(o){
                    return o.race == 'DIR';
                },
                on_ok : function(objs){
                    if(objs && objs.length > 0){
                        UI.setData(objs[0]);
                    }
                }
            }, conf)).render();
        }
    },
    //...............................................................
    setData : function(o){
        var UI   = this;
        var jBox = UI.arena.find(".picker-box").empty();
        if(!o){
            UI.$el.removeData("@OBJ");
        }
        else{
            UI.$el.data("@OBJ");
            var jq = UI.ccode("obj");
            jq.find("i").attr("otp", Wn.objTypeName(o));
            jq.find("b").text(Wn.objDisplayName(o));
            jBox.append(jq);
        }
    },
    //...............................................................
    getData : function(o){
        return UI.$el.data("@OBJ");
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jBtn = UI.arena.find(".picker-btn");
        var jBox = UI.arena.find(".picker-box");
        jBox.css("line-height", (jBtn.height() - 2)+"px");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);