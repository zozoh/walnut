(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-time">
    <div class="ct-show">
        <ul>
            <li class="ct-show-time">--:--:--</li>
            <li class="ct-clean"><a>{{clear}}</a></li>
        </ul>
    </div>
    <div class="ct-drop">
        <div class="ct-drop-mask"></div>
        <div class="ct-drop-con">
            <ul key="hour"></ul>
            <ul key="minute"></ul>
            <ul key="second"></ul>
        </div>
    </div>
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

        $z.setUndefined(opt, "clearable", true);
        // 默认编辑精确到秒, 指定  "minute" 将会显示编辑框到分
        $z.setUndefined(opt, "editAs", "second");
    },
    //...............................................................
    events : {
        // 弹出输入框·显示
        "click .ct-show" : function(e){
            var UI = this;
            var jShow = UI.arena.find('.ct-show ul');
            var jDrop = UI.arena.find('.ct-drop-con');

            // 设置时间
            var val = UI._get_data();
            UI.__set_val(val);

            // 显示
            UI.arena.attr('show-drop', 'yes');

            // 停靠
            $z.dock(jShow, jDrop);
        },
        // 弹出输入框·隐藏
        "click .ct-drop" : function(e){
            this.arena.removeAttr('show-drop');
            this.__on_change();
        },
        // 弹出输入框·选择
        "click .ct-drop-con ul li" : function(e){
            e.stopPropagation();
            this.on_select_li($(e.currentTarget));
        },
        // 清除时间
        "click .ct-clean a" : function(e){
            e.stopPropagation();
            console.log("haha!!!")
            this.on_clean();
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        var opt = UI.options;
        var jShow = UI.arena.find('.ct-show-time');
        var jHH = UI.arena.find('.ct-drop [key="hour"]');
        var jmm = UI.arena.find('.ct-drop [key="minute"]');
        var jss = UI.arena.find('.ct-drop [key="second"]');

        // 默认格式化
        UI.__time_format = "HH:mm:ss";
        UI.__time_blank  = "--:--:--";

        // 如果精确到分钟，移除秒
        if(opt.editAs =='minute') {
            jss.remove();
            jss = null;
            UI.__time_format = "HH:mm";
            UI.__time_blank  = "--:--";
        }
        // 不用取消
        if(!opt.clearable){
            UI.arena.find('.ct-clean').remove();
        }
        // 填充默认值
        jShow.text(UI.__time_blank);

        // 填充时间·时
        for(var i=0;i<24;i++) {
            $('<li>').text($z.alignRight(i,2,'0')).appendTo(jHH);
        }
        // 填充时间·分
        for(var i=0;i<60;i++) {
            $('<li>').text($z.alignRight(i,2,'0')).appendTo(jmm);
        }
        // 填充时间·秒
        if(jss)
            for(var i=0;i<60;i++) {
                $('<li>').text($z.alignRight(i,2,'0')).appendTo(jss);
            }
    },
    //...............................................................
    __get_val_in_ul : function(jUl) {
        var jLi = jUl.find('li[current]');
        if(jLi.length > 0)
            return jLi.text();
        return "00";
    },
    //...............................................................
    __get_val : function() {
        var UI = this;
        var vals = [];
        UI.arena.find('.ct-drop-con > ul').each(function(){
            var val = UI.__get_val_in_ul($(this));
            vals.push(val);
        });
        return vals.join(":");
    },
    //...............................................................
    __set_val_in_ul : function(jUl, val) {
        jUl.find('[current]').removeAttr('current');
        jUl.children().each(function(){
            if($(this).text() == val) {
                $(this).attr('current', 'yes');
            }
        });
    },
    //...............................................................
    __set_val : function(val) {
        var UI = this;
        var vals = val ? val.split(":") : ['--','--','--'];
        UI.arena.find('.ct-drop-con > ul').each(function(index){
            var val = vals[index];
            UI.__set_val_in_ul($(this), val);
        });
        return vals.join(":");
    },
    //...............................................................
    on_select_li : function(jLi) {
        var UI = this;

        // 高亮自身
        jLi.parent().children().removeAttr("current");
        jLi.attr('current', 'yes');

        // 得到值
        var val = UI.__get_val();

        // 设置值
        UI.__set_val(val);
        UI._set_data(val);
    },
    //...............................................................
    on_clean : function() {
        var UI = this;
        var jShow = UI.arena.find('.ct-show-time');
        jShow.text(UI.__time_blank);

        UI.__on_change();
    },
    //...............................................................
    _get_data : function(format){
        var UI = this;
        var opt = UI.options;
        var jShow = UI.arena.find('.ct-show-time');

        // 收集
        var str = jShow.text();
        
        // 空值
        if(/^(--:--)(:--)?$/.test(str)){
            return null;
        }
        // 返回时间
        else {
            return str;
        }
    },
    //...............................................................
    _set_data : function(val, jso){
        var UI = this;
        var ti = $z.parseTimeInfo(val);
        var jShow = UI.arena.find('.ct-show-time');

        // 得到时间显示字符串
        if(val || (_.isNumber(val) && val >= 0)) {
            jShow.text(ti.toString(UI.__time_format));
        }
        // 否则显示空
        else {
            jShow.text(UI.__time_blank);
        }
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jShow = UI.arena.find('.ct-show');
        var jDrop = UI.arena.find('.ct-drop-con');

        jDrop.css('width', jShow.outerWidth());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);