(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-time">
    <ul>
        <li dballoon="{{com.time.hour}}">
            <input name="hour"   placeholder="{{com.time.hour}}"
                   :range="0,23">
        </li>
        <li dballoon="{{com.time.minute}}">
            <b>:</b>
            <input name="minute" placeholder="{{com.time.minute}}"
                   :range="0,59">
        </li>
        <li dballoon="{{com.time.second}}">
            <b>:</b>
            <input name="second" placeholder="{{com.time.second}}"
                   :range="0,59">
        </li>
        <li class="ct-clean">
            <a>{{clear}}</a>
        </li>
    </ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_time", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        FormMethods(this);

        // 默认编辑精确到秒, 指定  "minute" 将会显示编辑框到分
        $z.setUndefined(opt, "editAs", "second");
        // 默认的输出格式 (getData)
        $z.setUndefined(opt, "format", "HH:mm:ss");
    },
    //...............................................................
    events : {
        // 修改
        "change input" : function(e){
            var UI  = this;
            var jIn = $(e.currentTarget);
            
            // 得到输入的值，如果不是数字，自动变 0
            var val = $.trim(jIn.val());
            var sec = parseInt(val);
            if(!_.isNumber(sec) || isNaN(sec)) {
                sec = 0;
            }
            // 判断是否超过了区间
            else {
                var range = jIn.attr(':range').split(',');
                var r0 = range[0] * 1;
                var r1 = range[1] * 1;
                if(sec < r0) {
                    sec = r0;
                } else if(sec > r1) {
                    sec = r1;
                }
            }

            // 更新一下输入框
            jIn.val(sec);
            UI.__on_change();
        },
        // 清除
        "click .ct-clean a" : function() {
            var UI = this;
            UI.arena.find('input').val('');
            UI.__on_change();
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        var opt = UI.options;

        // 如果精确到分钟，移除秒
        if(opt.editAs =='minute') {
            UI.arena.find('input[name="second"]').closest('li').remove();
        }
    },
    //...............................................................
    _get_data : function(){
        var UI = this;
        var opt = UI.options;

        // 收集
        var ss = [];
        UI.arena.find('input').each(function(){
            var jIn = $(this);
            ss.push($.trim(jIn.val()));
        });
        
        // 如果全部为空，则返回 null
        var allEmpty = true;
        for(var i=0; i<ss.length; i++) {
            if(ss[i]) {
                allEmpty = false;
                break;
            }
        }
        if(allEmpty)
            return null;

        // 计算
        var rr = [3600, 60, 1];
        var sec = 0;
        for(var i=0; i<ss.length; i++) {
            sec += ss[i]*rr[i] || 0;
        }

        // 解析
        var ti = $z.parseTimeInfo(sec);
        return ti.toString(opt.format);
    },
    //...............................................................
    _set_data : function(val, jso){
        var UI = this;
        var ti = $z.parseTimeInfo(val);

        UI.arena.find('input').each(function(){
            var jIn = $(this);
            var key = jIn.attr('name');
            var val = "";
            if(ti) {
                val = $z.alignRight(ti[key], 2, '0');
            }
            jIn.val(val);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);