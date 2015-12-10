(function($z){
$z.declare('zui', function(ZUI){
//==============================================
return ZUI.def("ui.oadaptor", {
    dom  : '/*<div class="ui-arena" ui-fitparent="yes" ui-gasket="main"></div>*/',
    //..............................................
    redraw : function(){
        var UI = this;
        if(UI.delegateUI)
            return UI.delegateUI.redraw.call(UI.delegateUI);
    },
    //..............................................
    getData : function(){
        var UI = this;
        if(UI.delegateUI && _.isFunction(UI.delegateUI.getData)){
            return UI.delegateUI.getData();
        }
        return null;
    },
    //..............................................
    setData : function(o, callback, context){
        var UI = this;
        //console.log("oadasptor:", o)
        if(o) {
            //UI.$el.data("@DATA", D);
            $z.loadResource(o.uiAdaptor, function(uiDef){
                // console.log("ahhahah", _.extend({},uiDef))
                // 普通设置数据
                if(!uiDef) {
                    if(UI.delegateUI){
                        UI.__set_data(o, callback, context);
                    }
                }
                // 按照约定，需要先改变 UI 再设置数据
                else{
                    UI.changeUI(uiDef, function(){
                        //console.log("it will set data:", UI.delegateUI)
                        UI.__set_data(o, callback, context);
                    });
                }
            });
        }
    },
    __set_data : function(o, callback, context){
        var UI = this;
        // 适配 UI 可以设置数据
        //console.log("oadasptor.__set_data:", o)
        if(UI.delegateUI){
            if(_.isFunction(UI.delegateUI.setData)){
                UI.delegateUI.setData(o, callback, context);
            }
        }
    },
    //..............................................
    changeUI : function(uiDef, callback, context){
        var UI = this;
        // 非法
        if(!uiDef) {
            throw "oadaptor need 'conf'";
        }
        // 已经是同样的 UI 了，没必要重新加载
        else if(_.isEqual(uiDef, UI.delegateDef)){
            var myUI = UI.delegateUI;
            if(_.isFunction(callback)){
                callback.call(context||myUI);
            }
        }
        // 加载 UI
        else if(uiDef.uiType){
            //console.log("loadUI", uiDef.uiType)
            seajs.use(uiDef.uiType, function(MyUI){
                var uiConf = $z.evalFunctionField(uiDef.uiConf);
                UI.delegateUI = new MyUI(_.extend(uiConf || {},{
                    parent : UI,
                    gasketName : "main"
                })).render(function(){
                    //console.log("loadUI", UI.delegateUI)
                    var myUI = this;
                    UI.delegateDef = uiDef;
                    if(_.isFunction(callback)){
                        callback.call(context||myUI);
                    }
                });
            });    
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);