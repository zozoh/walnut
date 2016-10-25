(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods_panel',
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
    <div class="hmpc-form" ui-gasket="form"></div>
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
            var UI = this;

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
            var oldSkin = UI.__skin;
            UI.__skin = jLi.attr("value");
            UI.__update_skin_info();
            UI.arena.find(".hmpc-skin").removeAttr("skin-list-show");
            UI.notifyComChange({
                _skin : UI.__skin,
                _skin_old : oldSkin
            });
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
    update : function(com) {
        var UI = this;

        // 处理 info 区域
        if(com._type) {
            UI.__com_type = com._type;
            var jInfo = UI.arena.find(">.hmpc-info").css("display", "");
            // 图标
            jInfo.children('span').html(UI.msg("hmaker.com."+com._type+".icon"));
            // 名称
            jInfo.children('b').text(UI.msg("hmaker.com."+com._type+".name"));
            // ID
            jInfo.children('em').text(com._id);
        }

        // 处理皮肤选择区
        UI.__skin = com._skin;
        UI.__update_skin_info();

        // 处理控件扩展区域
        $z.invoke(UI.gasket.form, "update", [com]);
    },
    //...............................................................
    __update_skin_info : function(){
        var UI = this;
        var sText = UI.getSkinTextForCom(UI.__com_type, UI.__skin);
        UI.arena.find(">.hmpc-skin>div>b")
            .text(sText || UI.msg("hmaker.prop.skin_none"))
            .attr({
                "skin-none" : sText ? null : "yes"
            });
    },
    //...............................................................
    showBlank : function() {
        var UI = this;

        UI.arena.find(".hmpc-info").hide();

        if(UI.gasket.form)
            UI.gasket.form.destroy();
    },
    //...............................................................
    draw : function(uiDef, callback) {
        var UI = this;

        // 先销毁
        UI.release();

        // 没定义，就直接回调了
        if(!uiDef) {
            $z.doCallback(callback, [], UI);
        }
        // 设置
        else {
            // 重新绑定控件
            seajs.use(uiDef.uiType, function(EleUI){
                new EleUI(_.extend({}, uiDef.uiConf||{}, {
                    parent : UI,
                    gasketName : "form"
                })).render(function(){
                    $z.doCallback(callback, [this], UI);
                });
            });
        }
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