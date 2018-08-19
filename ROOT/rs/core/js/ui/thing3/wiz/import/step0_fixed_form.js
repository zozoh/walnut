(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-0-fixed-form" ui-fitparent="yes" ui-gasket="form">
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_0_fixed_form", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 加载表单配置
        Wn.exec('cat \'' + opt.fixedForm + '\'', function(re) {
            if(/^e./.test(re)) {
                UI.alert(re, "warn");
                return;
            }
            // 解析，因为可能有函数，所以用 eval
            var formSetup = eval('(' + re + ')');
            new FormUI(_.extend(formSetup, {
                parent : UI,
                gasketName : "form",
                on_update : function() {
                    UI.parent.checkNextBtnStatus();
                }
            })).render(function(){
                UI.defer_report("form");
            });
        });

        return ["form"];
    },
    //...............................................................
    isDataReady : function() {
        var fd = this.gasket.form.getData();
        //console.log(fd)
        return fd.yq_id && fd.yq_nm && fd.yq_section;
    },
    //...............................................................
    getData : function(){
        return  {
            fixedData : this.gasket.form.getData()
        };
    },
    //...............................................................
    setData : function(data) {
        //console.log("step0", data)
        this.gasket.form.setData(data.fixedData || {});
    },
});
//===================================================================
});
})(window.NutzUtil);