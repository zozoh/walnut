(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
    'ui/menu/menu',
    'ui/form/form',
    'ui/support/dom'
], function(ZUI, Wn, SearchUI, MenuUI, FormUI, DomUI){
//==============================================
var THING_DETAIL = function(){
    return {
        loadContent : function(obj, callback) {
            // console.log("loadContent", obj.id);
            // 有内容
            if (obj.len > 0) {
                Wn.execf("thing {{th_set}} detail {{id}}", obj, function(re) {
                    callback(re);
                });
            }
            // 无内容
            else {
                callback("");
            }
        },
        saveContent : function(obj, content, callback) {
            // console.log("saveContent", obj.id, content.substring(0, 10));
            Wn.execf("thing {{th_set}} detail {{id}} -content", content, obj, function(re) {
                callback(re);
            });
        },
        parseData : function(th) {
            // console.log("parseData:", th.th_nm);
            return {
                id : th.id,
                contentType : {
                    "text/plain"    : "text",
                    "text/markdown" : "markdown",
                    "text/html"     : "html",
                }[th.mime] || "text/plain",
                brief  : th.brief,
                len    : th.len,
                th_set : th.th_set
            };
        },
        formatData : function(obj) {
            // console.log("formatData:", obj);
            return {
                id    : obj.id,
                mime  : {
                    "text"      : "text/plain",
                    "markdown"  : "text/markdown",
                    "html"      : "text/html"
                }[obj.contentType] || 'txt',
                brief : obj.brief
            }
        }
    };
};
//==============================================
var THING_FILE = function(fld, subhdl, validate) {
    return {
        title : fld.title || "i18n:thing.key." + subhdl,
        escapeHtml : false,
        display : function(o, jso, UI) {
            var nb = o["th_" + subhdl + "_nb"] || 0;
            var html = "";
            // 显示数量
            if(nb > 0 ){
                html += '<span class="th_file_nb">';
                // 媒体文件
                if("media" == subhdl){
                    html += '<i class="zmdi zmdi-camera-alt"></i>';
                }
                // 附件
                else if("attachment" == subhdl) {
                    html += '<i class="zmdi zmdi-attachment-alt"></i>';
                }
                // 其他
                else {
                    html += '<i class="zmdi zmdi-file"></i>';
                }
                html += '<em>' + nb + '</em>';
                html += '</span>';
            }
            // 显示空
            else {
                html += '<em class="th_file_none">' + UI.msg("none") + '</em>';
            }
            return html;
        },
        virtual : true,
        editAs  : "file",
        uiConf  : {
            multi : _.isUndefined(fld.multi) ? true : fld.multi,
            dataForceArray : true,
            asyncParseData : function(th, callback) {
                Wn.execf("thing {{th_set}} {{subhdl}} {{id}} -json -l", {
                    th_set : th.th_set,
                    id     : th.id,
                    subhdl : subhdl
                }, function(re){
                    callback($z.fromJson(re));
                });
            },
            formatData : function(objList, UI) {
                var re = {};
                if(_.isArray(objList) && objList.length > 0){
                    re["th_"+subhdl+"_nb"] = objList.length;
                    var ids = [];
                    for(var obj of objList)
                        ids.push(obj.id);
                    re["th_"+subhdl+"_ids"] = ids;
                } else {
                    re["th_"+subhdl+"_nb"]  = 0;
                    re["th_"+subhdl+"_ids"] = [];
                }
                
                return re;
            },
            on_add : function(callback, UI) {
                var th = UI.parent.getData();
                Wn.selectFilePanel({
                    body  : {
                        multi : UI.options.multi,
                        max   : UI.options.max,
                        uploader : {
                            target : {
                                ph   : "id:"+th.th_set+"/data/"+th.id+"/"+subhdl,
                                race : "DIR"
                            },
                            validate : fld.validate || validate
                        }
                    },
                    on_ok : function(objs) {
                        if(!objs)
                            return;
                        if(!_.isArray(objs)) {
                            objs = [objs];
                        }
                        // 准备生成的命令
                        var cmdText = "";
                        var cmdTmpl = $z.tmpl("thing {{th_set}} {{subhdl}} {{th_id}} -add '{{nm}}' -overwrite -read id:{{id}} -Q;\n")
                        for(var o of objs){
                            cmdText += cmdTmpl({
                                th_set : th.th_set,
                                th_id  : th.id,
                                id     : o.id,
                                nm     : o.nm.replace(/'/g,"\\'"),
                                subhdl : subhdl
                            });
                        }

                        // 有命令就执行
                        if(cmdText) {
                            // 最后加入查询语句
                            cmdText += $z.tmpl("thing {{th_set}} {{subhdl}} {{id}} -json -l")({
                                th_set : th.th_set,
                                id     : th.id,
                                subhdl : subhdl
                            });
                            UI.parent.showPrompt(fld.key, "spinning");
                            Wn.exec(cmdText, function(re){
                                UI.parent.hidePrompt(fld.key);
                                callback($z.fromJson(re), true);
                            });
                        }
                        // 否则直接调用回调
                        else {
                            callback(objs);
                        }
                    },
                    on_cancel : function(){
                        var cmdText = $z.tmpl("thing {{th_set}} {{subhdl}} {{id}} -json -l")({
                            th_set : th.th_set,
                            id     : th.id,
                            subhdl : subhdl
                        });
                        UI.parent.showPrompt(fld.key, "spinning");
                        Wn.exec(cmdText, function(re){
                            UI.parent.hidePrompt(fld.key);
                            callback($z.fromJson(re), true);
                        });
                    }
                });
            },
            on_remove : function(o, callback, jItem, UI) {
                var th = UI.parent.getData();
                Wn.execf("thing {{th_set}} {{subhdl}} {{th_id}} -del '{{nm}}' -Q", {
                    th_set : th.th_set,
                    th_id  : th.id,
                    nm     : o.nm.replace(/'/g,"\\'"),
                    subhdl : subhdl
                }, function(re){
                    callback(re);
                });
            }
        }
    };
};
//==============================================
var html = function(){/*
<div class="ui-arena thing" ui-fitparent="yes">
    <header>heading</header>
    <section class="th-search"><div class="th-con" ui-gasket="search"></div></section>
    <section class="th-form"><div class="th-con" ui-gasket="form"></div></section>
    <footer ui-gasket="formMenu"></footer>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "app/wn.thing/theme/thing-{{theme}}.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "showThingSetId", true);
        $z.setUndefined(opt, "dynamic", true);
    },
    //...............................................................
    update : function(o) {
        var UI  = this;
        var opt = UI.options;
        UI.$el.attr("thing-set-id", o.id);

        // 更新标题
        var html = Wn.objIconHtml(o) + '<b>' + (o.title || o.nm) + '</b>';
        if(opt.showThingSetId)
            html += '<em>' + o.id +'</em>';
        UI.arena.children("header").html(html);

        // 重新加载数据 
        UI.reload(function(){
            UI.subUI("search/list").setActived(0);
        });
    },
    //...............................................................
    reload : function(callback){
        var UI  = this;
        var opt = UI.options;
        var oid = UI.$el.attr("thing-set-id");

        // 得到配置对象
        var conf = _.extend({}, $z.pick(opt, "!^(parent|gasketName|dynamic|editor|on_init)$"));

        // 读取所有的字段
        if(opt.dynamic){
            UI.showLoading();
            $z.loadResource("jso:///o/read/id:"+oid+"/thing.js", function(thConf){
                UI.hideLoading();
                // 加载 thing.js 失败，显示错误信息
                if(!thConf || (false === thConf.ok)){
                    UI.releaseAllChildren(true);
                    UI.arena.html('<div class="th-err"><i class="fa fa-warning"></i>'
                            + UI.msg('thing.err.nothingjs') + '</div>');
                }
                else if(thConf) {
                    _.extend(conf, thConf);
                    UI.__draw(conf, callback);
                }
            });
        }
        // 静态字段
        else {
            UI.__draw(conf, callback);
        }

    },
    //...............................................................
    showBlank : function(){
        var UI  = this;
        var opt = UI.options;

        var blankTip = opt.blankTip || UI.msg('thing.blank');

        new DomUI({
            parent : UI,
            gasketName : "form",
            dom : '<div class="th-blank">' + blankTip + '</div>'
        }).render();
    },
    //...............................................................
    showThing : function(th) {
        var UI  = this;
        var opt = UI.options;
        var thConf = UI.thConf;

        th = th || UI.gasket.search.uiList.getActived();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 没东西，那么久显示空
        if(!th) {
            UI.showBlank();
            return;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 已经是表单了，更新
        else if(UI.gasket.form.uiName == "ui.form") {
            UI.gasket.form.setData(th);
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 重新显示表单
        else {
            new FormUI({
                parent : this,
                gasketName : "form",
                uiWidth : "all",
                fields : UI.thConf.fields,
                on_change : function(key, val, fld) {
                    $z.invoke(opt, "form_change", [key, val, fld], UI);
                },
                on_update : function(data, fld) {
                    var th      = this.getData();
                    var oTS     = UI.getThingSetObj();
                    var json    = $z.toJson(data);
                    var cmdTmpl = UI.__cmd(thConf.updateBy, oTS.id, json);
                    var cmdText = $z.tmpl(cmdTmpl)(th);
                    // console.log(data)
                    // 执行命令
                    var uiForm = this;
                    this.showPrompt(fld.key, "spinning");
                    Wn.exec(cmdText, function(re) {
                        var newTh = $z.fromJson(re);
                        // 计入缓存
                        Wn.saveToCache(newTh);
                        // 更新左侧列表
                        UI.gasket.search.uiList.update(newTh);
                        // 隐藏提示
                        uiForm.hidePrompt(fld.key);

                        // 调用回调
                        $z.invoke(opt, "form_update", [data, newTh, fld], UI);
                    });
                }
            }).render(function(){
                this.setData(th);
            });
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 更新表单快捷命令菜单
        var jForm   = UI.arena.find(">.th-form");
        var jFooter = UI.arena.find(">footer");
        var formActions = opt.formActions;
        if(_.isFunction(formActions)){
            formActions = formActions.apply(UI, [th]);
        }
        // 显示
        if(formActions) {
            jFooter.show();
            new MenuUI({
                parent : UI,
                gasketName : "formMenu",
                arenaClass : "th-form-menu",
                setup : formActions
            }).render(function(){
                jForm.css("bottom", jFooter.outerHeight(true));
            });
        }
        // 隐藏
        else {
            jForm.css("bottom", "");
            jFooter.hide();
            if(UI.gasket.formMenu)
                UI.gasket.formMenu.destroy();
        }

    },
    //...............................................................
    getThingSetObj : function(){
        var UI  = this;
        var oid = UI.$el.attr("thing-set-id");
        return Wn.getById(oid);
    },
    //...............................................................
    __cmd : function(cmd, TsId, str) {
        var re = cmd.replace(/<TsId>/g, TsId);
        if(str)
            return re.replace(/<str>/g, str.replace(/'/g, "\\'").replace(/"/g, "\\\""));
        return re;
    },
    //...............................................................
    // 处理配置文件的特殊字段定义
    __format_thConf : function(thConf) {
        // 逐个处理字段
        if(_.isArray(thConf.fields)){
            for(var fld of thConf.fields) {
                this.__format_theConf_field(fld);
            }
        }
        // 默认搞他个空数组
        else {
            thConf.fields = [];
        }
        // 返回处理结果
        return thConf;
    },
    //...............................................................
    __format_theConf_field : function(fld) {
        // id
        if("id" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.id");
            $z.setUndefined(fld, "hide", true);
            $z.extend(fld, {
                type   : "string",
                editAs : "label"
            });
        }
        // th_nm
        else if("th_nm" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.th_nm");
            $z.extend(fld, {
                type : "string",
                editAs : "input",
                escapeHtml : false,
                display : function(o) {
                    var html = "";
                    if(o.thumb){
                        html += '<img src="/o/thumbnail/id:'+o.id+'?_t='+$z.timestamp()+'">';
                    }else{
                        html += '<i class="fa fa-cube th_thumb"></i>';
                    }
                    html += $z.escapeText(o.th_nm);
                    if(o.len > 0) {
                        html += '<span><i class="zmdi zmdi-format-align-left"></i><em>' + o.len + '</em></span>';
                    }
                    return html;
                }
            });
        }
        // th_ow
        else if("th_ow" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.th_ow");
            $z.setUndefined(fld, "hide", true);
            $z.extend(fld, {
                type   : "string",
                editAs : "input"
            });
        }
        // __brief_and_content__
        else if("__brief_and_content__" == fld.key){
            $z.setUndefined(fld, "title", "i18n:thing.key.brief_and_content");
            $z.setUndefined(fld, "hide", true);
            $z.extend(fld, {
                virtual : true,
                editAs  : "content",
                uiConf  : THING_DETAIL()
            });
        }
        // __media__
        else if("__media__" == fld.key){
            $z.extend(fld, THING_FILE(fld, "media", /^.+[.](png|jpe?g|gif|mov|mp4|wmv|avi)$/i));
        }
        // __attachment__
        else if("__attachment__" == fld.key){
            $z.extend(fld, THING_FILE(fld, "attachment"));
        }
        // lbls
        else if("lbls" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.lbls");
            $z.setUndefined(fld, "tip", "i18n:thing.key.lbls_tip");
            $z.extend(fld, {
                type : "object",
                virtual : false,
                editAs : "input",
                uiConf : {
                    parseData : function(lbls){
                        if(_.isArray(lbls))
                            return lbls.join(", ");
                        return "";
                    },
                    formatData : function(str) {
                        if(str)
                            return str.split(/[,， \t]+/g);
                        return [];
                    }
                },
                display : function(o) {
                    var html = "";
                    if(_.isArray(o.lbls)){
                        for(var lbl of o.lbls) {
                            html += '<span class="th-lbl">' + lbl + '</span>';
                        }
                    }
                    return html;
                }
            });
        }
        // thumb
        else if("thumb" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.thumb");
            $z.extend(fld, {
                hide : true,
                type : "string",
                beforeSetData : function(o){
                    this.UI.setTarget($z.tmpl("id:{{th_set}}/data/{{id}}/thumb.jpg")(o));
                },
                editAs : "image",
                uiConf : {
                    dataType : "idph"
                }
            });
        }
        // th_site
        else if("th_site" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.th_site");
            $z.extend(fld, {
                hide : true,
                type   : "object",
                editAs : "droplist",
                uiConf : {
                    multi : true,
                    items : 'hmaker sites -key nm',
                    icon  : '<i class="fa fa-sitemap"></i>',
                    text  : function(it, i, UI) {
                        return "all" == it
                                    ? UI.msg("thing.th_site_all")
                                    : it;
                    },
                    value : function(it) {return it;},
                    fixItems : ["all"]
                },
                escapeHtml : false,
                display : function(o) {
                    var html = "";
                    if(o.th_site && o.th_site.length > 0) {
                        for(var i=0;i<o.th_site.length;i++){
                            var it = o.th_site[i];
                            html += '<span class="th-site">';
                            html += "all" == it
                                        ? this.msg("thing.th_site_all")
                                        : it;
                            html += '</span>';
                        }
                        return html;
                    }else{
                        html = '<em>{{none}}</em>';
                    }
                    return this.compactHTML(html);
                }
            });
        }
        // th_pub
        else if("th_pub" == fld.key) {
            $z.setUndefined(fld, "title", "i18n:thing.key.th_pub");
            $z.setUndefined(fld, "dft", false);
            $z.extend(fld, {
                title : "发布",
                type  : "boolean",
                editAs : "toggle",
                escapeHtml : false,
                display : function(o){
                    if(o.th_pub){
                        return '<span class="th-pub" pub="yes"><i class="zmdi zmdi-badge-check"></i></span>';
                    }
                    return '<span class="th-pub" pub="no"><i class="zmdi zmdi-edit"></i></span>';
                }
            });
        }
        // 递归
        else if(_.isArray(fld.fields)){
            for(var subFld of fld.fields){
                this.__format_theConf_field(subFld);
            }
        }
    },
    //...............................................................
    __draw : function(thConf, callback) {
        var UI  = this;
        var oTS = UI.getThingSetObj();
        //console.log(thConf)
        // 保存 thConf (thing.js) 定义
        UI.thConf = UI.__format_thConf(thConf);

        // 定义默认命令模板
        $z.setUndefined(thConf, "updateBy", "thing <TsId> update {{id}} -fields '<str>'");
        $z.setUndefined(thConf, "queryBy" , "thing <TsId> query '<%=match%>' -skip {{skip}} -limit {{limit}} -json -pager -sort 'ct:-1'");
        $z.setUndefined(thConf, "createBy", "thing <TsId> create '<str>'");
        $z.setUndefined(thConf, "deleteBy", "thing <TsId> delete {{id}} -quiet");

        // 创建搜索条
        new SearchUI({
            parent : UI,
            gasketName : "search",
            menu : [{
                qkey  : "refresh",
                icon  : '<i class="zmdi zmdi-refresh"></i>',
            }, {
                text  : "i18n:thing.create",
                icon  : '<i class="zmdi zmdi-flare"></i>',
                handler : function() {
                    UI.prompt("thing.create_tip", {
                        icon  : oTS.icon || '<i class="fa fa-cube"></i>',
                        btnOk : "thing.create_do",
                        // 用户点击了确定
                        ok : function(objName){
                            // 创建对象
                            if(objName){
                                var cmdText = UI.__cmd(thConf.createBy, oTS.id, objName);
                                Wn.exec(cmdText, function(re){
                                    var newObj = $z.fromJson(re);
                                    UI.gasket.search.refresh(function(){
                                        UI.subUI("search/list").setActived(newObj.id);
                                    });
                                });
                            }
                        }
                    });
                }
            }, {
                qkey  : "delete",
                icon  : '<i class="zmdi zmdi-delete"></i>',
            }],
            data : UI.__cmd(thConf.queryBy, oTS.id),
            edtCmdTmpl : {
                "delete" : UI.__cmd(thConf.deleteBy, oTS.id),
            },
            filter : {
                keyField : "th_nm"
            },
            list : {
                layout : thConf.layout,
                fields : thConf.fields,
                on_actived : function(th,jRow,prevTh) {
                    // console.log("AA", prevTh);
                    if(!prevTh || prevTh.id != th.id)
                        UI.showThing(th);
                },
                // on_checked : function(jItems) {
                //     console.log("on_checked", this.getObj(jItems))
                // },
                // on_unchecked : function(jItems) {
                //     console.log("on_unchecked", this.getObj(jItems))
                // },
                on_blur : function(objs, jRows, nextObj) {
                    // console.log("BB", nextObj);
                    if(!nextObj)
                        UI.showBlank();
                }
            },
            maskConf : {
                width  : 500,
                height : "90%"
            },
        }).render(function(){
            this.refresh(function(){
                $z.doCallback(callback, [oTS], UI);
            });
        });

        // 右侧显示空
        UI.showBlank();
    },
    //...............................................................
    getActived : function(){
        return this.gasket.search.uiList.getActived();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);