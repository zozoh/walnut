(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/menu/menu',
    'ui/pop/pop',
], function(ZUI, Wn, HmMethods, FormUI, MenuUI, POP){
//==============================================
var html = `<div class="ui-arena hmc-filter-prop hmc-cnd-prop" ui-fitparent="yes">
    <aside ui-gasket="menu"></aside>
    <section></section>
    <footer ui-gasket="form"></footer>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_filter_prop", HmMethods({
    dom  : html,
    //...............................................................
    init : function(opt) {
        // 定义字段信息修改的表单
        this.FORM_FIELDS_field = [{
            key    : "text",
            title  : 'i18n:hmaker.com._.fld_text',
            tip  : 'i18n:hmaker.com.filter.fld_text_tip',
            type   : "string",
            required : true,
            dft    : "",
        }, {
            key    : "name",
            title  : 'i18n:hmaker.com._.fld_name',
            tip    : 'i18n:hmaker.com.filter.fld_name_tip',
            type   : "string",
            required : true,
            dft    : "",
        }, {
            key    : "multi",
            title  : 'i18n:hmaker.com.filter.fld_multi',
            type   : "boolean",
            dft    : false,
            editAs : "toggle"
        }, {
            key    : "hide",
            title  : 'i18n:hmaker.com.filter.fld_hide',
            tip    : 'i18n:hmaker.com.filter.fld_hide_tip',
            type   : "boolean",
            dft    : false,
            editAs : "toggle"
        }];
        // 定义字段选项修改的
        this.FORM_FIELDS_item = [{
            key    : "text",
            title  : 'i18n:hmaker.com.filter.fld_item_text',
            tip    : 'i18n:hmaker.com.filter.fld_item_text_tip',
            type   : "string",
            required : true,
            dft    : "",
        }, {
            key    : "type",
            title  : 'i18n:hmaker.com.filter.fld_item_type',
            type   : "string",
            dft    : "string",
            editAs : "switch",
            uiConf : {
                items : [
                    {value: "string",       text: "i18n:hmaker.com.filter.fld_item_tp_string"},
                    {value: "number_range", text: "i18n:hmaker.com.filter.fld_item_tp_number_range"},
                    {value: "date_range",   text: "i18n:hmaker.com.filter.fld_item_tp_data_range"},
                ]
            }
        }, {
            key    : "value",
            title  : 'i18n:hmaker.com.filter.fld_item_value',
            type   : "string",
            dft    : "",
            editAs : "input",
        }];
    },
    //...............................................................
    events : {
        // 取消高亮： 点在菜单项里面的情况无视
        "click .hmc-cnd-prop" : function(e){
            if($(e.target).closest(".menu-item").length == 0)
                this.arena.find("section > div").removeAttr("current");
        },
        // 切换当前字段
        "click section > div.cnd-fld" : function(e) {
            e.stopPropagation();
            this.setCurrent($(e.currentTarget).prevAll().length);
        },
        // 快速修改字段属性
        'click div[current] .fld-info .fi-box' : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);
            var key = jLi.attr("key");
            var jInfo = $(e.currentTarget).closest(".fld-info");
            var jFld  = jInfo.closest("div.cnd-fld");
            var index = jFld.prevAll().length;

            // 不要冒泡了
            e.stopPropagation();

            // 切换
            var attnm = "is-" + key;
            $z.toggleAttr(jInfo, attnm, "yes");

            var com = UI.getComData();
            com.fields[index][key] = jInfo.attr(attnm) ? true : false;

            // 通知修改
            UI.uiCom.saveData(null, com, true);
        },
        // 编辑字段信息
        "click div[current] .fld-edit a" : function(e) {
            var UI = this;
            var jFld  = $(e.currentTarget).closest("div.cnd-fld");
            var index = jFld.prevAll().length;

            // 打开编辑对话框
            UI.editField(index);
        },
        // 添加字段选项
        "click div[current] .fld-items aside" : function(e){
            var UI = this;
            var jFld  = $(e.currentTarget).closest("div.cnd-fld");
            var index = jFld.prevAll().length;

            // 打开编辑对话框
            UI.createItemOfField(index);
        },
        // 编辑字段选项
        "click div[current] .fld-items > li > dl" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var fldIndex  = jq.closest("div.cnd-fld").prevAll().length;
            var itemIndex = jq.closest("li").prevAll().length;

            // 打开编辑对话框
            UI.editItemOfField(fldIndex, itemIndex);
        },
        // 删除字段选项
        'click div[current] .fld-items > li > span b[a="del"]' : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var fldIndex  = jq.closest("div.cnd-fld").prevAll().length;
            var itemIndex = jq.closest("li").prevAll().length;

            // 停止冒泡
            e.stopPropagation();

            // 执行删除
            UI.removeItemOfField(fldIndex, itemIndex);
        },
        // 上移字段选项
        'click div[current] .fld-items > li > span b[a="up"]' : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var fldIndex  = jq.closest("div.cnd-fld").prevAll().length;
            var itemIndex = jq.closest("li").prevAll().length;

            // 停止冒泡
            e.stopPropagation();

            // 执行删除
            UI.moveUpItemOfField(fldIndex, itemIndex);
        },
        // 下移字段选项
        'click div[current] .fld-items > li > span b[a="down"]' : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var fldIndex  = jq.closest("div.cnd-fld").prevAll().length;
            var itemIndex = jq.closest("li").prevAll().length;

            // 停止冒泡
            e.stopPropagation();

            // 执行删除
            UI.moveDownItemOfField(fldIndex, itemIndex);
        },
    },
    //...............................................................
    redraw : function() {
        var UI  = this;

        // 创建基本信息
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth    : "all",
            fitparent  : false,
            mergeData  : false,
            on_change : function(key, val) {
                UI.uiCom.saveData("panel", $z.obj(key, val), true);
            },
            fields  : [{
                key : "btnExtTextShow",
                title : "i18n:hmaker.com.filter.btnExtTextShow",
                dft   : UI.msg("hmaker.com.filter.ext_show"),
            }, {
                key : "btnExtTextHide",
                title : "i18n:hmaker.com.filter.btnExtTextHide",
                dft   : UI.msg("hmaker.com.filter.ext_hide"),
            }, {
                key : "btnMultiText",
                title : "i18n:hmaker.com.filter.btnMultiText",
                dft   : UI.msg("hmaker.com.filter.multi"),
            }, {
                key : "btnMultiOkText",
                title : "i18n:hmaker.com.filter.btnMultiOkText",
                dft   : UI.msg("hmaker.com.filter.multi_ok_txt"),
            }, {
                key : "btnMultiCancelText",
                title : "i18n:hmaker.com.filter.btnMultiCancelText",
                dft   : UI.msg("hmaker.com.filter.multi_cancel_txt"),
            }, {
                key : "moreItemsMode",
                title : "i18n:hmaker.com.filter.moreItemsMode",
                dft   : "auto",
                editAs : "switch",
                uiConf : {items:[
                    {text:"i18n:hmaker.com.filter.fim_auto",   value:"auto"},
                    {text:"i18n:hmaker.com.filter.fim_always", value:"always"},
                    {text:"i18n:hmaker.com.filter.fim_never",  value:"never"},
                ]}
            }, {
                key : "btnFldMoreText",
                title : "i18n:hmaker.com.filter.btnFldMoreText",
                dft   : UI.msg("hmaker.com.filter.fld_more_txt"),
            }, {
                key : "btnFldLessText",
                title : "i18n:hmaker.com.filter.btnFldLessText",
                dft   : UI.msg("hmaker.com.filter.fld_less_txt"),
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 创建动作菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            tipDirection : "up",
            setup : [{
                icon : '<i class="zmdi zmdi-plus"></i>',
                text : 'i18n:hmaker.com.filter.add',
                handler : function(){
                    UI.createField();
                }
            }, {
                icon : '<i class="zmdi zmdi-delete"></i>',
                tip  : 'i18n:hmaker.com.filter.del',
                handler : function(){
                    var index = UI.getCurrentIndex();
                    if(index >= 0) {
                        UI.removeField(index);
                    }
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                tip  : 'i18n:hmaker.com._.move_up',
                handler : function(){
                    var index = UI.getCurrentIndex();
                    if(index >= 0) {
                        UI.moveUpField(index);
                    }
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                tip  : 'i18n:hmaker.com._.move_down',
                handler : function(){
                    var index = UI.getCurrentIndex();
                    if(index >= 0) {
                        UI.moveDownField(index);
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("menu");
        });

        // 返回延迟加载
        return ["form", "menu"];
    },
    //...............................................................
    getComData : function(){
        var com = this.uiCom.getData();
        com.fields = com.fields || [];
        return com;
    },
    //...............................................................
    getCurrentIndex : function(){
        var jCurrent = this.arena.find("section > div[current]");
        if(jCurrent.length > 0)
            return jCurrent.prevAll().length;
        return -1;
    },
    //...............................................................
    setCurrent : function(index) {
        var jFlds = this.arena.find("section > div.cnd-fld").removeAttr("current");
        if(index >= 0)
            jFlds.eq(index).attr("current", "yes");
    },
    //...............................................................
    saveItemOfField : function(fldIndex, itemIndex, item, com) {
        var UI  = this;
        com  = com || UI.getComData();
        var fld = com.fields[fldIndex];
        fld.items = fld.items || [];

        // 追加
        if(itemIndex < 0) {
            fld.items.push(item);
        }
        // 修改
        else {
            fld.items[itemIndex] = item;
        }

        // 通知修改
        UI.uiCom.saveData(null, com, true);
    },
    //...............................................................
    saveField : function(index, fld, com) {
        var UI  = this;
        com  = com || UI.getComData();
        com.fields = com.fields || [];
        
        // 追加
        if(index < 0) {
            com.fields.push(fld);
        }
        // 修改
        else {
            com.fields[index] = fld;
        }

        // 通知修改
        UI.uiCom.saveData(null, com, true);
    },
    //...............................................................
    createItemOfField : function(fldIndex, com){
        var UI  = this;
        // 打开编辑对话框
        UI.openItemOfFieldEditor("i18n:hmaker.com.filter.fld_item_add", {}, function(obj){
            UI.saveItemOfField(fldIndex, -1, obj, com);
        });
    },
    //...............................................................
    removeItemOfField : function(fldIndex, itemIndex, com){
        var UI  = this;
        com  = com || UI.getComData();
        var fld  = com.fields[fldIndex];

        // 删除数组里面指定下标的元素
        var list = [];
        if(_.isArray(fld.items) && fld.items.length > 0)
            for(var i=0; i<fld.items.length; i++){
                if(i != itemIndex) {
                    list.push(fld.items[i]);
                }
            }
        // 更新到字段里
        fld.items = list;

        // 保存
        UI.saveField(fldIndex, fld, com);
    },
    //...............................................................
    moveUpItemOfField : function(fldIndex, itemIndex, com){
        var UI  = this;
        com  = com || UI.getComData();
        var fld   = com.fields[fldIndex];

        // 不能再向上移动了
        if(itemIndex <= 0 )
            return;

        // 交换
        var it = fld.items[itemIndex];
        fld.items[itemIndex]   = fld.items[itemIndex-1];
        fld.items[itemIndex-1] = it;

        // 保存
        UI.saveField(fldIndex, fld, com);
    },
    //...............................................................
    moveDownItemOfField : function(fldIndex, itemIndex, com){
        var UI  = this;
        com  = com || UI.getComData();
        var fld   = com.fields[fldIndex];

        // 不能再向后移动了
        if(fld.items.length <= 1 && itemIndex >= (fld.items.length - 1))
            return;

        // 交换
        var it = fld.items[itemIndex];
        fld.items[itemIndex]   = fld.items[itemIndex+1];
        fld.items[itemIndex+1] = it;

        // 保存
        UI.saveField(fldIndex, fld, com);
    },
    //...............................................................
    editItemOfField : function(fldIndex, itemIndex, com){
        var UI  = this;
        com  = com || UI.getComData();
        var fld  = com.fields[fldIndex];
        var item = fld.items[itemIndex];

        // 打开编辑对话框
        UI.openItemOfFieldEditor("i18n:hmaker.com.filter.fld_item_edit", item, function(obj){
            UI.saveItemOfField(fldIndex, itemIndex, obj, com);
        });
    },
    //...............................................................
    openItemOfFieldEditor : function(title, data, callback) {
        var UI = this;

        // 同步修改函数
        var sync_input_ass = function(uiForm, assType){
            //console.log(uiForm, assType)
            // 假设自己在表单中，那么 this 就一定是字段对象
            var uiInput = uiForm.getFormCtrl("value");
            // 助理类型: 数字范围
            if("number_range" == assType) {
                uiInput.setAssist({
                    icon   : '<i class="zmdi zmdi-n-1-square"></i>',
                    uiType : "ui/form/c_number_range"
                });
            }
            // 助理类型: 日期范围
            else if("date_range" == assType) {
                uiInput.setAssist({
                    icon   : '<i class="fa fa-calendar"></i>',
                    uiType : "ui/form/c_date_range"
                });
            }
            // 清除助理
            else {
                uiInput.setAssist();
            }
        };

        // 打开表单对话框
        POP.openFormPanel({
            title : title,
            i18n  : UI._msg_map,
            arenaClass : "hm-prop-form hmc-cnd-pop-form",
            width : 400,
            height: 300,
            form  : {
                on_change: function(key, val){
                    if("type" == key) {
                        sync_input_ass(this, val);
                    }
                },
                fields : UI.FORM_FIELDS_item
            },
            data  : data,
            after : function(data) {
                sync_input_ass(this, data.type);
            },
            callback : callback
        });
    },
    //...............................................................
    createField : function(com){
        var UI  = this;
        UI.openFieldEditor("i18n:hmaker.com.filter.add", {}, function(obj){
            UI.saveField(-1, obj, com);
        });
    },
    //...............................................................
    removeField : function(index, com){
        var UI  = this;
        com  = com || UI.getComData();

        // 删除数组里面指定下标的元素
        var list = [];
        if(_.isArray(com.fields) && com.fields.length > 0)
            for(var i=0; i<com.fields.length; i++){
                if(i != index) {
                    list.push(com.fields[i]);
                }
            }
        // 更新到字段里
        com.fields = list;

        // 通知修改
        UI.uiCom.saveData(null, com);
    },
    //...............................................................
    moveUpField : function(index, com){
        var UI  = this;
        com  = com || UI.getComData();

        // 不能再向上移动了
        if(index <= 0 )
            return;

        // 交换
        var fld = com.fields[index];
        com.fields[index]   = com.fields[index-1];
        com.fields[index-1] = fld;

        // 通知修改
        UI.uiCom.saveData(null, com);

        // 重新高亮
        console.log("rehigh", index - 1);
        UI.setCurrent(index - 1);
    },
    //...............................................................
    moveDownField : function(index, com){
        var UI  = this;
        com  = com || UI.getComData();

        // 不能再向后移动了
        if(com.fields.length <= 1 && index >= (com.fields.length - 1))
            return;

        // 交换
        var fld = com.fields[index];
        com.fields[index]   = com.fields[index+1];
        com.fields[index+1] = fld;

        // 通知修改
        UI.uiCom.saveData(null, com);

        // 重新高亮
        UI.setCurrent(index + 1);
    },
    //...............................................................
    editField : function(index, com){
        var UI  = this;
        com  = com || UI.getComData();
        var fld = com.fields[index];

        UI.openFieldEditor("i18n:hmaker.com.filter.edit", fld, function(obj){
            UI.saveField(index, obj, com);
        });
    },
    //...............................................................
    openFieldEditor : function(title, data, callback) {
        var UI = this;
        POP.openFormPanel({
            title : title,
            i18n  : UI._msg_map,
            escape : true,
            arenaClass : "hm-prop-form hmc-cnd-pop-form",
            width : 400,
            height: 480,
            form  : {
                fields : UI.FORM_FIELDS_field
            },
            data  : data,
            callback : callback
        });
    },
    //...............................................................
    /*
    数据结构:
    com : {
        fields : [{
            name  : "xxx",
            text  : "xxx",
            multi : false,
            hide  : false,
            items : [{
                text  : "字符串型的值",
                type  : "string",
                value : "值"
            },{
                text  : "数字区间",
                type  : "number_range",
                value : "(19, 22.6]"
            },{
                text  : "日期区间",
                type  : "date_range",
                value : "(2016-02-12, 2016-02-15T15:23:12]" 
            }],
        }, {..}]
    }
    */
    update : function(com) {
        var UI   = this;

        // 更新表单
        UI.gasket.form.setData(com);

        // 得到当前的高亮下标
        var currentIndex = UI.getCurrentIndex();

        // 清空
        var jSec = UI.arena.find(">section").empty();

        // 项目列表
        var fields = com.fields || [];

        // 内容为空
        if(fields.length == 0) {
            jSec.html(UI.compactHTML(`<div class="empty">
                <i class="zmdi zmdi-alert-circle-o"></i>
                {{hmaker.com.filter.empty}}
            </div>`));
        }


        for(var i=0; i<fields.length; i++) {
            var jFld = UI.__update_field($('<div class="cnd-fld">'), i, com);
            jSec.append(jFld);
        }

        // 恢复高亮
        if(currentIndex>=0)
            UI.setCurrent(currentIndex);

        // 开启提示
        UI.balloon();
    },
    //...............................................................
    __update_field : function(jFld, index, com) {
        var UI  = this;
        com = com || UI.getComData();
        var fld = com.fields[index];

        // 更新属性
        jFld.attr("index", index);

        // 绘制本身信息
        var jInfo = $(UI.compactHTML(`<ul class="fld-info">
            <li key="multi" class="fi-box"><b>{{hmaker.com.filter.fld_multi}}</b></li>
            <li key="hide"  class="fi-box"><b>{{hmaker.com.filter.fld_hide}}</b></li>
            <li key="text"></li>
            <li key="name"></li>
        </ul>`)).attr({
            "is-multi" : fld.multi ? "yes" : null,
            "is-hide"  : fld.hide  ? "yes" : null,
        }).appendTo(jFld);
        jInfo.find('>li[key="name"]').text(fld.name);
        jInfo.find('>li[key="text"]').text(fld.text);

        // 绘制编辑按钮
        $('<div class="fld-edit"><a></a></div>').appendTo(jFld)
            .find(">a").text(UI.msg("edit"));

        // 字段数量标识
        if(fld.items && fld.items.length > 0) {
            $('<div class="fld-itnb">').text(fld.items.length).appendTo(jFld);
        }

        // 绘制项目
        var dft_d = UI.msg("hmaker.com._.description");
        var dft_v = "Unknown";
        var jList = $('<ul class="fld-items">').appendTo(jFld);
        if(_.isArray(fld.items)) {
            for(var i=0 ;i<fld.items.length; i++) {
                var it  = fld.items[i];
                // 描述信息
                var jLi = $('<li>').appendTo(jList);
                var jDl = $('<dl>').appendTo(jLi);
                $('<dt>').text(it.text  || dft_d).appendTo(jDl);
                $('<dd>').text(it.value || dft_v).appendTo(jDl);

                // 动作
                $(UI.compactHTML(`<span>
                    <b a="del" balloon="left:hmaker.com._.del"><i class="zmdi zmdi-close"></i></b>
                    <b a="up"><i class="zmdi zmdi-long-arrow-up"></i></b>
                    <b a="down"><i class="zmdi zmdi-long-arrow-down"></i></b>
                </span>`)).appendTo(jLi);
            }
        }

        // 绘制新添加按钮
        $(UI.compactHTML(`<aside>
            <i class="zmdi zmdi-plus"></i>
            <span>{{hmaker.com.filter.fld_item_add}}</span>
        </aside>`)).appendTo(jList);

        // 返回构建的项目
        return jFld;
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jF = UI.arena.find(">footer");
        var jA = UI.arena.find(">aside");
        var jS = UI.arena.find(">section");

        jS.css("height", UI.arena.height() - jF.outerHeight(true) - jA.outerHeight(true));
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);