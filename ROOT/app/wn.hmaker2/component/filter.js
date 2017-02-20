(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-filter hmc-cnd">hahahaha</div>';
//==============================================
return ZUI.def("app.wn.hm_com_filter", {
    dom     : html,
    keepDom   : true,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        "click .hmcf-exts b" : function(){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 切换折叠状态
            UI.__is_folder_show = !UI.__is_folder_show;
            UI.__sync_folder();
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 清空绘制区
        UI.arena.empty();

        // 绘制
        if(_.isArray(com.fields) && com.fields.length > 0){
            // 准备折叠项目列表
            var folders = [];

            // 绘制显示项目
            var jList = $('<div class="hmcf-list">').appendTo(UI.arena);
            for(var i=0; i<com.fields.length; i++) {
                var fld = com.fields[i];
                // 折叠项目
                if(fld.hide) {
                    folders.push(fld);
                }
                // 绘制项目
                else {
                    UI.__draw_fld(fld, jList);
                }
            }

            // 绘制折叠项目
            if(folders.length > 0) {
                var jFolder = $('<div class="hmcf-folder">').appendTo(UI.arena);
                for(var i=0; i<folders.length; i++) {
                    UI.__draw_fld(folders[i], jFolder);
                }
                $(UI.compactHTML(`<div class="hmcf-exts">
                    <b msg-show="{{hmaker.com.filter.ext_show}}"
                    msg-hide="{{hmaker.com.filter.ext_hide}}"></b>
                </div>`)).appendTo(UI.arena);
                UI.__sync_folder();
            }
        }
        // 显示空
        else {
            UI.arena.html(UI.compactHTML(`<div class="empty">
                <i class="zmdi zmdi-alert-circle-o"></i>
                {{hmaker.com.filter.empty}}
            </div>`));
        }
        
    },
    //...............................................................
    __sync_folder : function(){
        var UI = this;
        var jFolder = UI.arena.find(".hmcf-folder");
        var jExt = UI.arena.find(".hmcf-exts");
        var jExtBtn = jExt.find("b");
        // 显示折叠项
        if(UI.__is_folder_show) {
            jFolder.show();
            jExtBtn.text(jExtBtn.attr("msg-hide"));
        }
        // 隐藏折叠项
        else{
            jFolder.hide();
            jExtBtn.text(jExtBtn.attr("msg-show"));
        }
    },
    //...............................................................
    // 绘制项目
    __draw_fld : function(fld, jList) {
        var UI   = this;
        var jDiv = $('<div class="hmcf-fld">').attr("key", fld.name);
        
        // 绘制字段标题
        $('<span class="fld-info"><em></em></span>').appendTo(jDiv)
            .find("em").text(fld.text);

        // 绘制选项
        var jUl = $("<ul>").appendTo(jDiv);
        if(_.isArray(fld.items) && fld.items.length > 0) {
            for(var i=0; i<fld.items.length; i++) {
                var item = fld.items[i];
                var jLi  = $('<li>').appendTo(jUl);
                jLi.attr({
                    "it-type"  : item.type,
                    "it-value" : item.value,
                });
                $('<span>').text(item.text).appendTo(jLi);
            }
        }

        // 绘制可多选
        if(fld.multi){
            $('<span class="fld-multi"><b></b></span>').appendTo(jDiv)
                .find("b").text(UI.msg("hmaker.com.filter.multi"));
        }

        // 加入 DOM
        jDiv.appendTo(jList);
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            // "lineHeight" : ".24rem",
            // "fontSize"   : ".14rem",
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/filter_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);