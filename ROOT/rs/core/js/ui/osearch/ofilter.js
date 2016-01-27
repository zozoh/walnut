(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
<div class="ofilter-keyword">
    <input placeholder="{{osearch.filter.tip}}">
    <div class="ofilter-icon"><i class="fa fa-search"></i></div>
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
    redraw : function(){
        var UI = this;
        UI.arena.find(".ofilter-keyword input").val(UI.options.query);
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
        var UI  = this;
        var opt = UI.options;
        // 查询总是带着父 ID
        var q = {};
        var pid = UI.$el.attr("pid");
        if(pid)
            q.pid = pid;
        
        // 处理关键字
        var kwd = UI.arena.find("input").val();

        var regex = /((\w+)=([^'" ]+))|((\w+)="([^"]+)")|((\w+)='([^']+)')|('([^']+)')|("([^"]+)")|([^ \t'"]+)/g;
        var i = 0;
        var m = regex.exec(kwd);
        var ss = [];
        while(m){
            // 控制无限循环
            if((i++) > 100)
                break;
            // m.forEach(function(v, index){
            //     console.log(i+"."+index+")", v);
            // });
            // 找到纯字符串 
            if(m[14]){
                ss.push(m[14]);
            }
            else if(m[13]){
                ss.push(m[13]);
            }
            else if(m[11]){
                ss.push(m[11]);
            }
            // 找到等式
            else if(m[7]){
                q[m[8]] = m[9];
            }
            else if(m[4]){
                q[m[5]] = m[6];
            }
            else if(m[1]){
                q[m[2]] = $z.strToJsObj(m[3]);
            }
            // 继续执行
            m = regex.exec(kwd);
        }

        kwd = ss.join(" ");
        if(kwd && opt.keyField.length>0) {
            var kwdList = [];
            opt.keyField.forEach(function(key){
                var map = {};
                map[key] = "*" + kwd + "*";
                kwdList.push(map);
            });
            // console.log(kwdList)
            // 只有一个，那么不用『或』了
            if(kwdList.length == 1){
                _.extend(q, kwdList[0]);
            }
            // 多个条件『或』
            else{
                q["$or"] = kwdList;
            }
        }

        // 最后如果有扩展的定制函数，调用一下
        q = $z.invoke(opt, "format", [q], UI) || q;

        // 返回
        var json = $z.toJson(q);
        return {
            condition : json
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);