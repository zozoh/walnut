(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods',
    'app/wn.hmaker2/support/dynamic_data_setting',
    'ui/form/form',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmMethods, DynamicDataSettingUI, FormUI, DroplistUI){
//==============================================
var OLSTP = {
    filter : {
        html : `
        <div class="part-item">
            <header>
                <em>{{hmaker.com.objlist.parti_key}}</em>
                <div><input name="key"></div>
                <em>{{hmaker.com.objlist.parti_text}}</em>
                <div><input name="text"></div>
            </header>
            <section>
                <div>{{hmaker.com.objlist.flti_list}}</div>
                <div key="multi"><em>{{hmaker.com.objlist.flti_multi}}</em><span class="ui-toggle"></span></div>
                <div key="show"><em>{{hmaker.com.objlist.flti_show}}</em><span class="ui-toggle"></span></div>
            </section>
            <footer balloon="up:large:{{hmaker.com.objlist.flti_list_tip}}">
                <textarea placeholder="{{hmaker.com.objlist.flti_list_demo}}"></textarea>
            </footer>
        </div>
        `,
        set : function(jDiv, item){
            jDiv.find('input[name="key"]').val(item.key || "");
            jDiv.find('input[name="text"]').val(item.text || "");
            jDiv.find('[key="multi"] .ui-toggle').attr("on", item.multi ? "yes" : null);
            jDiv.find('[key="show"]  .ui-toggle').attr("on", item.show  ? "yes" : null);
            var str = "";
            for(var li of (item.list||[])){
                str += (li.text||"") + " : " + (li.value||"") + "\n";
            }
            jDiv.find('textarea').val($.trim(str));
        },
        get : function(jDiv) {
            var item = {
                key   : $.trim(jDiv.find('input[name="key"]').val()),
                text  : $.trim(jDiv.find('input[name="text"]').val()),
                multi : jDiv.find('[key="multi"] .ui-toggle').attr("on") ? true : false,
                show  : jDiv.find('[key="show"]  .ui-toggle').attr("on") ? true : false,
                list  : []
            };
            var str = $.trim(jDiv.find('textarea').val());
            var lines = str.split(/\r?\n/g);
            for(var line of lines) {
                var ss = line.split(/[:：]/g);
                if(ss && ss.length > 1) {
                    item.list.push({
                        text  : $.trim(ss[0]),
                        value : $.trim(ss[1])
                    });
                }
            }
            return item;
        },
        getPartData : function(UI) {
            var jFlt = UI.arena.find(".olstp-filter");
            var re   = {
                keyword : null
            };

            // 关键字
            var jKwd = jFlt.find(".olstp-kwd");
            if(!jKwd.attr("part-disabled")){
                var kwd = $.trim(jKwd.find("textarea").val());
                if(kwd)
                    re.keyword = kwd.split(/[ \t]*\r?\n[ \t]*/g);
            }

            // 列表
            var jItems = jFlt.find(".olstp-items");
            re.fields = [];
            jItems.find(".part-item").each(function(){
                var item = OLSTP.filter.get($(this));
                if(item.key && item.list.length > 0)
                    re.fields.push(item);
            });

            // 返回
            return (re.keyword || re.fields.length > 0) ? re : null;
        },
        setPartData : function(UI, data) {
            data = data || {};
            var jFlt = UI.arena.find(".olstp-filter");

            // 关键字
            var jKwd = jFlt.find(".olstp-kwd");
            var kwd  = "";
            if(_.isArray(data.keyword) && data.keyword.length > 0){
                kwd = data.keyword.join("\n");
            }
            jKwd.attr("part-disabled", kwd ? null : "yes")
                .find("textarea").val(kwd);

            // 列表
            jFlt.find(".olstp-items").empty();
            if(_.isArray(data.fields) && data.fields.length > 0){
                for(var item of data.fields)
                    UI._append_list_item(jFlt, item);
            }
        }
    },
    sorter : {
        html : `
        <div class="part-item">
            <header>
                <em>{{hmaker.com.objlist.parti_key}}</em>
                <div><input name="key"></div>
                <em>{{hmaker.com.objlist.parti_text}}</em>
                <div><input name="text"></div>
            </header>
            <section>
                <div>{{hmaker.com.objlist.sorti_more}}</div>
                <div key="toggleable"><em>{{hmaker.com.objlist.sorti_toggleable}}</em><span class="ui-toggle"></span></div>
                <div key="order">
                    <span>
                        <i class="fa fa-sort-alpha-asc"></i>
                        <i class="fa fa-sort-alpha-desc"></i>
                    </span>
                    <em asc="{{hmaker.com.objlist.sorti_asc}}" desc="{{hmaker.com.objlist.sorti_desc}}"></em>
                </div>
            </section>
            <footer>
                <div key="more" balloon="up:large:{{hmaker.com.objlist.sorti_more_tip}}">
                    <textarea placeholder="{{hmaker.com.objlist.sorti_more_demo}}"></textarea>
                </div>
                <div key="lskey" balloon="up:large:{{hmaker.com.objlist.sorti_lskey_tip}}">
                    <em>{{hmaker.com.objlist.sorti_lskey}}</em>
                    <input placeholder="{{hmaker.com.objlist.sorti_lskey_demo}}">
                </div>
            </footer>
        </div>
        `,
        set : function(jDiv, item){
            jDiv.find('input[name="key"]').val(item.key || "");
            jDiv.find('input[name="text"]').val(item.text || "");
            jDiv.find('[key="toggleable"] .ui-toggle').attr("on", item.toggleable ? "yes" : null);
            jDiv.find('[key="order"]').attr("order", item.order < 0 ? 'desc' : 'asc');
            jDiv.find('[key="more"] textarea').val(item.more ? $z.toJson(item.more) : "");
            jDiv.find('[key="lskey"] input').val(item.localStoreKey || "");
        },
        get : function(jDiv) {
            var item = {
                key  : $.trim(jDiv.find('input[name="key"]').val()),
                text : $.trim(jDiv.find('input[name="text"]').val()),
                toggleable : jDiv.find('[key="toggleable"] .ui-toggle').attr("on") ? true : false,
                order      : jDiv.find('[key="order"]').attr("order") == 'desc' ? -1 : 1,
                localStoreKey  : $.trim(jDiv.find('[key="lskey"] input').val()) || null
            };
            var more = jDiv.find('[key="more"] textarea').val();
            if(more)
                item.more = $z.fromJson(more);

            return item;
        },
        getPartData : function(UI) {
            var jSort = UI.arena.find(".olstp-sorter");
            var re   = [];

            // 列表
            var jItems = jSort.find(".olstp-items");
            jItems.find(".part-item").each(function(){
                var item = OLSTP.sorter.get($(this));
                re.push(item);
            });

            // 返回
            return re.length > 0 ? re : null;
        },
        setPartData : function(UI, data) {
            data = data || {};
            var jSort = UI.arena.find(".olstp-sorter");

            // 列表
            jSort.find(".olstp-items").empty();
            if(_.isArray(data) && data.length > 0){
                for(var item of data)
                    UI._append_list_item(jSort, item);
            }
        }
    },
    pager : {
        getPartData : function(UI) {
            return UI.gasket.pager.getData();
        },
        setPartData : function(UI, data) {
            UI.gasket.pager.setData(data || {});
        }
    },
};
//==============================================
var html = `
<div class="ui-arena hmc-objlist-prop" ui-fitparent="yes">
    <section class="olstp-dds" part-disabled="yes">
        <h4><i class="fa fa-database"></i> <span>{{hmaker.com.objlist.dds}}</span></h4>
        <div ui-gasket="dds"></div>
    </section>
    <section class="olstp-part olstp-filter" part="filter">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.filter}}</b>
            <ul>
                <li a="add"  balloon="up:hmaker.com.objlist.flt_add"> <i class="zmdi zmdi-plus"></i></li>
                <li a="del"  balloon="up:hmaker.com.objlist.flt_del"> <i class="zmdi zmdi-delete"></i></li>
                <li a="up"   balloon="up:hmaker.com.objlist.flt_mvup"><i class="zmdi zmdi-long-arrow-up"></i></li>
                <li a="down" balloon="up:hmaker.com.objlist.flt_mvdown"><i class="zmdi zmdi-long-arrow-down"></i></li>
            </ul>
        </h4>
        <div>
            <div>
                <div class="olstp-kwd" part-disabled="yes">
                    <div class="olstp-kwd-label">
                        <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
                        <b>{{hmaker.com.objlist.flt_kwd}}</b>
                        <textarea placeholder="{{hmaker.com.objlist.flt_kwd_tip}}"></textarea>
                    </div>
                    <div  class="olstp-kwd-tip">{{hmaker.com.objlist.flt_kwd_tip2}}</div>
                </div>
            </div>
            <div class="olstp-items"></div>
        </div>
    </section>
    <section class="olstp-part olstp-sorter" part="sorter">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.sort}}</b>
            <ul>
                <li a="add"  balloon="up:hmaker.com.objlist.sort_add"> <i class="zmdi zmdi-plus"></i></li>
                <li a="del"  balloon="up:hmaker.com.objlist.sort_del"> <i class="zmdi zmdi-delete"></i></li>
                <li a="up"   balloon="up:hmaker.com.objlist.sort_mvup"><i class="zmdi zmdi-long-arrow-up"></i></li>
                <li a="down" balloon="up:hmaker.com.objlist.sort_mvdown"><i class="zmdi zmdi-long-arrow-down"></i></li>
            </ul>
        </h4>
        <div><div class="olstp-items"></div></div>
    </section>
    <section class="olstp-pager" part="pager">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.pager}}</b>
        </h4>
        <div ui-gasket="pager"></div>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objlist_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    events : {
        // 展开/收起数据源设定
        'click .olstp-dds h4' : function(e){
            $z.toggleAttr($(e.currentTarget).closest("section"), "part-disabled", "yes");
        },
        // 启用/关闭 过滤器/排序器/分页器
        'click h4 .olstp-check, h4 b' : function(e) {
            var UI = this;
            var jPart = $(e.currentTarget).closest("section");
            $z.toggleAttr(jPart, "part-disabled", "yes");

            // 通知改动
            UI._notify_part_change(jPart);

            // 调整页面布局
            UI.resize(true);
        },
        // 启用/关闭 关键字
        'click .olstp-kwd .olstp-check' : function(e) {
            var UI = this;
            var jKwd = $(e.currentTarget).closest(".olstp-kwd");
            $z.toggleAttr(jKwd, "part-disabled", "yes");

            // 通知改动
            UI._notify_part_change(jKwd);
        },
        // 添加 filter/sorter 的项目
        'click .olstp-part h4 > ul > li[a="add"]' : function(e) {
            var jDiv = this._append_list_item($(e.currentTarget).closest("section"), {});
            jDiv.click();
        },
        // 删除 filter/sorter 的项目
        'click .olstp-part h4 > ul > li[a="del"]' : function(e) {
            var UI = this;
            var jItem = UI._check_part_current_item(e.currentTarget);
            if(jItem){
                // 高亮下一个
                var jNext = jItem.next();
                if(jNext.length == 0)
                    jNext = jItem.prev();
                if(jNext.length > 0)
                    jNext.attr("current", "yes");

                // 移除自身
                jItem.remove();

                // 通知改动
                UI._notify_part_change(e.currentTarget);
            }
        },
        // 上移 filter/sorter 的项目
        'click .olstp-part h4 > ul > li[a="up"]' : function(e) {
            var UI    = this;
            var jItem = UI._check_part_current_item(e.currentTarget);
            if(jItem) {
                var jPrev = jItem.prev();
                if(jPrev.length > 0){
                    jItem.insertBefore(jPrev);
                    UI._notify_part_change(jItem);
                }
            }
        },
        // 下移 filter/sorter 的项目
        'click .olstp-part h4 > ul > li[a="down"]' : function(e) {
            var UI    = this;
            var jItem = UI._check_part_current_item(e.currentTarget);
            if(jItem) {
                var jNext = jItem.next();
                if(jNext.length > 0){
                    jItem.insertAfter(jNext);
                    UI._notify_part_change(jItem);
                }
            }
        },
        // filter/sorter 项目的 toggle 按钮
        'click .olstp-items .ui-toggle' : function(e) {
            $z.toggleAttr(e.currentTarget, "on");
            this._notify_part_change(e.currentTarget);
        },
        // sorter 的排序
        'click .olstp-sorter [key="order"]' : function(e) {
            var UI = this;
            var jOrder = $(e.currentTarget);
            $z.toggleAttr(jOrder, "order", "asc", "desc");
            $z.blinkIt(jOrder);
            UI._notify_part_change(jOrder);
        },
        // 通知 filter/sorter 的改变
        'change .olstp-part input, .olstp-part textarea' : function(e) {
            this._notify_part_change(e.currentTarget);
        },
        // 高亮 filter/sorter 的项目
        'click .olstp-part .part-item' : function(e){
            var UI = this;

            // 防止冒泡
            e.stopPropagation();

            // 取消其他高亮
            UI.balloon(".part-item", false);
            UI.arena.find('.part-item[current]').removeAttr("current");

            // 高亮当前
            var jItem = $(e.currentTarget);
            this.balloon(jItem);
            jItem.parent().children().removeAttr("current");
            jItem.attr("current", "yes");
        },
        // 取消高亮 filter/sorter 的项目
        'click .olstp-part' : function(e){
            var UI = this;
            var jq = $(e.target);
            // 如果在快捷菜单里，就不取消了
            if(jq.closest('li[a]').length > 0) {
                return;
            }
            // 取消其他高亮
            UI.balloon(".part-item", false);
            UI.arena.find('.part-item[current]').removeAttr("current");
        },
    },
    //...............................................................
    _check_part_current_item : function(ele) {
        var UI    = this;
        var jPart = UI.$part(ele);
        var jItem = jPart.find(".olstp-items .part-item[current]");
        if(jItem.length == 0){
            alert(UI.msg("hmaker.com.objlist.nopartitem"));
            return;
        }
        return jItem;
    },
    //...............................................................
    _append_list_item : function(jPart, item) {
        var UI = this;
        var part   = UI._get_part_by(jPart);
        var jItems = jPart.find(".olstp-items");
        var HDL    = OLSTP[part];
        var jDiv   = $(UI.compactHTML(HDL.html)).appendTo(jItems);
        HDL.set(jDiv, item);
        return jDiv;
    },
    //...............................................................
    $part : function(ele) {
        return $(ele).closest("section[part]");
    },
    _get_part_by : function(jPart) {
        return jPart.attr("part");
    },
    //...............................................................
    _notify_part_change : function(ele) {
        var UI = this;
        var jPart = UI.$part(ele);
        var part  = UI._get_part_by(jPart);

        // 取值
        var partData = null;
        var com = {__prop_ignore_update : true};
        if(!jPart.attr("part-disabled")){
            var HDL  = OLSTP[part];
            partData = HDL.getPartData(UI);
        }

        // 通知
        com[part] = partData;
        UI.fire("change:com", com);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 打开提示
        UI.balloon();

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // DDS
        new DynamicDataSettingUI({
            parent : UI,
            gasketName : "dds",
            on_init : function(){
                this.uiCom = UI.uiCom;
            },
            on_change : function(com) {
                // console.log("haha", com)
                this.uiCom.notifyChange(com);
            }
        }).render(function(){
            UI.defer_report("dds");
        });

        // Pager
        new FormUI({
            parent : UI,
            gasketName : "pager",
            uiWidth : "all",
            fields : [{
                key   : "sizes",
                title : "i18n:hmaker.com.objlist.pg_sizes",
                type  : "object",
                editAs : "input",
                uiConf : {
                    formatData : function(str) {
                        str = $.trim(str);
                        var list = (str||"").split(/[ \t,，;；\n]+/g);
                        var re   = [];
                        for(var s of list) {
                            if(/^\d+$/.test(s)) {
                                re.push(parseInt(s));
                            }
                        }
                        return re.length > 0 ? re : null;
                    },
                    parseData : function(sizes) {
                        return (sizes || []).join(",");
                    }
                }
            }, {
                key   : "defaultPageSize",
                title : "i18n:hmaker.com.objlist.pg_dftpgsz",
                type  : "int",
                dft   : 50,
                editAs : "input"
            }, {
                key   : "localStoreKey",
                title : "i18n:hmaker.com.objlist.lskey",
                type  : "string",
                editAs : "input"
            }, {
                key   : "autoHide",
                title : "i18n:hmaker.com.objlist.pg_auto_hide",
                type  : "boolean",
                editAs : "switch"
            }, {
                key   : "style",
                title : "i18n:hmaker.com.objlist.pg_style",
                type  : "string",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : "i18n:hmaker.com.objlist.pg_style_normal",
                        val  : "normal",
                    }, {
                        text : "i18n:hmaker.com.objlist.pg_style_jump",
                        val  : "jump",
                    }]
                }
            }, {
                key   : "i18n",
                title : "i18n:hmaker.com.objlist.pg_i18n",
                type  : "object",
                editAs : "text",
                dft : UI.msg("hmaker.com.objlist.pg_i18n_dft"),
                uiConf : {
                    height: 110,
                    formatData : function(str) {
                        var i18n = {};
                        var lines = str.split(/(\r?\n)+/g);
                        for(var line of lines) {
                            var ss  = line.split(/:/);
                            if(ss.length > 1){
                                var key = $.trim(ss[0]);
                                var val = $.trim(ss[1]);
                                if(key && val)
                                    i18n[key] = val;
                            }
                        }
                        return i18n;
                    },
                    parseData : function(i18n) {
                        var str = "";
                        for(var key in i18n){
                            str += key + " : " + i18n[key] + "\n";
                        }
                        return $.trim(str);
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("pager");
        });


        // 返回延迟加载
        return ["dds", "pager"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        console.log("I am update:", com)
        
        // 更新数据源
        UI.gasket.dds.update(com);

        // 更新过滤器/排序器/翻页器
        UI._update_part("filter", com.filter);
        UI._update_part("sorter", com.sorter);
        UI._update_part("pager" , com.pager);

        // 确保翻页器有默认值
        if(!com.pager)
            UI.gasket.pager.setData({});

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    _update_part : function(part, partData) {
        var UI = this;
        var jPart = UI.arena.find(".olstp-" + part);
        // 失效
        if(!partData) {
            jPart.attr("part-disabled", "yes");
        }
        // 启用
        else {
            jPart.attr("part-disabled", null);
            OLSTP[part].setPartData(UI, partData);
        }
    },
    //...............................................................
    resize : function() {
        var UI = this;

    }
});
//===================================================================
});
})(window.NutzUtil);