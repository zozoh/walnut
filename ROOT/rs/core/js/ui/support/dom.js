(function($z){
$z.declare('zui', function(ZUI){
//==============================================
return ZUI.def("ui.dom", {
    dom  : '/*<div class="ui-arena">DOM UI</div>*/',
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 还要组合子 UI
        if(_.isArray(opt.setup)){
            // 记录每个子 UI 的类型
            var uiTypes = [];
            for(var i=0; i<opt.setup.length; i++){
                var conf = opt.setup[i];
                uiTypes.push(conf.uiType);
            }
            // 加载子 UI
            seajs.use(uiTypes, function(){
                for(var index=0; index<uiTypes.length; index++){
                    var SubUI = arguments[index];
                    var conf = opt.setup[index];
                    var uiConf = _.extend({}, conf.uiConf, {
                        parent : UI
                    });
                    // 用闭包，否则 index 会被搞乱
                    (function(index, uiType){
                        new SubUI(uiConf).render(function(){
                            //console.log("layout defer:", index, uiType);
                            UI.defer_report(index, uiType);
                        });
                    })(index, uiTypes[index]);
                };
            });
            // 需要延迟
            return uiTypes;
        }
    }
    //...............................................................
});
//==================================================
});
})(window.NutzUtil);