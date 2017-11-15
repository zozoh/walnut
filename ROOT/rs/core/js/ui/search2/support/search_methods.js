define(function (require, exports, module) {
var MenuUI = require('ui/menu/menu');
// ....................................
// 方法表
var methods = {
    //....................................................
    // 处理子 UI 的配置信息, 确保都是 {uiType, uiConf} 格式的
    __fmt_subUIs : function(opt, key, dftType){
        var ud = opt[key];
        // 不存在
        if(!ud)
            return;
        // 字符串表示类型
        if(_.isString(ud)) {
            opt[key] = {uiType:ud, uiConf:{}};
        }
        // 对象的话 ..
        else if(_.isObject(ud)){
            // 标准声明的
            if(ud.uiType) {
                ud.uiConf = ud.uiConf || {};
            }
            // 那么就当做是配置信息
            else{
                opt[key] = {uiType:dftType, uiConf:ud};
            }
        }
        // 采用默认
        else{
            opt[key] = {uiType:dftType, uiConf:{}};
        }
    },
    //....................................................
    // 解释快速菜单项
    __quick_menu : function(key) {
        var re = ({
            "refresh" : {
                icon  : '<i class="zmdi zmdi-refresh"></i>',
                text  : "i18n:refresh",
                handler : function(){
                    this.refresh();
                }
            },
            "delete" : {
                icon  : '<i class="zmdi zmdi-delete"></i>',
                text  : "i18n:delete",
                handler : function(){
                    this.deleteChecked();
                }
            },
            "create" : {
                icon  : '<i class="zmdi zmdi-flare"></i>',
                text  : "i18n:new",
                handler : function(){
                    this.openCreateMask();
                }
            },
            "edit" : {
                icon  : '<i class="zmdi zmdi-edit"></i>',
                text  : "i18n:edit",
                handler : function(){
                    this.openEditMask();
                }
            }
        })[key];
        if(!re)
            throw "Unknown quick menu [" + key + "]";
        return re;
    },
    //...............................................................
    // 绘制菜单
    _draw_menu : function(isNarrow, callback) {
        var UI  = this;
        var opt = UI.options;
        var jMenu = UI.arena.find(">header .search-menu-con");

        console.log(opt.menu)
        // 绘制菜单
        if(jMenu.length > 0){
            if(opt.menu){
                var menu_setup;
                // 标识宽窄模式
                if(isNarrow) {
                    jMenu.attr("narrow-mode", "yes");
                    menu_setup = [{
                        icon  : '<i class="fa fa-ellipsis-v"></i>',
                        items : opt.menu
                    }];
                }
                // 否则宽模式
                else {
                    jMenu.removeAttr("narrow-mode");
                    menu_setup = opt.menu;
                }
                // 创建菜单控件
                UI.uiMenu = new MenuUI({
                    parent     : UI,
                    gasketName : "menu",
                    fitparent : false,
                    setup : menu_setup,
                    autoLayout : true,
                }).render(function(){
                    // 记录一下长模式下的宽度
                    if(!isNarrow && !jMenu.attr("prime-width")){
                        jMenu.attr("prime-width", jMenu.outerWidth());
                    }
                    // 回调
                    $z.doCallback(callback);
                });
                // 返回 true 表示有菜单
                return true;
            }
            // 删除菜单
            else{
                jMenu.remove();
            }
        }
    },
    //....................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
