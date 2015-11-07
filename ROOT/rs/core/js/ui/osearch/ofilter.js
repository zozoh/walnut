(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
<div class="ofilter-keyword">
    <input placeholder="{{osearch.filter.tip}}">
</div>
</div>
*/};
//==============================================
return ZUI.def("ui.ofilter", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(options){
        $z.setUndefined(options, "keyField", ["nm"]);
        if(!_.isArray(options.keyField))
            options.keyField = [options.keyField];
    },
    //..............................................
    events : {
        "change input" : function(e){
            var q = this.getData();
            //console.log(q)
            this.trigger("filter:change", q);
        }
    },
    //..............................................
    redraw : function(callback){
    },
    //..............................................
    resize : function(){
        var UI = this;
        // var jKeyword = UI.arena.children(".ofilter-keyword");
        // var H = UI.arena.height();
        // var h_keyword = jKeyword.outerHeight(true);
        // var padding = Math.max(0, parseInt((H - h_keyword)/2));
        // jKeyword.css("padding", padding);
    },
    //..............................................
    setData : function(o){
        var UI = this;
        UI.$el.attr("pid", o.id);
        return this;
    },
    //..............................................
    getData : function(){
        var UI = this;
        // 查询总是带着父 ID
        var q = {
            pid : UI.$el.attr("pid")
        };
        // 处理关键字
        var kwd = UI.arena.find("input").val();
        if(kwd && UI.options.keyField.length>0) {
            var kwdList = [];
            UI.options.keyField.forEach(function(key){
                var map = {};
                map[key] = "*" + kwd + "*";
                kwdList.push(map);
            });
            console.log(kwdList)
            // 只有一个，那么不用『或』了
            if(kwdList.length == 1){
                _.extend(q, kwdList[0]);
            }
            // 多个条件『或』
            else{
                q["$or"] = kwdList;
            }
        }
        // 返回
        return {
            condition : $z.toJson(q)
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);