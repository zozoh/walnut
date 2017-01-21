(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_c_methods',
    'ui/support/edit_color',
], function(ZUI, FormMethods, EditColorUI){
//==============================================
var html = `
<div class="ui-arena com-color com-square-drop">
    <div class="cc-box"><div class="ccb-preview"></div></div>
    <div class="cc-edit">
        <div class="cce-mask"></div>
        <div class="cce-con" ui-gasket="edit"></div>
    </div>
</div>
`;
//===================================================================
return ZUI.def("ui.form_com_color", {
    dom  : html,
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = FormMethods(this);

        // ESC 键，将会隐藏自己
        UI.watchKey(27, function(e){
            UI.hideDrop();
        });
    },
    //...............................................................
    events : {
        // 显示颜色提取器
        'click .cc-box' : function() {
            this.showDrop();
        },
        // 隐藏颜色提取器 
        'click .cce-mask' : function() {
            this.hideDrop();
        },
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 初始化编辑界面
        new EditColorUI(_.extend({}, $z.pick(UI.options, "!^([$]?p?el|context)$"), {
            parent : UI,
            gasketName : "edit",
            parseData  : null,
            formatData : null,
            on_change  : function(v){
                UI.__update(v);
                UI.__on_change();
            }
        })).render(function(){
            UI.defer_report("edit");
        });

        // 返回延迟加载
        return ["edit"];
    },
    //...............................................................
    __update : function(val){
        var UI     = this;
        var color  = val ? $z.parseColor(val) : null;
        var jPrew = UI.arena.find(".ccb-preview");

        // 没颜色，清空
        if(!color){
            jPrew.css("background-color","").attr("empty","yes");
        }
        // 设置颜色
        else {
            jPrew.css("background-color",color.RGBA).removeAttr("empty");
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI.gasket.edit.getData();
        });
    },
    //...............................................................
    setData : function(val){
        //console.log(val)
        var UI = this;
        UI.ui_parse_data(val, function(s){
            UI.__update(s);
            UI.gasket.edit.setData(s);
        });
    },
    //...............................................................
    showDrop : function() {
        var UI = this;
        
        // 显示
        var jBox  = UI.arena.find(".cc-box");
        var jDrop = UI.arena.find(".cce-con");
        UI.arena.attr("show", "yes");
        jDrop.css({"top":"", "left":""});
        $z.dock(jBox,jDrop,"HA");

        // 下面不要让下拉框超出窗口
        var rect = $z.rect(jDrop);
        var viewport = $z.winsz();
        var rect2 = $z.rect_clip_boundary(rect, viewport);
        jDrop.css($z.rectObj(rect2, "top,left"));
    },
    //...............................................................
    // 隐藏颜色提取器 
    hideDrop : function() {
        var UI = this;
        UI.arena.removeAttr("show");
        UI.arena.find(".cce-con").css({"top":"", "left":""});
    },
    //...............................................................
    resize : function() {
        // 改变大小的时候，一定要隐藏
        this.arena.removeAttr("show");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);