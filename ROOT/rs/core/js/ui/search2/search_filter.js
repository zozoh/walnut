(function($z){
$z.declare(['zui', 'ui/form/form'], function(ZUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena search-filter">
    <div class="flt-con">
        <div class="flt-tabs">
            <div class="flt-tabs-show"><ul></ul></div>
            <div class="flt-tabs-drop"><ul></ul></div>
        </div>
        <div class="flt-keyword">
            <input placeholder="{{search.filter.tip}}"
                spellcheck="false"
                with-ass-btn="yes">
            <div class="flt-icon"><i class="zmdi zmdi-search"></i></div>
            <div class="flt-ass-btn"><i class="zmdi zmdi-settings"></i></div>
        </div>
    </div>
    <div class="flt-assist">
        <div class="flt-ass-mask"></div>
        <div class="flt-ass-form" ui-gasket="form"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.search_filter", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(opt){
        var UI = this;

        $z.setUndefined(opt, "keyField", ["nm"]);
        $z.setUndefined(opt, "forceWildcard", true);

        if(!_.isArray(opt.keyField))
            opt.keyField = [opt.keyField];

        // 修改助理
        if(opt.assist) {
            $z.setUndefined(opt.assist, "width", "inbox");
            $z.setUndefined(opt.assist, "autoOpen", true);
        }

        // 默认的记录一下自己原始的属性
        UI.__old_data = "{}";

        // 处理一下标签
        if(UI.__has_tabs()) {
            // 这里试图从 local 里恢复选中项目
            if(opt.tabsStatusKey) {
                var checkedIndexes = UI.local(opt.tabsStatusKey);
                if(_.isArray(checkedIndexes)){
                    // 对于单选，如果得到是多选，只保留第一个
                    if(!opt.tabsMulti && checkedIndexes.length>1){
                        checkedIndexes = [checkedIndexes[0]];
                    }
                    // 重新设置一下标签的选择状态
                    for(var i=0; i<opt.tabs.length; i++){
                        opt.tabs[i].checked = checkedIndexes.indexOf(i) >= 0;
                    }
                }
            }

            // 补充一下，如果是单选，且限制了必须有一个选中
            // 那么至少要有一个选中啊
            if(!opt.tabsMulti && opt.tabsKeepChecked) {
                var hasChecked = false;
                for(var i=0; i<opt.tabs.length; i++){
                    if(opt.tabs[i].checked){
                        hasChecked = true;
                        break;
                    }
                }
                if(!hasChecked)
                    opt.tabs[0].checked = true;
            }
        }
    },
    //..............................................
    events : {
        // 助理事件：点击打开助理层
        "click .flt-ass-btn" : function(e) {
            this.toggleAssist();
        },
        // 助理事件：收起助理层，并应用修改
        "click .flt-ass-mask" : function(){
            this.__on_change();
            this.closeAssist();
        },
        // 助理事件：聚焦打开助理层
        "focus .flt-keyword > input" : function(){
            var ass = this.options.assist;
            if(ass && ass.autoOpen) {
                this.openAssist();
            }
        },
        // 助理事件：下面三个时间，完美检测中文输入
        "compositionstart .flt-keyword > input" : function(e){
            this.__compositionstart = true;
        },
        "compositionend .flt-keyword > input" : function(e){
            this.__compositionstart = false;
            this.__do_when_input();
        },
        "input .flt-keyword > input" : function(e){
            if(this.__compositionstart)
                return;
            this.__do_when_input();
        },
        // 助理事件：特殊键盘事件
        "keydown .flt-keyword > input" : function(e){
            var UI = this;
            // 回车
            if(13 == e.which) {
                e.preventDefault();
                UI.__on_change();
                UI.closeAssist();
            }
            // 上箭头
            else if(38 == e.which) {
                UI.closeAssist();
            }
            // 下箭头
            else if(40 == e.which) {
                UI.openAssist();
            }
        },
        // 标签事件(DROP)：打开 drop 的菜单
        'click .flt-con[tabs-pos="drop"] .flt-tabs-show li' : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);
            var jTabs = UI.arena.find(".flt-tabs");
            var jDrop = jTabs.find(".flt-tabs-drop");
            jTabs.attr("show-drop", "yes");
            $z.dock(jLi, jDrop.find(">ul"), "H");
        },
        // 标签事件(DROP)：关闭 drop 的菜单
        'click .flt-con[tabs-pos="drop"] .flt-tabs-drop' : function(e){
            this.closeTabsDrop();
        },
        // 标签事件(DROP)：点击项目
        'click .flt-con[is-drop] .flt-tabs-drop li' : function(e){
            // 首先，不要冒泡了
            e.stopPropagation();

            // 处理一下
            var UI  = this;
            var opt = UI.options;
            var jLi = $(e.currentTarget);
            var jShow = UI.arena.find(".flt-tabs-show");
            var jDrop = UI.arena.find(".flt-tabs-drop");
            var jUl = jDrop.find(">ul");

            // 多选的处理
            if(opt.tabsMulti) {
                // 切换自己的显示
                $z.toggleAttr(jLi, "fti-status", "checked", "ava");
                UI.__sync_li_color(jLi);
            }
            // 单选的处理
            else {
                // 取消其他选中
                jUl.find('li[fti-status="checked"]')
                    .not(jLi)
                        .attr("fti-status", "ava")
                        .css({
                            "color" : "",
                            "background" : ""
                        });
                // 切换到自己
                $z.toggleAttr(jLi, "fti-status", "checked", "ava");
                UI.__sync_li_color(jLi);
            }
            // 同步 drop 到 show
            UI.__sync_drop_to_show();

            // 记录标签选择状态
            UI.__save_tabs_to_local();

            // 重新 Dock
            $z.dock(jShow.find('li').last(), jUl, "H");

            // 无论是单选还是多选，要触发更新
            UI.__on_change();
        },
        // 标签事件(普通)：点击项目
        'click .flt-con[is-label] .flt-tabs-show li' : function(e) {
            var UI  = this;
            var opt = UI.options;
            var jLi = $(e.currentTarget);
            var jUl = jLi.closest("ul");
            
            // 多选处理
            if(opt.tabsMulti) {
                $z.toggleAttr(jLi, "fti-status", "checked", "ava");
                UI.__sync_li_color(jLi);
            }
            // 单选处理
            else {
                // 取消其他选中
                jUl.find('li[fti-status="checked"]')
                    .not(jLi)
                        .attr("fti-status", "ava")
                        .css({
                            "color" : "",
                            "background" : ""
                        });
                // 如果确保有选中的，那么自己如果已经选中就啥也不干了
                if(opt.tabsKeepChecked && "checked" == jLi.attr("fti-status")) {
                    return;
                }
                // 切换到自己
                $z.toggleAttr(jLi, "fti-status", "checked", "ava");
                UI.__sync_li_color(jLi);
            }

            // 记录标签选择状态
            UI.__save_tabs_to_local();

            // 无论是单选还是多选，要触发更新
            UI.__on_change();
        }
    },
    //..............................................
    redraw : function(){
        var UI = this;
        var opt = UI.options;
        var jInput = UI.arena.find(".flt-keyword input");

        // 初始化查询字符串
        jInput.val(opt.query||"");

        // 初始化条件标签 
        UI.__draw_tabs();

        // 是否显示表单按钮
        if(!opt.assist) {
            UI.arena.find(".flt-ass-btn").remove();
            jInput.removeAttr("with-ass-btn");
            UI.arena.removeAttr("show-ass");
            UI.arena.find(".flt-assist").remove();
            return;
        }
        // 确保属性
        jInput.attr("with-ass-btn", "yes");

        // 那么就显示表单咯
        UI._ui_assist = new FormUI(_.extend({
            displayMode : "compact",
        }, opt.assist.form, {
            parent : UI, 
            gasketName : "form",
            fitparent : opt.assist.height ? true : false,
            on_update : function(){
                UI.__update_input_by_assist();
            }
        })).render(function(){
            UI.__update_assist_by_input();
            UI.defer_report("form");
        });

        // 返回
        return ["form"];
    },
    //..............................................
    __has_tabs : function(){
        var UI  = this;
        var opt = UI.options;
        return _.isArray(opt.tabs) && opt.tabs.length>0;
    },
    //..............................................
    __save_tabs_to_local : function(){
        var UI  = this;
        var opt = UI.options;

        if(opt.tabsStatusKey){
            var checkedIndexes = [];
            UI.arena.find('.flt-tabs ul[for-save] li').each(function(index, ele){
                if("checked" == $(ele).attr("fti-status")){
                    checkedIndexes.push(index);
                }
            });
            UI.local(opt.tabsStatusKey, checkedIndexes);
        }
    },
    //..............................................
    __draw_tabs : function() {
        var UI  = this;
        var opt = UI.options;

        // 得到关键 DOM 元素
        var jCon  = UI.arena.find(">.flt-con");
        var jTabs = jCon.find(">.flt-tabs");

        // 开始绘制
        if(UI.__has_tabs()) {
            // 指定显示位置
            jCon.attr({
                "tabs-pos" : opt.tabsPosition||"left",
                "is-drop"  : "drop" == opt.tabsPosition ? "yes" : null,
                "is-label" : /^(left|top)$/.test(opt.tabsPosition) ? "yes" : null,
            });

            // 准备填充数据
            var jShow = jTabs.find(".flt-tabs-show");
            var jDrop = jTabs.find(".flt-tabs-drop");

            // 全部折叠进去
            if("drop" == opt.tabsPosition) {
                UI.__append_tab_items(jDrop.find(">ul"));
                // 多选，来个加号
                if(opt.tabsMulti) {
                    $('<li fti-status="ava">')
                        .html('<i class="drop-icon zmdi zmdi-plus" multi></i>')
                            .appendTo(jShow.find(">ul"));
                }
                // 单选，显示一个虚条件
                else {
                    $('<li fti-status="ava">').html('<span>'
                        + UI.msg("search.filter.all")
                        + '</span><i class="drop-icon fa fa-chevron-right"></i>')
                            .appendTo(jShow.find(">ul"));
                }
                // 同步 drop 到 show
                UI.__sync_drop_to_show();
            }
            // 直接显示
            else {
                UI.__append_tab_items(jShow.find(">ul"));
                jDrop.remove();
            }
        }
        // 隐藏标签
        else {
            jTabs.remove();
        }
    },
    //..............................................
    __append_tab_items : function(jUl) {
        var UI  = this;
        var opt = UI.options;

        // 标识一下 ul ，标识这个 ul 用来作为标签的状态
        jUl.attr("for-save", "yes");

        // 绘制 <li>
        for(var i=0; i<opt.tabs.length; i++) {
            var tab = opt.tabs[i];
            var jLi = $('<li>');

            // 图标
            if(tab.icon) {
                $('<span class="fti-icon">').html(tab.icon)
                    .appendTo(jLi);
            }

            // 文字
            if(tab.text) {
                $('<span class="fti-text">').html(UI.text(tab.text))
                    .appendTo(jLi);
            }

            // 默认
            if(!tab.icon && !tab.text) {
                jLi.text("Condition " + i);
            }

            // 设置颜色背景等样式
            jLi.attr({
                "checked-color" : tab.color || "",
                "checked-background" : tab.background || ""
            });

            // 标识选中
            jLi.attr("fti-status", tab.checked ? "checked" : "ava");
            UI.__sync_li_color(jLi);

            // 记入数据并加入 DOM
            jLi.data("@VALUE", tab.value).appendTo(jUl);
        }
    },
    //..............................................
    __sync_drop_to_show : function(){
        var UI  = this;
        var opt = UI.options;
        var jShow = UI.arena.find(".flt-tabs-show");
        var jDrop = UI.arena.find(".flt-tabs-drop");
        var jUl = jDrop.find(">ul");

        // 多选的处理
        if(opt.tabsMulti) {
            // 清空显示的标签
            jShow.find('li[fti-status="checked"]').remove();

            // 开始同步...
            var jShowUl  = jShow.find(">ul");
            var jShowBtn = jShowUl.find('li[fti-status="ava"]');
            jUl.find('li[fti-status="checked"]').each(function(){
                var jChecked = $(this);
                var jShowLi  = $('<li>').insertBefore(jShowBtn);
                // Copy 设置
                UI.__copy_li_setting(jChecked, jShowLi);
                UI.__sync_li_color(jShowLi);
            });
        }
        // 单选的处理
        else {
            var jShowLi  = jShow.find("li").first();
            var jChecked = jUl.find('li[fti-status="checked"]').first();

            // Copy 设置
            UI.__copy_li_setting(jChecked, jShowLi);
            UI.__sync_li_color(jShowLi);
        }
    },
    //..............................................
    __copy_li_setting : function(jLiSrc, jLiTa) {
        var UI  = this;
        var opt = UI.options;
        // 设置默认项
        var val = null;
        var stat = "ava";
        var color = "";
        var background = "";
        var html;
        // 显示高亮项目
        if(jLiSrc.length > 0) {
            val = jLiSrc.data("@VALUE");
            stat = jLiSrc.attr("fti-status");
            color = jLiSrc.attr("checked-color") || "";
            background = jLiSrc.attr("checked-background") || "";
            html = jLiSrc.html();
        }
        // 恢复默认
        else {
            html = '<span>'+ UI.msg("search.filter.all") + '</span>';
        }
        // 单选，需要下箭头
        if(!opt.tabsMulti)
            html += '<i class="drop-icon fa fa-chevron-right"></i>';
        // 设置
        jLiTa.data("@VALUE", val).html(html).attr({
            "fti-status"         : stat,
            "checked-color"      : color,
            "checked-background" : background,
        });
    },
    //..............................................
    __sync_li_color : function(jLi) {
        // 显示自定义颜色
        if("checked" == jLi.attr("fti-status")) {
            jLi.css({
                "color" : jLi.attr("checked-color") || "",
                "background" : jLi.attr("checked-background") || "",
            });
        }
        // 取消自定义颜色
        else {
            jLi.css({
                "color" : "",
                "background" : "",
            });
        }
    },
    //..............................................
    toggleAssist : function(){
        if(this.arena.attr("show-ass")) {
            this.closeAssist();
        } else {
            this.openAssist();
        }
    },
    //..............................................
    openAssist : function(){
        if(this._ui_assist){
            this.arena.attr("show-ass", true);
            this.resize(true);
        }
    },
    //..............................................
    closeAssist : function(){
        this.arena.removeAttr("show-ass");
    },
    //..............................................
    closeTabsDrop : function() {
        this.arena.find(".flt-tabs").removeAttr("show-drop");
    },
    //..............................................
    setKeyword : function(str) {
        this.arena.find("input").val(str||"");
    },
    //..............................................
    __on_change : function() {
        var data = this.getData();
        var json = $z.toJson(data);
        if(json != this.__old_data) {
            this.__old_data = json;
            this.trigger("filter:change", data);
        }
    },
    //..............................................
    setData : function(data){
        var UI = this;
        UI.ui_parse_data(data, function(data2){
            UI.__query_base = data2 || {};
        });
    },
    //..............................................
    __do_when_input : function() {
        var UI  = this;
        var opt = UI.options;
        var ass = opt.assist;

        // 自动打开助理
        if(ass && ass.autoOpen) {
            UI.openAssist();
        }

        // 如果有助理，同步修改的值
        if(UI._ui_assist) {
            UI.__update_assist_by_input();
        }
    },
    //..............................................
    __update_assist_by_input : function(){
        var UI = this;
        if(UI._ui_assist) {
            var cri = UI._get_criteria();
            UI._ui_assist.setData(cri.match);
        }
    },
    __update_input_by_assist : function(){
        var UI = this;
        if(UI._ui_assist) {
            // 得到原始的值
            var cri = UI._get_criteria();

            // 与表单里的合并
            var mat = UI._ui_assist.getData();
            //console.log(mat)
            _.extend(cri.match, mat);
            

            // 更新到输出框里
            var ss = [];
            for(var key in cri.match) {
                var val = cri.match[key];
                if(_.isNull(val) || _.isUndefined(val))
                    continue;
                ss.push(key + ":" + cri.match[key]);
            }
            var str = ss.concat(cri.keywords).join(" ") || "";
            UI.arena.find(".flt-keyword input").val(str);
        }
    },
    //..............................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        return this.ui_format_data(function(){
            var cri = UI._get_criteria();
            var re  = _.extend({});

            // 根据 keyField 的设定，添加字段
            for(var i=0; i<cri.keywords.length; i++){
                UI._fill_key_field(re, cri.keywords[i]);
            }

            // 叠加搜索结果
            _.extend(re, cri.match);

            // 追加上标签的设定
            var tabs = [];
            UI.arena.find('.flt-tabs-show li[fti-status="checked"]')
                .each(function(){
                    tabs.push($(this).data("@VALUE"));
                });

            // 仅仅是追加
            if(tabs.length == 1) {
                _.extend(re, tabs[0]);
            }
            // 变成 or
            else if(tabs.length > 1) {
                re["%or"] = tabs;
            }

            //console.log(cri)

            // 返回最后结果，并且一定用基础对象覆盖
            return _.extend(re, UI.__query_base);
        });
    },
    /*..............................................
    得到一个查询对象，格式为: 
    {
        keywords : [],
        match : {...}    
    }
    */
    _get_criteria : function() {
        var UI = this;
        // 查询的基础
        var mch = {};
        
        // 处理关键字
        var kwd = $.trim(UI.arena.find("input").val());

        var regex = /((\w+)[:=]([^'" ]+))|((\w+)[:=]"([^"]+)")|((\w+)[:=]'([^']+)')|('([^']+)')|("([^"]+)")|([^ \t'"]+)/g;
        var i = 0;
        var m = regex.exec(kwd);
        var ss = [];
        while(m){
            // 控制无限循环
            if((i++) > 100)
                break;
            // m.forEach(function(v, index){
            //     console.log(i+"."+index+")", v);
            // });
            //.............................
            // 找到纯字符串：作为关键字
            if(m[14]){
                ss.push(m[14]);
            }
            else if(m[13]){
                ss.push(m[13]);
            }
            else if(m[11]){
                ss.push(m[11]);
            }
            //.............................
            // 找到等式
            else if(m[7]){
                mch[m[8]] = m[9];
            }
            else if(m[4]){
                mch[m[5]] = m[6];
            }
            else if(m[1]){
                mch[m[2]] = $z.strToJsObj(m[3], UI.parent.getFieldType(m[2]));
            }
            //.............................
            // 继续执行
            m = regex.exec(kwd);
        }

        // 返回
        return {
            keywords : ss,
            match    : mch,
        };
    },
    //..............................................
    _fill_key_field : function(mch, str){
        var UI  = this;
        var opt = UI.options;       

        // 准备键值列表
        var keyList = [];

        // 根据 keyField 的设定，看看应该搜索哪个字段
        for(var i=0; i<opt.keyField.length; i++){
            var kf  = opt.keyField[i];
            var key = null;
            // F(str):key
            if(_.isFunction(kf)){
                key = kf(str);
            }
            // {regex:/../, key:"xxx"}
            else if(kf.regex && _.isString(kf.key)){
                if(new RegExp(kf.regex).test(str))
                    key = kf.key;
            }
            // 字符串
            else if(kf && _.isString(kf)){
                var pos = kf.indexOf(":^");
                // "mobile:^[0-9]+$"
                if(pos>0){
                    if(new RegExp(kf.substring(pos+1)).test(str))
                        key = kf.substring(0, pos);
                }
                // "nm"
                else {
                    key = kf;
                }
            }
            // 那么最后判断一下是否取到 key 了
            if(key){
                keyList.push(key);
                // 不是 or 的关系，就没必要继续搜索下去了
                if(!opt.keyFieldIsOr) {
                    break;
                }
            }
        }
        // 没找到可用的键，无视
        if(keyList.length == 0) {
            return;
        }
        // 准备一下值
        // 如果 str 以 ^ 开头，则为正则表达式，不管它
        // 否则看看是否要强制升级通配符
        if(opt.forceWildcard && !/^\^/.test(str)){
            str = "^.*" + str + ".*";
        }
        //console.log(str, keyList)
        // 多于一个键，那么需要需要组装成 or
        if(keyList.length > 1) {
            var or = [];
            for(var i=0;i<keyList.length;i++){
                or.push($z.obj(keyList[i], str));
            }
            $z.pushValue(mch, "%or", or);
        }
        // 正则表达式，直接替换了
        else if(/^\^/.test(str)){
            mch[keyList[0]] = str;
        }
        // 否则试图融合
        else {
            var key = keyList[0];
            var val = mch[key];
            if(!val)
                mch[key] = str;
            else if(_.isArray(val))
                val.push(str);
            else
                mch[key] = [val, str];
        }
    },
    //..............................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;
        var ass = opt.assist;
        var jKwd  = UI.arena.find(".flt-keyword");
        var jForm = UI.arena.find(".flt-ass-form");

        if(ass) {
            var uiSearch = UI.parent;
            // 修订宽度
            if("inbox" == opt.assist.width) {
                jForm.css("width", jKwd.outerWidth());
            }
            // 剩下的自动计算
            else {
                jForm.css("width",
                    $z.dimension(opt.assist.width, uiSearch.arenaWidth()));
            }
            // 修订高度
            if(!_.isUndefined(opt.assist.height)){
                jForm.css("height",
                    $z.dimension(opt.assist.width, uiSearch.arenaHeight()));
            }

            // 确保停靠
            $z.dock(jKwd, jForm, "H");
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);