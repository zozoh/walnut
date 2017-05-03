(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/support/dom',
    'ui/pop/pop',
], function(ZUI, FormMethods, DomUI, POP){
//==============================================
var html = function(){/*
<div class="ui-arena com-icon">
    <span></span>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_icon", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        FormMethods(this);

        $z.setUndefined(opt, "dftIcon", '<i class="zmdi zmdi-image-o"></i>');
        $z.setUndefined(opt, "balloon", 'up:modify');
        $z.setUndefined(opt, "pop_title", "i18n:choose");
        $z.setUndefined(opt, "pop_width", 470);
        $z.setUndefined(opt, "pop_height", 526);
        $z.setUndefined(opt, "icons", [
            '<i class="zmdi zmdi-car"></i>',
            '<i class="zmdi zmdi-cake"></i>',
            '<i class="zmdi zmdi-home"></i>',
            '<i class="zmdi zmdi-globe"></i>',
            '<i class="zmdi zmdi-label"></i>',
            '<i class="zmdi zmdi-library"></i>',
            '<i class="zmdi zmdi-movie"></i>',
            '<i class="zmdi zmdi-print"></i>',
            '<i class="zmdi zmdi-settings"></i>',
            '<i class="zmdi zmdi-shape"></i>',
            '<i class="zmdi zmdi-shield-security"></i>',
            '<i class="zmdi zmdi-store"></i>',
            '<i class="zmdi zmdi-subway"></i>',
            '<i class="zmdi zmdi-traffic"></i>',
            '<i class="zmdi zmdi-wrench"></i>',
            '<i class="zmdi zmdi-camera-roll"></i>',
            '<i class="zmdi zmdi-devices"></i>',
            '<i class="zmdi zmdi-desktop-mac"></i>',
            '<i class="zmdi zmdi-laptop-mac"></i>',
            '<i class="zmdi zmdi-mouse"></i>',
            '<i class="zmdi zmdi-mic"></i>',
            '<i class="zmdi zmdi-phone"></i>',
            '<i class="zmdi zmdi-smartphone-iphone"></i>',
            '<i class="zmdi zmdi-usb"></i>',
            '<i class="zmdi zmdi-wifi-alt"></i>',
            '<i class="fa fa-bug"></i>',
            '<i class="fa fa-keyboard-o"></i>',
            '<i class="fa fa-paper-plane-o"></i>',
            '<i class="fa fa-life-ring"></i>',
            '<i class="fa fa-rocket"></i>',
            '<i class="fa fa-plane"></i>',
            '<i class="fa fa-credit-card-alt"></i>',
            '<i class="fa fa-pie-chart"></i>',
            '<i class="fa fa-area-chart"></i>',
            '<i class="fa fa-bluetooth"></i>',
            '<i class="fa fa-github"></i>',
            '<i class="fa fa-qq"></i>',
            '<i class="fa fa-weibo"></i>',
            '<i class="fa fa-weixin"></i>',
            '<i class="fa fa-youtube-square"></i>',
            '<i class="fa fa-twitter"></i>',
            '<i class="fa fa-facebook-official"></i>',
            '<i class="fa fa-amazon"></i>',
            '<i class="fa fa-android"></i>',
            '<i class="fa fa-chrome"></i>',
            '<i class="fa fa-edge"></i>',
            '<i class="fa fa-html5"></i>',
            '<i class="fa fa-css3"></i>',
        ]);
    },
    //...............................................................
    events : {
        "click span" : function(){
            var UI  = this;
            var opt = UI.options;

            // 准备要显示的 HTML
            var html = '<div class="ui-arena cil-con">';
            html += '<div class="cil-list"><ul>';
            for(var i=0; i<opt.icons.length; i++) {
                html += '<li>' + opt.icons[i] + '</li>';
            }
            html += '</ul></div>';
            html += '<div class="cil-input">';
            html += '<input spellcheck="false" placeholder="{{com.icon.html_tip}}">';
            html += '<div class="cil-more">';
            html += '<b>{{com.icon.more}}</b>';
            html += '<a target="_blank" href="http://fontawesome.io/icons/">Fontawesome</a>'
            html += ' | <a target="_blank" href="http://zavoloklom.github.io/material-design-iconic-font/icons.html">Material Design</a>'
            html += '</div></div></div>';
            
            // 显示一个遮罩
            POP.openUIPanel({
                title  : UI.text(opt.pop_title),
                i18n   : UI._msg_map,
                width  : opt.pop_width,
                height : opt.pop_height,
                arenaClass : "com-icon-pop",
                setup : {
                    uiType : 'ui/support/dom',
                    uiConf : {
                        fitparent : true,
                        dom : html,
                        events : {
                            "click .cil-list li" : function(e){
                                var jLi = $(e.currentTarget);
                                this.arena.find(".cil-input input").val(jLi.html());
                            }
                        }
                    }
                },
                ok : function(uiBody) {
                    var icon = uiBody.arena.find(".cil-input input").val();
                    UI._set_data(icon);
                    UI.__on_change();
                }
            });
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        UI.arena.attr("balloon", opt.balloon);

        UI.balloon();
    },
    //...............................................................
    _get_data : function(){
        var UI  = this;
        var opt = UI.options;
        var icon = UI.arena.find("span").html();
        if(icon == opt.dftIcon)
            return null;
        return icon;
    },
    //...............................................................
    _set_data : function(val, jso){
        var UI  = this;
        var opt = UI.options;
        UI.arena.find("span").html(val || opt.dftIcon);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);