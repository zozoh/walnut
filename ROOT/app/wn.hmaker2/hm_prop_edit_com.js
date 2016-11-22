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
    <div class="hmpc-skin" balloon="left:{{hmaker.prop.skin_tip}}"><div>
        <span><%=hmaker.icon.skin%></span><b></b>
        <div class="hmpc-skin-mask"><ul></ul></div>
        <div class="hmpc-skin-list"><ul></ul></div>
    </div></div>
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
        // "click .hmpc-info em" : function(e){
        //     //alert($(e.currentTarget).text())
        //     $z.editIt(e.currentTarget);
        // },
        // 显示皮肤选择器
        "click .hmpc-skin" : function(e) {
            e.stopPropagation();
            var UI  = this;

            // 得到可用皮肤列表
            var sList = UI.getSkinListForCom(UI.__com_type);

            // 准备绘制
            var jList = UI.arena.find(".hmpc-skin-list").empty();

            // 站点没设置皮肤
            if(!_.isArray(sList)){
                jList.attr("warn","unset").html(UI.msg("hmaker.prop.skin_unset"));
            }
            // 没有可用样式
            else if(sList.length == 0) {
                jList.attr("warn","empty").html(UI.msg("hmaker.prop.skin_empty"));
            }          
            // 绘制项
            else {
                var jUl = $('<ul>').appendTo(jList.removeAttr("warn"));
                
                // 绘制第一项
                $('<li class="skin-none">').text(UI.msg("hmaker.prop.skin_none")).attr({
                    "value"   : "",
                    "checked" : !UI.__skin ? "yes" : null
                }).appendTo(jUl);

                // 循环绘制其余项目
                for(var si of sList) {
                    $('<li>').text(si.text).attr({
                        "value"   : si.selector,
                        "checked" : si.selector == UI.__skin ? "yes" : null
                    }).appendTo(jUl);
                }
            }

            // 最后显示出来
            UI.arena.find(".hmpc-skin").attr("skin-list-show", "yes");
        },
        // 改变皮肤项目
        "click .hmpc-skin li" : function(e){
            e.stopPropagation();
            var UI  = this;
            var jLi = $(e.currentTarget);
            
            var ctype   = UI.uiCom.getComType();
            var oldSkin = UI.__skin;
            
            UI.__skin = jLi.attr("value");
            
            UI.arena.find(".hmpc-skin").removeAttr("skin-list-show");

            var com = UI.uiCom.saveData("panel", {
                skin : UI.__skin,
                _skin_old : oldSkin
            }, true);
            console.log(com)

            UI.__update_skin_info(ctype, com);
        },
        // 隐藏皮肤选择器
        "click .hmpc-skin-mask" : function(e){
            e.stopPropagation();
            this.arena.find(".hmpc-skin").removeAttr("skin-list-show");
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
        UI.__update_skin_info(ctype, com);
        
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
    __update_skin_info : function(ctype, com){
        var UI = this;
        UI.__skin = com.skin;
        var sText = UI.getSkinTextForCom(ctype, com.skin);
        UI.arena.find(">.hmpc-skin>div>b")
            .text(sText || UI.msg("hmaker.prop.skin_none"))
            .attr({
                "skin-none" : sText ? null : "yes"
            });
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