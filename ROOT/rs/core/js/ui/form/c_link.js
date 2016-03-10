(function($z){
$z.declare([
    'zui',
    'ui/mask/mask'
], 
function(ZUI, MaskUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="mask">
        <div class="ui-arena picker-mask">
            <div class="ui-mask-bg"></div>
            <div class="ui-mask-main"><div class="picker-mask-main">
                <div class="pm-title"></div>
                <div class="pm-body" ui-gasket="main"></div>
                <div class="pm-btns">
                    <b class="pm-btn-ok">{{ok}}</b>
                    <b class="pm-btn-cancel">{{cancel}}</b>
                </div>
            </div></div>
            <div class="ui-mask-closer"><i class="fa fa-close"></i></div>
        </div>
    </div>
</div>
<div class="ui-arena com-link" link-type="unknown">
    <span>
        <i tp="cl-obj" class="fa fa-link"></i>
        <i tp="cl-ext" class="fa fa-external-link"></i>
        <i tp="cl-unknown" class="fa fa-question"></i>
    </span>
    <u blank="yes">{{edit}}</u>
</div>
*/};
//==============================================
return ZUI.def("ui.form_com_link", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/picker/picker.css",
    i18n : "ui/picker/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click u" : function(){
            var UI    = this;
            var opt   = UI.options;
            var setup = opt.setup || {};
            var str   = UI.getData();
            // 准备遮罩的宽高
            //$z.setUndefined(setup, "blockNumber",  "range"==setup.mode?2:1);
            // $z.setUndefined(setup, "width",  300 * (setup.blockNumber||1));
            // $z.setUndefined(setup, "height", "range"==setup.mode?408:360);
            
            // 弹出遮罩层
            new MaskUI(_.extend({
                app : UI.app,
                dom : UI.ccode("mask").html(),
                events : {
                    "click .pm-btn-ok" : function(){
                        UI._update(this.body.getData(), true);
                        this.close();
                    },
                    "click .pm-btn-cancel" : function(){
                        this.close();
                    }
                }, 
                setup : {
                    uiType : 'ui/support/edit_link',
                    uiConf : {}
                }
            }, setup)).render(function(){
                this.body.setData(str);
            });
        }
    },
    //...............................................................
    setData : function(link){
        this.ui_parse_data(link, function(str){
            this._update(str);
        });
    },
    //...............................................................
    // 只接受 Date 对象或者 Date 对象的数组
    _update : function(str, showBlink){
        var UI = this;
        var jU = UI.arena.find("u");
        str = $.trim(str);
        if(str){
            jU.removeAttr("blank").text(str);
            // 修改链接类型: 外部链接
            if(/^https?:\/\/.+/i.test(str)){
                UI.arena.attr("link-type", "ext");
            }
            // 内部文件
            else if(/^id:.+/.test(str)){
                UI.arena.attr("link-type", "obj");
            }
            // 其他
            else {
                UI.arena.attr("link-type", "unknown");
            }
        }
        else{
            jU.attr("blank", "yes").text(UI.msg("edit"));
            UI.arena.attr("link-type", "unknown");
        }
        if(showBlink){
            $z.blinkIt(UI.arena);
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        var jU = UI.arena.find("u");
        return this.ui_format_data(function(opt){
            return jU.attr("blank") ? "" : jU.text();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);