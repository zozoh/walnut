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
    <div code-id="el.ext" class="el-ext">
        <input>
    </div>
</div>
<div class="ui-arena edit-link" ui-fitparent="yes">
    <header>
        <ul>
            <li tp="ext">{{support.edit_link.ext}}</li>
            <li tp="obj">{{support.edit_link.obj}}</li>
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
                case "ext":
                    this._show_ext(str);
                    break;
                case "obj":
                    this._show_obj(str);
                    break;
            }
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
    _show_ext : function(val){
        var UI  = this;
        var opt = UI.options;

        // 切换标签
        UI.__swich_tab("ext");
        
        // 释放插件并清空 
        UI.releaseSubUI("main");
        UI.arena.find("section>div").empty();

        // 绘制主体区域
        var jq = UI.ccode("el.ext");
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
        UI.arena.find("section>div").empty();

        // 绘制主体 
        new BrowserUI(_.extend({
            parent     : UI,
            gasketName : "main",
            lastObjId  : "com_edit_link",
            sidebar    : false,
            footbar    : false,
            history    : false,
            multi      : false,
            canOpen : function(o) {
                return true;
            }
        //lastObjId : "app-browser"
        }, opt.setup)).render(function(){
            // 指定了对象
            if(o){
                this.setData("id:" + o.pid);
            }
            // 打开默认目录
            else {
                this.setData(opt.baseDir || "~");
            }
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
        console.log("update:", str)
        // 用内部文件 [默认]
        if(!str || /^id:.+$/.test(str)) {
            //UI._show_ext(str);
            UI._show_obj(str);
        }
        // 是外部链接
        else if(/^https?:\/\//i.test(str)){
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
        // 如果是内部对象
        if(UI.gasket.main){
            var obj = UI.gasket.main.getActived();
            console.log(obj)
            if(!obj){
                return "";
            }
            return "id:" + obj.id;
        }
        // 如果是外部链接
        return $.trim(UI.arena.find(".el-ext input").val());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);