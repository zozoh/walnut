(function($z){
$z.declare('zui', function(ZUI){
//==============================================
return ZUI.def("ui.support.uitester", {
    dom  : '<div class="ui-arena" ui-fitparent="yes" ui-gasket="main" style="padding:10px;"></div>',
    //...............................................................
    events : {
        "click button" : function(){
            // 得到 uiType/uiConf
            var uiType = $.trim(this.arena.find("input").val());
            var uiConf = $z.fromJson($.trim(this.arena.find("textarea").val())||"{}");

            this.applyUI(uiType, uiConf);
        }
    },
    //...............................................................
    update : function(o, callback, args) {
        var UI = this;

        //  读取上次加载的
        var lastUiType = UI.local("uiType");
        var lastUiConf = UI.local("uiConf");

        // 强制询问用户
        if("!" == args) {
            UI.__draw_ui_setup();
        }
        // 没有指定 uiType 参数，让用户自己选择
        else if(!args) {
            // 绘制上次的
            if(lastUiType) {
                UI.applyUI(lastUiType, lastUiConf);
            }
            // 否则问用户
            else {
                UI.__draw_ui_setup();
            }
        }
        // 指定的话
        else  {
            var uiType = args;
            var uiConf = lastUiConf || {};
            if(uiType != lastUiType) {
                uiConf = {};
            }
            UI.applyUI(uiType, uiConf);
        }
    },
    //...............................................................
    applyUI : function(uiType, uiConf) {
        var UI = this;

        // 执行 UI 的加载 
        seajs.use(uiType, function(TheUI){
            UI.arena.empty();
            new TheUI(_.extend({}, uiConf, {
                parent : UI,
                gasketName : "main"
            })).render(function(){
                // 保存本地设置
                UI.local("uiType", uiType);
                UI.local("uiConf", uiConf);
                // 执行更新
                $z.invoke(this, "update", [{}]);
            });
        });
    },
    //...............................................................
    __draw_ui_setup : function() {
        var UI = this;
        $('<input placeholder="Please input uiType here" spellcheck="false">').css({
            "width" : "100%",
            "outline" : "none",
            "border" : "3px solid #CCC",
            "margin-bottom" : "8px",
            "padding" : "10px",
            "font-size" : "16px",
            "font-family" : "Monaco, Consolas, Courier New",
        }).appendTo(UI.arena);
        $('<textarea placeholder="uiConf as JSON string here" spellcheck="false">').css({
            "width" : "100%",
            "height" : "400px",
            "outline" : "none",
            "border" : "3px solid #CCC",
            "margin-bottom" : "8px",
            "padding" : "10px",
            "font-size" : "16px",
            "font-family" : "Monaco, Consolas, Courier New",
        }).appendTo(UI.arena);
        $('<button>Show UI Right Now!!!</button>').css({
            "display" : "block",
            "cursor" : "pointer",
            "outline" : "none",
            "width" : "100%",
            "background" : "#000",
            "color" : "#FFF",
            "text-align" : "center",
            "padding" : "10px",
            "font-size" : "16px",
            "font-family" : "Monaco, Consolas, Courier New",
            "user-select": "none",  
        }).appendTo(UI.arena);
    },
    //...............................................................
});
//==================================================
});
})(window.NutzUtil);