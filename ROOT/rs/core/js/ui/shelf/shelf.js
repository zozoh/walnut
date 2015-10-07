(function($z){
$z.declare(['zui','wn/util'], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="true">
    <div  class="shelf-sky"   ui-gasket="sky"></div>
    <div class="shelf-chute"  ui-gasket="chute"></div>
    <div class="shelf-main"   ui-gasket="main"></div>
    <div class="shelf-view"   ui-gasket="view"></div>
    <div class="shelf-footer" ui-gasket="footer"></div>
</div>
*/};
//=======================================================================
function _check_display_item(options, nm, dft){
    if(options[nm]){
        if(!options.display[nm])
            options.display[nm] = dft;
    }
}
function _resize_sky_or_footer(sz, UI, dis, nm){
    var jq = UI.arena.children(".shelf-"+nm);
    if(dis[nm]) {
        jq.css({"width": sz.width, "height": dis[nm]});
        return jq.outerHeight();
    }
    jq.remove();
    return 0;
}
function _resize_chute_main_view(sz, avgs, UI, dis, nm){
    var v = dis[nm];
    var jq = UI.arena.children(".shelf-"+nm)
    if(!v){
        jq.remove();
        return;
    }
    jq.css({
        "top"   : sz.top,
        "height": sz.H
    });
    // *
    if("*" == v){
        avgs.push(jq);
        return;
    }
    // 20%
    var m = /^([0-9]{1,2})%$/g.exec(v);
    var w;
    if(m)
        w = parseInt(m[1] * 1 * sz.width / 100);
    // 341
    else
        w = v * 1;
    
    jq.css("width", w);
    sz.W -= w;
}
//=======================================================================
return ZUI.def("ui.shelf", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/shelf/shelf.css",
    //...............................................................
    init : function(options){
        if(typeof options.fitparent == "undefined")
            options.fitparent = true;
        options.display = options.display || {};
        _check_display_item(options, "dock", {size:80, position:"bottom", autoHide:true});
        _check_display_item(options, "sky",    80);
        _check_display_item(options, "chute",  240);
        _check_display_item(options, "main",   "*");
        _check_display_item(options, "view",   "20%");
        _check_display_item(options, "footer", 32);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        var uiTypes = [];
        var confs = [];
        ["sky","chute","main","view","footer"].forEach(function(nm){
            var conf = UI.options[nm] || {};
            if(conf.uiType){
                uiTypes.push(conf.uiType);
                confs.push(_.extend({}, conf.uiConf, {
                    parent     : UI,
                    gasketName : nm
                }));
            }
        });
        // 记录回调，准备延迟加载
        seajs.use(uiTypes, function(){
            for(var i in uiTypes){
                var SubUI = arguments[i];
                var conf = confs[i];
                try{
                    (function(index, uiType, conf){
                        new SubUI(conf).render(function(){
                            UI.defer_report(index, uiType);
                        });
                    })(i, uiTypes[i], conf);
                }catch(E){
                    throw "fail SubUI: (" + i + ")" + uiTypes[i] + " : " + E;
                }
            }
        });
        // 需要延迟
        return uiTypes;
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var dis = UI.options.display;
        if(dis.dock){

        }
        var sz = {
            width  : UI.arena.width(),
            height : UI.arena.height()
        }
        sz.W = sz.width;
        sz.H = sz.height;

        sz.top    = _resize_sky_or_footer(sz, UI, dis, "sky");
        sz.bottom = _resize_sky_or_footer(sz, UI, dis, "footer");
        sz.H -= sz.top;
        sz.H -= sz.bottom;
        
        // 准备平均分配项目
        var avgs = [];
        _resize_chute_main_view(sz, avgs, UI, dis, "chute");
        _resize_chute_main_view(sz, avgs, UI, dis, "main");
        _resize_chute_main_view(sz, avgs, UI, dis, "view");

        // 均分一下
        if(avgs.length > 0){
            if(sz.W>0){
                var n = parseInt(sz.W / avgs.length);
                avgs.forEach(function(jq){
                    jq.__width = n;
                    sz.W -= n;
                });
            }
            // 还有剩余，则循环分配一次 1 像素
            if(sz.W>0){
                avgs.forEach(function(jq){
                    jq.__width += 1;
                    sz.W -= 1;
                });
            }
            // 执行设置
            avgs.forEach(function(jq){
                jq.css({"width": jq.__width});
            });
        }

        // 如果有 chute 那么 main 的 left 也需要改改
        if(dis.chute && dis.main){
            var jChute = UI.arena.children(".shelf-chute");
            var jMain  = UI.arena.children(".shelf-main");
            jMain.css("left", jChute.outerWidth());
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);