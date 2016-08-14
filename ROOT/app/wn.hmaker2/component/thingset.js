(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var FLD_ICONS = {
    'text'     : '<i class="fa fa-font"></i>',
    'img'      : '<i class="fa fa-image"></i>',
    'html'     : '<i class="fa fa-code"></i>',
    'sub:eq'   : '<i class="fa fa-code-fork"></i>',
    'sub:loop' : '<i class="zmdi zmdi-repeat"></i>',
};
//==============================================
var html = function(){/*
<style rel="stylesheet" type="text/css" class="hm-del-save"></style>
<div class="hmc-thingset hm-del-save">
    <section class="hmc-th-W hmc-ths-main hm-del-save" mode="ok">
        <div class="hmc-ths-part hmc-ths-filter">
            <div class="part-enabled">{{hmaker.com.thingset.flt.enabled}}</div>
            <div class="part-disabled"><i class="fa fa-eye-slash"></i> {{hmaker.com.thingset.flt.disabled}}</div>
        </div>
        <div class="hmc-ths-list">
            <div class="hmc-ths-more"></div>
            <div class="hmc-ths-item"></div>
            <div class="hmc-ths-more"></div>
            <div class="hmc-ths-more"></div>
            <div class="hmc-ths-more"></div>
        </div>
        <div class="hmc-ths-part hmc-ths-pager">
            <div class="part-enabled">{{hmaker.com.thingset.pg.enabled}}</div>
            <div class="part-disabled"><i class="fa fa-eye-slash"></i> {{hmaker.com.thingset.pg.disabled}}</div>
        </div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="none">
        <div class="tiptxt info">
            <i class="zmdi zmdi-info"></i>
            {{hmaker.com.thingset.mode.none}}
        </div>
        <div class="lnkbtn redata">{{hmaker.com.thingset.redata}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="gone">
        <div class="tiptxt warn">
            <i class="fa fa-trash-o" aria-hidden="true"></i>
            {{hmaker.com.thingset.mode.gone}}
        </div>
        <div class="lnkbtn redata">{{hmaker.com.thingset.redata}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="invalid">
        <div class="tiptxt warn">
            <i class="zmdi zmdi-alert-polygon"></i>
            {{hmaker.com.thingset.mode.invalid}}
        </div>
        <div class="lnkbtn redata">{{hmaker.com.thingset.redata}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="lackdef">
        <div class="tiptxt error">
            <i class="zmdi zmdi-alert-circle-o"></i>
            {{hmaker.com.thingset.mode.lackdef}}
        </div>
        <div class="lnkbtn redata">{{hmaker.com.thingset.redata}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="wrongdef">
        <div class="tiptxt error">
            <i class="zmdi zmdi-alert-octagon"></i>
            {{hmaker.com.thingset.mode.wrongdef}}
        </div>
        <div class="lnkbtn redata">{{hmaker.com.thingset.redata}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmplnone">
        <div class="tiptxt warn">
            <i class="zmdi zmdi-view-compact"></i>
            {{hmaker.com.thingset.mode.tmplnone}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmplgone">
        <div class="tiptxt warn">
            <i class="zmdi zmdi-folder-outline"></i>
            {{hmaker.com.thingset.mode.tmplgone}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmplnodom">
        <div class="tiptxt error">
            <i class="zmdi zmdi-language-html5"></i>
            {{hmaker.com.thingset.mode.tmplnodom}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmplnocss">
        <div class="tiptxt error">
            <i class="zmdi zmdi-language-css3"></i>
            {{hmaker.com.thingset.mode.tmplnocss}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmpldom_E">
        <div class="tiptxt error">
            <i class="fa fa-square-o" aria-hidden="true"></i>
            {{hmaker.com.thingset.mode.tmpldom_E}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
    <section class="hmc-th-W hmc-th-tip hm-del-save" mode="tmplcss_E">
        <div class="tiptxt error">
            <i class="fa fa-circle-o" aria-hidden="true"></i>
            {{hmaker.com.thingset.mode.tmplcss_E}}
        </div>
        <div class="lnkbtn retemplate">{{hmaker.com.thingset.retemplate}}</div>
    </section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_thingset", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);

        UI.listenBus("hide:com:ele",  function(){
            UI.$el.find(".hm-tho-avatar").removeAttr("current");
        });
        UI.listenBus("change:com:ele", UI.on_change_com_ele);

        UI.$el.css("visibility", "hidden");

        // window.setTimeout(function(){
        //     UI.setPropToDom({
        //         dsId     : "thing:5osnmkm988jeuoduvmu773a260",
        //         template : "test",
        //         fltEnabled : true,
        //         pgEnabled : true
        //     });
        // }, 0);
    },
    //...............................................................
    events : {
        "click .redata" : function() {
            alert("功能太多，写不完啊，右侧属性面板手动输入吧，不好意思哈 -_-!")
        },
        "click .retemplate" : function(){
            alert("额... 你点了这里 ... 还没实现呢，右侧属性面板手动输入吧 -_-!")
        },
        "click .hm-tho-avatar" : function(e) {
            var UI   = this;
            var jTa  = $(e.currentTarget);
            var jFld = jTa.closest("[t-key]");
            
            // 在激活的块中，可以激活元素
            if(UI.isInActivedBlock(jFld)) {
                // 激活的块中就不要再冒泡了
                e.stopPropagation();

                console.log("click var:", jFld.attr("t-key"));

                // 显示控件元素的动态属性面板
                UI.fire("show:com:ele");

                // 激活自身 
                UI.$el.find(".hm-tho-avatar").removeAttr("current");
                jTa.attr("current", "yes");

                // 丢出内容改变事件
                UI.fire("change:com:ele", UI.getFldInfoFromComEle(jFld))
;            }
        }
    },
    //...............................................................
    getFldInfoFromComEle : function(jFld){
        var UI = this;
        var re = {
            _info : UI.__gen_avatar_innerHtml(jFld),
            className : jFld[0].className,
            key : jFld.attr("t-key"),
            as  : jFld.attr("as"),
            replace : jFld.attr('replace') || 'DIV'
        };
        // 得到映射
        var prop = this.getPropFromDom();
        re.valueBy = (prop.mapping || {})[re.key] || null;
        // 返回 
        return re;
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
        return {
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth : "all",
                fields : [{
                    title : 'i18n:hmaker.com.thingset.tt_ds',
                    fields : [{
                        key : "dsId",
                        title : "i18n:hmaker.com.thingset.ds_id",
                        type : "string",
                        editAs : "input",
                    },{
                        key : "template",
                        title : "i18n:hmaker.com.thingset.template",
                        type : "string",
                        editAs : "input",
                    }]
                }, {
                    title : 'i18n:hmaker.com.thingset.tt_flt',
                    fields : [{
                        key : "fltEnabled",
                        title : "i18n:hmaker.com.thingset.flt_enabled",
                        type : "boolean",
                        editAs : "switch",
                    },{
                        key : "fltCnd",
                        title : "i18n:hmaker.com.thingset.flt_cnd",
                        type : "object",
                        editAs : "text",
                    },{
                        key : "fltSort",
                        title : "i18n:hmaker.com.thingset.flt_sort",
                        type : "object",
                        editAs : "text",
                    }]
                }, {
                    title : 'i18n:hmaker.com.thingset.tt_pager',
                    fields : [{
                        key : "pgEnabled",
                        title : "i18n:hmaker.com.thingset.pg_enabled",
                        type : "boolean",
                        editAs : "switch",
                    },{
                        key : "pgSize",
                        title : "i18n:hmaker.com.thingset.pg_size",
                        type : "int",
                        editAs : "input"
                    }]
                }]
            }
        };
    },
    //...............................................................
    __show_mode : function(mode) {
        this.$el.find('[mode!="'+mode+'"]').removeAttr("show");
        this.$el.find('[mode="'+mode+'"]').attr("show", "yes");
        return "ok" == mode;
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // console.log(com)

        // 检查显示模式
        if(!UI.__check_mode(com)){
            UI.$el.css("visibility", "");
            return ;
        }

        UI.$el.css("visibility", "");

        // 绘制
        UI.__paint_filter(com);
        UI.__paint_item(com);
        UI.__paint_pager(com);

        // 绘制空间内元素的面板
        UI.drawComEleInProp({
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth : 'all',
                fields  : [{
                    key    : 'className',
                    title  : 'i18n:hmaker.com.thingset.fld.className',
                    type   : 'string',
                    editAs : 'label',
                }, {
                    key    : 'key',
                    title  : 'i18n:hmaker.com.thingset.fld.key',
                    type   : 'string',
                    editAs : 'label'
                }, {
                    key    : 'as',
                    title  : 'i18n:hmaker.com.thingset.fld.as',
                    type   : 'string',
                    editAs : 'label'
                }, {
                    key    : 'replace',
                    title  : 'i18n:hmaker.com.thingset.fld.replace',
                    type   : 'string',
                    editAs : 'label'
                }, {
                    key    : 'valueBy',
                    title  : 'i18n:hmaker.com.thingset.fld.valueBy',
                    type   : 'string',
                    editAs : 'input'
                }],
                on_change : function(key, val){
                    UI.fire("change:com:ele", $z.obj(key, val));
                }
            }
        });
    },
    //...............................................................
    on_change_com_ele : function(comEle) {
        var UI = this;
        
        // 得到属性，并确保有 mapping 字段
        var prop = this.getPropFromDom();
        prop.mapping = prop.mapping || {};

        // 得到当前激活的字段
        var jFld = UI.arena.find(".hm-tho-avatar[current]").closest("[t-key]");

        // 没有激活字段，啥都表处理了，不过我想这应该不太可能吧，打个警告咯
        if(jFld.size() == 0) {
            $z.invoke(console, "warn", ["no actived .hm-tho-avatar for thingset :", UI.cid]);
            return;
        }
        var tKey = jFld.attr("t-key");

        // 只处理 valueBy
        $z.setUndefined(comEle, "valueBy", "");
        prop.mapping[tKey] = comEle.valueBy
        
        // 保存属性
        UI.setPropToDom(prop);

    },
    //...............................................................
    __paint_filter : function(com) {
        var UI = this;

        //console.log(com)

        // 标记属性
        var jFlt = UI.$el.find(".hmc-ths-filter").attr({
            "enabled" :  com.fltEnabled ? "yes" : null,
            "disabled":  !com.fltEnabled ? "yes" : null,
        });

        
     },
     //...............................................................
    __paint_pager : function(com) {
        var UI = this;

        // 标记属性
        var jPg = UI.$el.find(".hmc-ths-pager").attr({
            "enabled" :  com.pgEnabled ? "yes" : null,
            "disabled":  !com.pgEnabled ? "yes" : null,
        });
        
    },
     //...............................................................
    __paint_item : function(com) {
        var UI = this;

        // 生成 CSS
        UI.$el.children("style").html(this.genCssText(UI._R.css, "#"+com._id));

        // 插入 DOM
        UI.$el.find(".hmc-ths-item").html(UI._R.itemHtml)
            // 处理插入的 DOM，将所有的值包裹
            .find(".hm-th-obj>ul>li").each(function(){
                var jFld = $(this);

                // 得到所有的子节点
                var jSubs = jFld.children();

                // 包裹值
                var jLiW = $('<div class="hm-tho-subs">').appendTo(jFld);
                jSubs.appendTo(jLiW);

                // 设置每个区域的显示
                var jAvatar = $('<div class="hm-tho-avatar">').prependTo(jFld);
                var html = UI.__gen_avatar_innerHtml(jFld);
                jAvatar.html(html);
            });
    },
    //...............................................................
    __gen_avatar_innerHtml : function(jFld) {
        return FLD_ICONS[jFld.attr("as")||"text"] + '<em>' + jFld.attr("t-key") + '</em>';
    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;

        // 显示模式
        //console.log(com)
        if(!com.dsId) {
            return UI.__show_mode("none");
        }

        // 检查数据源格式
        var m = /^thing:([\w\d]{5,})$/.exec(com.dsId);
        if(!m) {
            return UI.__show_mode("invalid");
        }

        // 获取数据源
        var oTS = Wn.getById(m[1], true);
        if(!oTS) {
            return UI.__show_mode("gone");
        }

        // 检查数据源的合法性
        var oThConf = Wn.fetch("id:"+oTS.id+"/thing.js", true);
        if(!oThConf) {
            return UI.__show_mode("lackdef");
        }
        var thConf;
        try{
            var str = Wn.read(oThConf);
            thConf = eval('(' + str + ')');
        }catch(E){
            return UI.__show_mode("wrongdef");
        }

        // 获取显示模板
        if(!com.template){
            return UI.__show_mode("tmplnone");
        }

        var oTmpl = Wn.fetch("~/.hmaker/template/thingset/"+com.template, true);
        if(!oTmpl) {
            return UI.__show_mode("tmplgone");
        }

        // 检查模板的 DOM
        var oTmplDom = Wn.fetch("id:"+oTmpl.id+"/dom.html", true);
        if(!oTmplDom){
            return UI.__show_mode("tmplnodom");
        }

        var tHtml = $.trim(Wn.read(oTmplDom));
        if(!tHtml) {
            return UI.__show_mode("tmpldom_E");
        }

        // 检查模板的 CSS
        var oTmplCss = Wn.fetch("id:"+oTmpl.id+"/css.json", true);
        if(!oTmplCss){
            return UI.__show_mode("tmplnocss");
        }

        var tCssStr = $.trim(Wn.read(oTmplCss));
        if(!tCssStr) {
            return UI.__show_mode("tmplcss_E");
        }

        var tCss;
        try{
            tCss = $z.fromJson(tCssStr);
        }catch(E) {
            return UI.__show_mode("tmplcss_E");
        }

        // 嗯，全部检测成功，将对应记录记录在自己上面
        UI._R = {
            oThConf  : oThConf,
            thConf   : thConf,
            itemHtml : tHtml,
            css      : tCss,
        };

        // 成功了
        return UI.__show_mode("ok");
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);