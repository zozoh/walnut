(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = '<div class="ui-arena hm-prop-page-skin" ui-fitparent="yes" ui-gasket="form"></div>';
//==============================================
return ZUI.def("app.wn.hm_prop_page_skin", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    __draw : function(skinVar, callback) {
        var UI = this;

        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            fields : skinVar.form.fields,
        }).render(function(){
            this.setData(skinVar.data);
            $z.doCallback(callback);
        });
    },
    //...............................................................
    refresh : function(){
        var UI = this;
        console.log("I am page skin refresh");

        UI.showLoading();
        UI.reloadSkinVarSet(function(str){
            var skinVar = UI.parseSkinVar(str);
            console.log(skinVar);
            UI.__draw(skinVar, function(){
                UI.hideLoading();
                console.log("all done");
            });
        });
    },
    //...............................................................
    resize : function(){
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);