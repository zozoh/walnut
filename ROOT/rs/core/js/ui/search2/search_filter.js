(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena search-filter">
    <div class="flt-keyword">
        <input placeholder="{{search.filter.tip}}"
            spellcheck="false"
            with-form-btn="yes">
        <div class="flt-icon"><i class="zmdi zmdi-search"></i></div>
        <div class="flt-form-btn"><i class="zmdi zmdi-settings"></i></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.search_filter", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(opt){
        $z.setUndefined(opt, "keyField", ["nm"]);
        $z.setUndefined(opt, "forceWildcard", true);

        if(!_.isArray(opt.keyField))
            opt.keyField = [opt.keyField];
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
        var opt = UI.options;
        var jInput = UI.arena.find(".flt-keyword input");

        // 初始化查询字符串
        jInput.val(opt.query||"");

        // 是否显示表单按钮
        if(!opt.form) {
            UI.arena.find(".flt-form-btn").remove();
            jInput.removeAttr("with-form-btn");
        }
    },
    //..............................................
    setKeyword : function(str) {
        this.arena.find("input").val(str||"");
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
        var UI  = this;
        var opt = UI.options;
        return this.ui_format_data(function(){
            // 查询的基础
            var mch = $z.extend({}, UI.$el.data("@M"));
            var srt = $z.extend({}, UI.$el.data("@S"));
            
            // 处理关键字
            var kwd = $.trim(UI.arena.find("input").val());

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
                //.............................
                // 找到纯字符串：作为关键字
                if(m[14]){
                    ss.push(m[14]);
                }
                else if(m[13]){
                    ss.push(m[13]);
                }
                else if(m[11]){
                    ss.push(m[11]);
                }
                //.............................
                // 找到等式
                else if(m[7]){
                    mch[m[8]] = m[9];
                }
                else if(m[4]){
                    mch[m[5]] = m[6];
                }
                else if(m[1]){
                    mch[m[2]] = $z.strToJsObj(m[3], UI.parent.getFieldType(m[2]));
                }
                //.............................
                // 继续执行
                m = regex.exec(kwd);
            }

            // 根据 keyField 的设定，添加字段
            for(var i=0; i<ss.length; i++){
                UI._fill_key_field(mch, ss[i]);
            }

            // 返回最后结果
            return {
                match : mch,
                sort  : srt
            };
        });
    },
    _fill_key_field : function(mch, str){
        var UI  = this;
        var opt = UI.options;
        // 根据 keyField 的设定，添加字段
        for(var i=0; i<opt.keyField.length; i++){
            var kf  = opt.keyField[i];
            var key = null;
            // F(str):key
            if(_.isFunction(kf)){
                key = kf(str);
            }
            // {regex:/../, key:"xxx"}
            else if(kf.regex && _.isString(kf.key)){
                if(new RegExp(kf.regex).test(str))
                    key = kf.key;
            }
            else if(kf && _.isString(kf)){
                var pos = kf.indexOf(":^");
                // "mobile:^[0-9]+$"
                if(pos>0){
                    if(new RegExp(kf.substring(pos+1)).test(str))
                        key = kf.substring(0, pos);
                }
                // "nm"
                else {
                    key = kf;
                }
            }
            // 如果 str 以 ^ 开头，则为正则表达式，不管它
            // 否则看看是否要强制升级通配符
            if(opt.forceWildcard && !/^\^/.test(str)){
                str = "^.*" + str + ".*";
            }
            // 那么最后判断一下是否取到 key 了
            if(key){
                mch[key] = str;
                break;
            }
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);