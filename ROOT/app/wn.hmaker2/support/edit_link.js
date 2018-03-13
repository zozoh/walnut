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
            on_change : function(key, val) {
                // 清除锚点的选择
                if("href" == key) {
                    this.update({anchor:null});
                }
            },
            fields : [{
                key : "href",
                icon : '<i class="fa fa-file"></i>',
                title : "i18n:hmaker.link.href",
                tip : "i18n:hmaker.link.href_tip",
                uiType : "@input",
                uiConf : {
                    assist : {
                        icon : '<i class="zmdi zmdi-caret-down"></i>',
                        uiType : "ui/form/c_list",
                        uiConf : {
                            drawOnSetData : true,
                            emptyItem : opt.emptyItem,
                            fixItems  : opt.fixItems,
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
                uiType : "@input",
                uiConf : {
                    assist : {
                        icon : '<i class="zmdi zmdi-caret-down"></i>',
                        uiType : "ui/form/c_list",
                        uiConf : {
                            drawOnSetData : true,
                            items : function(params, callback){
                                // 得到要选择的页面
                                var href = UI.gasket.main.getData("href");
                                // console.log("href", href);
                                // console.log("opt.pagePath", opt.pagePath)
                                // console.log(opt.pageObj.ph)
                                // console.log(opt.homeObj.ph)
                                
                                // 自动获取的 href 的话，不显示页内链接，因为不知道
                                if("@auto" == href) {
                                    return [];
                                }
                                // 如果就是当前页面，那么直接在页面里搜索
                                if(!href || opt.pagePath == href) {
                                    var uiComs = opt.pageUI.getComList();
                                    var list = [];
                                    for(var i=0; i<uiComs.length; i++) {
                                        var uiCom = uiComs[i];
                                        var lib   = uiCom.getMyLibInfo();
                                        // 忽略所有的组件内组件
                                        if(lib && lib.isInLib)
                                            continue;
                                        // 计入
                                        list.push({
                                            id    : uiCom.getComId(),
                                            ctype : uiCom.getComType(),
                                            skin  : uiCom.getComSkin(),
                                            lib   : lib
                                        });
                                        // 尝试获取控件内锚点
                                        var ans = uiCom.getMyAnchors();
                                        for(var x=0; x<ans.length; x++) {
                                            list.push({
                                                id     : uiCom.getComId(),
                                                ctype  : uiCom.getComType(),
                                                anchor : ans[x],
                                            });
                                        }
                                    }
                                    return list;
                                }
                                // 否则请求服务器，得到页面控件的列表
                                Wn.exec('hmaker "id:' + opt.homeObj.id + href + '" com -dis anchor -nolib',
                                    function(re){
                                        var list = $z.fromJson(re || "[]");
                                        $z.doCallback(callback, [list]);
                                    });
                            },
                            escapeHtml : false,
                            icon  : function(o){
                                if(o.anchor)
                                    return '<i class="fas fa-anchor"></i>';
                                return UI.msg('hmaker.com.' + o.ctype + '.icon');
                            },
                            text : function(o) {
                                if(o.anchor)
                                    return '<em>#' + o.anchor + '</em>';
                                var str = '<span>' + o.id + '</span>';
                                str += '<em>' ;
                                str += opt.anchorText(o);
                                str += '</em>';
                                if(o.lib) {
                                    str += '<u>' + o.lib.name + '</u>';
                                }
                                return str;
                            },
                            value : function(o) {
                                return o.anchor || o.id;
                            },
                        },
                    }
                }
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
        if(data.anchor){
            if(/^#/.test(data.anchor))
                re.push(data.anchor);
            else    
                re.push("#" + data.anchor);
        }

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
            // 只有锚点咯
            else if(/^#/.test(href)) {
                data = {anchor:href.substring(1)};
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