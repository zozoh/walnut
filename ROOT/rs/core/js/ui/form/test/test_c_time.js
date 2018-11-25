(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_date',
    'ui/form/c_time',
    'ui/form/c_datetime'
], function(ZUI, Wn, DateUI, TimeUI, DateTimeUI){
//==============================================
var html = function(){/*
<div class="ui-arena" style="padding:20px;">
    <pre class="t-msg">
    </pre>
    <div class="t-item">
        <div ui-gasket="t2"></div>
        <button>Get Data</button>
    </div>
    <div class="t-item">
        <div ui-gasket="t1"></div>
        <button>Get Data</button>
    </div>
    <div class="t-item">
        <div ui-gasket="t0"></div>
        <button>Get Data</button>
    </div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_time", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : 'ui/form/test/test_c.css',
    //...............................................................
    events : {
        'click .t-item button' : function(e) {
            var jq = $(e.currentTarget);
            var d  = ZUI(jq.prev().children()).getData();
            var json = $z.toJson(d);
            alert(json);
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new DateUI({
            parent : UI,
            gasketName : "t1",
            on_change : function(val) {
                UI.showMsg("DateUI: " + val);
            },
        }).render(function(){
            UI.defer_report("t1");
        });
        //...........................................................
        new TimeUI({
            parent : UI,
            gasketName : "t0",
            editAs : "minute",
            on_change : function(val) {
                UI.showMsg("TimeUI: " + val);
            },
        }).render(function(){
            UI.defer_report("t0");
        });
        //...........................................................
        new DateTimeUI({
            parent : UI,
            gasketName : "t2",
            on_change : function(val) {
                UI.showMsg("DateUI: " + val);
            },
        }).render(function(){
            UI.defer_report("t2");
        });

        
        //...........................................................
        return ["t0", "t1", 't2'];
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