(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena srh-flt">
    <div class="flt-keyword">
        <input placeholder="{{srh.filter.tip}}">
        <div class="flt-icon"><i class="fa fa-search"></i></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.srh_flt", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(options){
        $z.setUndefined(options, "keyField", ["nm"]);
        if(!_.isArray(options.keyField))
            options.keyField = [options.keyField];
    },
    //..............................................
    events : {
        "change input" : function(e){
            var flt = this.getData();
            //console.log(q)
            this.trigger("filter:change", flt);
        }
    },
    //..............................................
    redraw : function(){
        var UI = this;
        UI.arena.find(".flt-keyword input").val(UI.options.query);
    },
    //..............................................
    setData : function(qbase){
        this.ui_parse_data(qbase, function(qbase){
            this.$el.data("@M", qbase.match || {});
            this.$el.data("@S", qbase.sort  || {});
        });
    },
    //..............................................
    getData : function(){
        return this.ui_format_data(function(){
            var UI  = this;
            var opt = UI.options;
            
            // 查询的基础
            var mch = $z.extend({}, UI.$el.data("@M"));
            var srt = $z.extend({}, UI.$el.data("@S"));
            
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
                    mch[m[8]] = m[9];
                }
                else if(m[4]){
                    mch[m[5]] = m[6];
                }
                else if(m[1]){
                    mch[m[2]] = $z.strToJsObj(m[3]);
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
                    _.extend(mch, kwdList[0]);
                }
                // 多个条件『或』
                else{
                    mch["$or"] = kwdList;
                }
            }

            // 返回最后结果
            return {
                match : mch,
                sort  : srt
            };
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);