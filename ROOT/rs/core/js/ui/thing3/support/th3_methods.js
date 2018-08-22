define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
__th3_methods_binded : true,
//....................................................
thMain : function() {
    var reUI = this;
    while(reUI.uiName != 'ui.th3.main') {
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
getHomeTitle : function(){
    var home = this.getHomeObj();
    return this.text(home.title || home.nm);
},
//....................................................
getHomeOneObjTitle : function(){
    var home = this.getHomeObj();
    return this.text(home.title_one || home.title || home.nm);
},
//....................................................
// 处理命令的通用回调
doActionCallback : function(re, ok, fail) {
    var UI = this;
    if(_.isFunction(ok.ok) || _.isFunction(ok.fail)) {
        fail = ok.fail;
        ok = ok.ok;
    }
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
    if(!uiSub || uiSub.__th3_methods_binded)
        return uiSub;
    return _.extend(uiSub, methods);
};
//=======================================================================
});
