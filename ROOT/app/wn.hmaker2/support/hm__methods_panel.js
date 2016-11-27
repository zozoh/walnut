define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    },
    // 更新皮肤选择框
    updateSkinBox : function(jBox, skin){
        jBox.attr("skin-selector", skin)
            .html('<span><i class="zmdi zmdi-texture"></i></span>');
        // 显示文字
        var jB = $('<b>').attr("skin-none", skin ? null : "yes");
        // 选择皮肤样式名
        if(skin){
            jB.text(this.getSkinTextForArea(skin));
        }
        // 显示默认
        else{
            jB.text(this.msg("hmaker.prop.skin_none"));
        }
        // 加入 DOM
        jB.appendTo(jBox);
    },
    // 显示皮肤下拉列表
    showSkinList : function(jBox, skinList, callback){
        var UI   = this;
        var jBox = $(jBox);
        var skin = jBox.attr("skin-selector") || "";
        
        // 准备绘制
        var jList = $('<div class="hm-skin-list">');

        // 站点没设置皮肤
        if(!_.isArray(skinList)){
            jList.attr("warn","unset").html(UI.msg("hmaker.prop.skin_unset"));
        }
        // 没有可用样式
        else if(skinList.length == 0) {
            jList.attr("warn","empty").html(UI.msg("hmaker.prop.skin_empty"));
        }          
        // 绘制项
        else {
            var jUl = $('<ul>').appendTo(jList);
            
            // 绘制第一项
            $('<li class="skin-none">').text(UI.msg("hmaker.prop.skin_none")).attr({
                "value"   : "",
                "checked" : !skin ? "yes" : null
            }).appendTo(jUl);

            // 循环绘制其余项目
            for(var si of skinList) {
                $('<li>').text(si.text).attr({
                    "value"   : si.selector,
                    "checked" : si.selector == skin ? "yes" : null
                }).appendTo(jUl);
            }
        }

        // 最后显示出来
        var jMask = $('<div class="hm-skin-mask"></div>').appendTo(jBox);
        jBox.append(jList);
        
        // 确保停靠在正确的位置
        $z.dock(jBox, jList, "H");
                
        // 响应事件: 取消
        var do_hide = function(e){
            e.stopPropagation();
            $(document).off("keyup", do_hide);
            jBox.find(".hm-skin-mask").off().remove();
            jBox.find(".hm-skin-list").off().remove();
        };
        jMask.on("click", do_hide);
        
        // 响应事件: 取消（键盘)
        $(document).on("keyup", do_hide);
        
        // 响应事件: 选中
        jList.on("click", "li", function(e){
            // 得到选中的 skin
            var skin = $(e.currentTarget).attr("value");
            
            // 设置显示的值
            UI.updateSkinBox(jBox, skin);
            
            // 执行回调
            $z.doCallback(callback, [skin]);
            
            // 取消
            do_hide(e);
        });
        
    },
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});