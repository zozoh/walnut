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
        return this.$el.data("@DATA");
    },
    //..............................................
    setData : function(D){
        var UI = this;
        if(D) {
            UI.$el.data("@DATA", D);
            var uiDef = D.uiAdaptor;

            // 非法
            if(!uiDef) {
                throw "oadaptor need 'uiAdaptor' in : \n" + $z.toJson(D);
            }
            // 已经是同样的 UI 了，没必要重新加载
            else if(_.isEqual(uiDef, UI.delegateDef)){
                var myUI = UI.delegateUI;
                if(_.isFunction(myUI.setData)){
                    myUI.setData.call(myUI, D);
                }
            }
            // 加载 UI
            else if(uiDef.uiType){
                seajs.use(uiDef.uiType, function(MyUI){
                    UI.delegateUI = new MyUI(_.extend(uiDef.uiConf || {},{
                        parent : UI,
                        gasketName : "main"
                    })).render(function(){
                        var myUI = UI.delegateUI;
                        UI.delegateDef = uiDef;
                        if(_.isFunction(myUI.setData)){
                            myUI.setData.call(myUI, D);
                        }
                    });
                });    
            }
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);