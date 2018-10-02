(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/list/list'
], function(ZUI, Wn, MenuUI, ListUI){
//==============================================
var html = function(){/*
<div class="ui-arena thdc-list" ui-fitparent="yes">
    <h4></h4>
    <textarea spellcheck="false"></textarea>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_commands_list", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "change textarea" : function() {
            var UI  = this;
            var jTx = UI.arena.find('textarea');
            var str = $.trim(jTx.val());

            try {
                var obj = $z.fromJson(str);

                // 必须是一个数组
                if(!_.isArray(obj)) {
                    obj = [obj];
                }

                // 重新写入
                jTx.val($z.toJson(obj, null, '  '));

                // 查看数据同步
                UI.parent.notifyChanged();
            }
            // 不是合法的 json
            catch(E) {
                alert('不是合法的 json 数组', 'warn');
                jTx.focus();
            }
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;       
        var opt = UI.options; 

        // 更新标题 
        UI.arena.find('h4').text(UI.text(opt.title));
        
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var jTx = UI.arena.find('textarea');
        var str = $.trim(jTx.val());
        if(!str) {
            return [];
        }
        var obj = $z.fromJson(str);

        // 必须是一个数组
        if(!_.isArray(obj)) {
            obj = [obj];
        }

        return obj;
    },
    //...............................................................
    setData : function(data) {
        var UI  = this;
        var jTx = UI.arena.find('textarea');
        if(_.isArray(data) && data.length > 0) {
            var json = $z.toJson(data, null, '  ') || "";
            jTx.val(json);
        }
        // 否则显示空
        else {
            jTx.val("");
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);