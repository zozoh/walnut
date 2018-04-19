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

        // 默认的记录一下自己原始的属性
        this.__old_data = "{}";
    },
    //..............................................
    events : {
        // 显示下拉列表
        'click .srt-show' : function(e){
            e.stopPropagation();
            var UI = this;
            var jShow  = UI.arena.find(">.srt-show");
            var jItemsUl = UI.arena.find(">.srt-items ul");
            this.arena.attr("show-drop", "yes");
            $z.dock(jShow, jItemsUl, "H");
        },
        // 隐藏下拉列表
        'click .search-sorter' : function(){
            this.arena.removeAttr("show-drop");
        },
        // 切换项目
        'click .srt-items li' : function(e) {
            e.stopPropagation();
            var UI  = this;
            var opt = UI.options;
            var jLi = $(e.currentTarget);
            var index = jLi.prevAll().length;

            UI.setData(index);
            UI.arena.removeAttr("show-drop");
            if(opt.storeKey)
                UI.local(opt.storeKey, index);
            UI.__on_change();
        }
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
                var icon = ({
                    asc  : '<i class="zmdi zmdi-sort-amount-asc"></i>',
                    desc : '<i class="zmdi zmdi-sort-amount-desc"></i>',
                })[srt.icon] || srt.icon;
                $('<span class="si-icon">')
                    .html(icon)
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

        // 得到默认显示的排序方式，并设置
        var index = 0;
        if(opt.storeKey)
            index = UI.local(opt.storeKey) || 0;
        UI.setData(index);

    },
    //..............................................
    __on_change : function() {
        var data = this.getData();
        var json = $z.toJson(data);
        if(json != this.__old_data) {
            this.__old_data = json;
            this.trigger("sorter:change", data);
        }
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
            if(_.isEqual(srt.value, obj)){
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
            
            var json = jItem.attr('val');
            jShow.attr('val', json);
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