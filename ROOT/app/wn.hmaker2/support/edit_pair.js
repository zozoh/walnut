(function($z){
$z.declare([
    'zui',
], function(ZUI){
//==============================================
var html = '<div class="ui-arena edit-pair"></div>';
//==============================================
return ZUI.def("ui.edit_pair", {
    dom  : html,
    //...............................................................
    events : {
    },
    //...............................................................
    __draw_data : function(obj) {
        var UI  = this;
        var opt = UI.options;

        // 如果没内容显示空
        if(!obj || _.isEmpty(obj)){
            
        }
        //
    },
    //...............................................................
    getData : function() {
        var UI = this;
        
        return UI.arena.text();
    },
    //...............................................................
    setData : function(obj) {
        // 如果是字符串，可能是 JSON
        if(_.isString(obj))
            obj = $z.fromJson(obj);

        // 那么开始绘制
        this.__draw_data(obj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);