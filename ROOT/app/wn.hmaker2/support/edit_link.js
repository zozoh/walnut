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

        UI.arena.addClass("hm-link-params");

        var homeId = opt.homeObj.id;

        new FormUI({
            parent : UI,
            gasketName : "main",
            displayMode : "compact",
            uiWidth : "all",
            on_change : function(key, val) {
                // 改变链接时
                if("href" == key) {
                    UI.__on_href_change(val);
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
                key : "params",
                icon : '<i class="fa fa-file"></i>',
                title : "i18n:hmaker.link.params",
                tip : "i18n:hmaker.link.params_tip",
                type : "object",
                uiType : "@pair",
                uiConf : {
                    templateAsDefault : false,
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
                                return UI.__load_anchor(callback);
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
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //...............................................................
    __on_href_change : function(href){
        var UI = this;

        // 获取该页面所有参数，更新参数表
        // 同时清除原先的锚点值
        UI.__load_param(href, function(params){
            UI.gasket.main.update({
                anchor: null,
                params: params
            });
        });
    },
    //...............................................................
    __load_param : function(href, callback) {
        var UI  = this;
        var opt = UI.options;

        // 判断参数形态
        if(_.isFunction(href)) {
            callback = href;
            href = null;
        }

        // 超链接，不做判断
        if(!href || /^https?:\/\//.test(href)){
            $z.doCallback(callback, [{}]);
            return;
        }

        // 获取一个纯纯页面链接
        href = UI._HREF(href);

        // 自动获取的 href 的话，不显示参数表，因为不知道
        if("@auto" == href) {
            $z.doCallback(callback, [{}]);
            return;
        }
        // 如果就是当前页面，那么直接在页面里搜索
        if(!href || opt.pagePath == href) {
            var uiComs = opt.pageUI.getComList();
            var params = {};
            for(var i=0; i<uiComs.length; i++) {
                var uiCom = uiComs[i];
                //console.log(uiCom.getComType(), uiCom.getComId())
                var pamap = uiCom.getMyParams();
                if(pamap) {
                    _.extend(params, pamap);
                }
            }
            $z.doCallback(callback, [params]);
        }
        // 否则就到服务器搜索
        else {
            Wn.exec('hmaker "id:' + opt.homeObj.id + href + '" com -dis param -nolib',
                function(re){
                    var params = $z.fromJson(re || "{}");
                    $z.doCallback(callback, [params]);
                });
        }
    },
    //...............................................................
    __load_anchor : function(callback) {
        var UI  = this;
        var opt = UI.options;
        // 得到要选择的页面
        var href = UI.gasket.main.getData("href");

        // 获取一个纯纯页面链接
        href = UI._HREF(href);
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
    //...............................................................
    // 获取一个纯纯页面链接
    _HREF : function(href) {
        if(!href)
            return "";

        var pos_a = href.lastIndexOf('#');
        var pos_q = href.lastIndexOf('?');
        var pos   = Math.min(pos_a, pos_q);

        if(pos > 0)
            return href.substring(0, pos);

        return href;
    },
    //...............................................................
    getData : function() {
        // 得到对象
        var data = this.gasket.main.getData();
        return $z.renderHref(data, true);
    },
    //...............................................................
    setData : function(href) {
        var UI = this;
        //console.log("setData(href):", href);
        // 将其分解成一个对象 `/abc#xyz`
        var data = href;
        if(_.isString(href)){
            data = $z.parseHref(href, true);
        }

        // 设置值
        UI.__load_param(data.href, function(params){
            // 更新参数
            if(data.params) {
                data.params = _.extend(params, data.params);
            }
            else
                data.params = params;

            // 设置值
            UI.gasket.main.setData(data);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);