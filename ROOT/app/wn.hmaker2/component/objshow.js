(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_dynamic'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="noapi" class="dds-warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.dds.noapi}}
    </div>
    <div code-id="notemplate" class="dds-warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.dds.notemplate}}
    </div>
    <div code-id="api.empty"   class="api-empty">{{hmaker.dds.api_empty}}</div>
    <div code-id="api.loading" class="api-loading">
        <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i> {{hmaker.dds.api_loading}}
    </div>
    <div code-id="api.lackParams" class="api-lack-params">
        <i class="zmdi zmdi-alert-circle-o"></i> {{hmaker.dds.api_lack_params}}
    </div>
</div>
<div class="hmc-objshow ui-arena hm-del-save hmc-dds">
    <section class="hmc-dds-msg"></section>
    <div class="hmc-objshow-data hmc-dynamic-con"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objshow", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        "click .dynamic-reload" : function(){
            var UI    = this;
            var jData = UI.arena.children(".hmc-objshow-data");
            UI.__reload_data(null, jData);
        }
    },
    //...............................................................
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/objshow_prop',
            uiConf : {}
        };
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 检查显示模式
        if(!UI.__check_mode(com)){
            return ;
        }

        // 记录一下接口的特征，以防止重复加载
        UI.__paint_data(com, UI.arena.children(".hmc-objshow-data").show());
    },
    //...............................................................
    __draw_data : function(obj, com) {
        var UI  = this;
        com = com || UI.getData();
        
        // 清空显示
        var jData = UI.arena.children(".hmc-objshow-data").empty();

        // 绘制动态参数键列表
        UI.__draw_dynamic_keys(jData);

        // 绘制重新加载按钮
        UI.__draw_dynamic_reload(jData);

        // 动态参数，但是缺少默认值，那么就没有足够的数据绘制了，显示一个信息吧
        if(UI.isDynamicButLackParams()) {
            UI.ccode("api.lackParams").appendTo(jData);
            return;
        }

        // 如果木有数据，就显示空
        if(!obj) {
            UI.ccode("api.empty").appendTo(jData);
            return;
        }

        // 加载模板
        var tmplInfo = UI.evalTemplate(com.template);

        // 得到皮肤选择器
        var skinSelector = UI.getSkinForTemplate(com.template);

        // 准备绘制模板参数
        var tmplOptions = _.extend({}, com.options, {
            API : UI.getHttpApiUrl()
        });

        // 绘制
        var ele  = document.createElement(tmplInfo.tagName || 'DIV');
        var jDiv = $(ele).appendTo(jData)[tmplInfo.name](obj, tmplOptions);
        if(skinSelector)
            jDiv.addClass(skinSelector);

    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);