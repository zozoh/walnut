(function($z){
$z.declare('zui', function(ZUI){
//==============================================
return ZUI.def("ui.support.uitester", {
    dom  : '<div class="ui-arena" ui-fitparent="yes" ui-gasket="main"></div>',
    //...............................................................
    events : {
        "click > .ui-arena > button" : function(){
            // 得到 uiType/uiConf
            var uiType = $.trim(this.arena.find("input").val());
            var uiConf = $z.fromJson($.trim(this.arena.find("textarea").val())||"{}");

            this.applyUI(uiType, uiConf);

            // 如果为清除 UI，设置一下浏览器地址

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
                UI.applyUI(lastUiType, lastUiConf, o);
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
            UI.applyUI(uiType, uiConf, o);
        }
    },
    //...............................................................
    applyUI : function(uiType, uiConf, o) {
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
                $z.invoke(this, "update", [o || {}]);
            });
        });
    },
    //...............................................................
    __draw_ui_setup : function() {
        var UI = this;
        $('<input>').attr({
            "placeholder" : "Please input uiType here",
            "spellcheck"  : false,
            "list" : "_ui_test_combo_list"
        }).css({
            "width" : "100%",
            "outline" : "none",
            "border" : "3px solid #CCC",
            "margin-bottom" : "8px",
            "padding" : "10px",
            "font-size" : "16px",
            "font-family" : "Monaco, Consolas, Courier New",
        }).appendTo(UI.arena);
        var jList = $('<datalist id="_ui_test_combo_list"></datalist>')
                        .appendTo(UI.arena);
        $('<option>ui/form/test/test_form0</option>').appendTo(jList);
        $('<option>ui/form/test/test_form1</option>').appendTo(jList);
        $('<option>ui/form/test/test_form2</option>').appendTo(jList);
        $('<option>ui/form/test/test_form3</option>').appendTo(jList);
        $('<option>ui/form/test/test_form_textarea_obj</option>').appendTo(jList);
        $('<option>ui/form/test/test_form_range</option>').appendTo(jList);
        $('<option>ui/form/test/test_form_address</option>').appendTo(jList);
        $('<option>ui/form/test/test_c_input</option>').appendTo(jList);
        $('<option>ui/form/test/test_c_combotable</option>').appendTo(jList);
        $('<option>ui/thing/test/test_for_thing</option>').appendTo(jList);
        $('<option>ui/thing/test/test_for_obj</option>').appendTo(jList);
        $('<option>ui/search2/test/test_for_search</option>').appendTo(jList);
        $('<option>ui/layout/test/test_for_layout</option>').appendTo(jList);

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