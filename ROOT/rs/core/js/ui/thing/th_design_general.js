(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/form/form',
    'ui/menu/menu',
    'ui/list/list',
    'ui/support/dom',
], function(ZUI, Wn, CIconUI, CNameUI, FormUI, MenuUI, ListUI, DomUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-general" ui-fitparent="yes" ui-gasket="main">
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_general", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI  = this;       
        //--------------------------------------- 集合通用设置
        new FormUI({
            parent : UI,
            gasketName : "main",
            uiWidth : "all",
            displayMode : "compact",
            on_change : function(){
                UI.notifyChanged();
            },
            fields : [{
                title : "显示设定",
                fields : [{
                    key : "searchMenuFltWidthHint",
                    title : "菜单收缩",
                    type : "string",
                    dft : "",
                    editAs : "input",
                    uiConf : {
                        placeholder : "50%"
                    }
                }, {
                    key : "thIndex",
                    title : "显示索引",
                    type : "object",
                    dft : [],
                    editAs : "switch",
                    uiConf : {
                        multi : true,
                        items : [
                            {value : "meta",   text : "对象属性"},
                            {value : "detail", text : "详情内容"},
                        ]
                    }
                }, {
                    key : "thData",
                    title : "显示数据",
                    type : "object",
                    dft : [],
                    editAs : "switch",
                    uiConf : {
                        multi : true,
                        items : [
                            {value : "media",      text : "多媒体"},
                            {value : "attachment", text : "附件"},
                        ]
                    }
                }]
            }],
        }).render(function(){
            UI.defer_report("setup");
        });

        // 返回延迟加载
        return ["setup"];
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var setupObj = UI.gasket.main.getData();
        //console.log(setupObj)
        return {
            searchMenuFltWidthHint : setupObj.searchMenuFltWidthHint,
            meta       : setupObj.thIndex.indexOf("meta")>=0,
            detail     : setupObj.thIndex.indexOf("detail")>=0,
            media      : setupObj.thData.indexOf("media")>=0,
            attachment : setupObj.thData.indexOf("attachment")>=0
        };
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 更新通用全局配置
        var setupObj = {
            searchMenuFltWidthHint : thConf.searchMenuFltWidthHint,
            thIndex : [],
            thData  : []
        };
        // thIndex
        if(thConf.meta)
            setupObj.thIndex.push("meta");
        if(thConf.detail)
            setupObj.thIndex.push("detail");
        
        // thData
        if(thConf.media)
            setupObj.thData.push("media");
        if(thConf.attachment)
            setupObj.thData.push("attachment");

        // 更新
        UI.gasket.main.setData(setupObj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);