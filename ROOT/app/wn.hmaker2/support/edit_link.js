(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
], function(ZUI, Wn, FormUI){
//==============================================
var html = `
<div class="ui-arena edit-link" ui-gasket="main"></div>`;
//==============================================
return ZUI.def("ui.edit_link", {
    dom  : html,
    css  : 'app/wn.hmaker2/support/theme/hmaker_support-{{theme}}.css',
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        var homeId = opt.homeObj.id;

        new FormUI({
            parent : UI,
            gasketName : "main",
            displayMode : "compact",
            uiWidth : "all",
            fields : [{
                key : "href",
                icon : '<i class="fa fa-file"></i>',
                title : "i18n:hmaker.link.href",
                tip : "i18n:hmaker.link.href_tip",
                uiType : "@input",
                uiConf : {
                    assist : {
                        icon : '<i class="zmdi zmdi-more"></i>',
                        uiType : "ui/form/c_list",
                        uiConf : {
                            drawOnSetData : true,
                            items : 'hmaker id:'+homeId+' links -key "rph,nm,tp,title" -site',
                            escapeHtml : false,
                            icon  : function(o){
                                // 页面
                                if('html' == o.tp && !$z.getSuffixName(o.nm)) {
                                    return  '<i class="fa fa-file"></i>';
                                }
                                // 其他遵守 walnut 的图标规范
                                return Wn.objIconHtml(o);
                            },
                            text : function(o) {
                                var str = '<span>/' + o.rph + '</span>';
                                if(o.title) {
                                    str += '<em>' + o.title + '</em>';
                                }
                                return str;
                            },
                            value : function(o) {
                                return "/" + o.rph;
                            },
                        },
                    }
                }
            }, {
                key : "anchor",
                icon : '<i class="fa fa-anchor"></i>',
                title : "i18n:hmaker.link.anchor",
                tip : "i18n:hmaker.link.anchor_tip",
                uiType : "@input"
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    getData : function() {
        // 得到对象
        var data = this.gasket.main.getData();

        // 返回拼合的字符串
        var re = [];
        if(data.href)
            re.push(data.href);
        if(data.anchor)
            re.push("#" + data.anchor);

        return re.join("");
    },
    //...............................................................
    setData : function(href) {
        // 将其分解成一个对象 `/abc#xyz`
        var data = href;
        if(_.isString(href)){
            var m = /^([^#]+)(#(.*))?$/.exec(href);
            // 有锚点
            if(m) {
                data = {
                    href   : m[1],
                    anchor : m[3],
                };
            }
            // 只有链接咯
            else {
                data = {href:href};
            }
        }

        // 设置值
        this.gasket.main.setData(data);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);