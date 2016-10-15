(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/obrowser'
], 
function(ZUI, Wn, BrowserUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="el.lnk" class="el-lnk">
        <input placeholder="{{support.edit_link.tip}}">
        <div class="el-lnk-comment">{{support.edit_link.tip}}</div>
    </div>
</div>
<div class="ui-arena edit-link" ui-fitparent="yes">
    <header>
        <ul>
            <li tp="lnk">{{support.edit_link.lnk}}</li>
            <li tp="obj">{{support.edit_link.obj}}</li>
            <li tp="ext">{{support.edit_link.ext}}</li>
        </ul>
    </header>
    <section><div ui-gasket="main"></div></section>
</div>
*/};
//==============================================
return ZUI.def("ui.support.edit_link", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/support/edit_link.css",
    i18n : "ui/support/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click header li"  : function(e){
            var str = this._get_data();
            switch($(e.currentTarget).attr("tp")){
                case "lnk":
                    this._show_lnk(str);
                    break;
                case "obj":
                    this._show_obj(str);
                    break;
                case "ext":
                    this._show_ext(str);
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 如果没有定义扩展行为，移除对应标签
        if(!opt.ext) {
            UI.arena.find('>header>ul li[tp="ext"]').remove();
        }
    },
    //...............................................................
    __swich_tab : function(tp){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("header>ul");

        jUl.find('.highlight').removeClass("highlight");
        jUl.find('[tp="'+tp+'"]').addClass("highlight");
    },
    //...............................................................
    _show_lnk : function(val){
        var UI  = this;
        var opt = UI.options;

        // 切换标签
        UI.__swich_tab("lnk");
        
        // 释放插件并清空 
        UI.releaseSubUI("main");
        UI.arena.find("section>div").empty();

        // 绘制主体区域
        var jq = UI.ccode("el.lnk");
        jq.appendTo(UI.arena.find("section>div"));
        jq.find("input").val(val);
    },
    //...............................................................
    _show_obj : function(val){
        var UI  = this;
        var opt = UI.options;
        var m   = /^(id:)(.+)$/.exec(val);
        var o   = m ? Wn.getById(m[2]) : null;

        // 切换标签
        UI.__swich_tab("obj");

        // 清空区域
        UI.releaseSubUI("main");
        UI.arena.find("section>div").empty();

        // 绘制主体 
        new BrowserUI(_.extend({
            parent     : UI,
            gasketName : "main",
            sidebar    : false,
            footbar    : false,
            history    : false,
            multi      : false,
            objTagName : 'SPAN',
            canOpen : function(o) {
                return true;
            }
        //lastObjId : "app-browser"
        }, opt.setup)).render(function(){
            var uiBW = this;
            // 指定了对象
            if(o){
                uiBW.setData("id:" + o.pid, function(){
                    console.log("I am browser setData callback!!");
                    uiBW.setActived(o.id);
                });                
            }
            // 否则用默认
            else {
                uiBW.setData();
            }
        });
    },
    //...............................................................
    _show_ext : function(val) {
        var UI  = this;
        var opt = UI.options;

        // 切换标签
        UI.__swich_tab("ext");

        // 清空区域
        UI.releaseSubUI("main");
        UI.arena.find("section>div").empty();

        // 绘制主体 
        seajs.use(opt.ext.uiType, function(ExtUI){
            new ExtUI(_.extend({
                parent     : UI,
                gasketName : "main",
            }, opt.ext.uiConf)).render(function(){
                this.setData(val);
            });
        });
    },
    //...............................................................
    setData : function(link){
        this.ui_parse_data(link, function(str){
            this._update(str);
        });
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _update : function(str){
        var UI   = this;
        var opt  = UI.options;
        //console.log("update:", str)
        // 用内部文件 [默认]
        if(!str || /^id:.+$/.test(str)) {
            //UI._show_lnk(str);
            UI._show_obj(str);
        }
        // 是外部链接
        else if(/^https?:\/\//i.test(str)){
            UI._show_lnk(str);
        }
        // 扩展行为
        else if($z.invoke(opt.ext, "test", [str])) {
            UI._show_ext(str);
        }
        // 不支持
        else{
            alert(UI.msg("support.unknown") + " : " + str)
        }

    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    _get_data : function(){
        var UI = this;

        // 得到类型
        var tp = UI.arena.find(">header>ul .highlight").attr("tp");

        // 如果是内部对象
        if("obj" == tp){
            var obj = UI.gasket.main.getActived();
            if(!obj){
                return "";
            }
            return "id:" + obj.id;
        }
        // 如果是外部链接
        if("lnk" == tp) {
            return $.trim(UI.arena.find(".el-lnk input").val());
        }
        // 扩展行为 
        if("ext" == tp) {
            return UI.gasket.main.getData();
        }
        // 不可能
        throw "invalid type: [" + tp + "]";
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);