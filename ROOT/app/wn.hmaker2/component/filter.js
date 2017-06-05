(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hmc_filter.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-filter hmc-cnd hm-empty-save"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_filter", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-filter",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 展开收起折叠项
        "click .hmcf-exts b" : function(){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 切换折叠状态
            UI.__is_folder_show = !UI.__is_folder_show;
            UI.__sync_folder();
        },
        // 选择选项
        "click li[it-type]" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 切换开关
            var jLi = $(e.currentTarget);
            UI.arena.hmc_filter("selectItem", jLi);

            // 保存默认值
            UI.__save_defaultValue();
        },
        // 清除全部选项
        "click .fld-info em" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;
            
            // 切换开关
            UI.arena.hmc_filter("selectItem", e.currentTarget);
            
            // 保存默认值
            UI.__save_defaultValue();
        },
        // 多选开关: 启用
        "click .fld-multi b" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;
            
            // 切换开关
            UI.arena.hmc_filter("enableMulti", e.currentTarget);
        },
        // 多选开关: 关闭
        "click .fld-it-check-cancel" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;
            
            // 切换开关
            UI.arena.hmc_filter("disableMulti", e.currentTarget);

            // 保存默认值
            UI.__save_defaultValue();
        },
        // 多选开关: 确认
        "click .fld-it-check-ok" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;
            
            // 切换开关
            UI.arena.hmc_filter("applyMulti", e.currentTarget);

            // 保存默认值
            UI.__save_defaultValue();
        },
        // 切换选项的更多/更少的开关
        "click .fld-more b" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 切换开关
            var jB = $(e.currentTarget);
            UI.arena.hmc_filter("toggleItems", jB);
        },
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 标识保存时属性
        UI.arena.addClass("hm-empty-save");

        // 得到之前的值
        var flt = com.defaultValue || UI.arena.hmc_filter("value");

        // 绘制
        UI.arena.hmc_filter(_.extend({
            forIDE    : true,
            emptyHtml : '<i class="zmdi zmdi-alert-circle-o"></i> ' 
                        + UI.msg("hmaker.com.filter.empty")
        }, com));

        // 将值设置回去
        UI.arena.hmc_filter("value", flt);

        // 同步折叠项的状态
        UI.__sync_folder();
        
    },
    //...............................................................
    getComValue : function() {
        return this.arena.hmc_filter("value");
    },
    //...............................................................
    __save_defaultValue : function(){
        this.saveData("page", {
            defaultValue : this.getComValue()
        }, true);
    },
    //...............................................................
    __sync_folder : function(){
        var UI = this;
        // 显示折叠项
        if(UI.__is_folder_show) {
            UI.arena.hmc_filter("showFolder");
        }
        // 隐藏折叠项
        else{
            UI.arena.hmc_filter("hideFolder");
        }
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
        var UI = this;
        return {
            btnExtTextShow : UI.msg("hmaker.com.filter.ext_show"),
            btnExtTextHide : UI.msg("hmaker.com.filter.ext_hide"),
            btnMultiText   : UI.msg("hmaker.com.filter.multi"),
            btnMultiOkText : UI.msg("hmaker.com.filter.multi_ok_txt"),
            btnMultiCancelText : UI.msg("hmaker.com.filter.multi_cancel_txt"),
            moreItemsMode      : "auto",
            btnFldMoreText     : UI.msg("hmaker.com.filter.fld_more_txt"),
            btnFldLessText     : UI.msg("hmaker.com.filter.fld_less_txt"),
            fields: [{
                    text: "价格", name: "price", multi: false, hide: false,
                    items: [
                        {text: "小于10元", type: "number_range", value: "(,10)"},
                        {text: "不超过100元", type: "number_range",value: "(,100)"},
                        {text: "不超过500元",type: "number_range",value: "(,500)"},
                        {text: "500以上",type: "number_range",value: "[500,)"}
                    ]
                }, {
                    text: "品牌", name: "brand", multi: true, hide: false,
                    items: [
                        {text: "苹果",type: "string",value: "apple"},
                        {text: "三星",type: "string",value: "samsung"},
                        {text: "小米",type: "string",value: "xiaomi"},
                        {text: "华为",type: "string",value: "huawi"},
                        {text: "其他",type: "string",value: "other"}
                    ]
                }
            ],
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/filter_prop',
            uiConf : {}
        };
    },
    //...............................................................
    resize : function(){
        this.arena.hmc_filter("moreAllItem");
    }
});
//===================================================================
});
})(window.NutzUtil);