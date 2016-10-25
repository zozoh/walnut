(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com_dynamic'
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
</div>
<div class="hmc-objshow ui-arena hm-del-save hmc-dds">
    <section class="hmc-dds-msg"></section>
    <div class="hmc-objshow-data"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objshow", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    setupProp : function(){
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