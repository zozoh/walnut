(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmComMethods, DroplistUI){
//==============================================
var html = `
<div class="ui-arena hm-dynamic-data-setting">
    <section part="api">
        <dl>
            <dt>{{hmaker.dds.api}}</dt>
            <dd ui-gasket="api"></dd>
            <dd class="refresh">
                <i class="zmdi zmdi-refresh-sync"></i>
                <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i>
            </dd>
        </dl>
    </section>
    <section class="hmdds-params"><table><tbody></tbody></table></section>
    <section part="template">
        <dl>
            <dt>{{hmaker.dds.template}}</dt>
            <dd ui-gasket="template"></dd>
            <dd class="refresh">
                <i class="zmdi zmdi-refresh-sync"></i>
                <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i>
            </dd>
        </dl>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_dynamic_data_setting", {
    dom  : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 刷新数据接口
        'click .refresh' : function(e) {
            console.log("hahah")
            var UI = this;
            var jq = $(e.currentTarget);
            var part = jq.closest("section").attr("part");
            jq.attr("loading", "yes");
            
            window.setTimeout(function(){
                UI.gasket[part].refresh(function(){
                    jq.removeAttr("loading");
                });
            }, 800);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // API
        new DroplistUI({
            parent : UI,
            gasketName : "api",
            items : "obj ~/.regapi/api/* -ExtendFilter -json -l",
            icon  : '<i class="fa fa-plug"></i>',
            text  : null,
            value : function(o){
                return "/" + Wn.getRelativePath(oApiHome, o);
            }
        }).render(function(){
            UI.defer_report("api");
        });

        // Template
        new DroplistUI({
            parent : UI,
            gasketName : "template",
            items : "obj ~/.hmaker/template/* -json -l",
            icon  : '<i class="fa fa-puzzle-piece"></i>',
            text  : null,
            value : function(o){
                return o.nm;
            }
        }).render(function(){
            UI.defer_report("template");
        });


        // 返回延迟加载
        return ["api", "template"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        
        console.log("I am update")

        // 更新翻页器
        UI.gasket.pager.setData(com.pager||{});

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    resize : function() {
        var UI = this;

    }
});
//===================================================================
});
})(window.NutzUtil);