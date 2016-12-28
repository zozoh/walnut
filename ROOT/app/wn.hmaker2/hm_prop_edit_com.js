(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = `
<div class="ui-arena hm-prop-com">
    <div class="hmpc-form" ui-gasket="prop"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_edit_com", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        this.balloon();
    },
    //...............................................................
    update : function(uiCom, com) {
        var UI = this;
        UI.uiCom  = uiCom;
        
        var comId = uiCom.getComId();
        var ctype = uiCom.getComType();
        
        // 如果控件类型发生了变化更新编辑区显示
        if (UI.__com_type != ctype) {
            // 先销毁
            UI.release();
            
            // 得到扩展属性定义
            var uiDef = uiCom.getDataProp();

            // 加载属性控件
            seajs.use(uiDef.uiType, function(DataPropUI){
                new DataPropUI(_.extend({}, uiDef.uiConf||{}, {
                    parent : UI,
                    gasketName : "prop",
                    on_update : function(com) {
                        this.uiCom.saveData("panel", com);
                    }
                })).render(function(){
                    UI.__com_type = ctype;
                    UI.gasket.prop.uiCom = uiCom;
                    UI.gasket.prop.update(com);
                });
            });
        }
        // 否则直接更新
        else {
            UI.gasket.prop.uiCom = uiCom;
            UI.gasket.prop.update(com);
        }
    },
    //..............................................................
    __update_com_info : function(comId, ctype){
        var UI = this;
        var jInfo = UI.arena.find(">.hmpc-info").css("display", "");
        // 图标
        jInfo.children('span').html(UI.msg("hmaker.com."+ctype+".icon"));
        // 名称
        jInfo.children('b').text(UI.msg("hmaker.com."+ctype+".name"));
        // ID
        jInfo.children('em').text(comId);
    },
    //...............................................................
    showBlank : function() {
        this.arena.find(".hmpc-info").hide();
        this.release();
    },
    //...............................................................
    release : function(){
        if(this.gasket.form)
            this.gasket.form.destroy();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);