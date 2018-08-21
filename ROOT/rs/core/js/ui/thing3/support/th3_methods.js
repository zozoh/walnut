define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
//....................................................
thMain : function() {
    var reUI = this;
    while(reUI.uiName != 'ui.th3.th_main') {
        reUI = reUI.parent;
        if(!reUI)
            return;
    }
    return reUI;
},
//....................................................
getMainData : function(){
    return this.thMain().__main_data;
},
//....................................................
getHomeObjId : function() {
    return this.getMainData().home.id;
},
//....................................................
getHomeObj : function() {
    return this.getMainData().home;
},
//....................................................
// 处理命令的通用回调
doActionCallback : function(re, ok, fail) {
    var UI = this;
    //console.log("after", re)
    if(!re || /^e./.test(re)){
        UI.alert(re || "empty", "warn");
        $z.doCallback(fail, [re], UI);
        return;
    }
    try {
        var reo = $z.fromJson(re);
        $z.doCallback(ok, [reo], UI);
    }
    // 出错了，还是要控制一下
    catch(E) {
        UI.alert(E, "warn");
        console.warn(E);
    }
}
//....................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
