(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-welcome" ui-fitparent="yes">
    <header><img src="/a/load/wn.hmaker2/site0_logo_word64_png8.png"></header>
    <section class="hmwe-art">
        <article hm-inner-html="app/wn.hmaker2/i18n/{{lang}}/welcome_letter.html">
        </article>
    </section>
</div>`
*/};
//==============================================
return ZUI.def("app.wn.hmaker_welcome", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        HmMethods(this);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);