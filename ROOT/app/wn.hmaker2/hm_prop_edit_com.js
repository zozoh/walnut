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
    <div class="hmpc-info">
        <span></span><b></b><em></em>
    </div>
    <div class="hmpc-skin hm-skin-box" 
        balloon="left:{{hmaker.prop.skin_tip}}"
        box-enabled="yes"></div>
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
    events:{
        // TODO com 可以修改 ID
        "click .hmpc-info em" : function(e){
            //alert($(e.currentTarget).text())
            var UI = this;
            $z.editIt(e.currentTarget, function(newval, oldval, jEle){
                var comNewId = $.trim(newval);
                if(comNewId != oldval) {
                    //console.log("change com ID", comNewId);
                    // 修改接口
                    if(UI.uiCom.setComId(comNewId)){
                        // 通知更新
                        UI.uiCom.notifyActived();
                        // 修改显示
                        jEle.text(comNewId);
                    }
                }
            });
        },
        // 显示皮肤选择器
        "click .hmpc-skin" : function(e) {
            e.stopPropagation();
            var UI   = this;
            var jBox = $(e.currentTarget);

            // 得到可用皮肤列表
            var skinList = UI.getSkinListForCom(UI.__com_type);

            UI.showSkinList(jBox, skinList, function(skin){
                UI.uiCom.setComSkin(skin);
            });
        }
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

        // 处理 info 区域
        UI.__update_com_info(comId, ctype);

        // 处理皮肤选择区
        //console.log(uiCom.uiName, uiCom.getComSkin())
        var jSkinBox = UI.arena.children(".hm-skin-box");
        UI.updateSkinBox(jSkinBox, uiCom.getComSkin(), function(skin){
            return this.getSkinTextForCom(ctype, skin);
        });
        
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