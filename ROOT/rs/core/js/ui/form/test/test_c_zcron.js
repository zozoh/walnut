(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_zcron'
], function(ZUI, Wn, CZCronUI){
//==============================================
var html = function(){/*
<div class="ui-arena" style="padding:20px;">
    <pre class="t-msg">
    </pre>
    <div class="t-item">
        <div ui-gasket="t0"></div>
        <button>Get Data</button>
    </div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_c_zcron", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : 'ui/form/test/test_c.css',
    //...............................................................
    events : {
        'click .t-item button' : function(e) {
            var jq = $(e.currentTarget);
            var d  = ZUI(jq.prev().children()).getData();
            alert(d);
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new CZCronUI({
            parent : UI,
            gasketName : "t0",
            on_change : function(val) {
                UI.showMsg("DateUI: " + val);
            },
        }).render(function(){
            UI.defer_report("t0");
        });

        
        //...........................................................
        return ["t0"];
    },
    //...............................................................
    showMsg : function(msg) {
        var UI = this;
        var jPre = UI.arena.find('>pre');
        jPre.text(msg);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);