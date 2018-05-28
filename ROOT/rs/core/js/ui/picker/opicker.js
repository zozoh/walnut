(function($z){
$z.declare([
    'zui', 
    'wn/util',
    'ui/form/support/form_ctrl',
    'ui/pop/pop',
], 
function(ZUI, Wn, FormMethods, POP){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <span code-id="obj" class="picker-obj">
        <a target="_blank"></a>
    </span>
</div>
<div class="ui-arena picker opicker">
    <div class="picker-box"></div>
    <div class="picker-btn picker-choose">{{choose}}</div>
    <div class="picker-btn picker-clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.picker.opicker", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/picker/theme/picker-{{theme}}.css",
    //...............................................................
    init : function(opt) {
        FormMethods(this);
        $z.setUndefined(opt, "mustInBase", true);
        $z.setUndefined(opt, "setup", {});
        $z.setUndefined(opt, "clearable", true);
        $z.setUndefined(opt, "keeyAppend", true);
        $z.setUndefined(opt, "dataAsId", false);
    },
    //...............................................................
    events : {
        // 选择对象
        "click .picker-choose" : function(){
            var UI    = this;
            var opt   = UI.options;

            // 确定基础目录
            var base = opt.base || Wn.fetch("~");

            // 准备数据，没有的话，采用上一次本对话框选择的数据
            var o = UI.getObj();
            //console.log(o)
            
            // 重新定义一下 base
            var lastPath = Wn.getBaseDirPath(o, UI, opt.lastBaseKey);
            var oLast = lastPath ? Wn.fetch(lastPath, true) : null;
            // 如果有上次打开的目录
            if(oLast) {
                if(opt.mustInBase && !Wn.isInDir(base, oLast)){
                    // 什么也不做，就用给定的 base 咯
                }
                // 否则，更新一下 base 咯
                else {
                    base = oLast;
                }
            }

            // 打开对话框
            POP.browser({
                title  : opt.title,
                width  : "80%",
                height : "80%",
                base   : base,
                setup  : _.extend({
                        checkable : false,
                        sidebar   : false,
                        objTagName : 'SPAN',
                        canOpen : function(o){
                            return o.race == 'DIR';
                        }
                    }, (opt.setup || {})),
                on_ready : function(){
                    if(o)
                        this.setActived(o);
                },
                ok : function(){
                    // 得到数据
                    var objs   = this.getChecked();

                    // 支持当前目录作为默认
                    if(objs.length == 0 && opt.defaultByCurrent){
                        objs = [this.getCurrentObj()];
                    }

                    if(objs && objs.length > 0){
                        console.log(objs)
                        // 记录第一个对象所在目录
                        if(opt.lastBaseKey) {
                            UI.local(opt.lastBaseKey, "id:" + objs[0].pid);
                        }
                        // 执行更新并通知
                        UI._set_data(objs);
                        UI.__on_change();
                    }
                }
            }, UI);
        },
        // 清除
        "click .picker-clear" : function(){
            this._set_data();
            this.__on_change();
        }
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        // 不用取消
        if(!opt.clearable){
            UI.arena.find(".picker-clear").remove();
        }
        // 标记多选
        if(opt.setup.checkable){
            UI.arena.addClass("picker-multi");
        }
        // 标记折行
        if(opt.wrapButton)
            UI.arena.attr("wrap-button", "yes");
    },
    //...............................................................
    // 接受标准的 WnObj
    __draw_items : function(o){
        var UI  = this;
        var opt = UI.options;
        var jBox = UI.arena.find(".picker-box");

        // 清除数据
        if(!o){
            jBox.empty();
        }
        // 多选
        else if(opt.setup.checkable) {
            // 确保是数组
            if(!_.isArray(o)){
                o = [o];
            }
            // 是否保持原来的数据
            if(!opt.keeyAppend)
                jBox.empty();
            // 循环添加
            for(var i=0; i<o.length; i++){
                UI.__append_item(o[i], jBox);
            }
        }
        // 单选
        else{
            // 确保不是数组
            if(_.isArray(o)){
                o = o.length > 0 ? o[0] : undefined;
            }
            // 清除数据
            jBox.empty();
            
            // 添加
            UI.__append_item(o, jBox);
        }
    },
    //...............................................................
    __append_item : function(o, jBox){
        var UI  = this;
        var opt = UI.options;

        jBox = jBox || UI.arena.find(".picker-box")
        var jq = UI.ccode("obj").data("@OBJ", o)
                    .prepend(Wn.objIconHtml(o));
        var jA = jq.find("a").prop("href", "/a/open/"
                                    + (window.wn_browser_appName||"wn.browser")
                                    + "?ph=id:"+o.id);

        // 显示名称
        if(opt.showPath) {
            var html = Wn.objDisplayPath(UI, o.ph, 
                    opt.showPath.offset,
                    opt.showPath.wrapper || null, 
                    opt.showPath.sep || null);
            jA.html( html || UI.msg("home"));
        } else {
            jA.text(Wn.objDisplayName(UI, o, 18));
        }
        
        // 加入 DOM
        jBox.append(jq);
    },
    //...............................................................
    getObj : function(opt){
        var UI  = this;
        var opt = opt || this.options;

        // 重新取得一个对象列表
        if(_.isArray(UI.__my_value)) {
            var list = [];
            for(var i=0; i<UI.__my_value.length; i++) {
                var oid = UI.__my_value[i];
                list.push(Wn.getById(oid));
            }
            // 不是多选的话，只取一个
            if(!opt.setup.checkable){
                return list.length > 0 ? list[0] : null;
            }
            return list;
        }
        // 就一个对象
        else if(UI.__my_value){
            var o = Wn.getById(UI.__my_value);
            // 多选的话，变数组
            if(opt.setup.checkable){
                return [o];
            }
            return o;
        }
        // 木有
        return opt.setup.checkable ? [] : null;
    },
    //...............................................................
    __ID : function(obj) {
        return _.isString(obj) ? obj : obj.id;
    },
    //...............................................................
    // 可以接受对象ID, 对象，对象ID数组，对象数组
    _set_data : function(obj){
        var UI = this;
        var opt = opt || this.options;

        // 首先分析一下，如果是数组，则变成对象 ID 数组
        if(_.isArray(obj)) {
            var list = [];
            for(var i=0; i<obj.length; i++) {
                list.push(this.__ID(obj[i]));
            }
            // 不是多选的话，只取一个
            if(!opt.setup.checkable){
                UI.__my_value = list.length > 0 ? list[0] : null;
            }
            // 存成 ID 列表
            else {
                UI.__my_value = list;
            }
        }
        // 否则就存一个对象的 ID
        else if(obj){
            var oid = this.__ID(obj);
            if(!oid) {
                UI.__my_value = null;
            }
            // 多选的话，变数组
            else if(opt.setup.checkable){
                UI.__my_value = [oid];
            }
            // 存成 ID
            else {
                UI.__my_value = oid;
            }
        }
        // 默认删除 val
        else {
            UI.__my_value = null;
        }

        // 重新取得一个对象列表
        var o = UI.getObj();
        //console.log("getObj", o)
        UI.__draw_items(o);
    },
    //...............................................................
    _get_data : function(){
        var UI  = this;
        var opt = opt || this.options;

        // 只是 ID
        if(opt.dataAsId) {
            return UI.__my_value;
        }
        // 返回对象
        return UI.getObj();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);