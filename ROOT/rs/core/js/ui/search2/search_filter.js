(function($z){
$z.declare(['zui', 'ui/form/form'], function(ZUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena search-filter">
    <div class="flt-keyword">
        <input placeholder="{{search.filter.tip}}"
            spellcheck="false"
            with-ass-btn="yes">
        <div class="flt-icon"><i class="zmdi zmdi-search"></i></div>
        <div class="flt-ass-btn"><i class="zmdi zmdi-settings"></i></div>
    </div>
    <div class="flt-assist">
        <div class="flt-ass-mask"></div>
        <div class="flt-ass-form" ui-gasket="form"></div>
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

        // 修改助理
        if(opt.assist) {
            $z.setUndefined(opt.assist, "width", "inbox");
            $z.setUndefined(opt.assist, "autoOpen", true);
        }

        // 默认的记录一下自己原始的属性
        this.__old_data = "{}";
    },
    //..............................................
    events : {
        // 点击打开助理层
        "click .flt-ass-btn" : function(e) {
            this.toggleAssist();
        },
        // 收起助理层，并应用修改
        "click .flt-ass-mask" : function(){
            this.__on_change();
            this.closeAssist();
        },
        // 聚焦打开助理层
        "focus .flt-keyword > input" : function(){
            var ass = this.options.assist;
            if(ass && ass.autoOpen) {
                this.openAssist();
            }
        },
        // 下面三个时间，完美检测中文输入
        "compositionstart .flt-keyword > input" : function(e){
            this.__compositionstart = true;
        },
        "compositionend .flt-keyword > input" : function(e){
            this.__compositionstart = false;
            this.__do_when_input();
        },
        "input .flt-keyword > input" : function(e){
            if(this.__compositionstart)
                return;
            this.__do_when_input();
        },
        // 特殊键盘事件
        "keydown .flt-keyword > input" : function(e){
            var UI = this;
            // 回车
            if(13 == e.which) {
                e.preventDefault();
                UI.__on_change();
                UI.closeAssist();
            }
            // 上箭头
            else if(38 == e.which) {
                UI.closeAssist();
            }
            // 下箭头
            else if(40 == e.which) {
                UI.openAssist();
            }
        },
    },
    //..............................................
    redraw : function(){
        var UI = this;
        var opt = UI.options;
        var jInput = UI.arena.find(".flt-keyword input");

        // 初始化查询字符串
        jInput.val(opt.query||"");

        // 是否显示表单按钮
        if(!opt.assist) {
            UI.arena.find(".flt-ass-btn").remove();
            jInput.removeAttr("with-ass-btn");
            UI.arena.removeAttr("show-ass");
            UI.arena.find(".flt-assist").remove();
            return;
        }
        // 确保属性
        jInput.attr("with-ass-btn", "yes");

        // 那么就显示表单咯
        UI._ui_assist = new FormUI(_.extend({
            displayMode : "compact",
        }, opt.assist.form, {
            parent : UI, 
            gasketName : "form",
            fitparent : opt.assist.height ? true : false,
            on_update : function(){
                UI.__update_input_by_assist();
            }
        })).render(function(){
            UI.__update_assist_by_input();
            UI.defer_report("form");
        });

        // 返回
        return ["form"];
    },
    //..............................................
    toggleAssist : function(){
        if(this.arena.attr("show-ass")) {
            this.closeAssist();
        } else {
            this.openAssist();
        }
    },
    //..............................................
    openAssist : function(){
        if(this._ui_assist){
            this.arena.attr("show-ass", true);
            this.resize(true);
        }
    },
    //..............................................
    closeAssist : function(){
        this.arena.removeAttr("show-ass");
    },
    //..............................................
    setKeyword : function(str) {
        this.arena.find("input").val(str||"");
    },
    //..............................................
    __on_change : function() {
        var data = this.getData();
        var json = $z.toJson(data);
        if(json != this.__old_data) {
            this.__old_data = json;
            this.trigger("filter:change", data);
        }
    },
    //..............................................
    setData : function(data){
        var UI = this;
        UI.ui_parse_data(data, function(data2){
            UI.__query_base = data2 || {};
        });
    },
    //..............................................
    __do_when_input : function() {
        var UI  = this;
        var opt = UI.options;
        var ass = opt.assist;

        // var jIn = UI.arena.find("> .box > input");
        // console.log("in>>", jIn.val());
        // 自动打开助理
        if(ass && ass.autoOpen) {
            UI.openAssist();
        }

        // 如果有助理，同步修改的值
        if(UI._ui_assist) {
            UI.__update_assist_by_input();
        }
    },
    //..............................................
    __update_assist_by_input : function(){
        var UI = this;
        if(UI._ui_assist) {
            var cri = UI._get_criteria();
            UI._ui_assist.setData(cri.match);
        }
    },
    __update_input_by_assist : function(){
        var UI = this;
        if(UI._ui_assist) {
            // 得到原始的值
            var cri = UI._get_criteria();

            // 与表单里的合并
            var mat = UI._ui_assist.getData();
            //console.log(mat)
            _.extend(cri.match, mat);
            

            // 更新到输出框里
            var ss = [];
            for(var key in cri.match) {
                var val = cri.match[key];
                if(_.isNull(val) || _.isUndefined(val))
                    continue;
                ss.push(key + ":" + cri.match[key]);
            }
            var str = ss.concat(cri.keywords).join(" ") || "";
            UI.arena.find(".flt-keyword input").val(str);
        }
    },
    //..............................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        return this.ui_format_data(function(){
            var cri = UI._get_criteria();
            var re  = {};

            // 根据 keyField 的设定，添加字段
            for(var i=0; i<cri.keywords.length; i++){
                UI._fill_key_field(re, cri.keywords[i]);
            }

            // 返回最后结果
            return _.extend(re, cri.match);
        });
    },
    /*..............................................
    得到一个查询对象，格式为: 
    {
        keywords : [],
        match : {...}    
    }
    */
    _get_criteria : function() {
        var UI = this;
        // 查询的基础
        var mch = $z.extend({}, UI.__query_base);
        
        // 处理关键字
        var kwd = $.trim(UI.arena.find("input").val());

        var regex = /((\w+)[:=]([^'" ]+))|((\w+)[:=]"([^"]+)")|((\w+)[:=]'([^']+)')|('([^']+)')|("([^"]+)")|([^ \t'"]+)/g;
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

        // 返回
        return {
            keywords : ss,
            match    : mch,
        };
    },
    //..............................................
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
    },
    //..............................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;
        var ass = opt.assist;
        var jKwd  = UI.arena.find(">.flt-keyword");
        var jForm = UI.arena.find(".flt-ass-form");

        if(ass) {
            var uiSearch = UI.parent;
            // 修订宽度
            if("inbox" == opt.assist.width) {
                jForm.css("width", jKwd.outerWidth());
            }
            // 剩下的自动计算
            else {
                jForm.css("width",
                    $z.dimension(opt.assist.width, uiSearch.arenaWidth()));
            }
            // 修订高度
            if(!_.isUndefined(opt.assist.height)){
                jForm.css("height",
                    $z.dimension(opt.assist.width, uiSearch.arenaHeight()));
            }

            // 确保停靠
            $z.dock(jKwd, jForm, "H");
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);