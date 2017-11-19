(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena search-sorter">
    <div class="srt-show srt-it">
        <span class="si-icon"></span>
        <span class="si-text">Sort</span>
        <span class="srt-drop-btn">
            <i class="zmdi zmdi-caret-down"></i>
        </span>
    </div>
    <div class="srt-items"><ul></ul></div>
</div>
*/};
//==============================================
return ZUI.def("ui.search_sorter", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(opt) {
        $z.setUndefined(opt, "setup", []);
    },
    //..............................................
    events : {
        
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        var jUl = UI.arena.find(">.srt-items>ul");

        // 首先绘制数据
        for(var i=0; i<opt.setup.length; i++) {
            var srt = opt.setup[i];
            var jLi = $('<li class="srt-it">');

            // 图标
            if(srt.icon) {
                $('<span class="si-icon">')
                    .html(srt.icon)
                        .appendTo(jLi);
            }

            // 文字
            if(srt.text) {
                $('<span class="si-text">')
                    .text(UI.text(srt.text))
                        .appendTo(jLi);
            }

            // 数据
            var json = $z.toJson(srt.value || {});
            jLi.attr("val", json);

            // 默认显示 
            if(!srt.icon && !srt.text) {
                $('<span class="si-text">')
                    .text(json)
                        .appendTo(jLi);
            }

            // 加入 DOM
            jLi.appendTo(jUl);
        }

        // 默认显示第一个
        UI.setData(0);

    },
    //..............................................
    __find_item_index : function(val) {
        var UI  = this;
        var opt = UI.options;
        // 字符串的话，变成对象 
        var obj = _.isString(val) ? $z.fromJson(val) : val;
        // 依次查找 
        for(var i=0; i<opt.setup.length; i++) {
            var srt = opt.setup[i];
            if(_.isEqual(srt, obj)){
                return i;
            }
        }
        // 返回 -1 表示找不到 
        return -1;
    },
    //..............................................
    setData : function(srt){
        var UI  = this;
        var opt = UI.options;
        
        // 数字的话 ...
        if(_.isNumber(srt)) {
            var jItem = UI.arena.find(".srt-items li").eq(srt);
            if(jItem.length <= 0){
                jItem = UI.arena.find(".srt-items li").eq(0);
            }
            var jShow = UI.arena.find(">.srt-show");
            jShow.find('>.si-icon').html(jItem.find('>.si-icon').html());
            jShow.find('>.si-text').html(jItem.find('>.si-text').html());
            jShow.attr('val', jItem.attr('val'));
        }
        // 直接是值，那么查找一下
        else {
            var index = UI.__find_item_index(srt);
            UI.setData(index >= 0 ? index : 0);
        }
    },
    //..............................................
    getData : function(asString){
        var json = this.arena.find(">.srt-show").attr("val");
        if(asString)
            return json;
        return $z.fromJson(json);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);