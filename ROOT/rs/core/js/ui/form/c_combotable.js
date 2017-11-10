(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/form/c_input'
], function(ZUI, FormMethods, InputUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-combotable">
    <div class="cct-list">
        <table><thead><tr></tr></thead><tbody></tbody></table>
        <div class="cct-empty">{{empty}}</div>
    </div>
    <div class="cct-input" ui-gasket="input"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_combotable", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        var UI = FormMethods(this);

        // 默认值
        $z.setUndefined(opt, "fields", []);
        $z.setUndefined(opt, "msgNoExists", "i18n:com.combotable.noneobj");
        $z.setUndefined(opt, "getObj", function(val){
            return null;
        });

        // 初始化字段类型
        UI.__fields = [];
        for(var i=0; i<opt.fields.length; i++) {
            var fld = _.extend({}, opt.fields[i]);
            UI.__fields.push(
                UI._normalize_fld_define(fld, "ui/form/c_label")
            );
        }
    },
    //...............................................................
    events : {
        // 删除
        'click .cct-list td[opt="del"] span' : function(e) {
            var UI = this;
            var jTr = $(e.currentTarget).closest("tr");
            jTr.fadeOut(300, function(){
                UI.__clean_table(jTr);
                jTr.remove();

                console.log("haha")

                UI._set_data(UI._get_data());
                UI.__on_change();
            });
        },
        // 上移
        'click .cct-list td[opt="up"] span' : function(e) {
            var UI = this;
            var jTr = $(e.currentTarget).closest("tr");
            var jPrev = jTr.prev();
            if(jPrev.length > 0) {
                jTr.insertBefore(jPrev);
                $z.blinkIt(jTr);
                UI.__on_change();
            }
        },
        // 下移
        'click .cct-list td[opt="down"] span' : function(e) {
            var UI = this;
            var jTr = $(e.currentTarget).closest("tr");
            var jNext = jTr.next();
            if(jNext.length > 0) {
                jTr.insertAfter(jNext);
                $z.blinkIt(jTr);
                UI.__on_change();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 绘制表头
        var jTHr = UI.arena.find(">.cct-list>table>thead>tr");
        $('<th opt="del">&nbsp;</th>').appendTo(jTHr);

        // 准备延迟的 UI 类型 
        var defer_ui_types = [];

        // 绘制字段
        for(var i = 0; i<UI.__fields.length; i++) {
            var fld = UI.__fields[i];
            // 计入延迟加载项
            if(defer_ui_types.indexOf(fld.uiType)<0)
                defer_ui_types.push(fld.uiType)

            if(fld.hide)
                continue;
            var jTh = $('<th>').attr({
                "fld-key"   : fld.key,
                "fld-index" : i,
                "width"     : fld.width || null
            }).text(UI.text(fld.title)).appendTo(jTHr);
        }

        // 绘制尾部移动顺序按钮
        $('<th opt-icon="yes">&nbsp;</th>').appendTo(jTHr);
        $('<th opt-icon="yes">&nbsp;</th>').appendTo(jTHr);

        // 准备延迟加载
        var defer_keys = [];

        // 预先加载字段类型
        if(defer_ui_types.length > 0){
            defer_keys.push("fld_ui");
            seajs.use(defer_ui_types, function(){
                //console.log("hahah")
                UI.defer_report("fld_ui");
            });
        }
        
        // 绘制输入框
        if(opt.combo) {
            // 转换过滤器为 c_list 的合法过滤器
            if(_.isFunction(opt.combo.filter)) {
                UI.__filter = opt.combo.filter;
                opt.combo.filter = function(o) {
                    var list = UI._get_data();
                    return UI.__filter(o, list);
                }
            }

            // 创建控件
            new InputUI({
                parent : UI,
                gasketName : "input",
                on_change : function(val) {
                    UI.__do_add_val(val);
                },
                assist : {
                    icon : '<i class="zmdi zmdi-more"></i>',
                    uiType : "ui/form/c_list",
                    uiConf : _.extend({
                        drawOnSetData : true,
                    }, opt.combo),
                }
            }).render(function(){
                this.setData("");
                UI.defer_report("combo");
            });
            // 返回以便延迟加载
            defer_keys.push("combo");
        }
        // 延迟加载了
        //console.log("return:", defer_keys)
        return defer_keys;
    },
    //...............................................................
    __do_add_val : function(val) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 得到对象
        var obj = opt.getObj.apply(context, [val]);

        // 如果不存在，弹出错信息
        if(!obj) {
            UI.alert(opt.msgNoExists, {
                divicon : "error",
                callback : function(){
                    UI.gasket.input.focus();
                }
            });
        }
        // 否则追加，并清空输入框
        else {
            // 清除空数据提示文字
            UI.arena.find(" .cct-list > .cct-empty").remove();
            // 确保是数组
            var list = [].concat(obj);
            // 加入内容
            for(var i=0; i<list.length; i++) {
                var jTr = UI.__add_obj(list[i]);
                $z.blinkIt(jTr);
            }
            // 清空输入框
            UI.gasket.input.setData("");
            // 通知更新
            UI.__on_change();
        }
    },
    //...............................................................
    __clean_table : function(selection){
        var UI = this;
        var jq = selection ? $(selection)
                           : UI.arena.find(">.cct-list>table>tbody");

        // 清除所有的子控件        
        jq.find("td[fld-key]").each(function(){
            ZUI($(this).children()).destroy();
        });

        // 清除自身
        jq.empty();
        
    },
    //...............................................................
    __add_obj : function(obj) {
        var UI  = this;
        var opt = UI.options;
        var jList  = UI.arena.find(">.cct-list");
        var jTBody = UI.arena.find(">.cct-list>table>tbody");
        var jTr = $('<tr>').appendTo(jTBody);

        // 准备一个空对象，存放隐藏的值
        var obj2 = {};
        jTr.data("@OBJ", obj2);

        // 绘制删除按钮
        $('<td opt="del"></td>')
            .html('<span><i class="zmdi zmdi-close"></i></span>')
                .appendTo(jTr);

        // 循环绘制字段
        for(var i = 0; i<UI.__fields.length; i++) {
            var fld = UI.__fields[i];
            //console.log(fld)
            // 得到字段值
            var jso = fld.JsObjType;
            var val = jso.parseByObj(obj).value();

            // 隐藏字段，设置一下完事
            if(fld.hide){
                obj2[fld.key] = val;
                continue;
            }

            // 创建单元格
            var jTd = $('<td>').attr({
                "fld-key"   : fld.key,
                "fld-index" : i,
            }).appendTo(jTr);

            // 绘制控件
            (function(jTd, val){
                seajs.use(fld.uiType, function(FldUI){
                    new FldUI(_.extend({}, fld.uiConf, {
                        parent : UI,
                        $pel   : jTd,
                        on_change : function(){
                            UI.__on_change();
                        }
                    })).render(function(){
                        this.setData(val);
                    });
                });
            })(jTd, val);
        }

        // 绘制操作按钮
        $('<td opt="down"></td>')
            .html('<span><i class="fa fa-angle-double-down"></i></span>')
                .appendTo(jTr);
        $('<td opt="up"></td>')
            .html('<span><i class="fa fa-angle-double-up"></i></span>')
                .appendTo(jTr);

        // 返回
        return jTr;
    },
    //...............................................................
    _get_data : function(){
        var UI    = this;

        // 准备返回值
        var re = [];

        // 循环读取字段
        UI.arena.find(">.cct-list>table>tbody>tr").each(function(){
            var jTr = $(this);
            var obj = jTr.data("@OBJ");
            jTr.children('[fld-key]').each(function(){
                var jTd = $(this);
                var index = jTd.attr("fld-index") * 1;
                var fld = UI.__fields[index];
                var fui = ZUI(jTd.children());
                var jso = fld.JsObjType;
                var v   = fui.getData();
                //console.log(v);
                var v2  = jso.parse(v).setToObj(obj);
                // 看看是否有必要重设一下值
                if(v != v2)
                    fui.setData(v2);
            });
            re.push(obj);
        });

        // 返回
        return re;
    },
    //...............................................................
    _set_data : function(objs, jso){
        var UI = this;
        var jList = UI.arena.find(">.cct-list");

        // 清空表格
        UI.__clean_table();

        // 清除空数据提示文字
        jList.find("> .cct-empty").remove();

        // 显示空数据
        if(!_.isArray(objs) || objs.length == 0) {
            $('<div class="cct-empty">').text(UI.msg("empty"))
                .appendTo(jList);
        }
        // 循环绘制数据
        else {
            for(var i=0; i<objs.length; i++) {
                var obj = objs[i];
                UI.__add_obj(obj);
            }
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);