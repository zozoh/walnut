(function($z){
$z.declare([
    'zui',
    "ui/form/support/form_c_methods",
    "ui/mask/mask"
], function(ZUI, ParentMethods, MaskUI){
//==============================================
var html = `
<div class="ui-arena com-content">
    <div class="ccnt-brief"></div>
    <footer>
        <div class="ccnt-cinfo"></div>
        <ul>
            <li a="edit_content">{{com.content.edit_content}}
            <li a="gen_brief">{{com.content.gen_brief}}
        </ul>
    </footer>
</div>`;
//===================================================================
return ZUI.def("ui.form_com_obj_detail", {
    //...............................................................
    dom  : html,
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = ParentMethods(this);

        // 读取 content 的方法
        $z.setUndefined(opt, "loadContent", function(th, callback){
            var msg = UI.msg("com.content.noloader");
            alert(msg);
            throw msg;
        });

        // 保存 content 的方法
        $z.setUndefined(opt, "saveContent", function(th, content, callback){
            var msg = UI.msg("com.content.nosaver");
            alert(msg);
            throw msg;
        });

        // 默认弹出层配置
        $z.setUndefined(opt, "pop", {
            width  : 900,
            height : "90%",
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/pop.css',
        });

        // 默认编辑器配置
        $z.setUndefined(opt, "editor", {});

    },
    //...............................................................
    events : {
        'click [a="edit_content"]' : function() {
            this.do_edit_content();
        },
        'click [a="gen_brief"]' : function() {
            this.do_gen_brief();
        },
        'click .ccnt-brief' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);

            $z.editIt(jq, function(newval, oldval){
                newval = $.trim(newval) || "";
                if(newval != oldval) {
                    UI.__data.brief = newval;
                    UI.__draw_data();
                    UI.__on_change();
                }
            });
        },
    },
    //...............................................................
    do_gen_brief : function() {
        var UI   = this;
        var opt  = UI.options;

        // 之前读取过，直接用
        if(UI.__content) {
            UI.__gen_brief();
        }
        // 否则读取后用
        else {
            opt.loadContent(UI.__data, function(content) {
                UI.__content = content;
                UI.__gen_brief();
            });
        }
    },
    //...............................................................
    __gen_brief : function() {
        var UI   = this;
        var opt  = UI.options;
        var data = UI.__data;
        data.brief = $.trim(UI.__content).substring(0, 50);
        if(!data.brief){
            data.brief = UI.msg("com.content.nobrief");
        }

        UI.__draw_data();
        UI.__on_change();
    },
    //...............................................................
    do_edit_content : function() {
        var UI   = this;
        var opt  = UI.options;
        var data = UI.__data;

        // 如果是有内容的
        if(data.len > 0){
            // 之前读取过，直接用
            if(UI.__content) {
                UI.__open_content_editor();
            }
            // 否则读取后用
            else {
                opt.loadContent(data, function(content) {
                    UI.__content = content;
                    UI.__open_content_editor();
                });
            }
        }
        // 直接打开空的
        else {
            UI.__content = "";
            UI.__open_content_editor();
        }
        
    },
    //...............................................................
    __open_content_editor : function() {
        var UI   = this;
        var opt  = UI.options;
        var data = UI.__data;

        new MaskUI(_.extend({
            events : {
                "click .pm-btn-ok" : function(){
                    var uiMask  = this;
                    var content = uiMask.body.getData();

                    // 保存
                    opt.saveContent(data, content, function(){
                        // 保存数据
                        UI.__content = content;
                        data.len = UI.__content.length;

                        // 关闭弹出层
                        uiMask.close();

                        // 更新显示并通知
                        UI.__draw_data();
                        UI.__on_change();
                    });
                },
                "click .pm-btn-cancel" : function(){
                    this.close();
                }
            }, 
            setup : {
                uiType : 'ui/zeditor/zeditor',
                uiConf : _.extend({}, opt.editor, {
                    contentType : data.contentType
                })
            }
        }, opt.pop)).render(function(){
            this.arena.find(".pm-title").html(UI.msg('com.content.edit_content'));
            this.body.setData(UI.__content);
        });
    },
    //...............................................................
    __draw_data : function() {
        var UI   = this;
        var data = UI.__data;

        $z.setUndefined(data, "contentType", "text");
        $z.setUndefined(data, "len", 0);

        // 绘制摘要
        var briefText = data.brief || UI.msg("com.content.nobrief");
        // briefText = $z.dupString(briefText, 2);
        UI.arena.find(".ccnt-brief").attr({
            "no-brief" : data.brief ? null : "yes"
        }).text(briefText);

        // 绘制更多信息
        UI.arena.find(".ccnt-cinfo").attr({
            "data-balloon": data.contentType,
            "data-balloon-pos" : "up"
        }).html(UI.msg("com.content.info", data));

        // 打开提示
        UI.balloon();
    },
    //...............................................................
    _set_data : function(data) {
        var UI = this;

        // 记录对象
        UI.__data = data;

        // 绘制数据
        UI.__draw_data();
    },
    //...............................................................
    _get_data : function() {
        return this.__data;
    },
    //...............................................................
    /*
    输入输出的数据格式:
    {
        id     : xxx   // 数据的唯一标识符
        brief  : xxx   // 数据摘要
        len    : 0     // 数据内容长度
        contentType : text|html|markdown  // 数据内容类型
    }
    */
    //...............................................................
    getData : function(){
        var UI = this;
        return UI.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    setData : function(val){
        var UI = this;
        this.ui_parse_data(val, function(data){
            UI._set_data(data);
        });
    },
    //...............................................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;
        var W   = UI.$pel.width();
        var H   = UI.$pel.height();

        // 指定宽度
        if(opt.width) {
            UI.arena.css("width", $z.dimension(opt.width, W));
        }

        if(opt.height){
            UI.arena.css("height", $z.dimension(opt.height, H));   
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);