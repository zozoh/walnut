(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
    'ui/form/form',
    'ui/support/dom'
], function(ZUI, Wn, SearchUI, FormUI, DomUI){
//==============================================
var THING_DETAIL = function(){
    return {
        loadContent : function(obj, callback) {
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
            console.log(obj)
            Wn.execf("thing {{th_set}} detail {{id}} -content", content, obj, function(re) {
                callback(re);
            });
        },
        parseData : function(th) {
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
        title : "i18n:thing.key." + subhdl,
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
<div class="ui-code-template">
    <div code-id="err.nothingjs" class="th-err">
        <i class="fa fa-warning"></i> <span>{{thing.err.nothingjs}}</span>
    </div>
    <div code-id="tip.blank">
        <div class="th-blank">
            <i class="fa fa-hand-o-left"></i> <span>{{thing.blank}}</span>
        </div>
    </div>
</div>
<div class="ui-arena thing" ui-fitparent="yes">
    <header>heading</header>
    <section class="th-search"><div class="th-con" ui-gasket="search"></div></section>
    <section class="th-form"><div class="th-con" ui-gasket="form"></div></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.thing/thing.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    update : function(o) {
        var UI = this;
        UI.$el.attr("thing-set-id", o.id);

        // 更新标题
        var html = Wn.objIconHtml(o) + '<b>' + (o.title || o.nm) + '</b>';
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
        var oid = UI.$el.attr("thing-set-id");

        // 读取所有的字段
        UI.showLoading();
        $z.loadResource("jso:///o/read/id:"+oid+"/thing.js", function(thConf){
            UI.hideLoading();
            // 加载 thing.js 失败，显示错误信息
            if(!thConf || (false === thConf.ok)){
                UI.ccode("err.nothingjs").appendTo(UI.arena.empty());
            }
            else if(thConf) {
                UI.__draw(thConf, callback);
            }
        });

    },
    //...............................................................
    showBlank : function(){
        new DomUI({
            parent : this,
            gasketName : "form",
            dom : this.ccode("tip.blank").html()
        }).render();
    },
    //...............................................................
    showThing : function(th) {
        var UI = this;
        var thConf = UI.thConf;

        th = th || UI.gasket.search.uiList.getActived();

        // 没东西，那么久显示空
        if(!th) {
            UI.showBlank();
            return;
        }
        // 已经是表单了，更新
        else if(UI.gasket.form.uiName == "ui.form") {
            UI.gasket.form.setData(th);
        }
        // 重新显示表单
        else {
            new FormUI({
                parent : this,
                gasketName : "form",
                uiWidth : "all",
                fields : UI.thConf.fields,
                on_update : function(data, fld) {
                    var th      = this.getData();
                    var oTS     = UI.getThingSetObj();
                    var json    = $z.toJson(data);
                    var cmdTmpl = UI.__cmd(thConf.updateBy, oTS.id, json);
                    var cmdText = $z.tmpl(cmdTmpl)(th);
                    console.log(data)
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
                    });
                    // var th = this.getData();
                    // UI.gasket.search.uiList.update(th);
                    // this.showPrompt(key, "spinning");
                    // window.setTimeout(function(){
                    //     UI.gasket.form.hidePrompt(key);
                    // }, 500);
                }
            }).render(function(){
                this.setData(th);
            });
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
        // th_nm
        if("th_nm" == fld.key) {
            $z.extend(fld, {
                title : "i18n:thing.key.th_nm",
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
                    html += (o.brief ? "<em>" + o.brief + "</em>" : "");
                    return html;
                }
            });
        }
        // __brief_and_content__
        else if("__brief_and_content__" == fld.key){
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
            $z.extend(fld, {
                title : "i18n:thing.key.lbls",
                tip   : "i18n:thing.key.lbls_tip",
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
            $z.extend(fld, {
                title : "i18n:thing.key.thumb",
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
                    var objName = window.prompt(UI.msg("thing.create_tip"));
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
                fields : thConf.fields,
                on_actived : function(th,jRow,prevTh) {
                    if(!prevTh || prevTh.id != th.id)
                        UI.showThing(th);
                },
                on_blur : function(objs, jRows, nextObj) {
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
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);