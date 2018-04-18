define(function (require, exports, module) {
var methods = {
    //..............................................
    __do_notify : function(){
        var UI  = this;
        var opt = UI.opt();
        var context = opt.context || UI;

        // 获得当前选中/激活的对象
        var aObj  = UI.getActived();
        var cObjs = UI.getChecked();

        // 通知脚注
        UI.browser().trigger("browser:info", UI.msg("obrowser.selectNobj", {n:cObjs.length}));

        // 通知选中
        $z.invoke(opt, "on_select", [aObj, cObjs], context);
        UI.browser().trigger("browser:select", aObj, cObjs);
    },
    //..............................................
    update : function(o, callback){
        var UI  = this;
        var opt = UI.opt();
        var uiList = UI.gasket.list;

        //console.log(opt)

        // 显示正在加载
        uiList.showLoading();

        // 得到当前所有的子节点
        Wn.getChildren(o, opt.filter, function(objs){
            uiList.hideLoading();
            
            // 显示列表
            uiList.setData(objs);

            // 调用回调
            $z.doCallback(callback, [objs]);
        }, true);

        // 表示自己是异步加载
        return true;
    },
    //..............................................
    getData : function(arg){
        return this.browser().getById($(arg).closest(".wnobj").attr("oid"));
    },
    //..............................................
    // 修改激活项目的名称
    rename : function(){
        this.gasket.list.rename();
    },
    //..............................................
    isActived : function(ele){
        return this.gasket.list.isActived(ele);
    },
    //..............................................
    getActived : function(){
        return this.gasket.list.getActived();
    },
    setActived : function(arg){
        return this.gasket.list.setActived(arg);
    },
    //..............................................
    getChecked : function(){
        return this.gasket.list.getChecked();
    }
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require('ui/obrowser/support/browser__methods');

//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(HmMethods(uiCom), methods);
};
//=======================================================================
});