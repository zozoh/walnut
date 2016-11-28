(function($z){
$z.declare('zui', function(ZUI){
//==============================================
return ZUI.def("ui.support.fake", {
    dom  : '<div class="ui-arena">not implement</div>',
    //...............................................................
    update : function(obj){
        this.arena.empty();
        $('<pre>').text($z.toJson(obj, null, '    ')).appendTo(this.arena);
    }
    //...............................................................
});
//==================================================
});
})(window.NutzUtil);