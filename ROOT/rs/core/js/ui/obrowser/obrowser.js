(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <button class="t1">T1</button>
    <button class="t2">T2</button>
    <button class="t3">T3</button>
    <button class="t4">T4</button>
    <div class="obrowser">
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.obrowser", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/obrowser/obrowser.css",
    //..............................................
    init : function(options){
        // 初始化控件的数据模块
        this._objs    = new (Backbone.Model.extend({}));
        this._runtime = new (Backbone.Model.extend({}));

        this._objs.on("change", function(e, obj){
            console.log(this)
            Array.from(arguments).forEach(function(o, i){
                console.log("-------------arg["+i+"]", o);
                if(_.isFunction(o.toJSON)){
                    console.log("toJson:", o.toJSON())
                }
            });
            console.log("alllllll:\n", this.toJSON());
        });

    },
    //..............................................
    events : {
        "click .t1" : function(){
            this._objs.set("A", {
                id : "A",
                pos : {x:1, y:2},
                name : "hahahah"
            });  
        },
        "click .t2" : function(){
            this._objs.set("B", {
                id : "c",
                pos : {x:3, y:4},
                name : "zozoh"
            });
        },
        "click .t3" : function(){
            this._objs.push("xyz", {
                id : "c",
                pos : {x:3, y:4},
                name : "zozoh"
            });
        }
    },
    //..............................................
    redraw : function(){
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);