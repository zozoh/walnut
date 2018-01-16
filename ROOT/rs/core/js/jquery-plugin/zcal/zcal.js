(function($, $z){
//...........................................................
var NM_OPT     = "ZCAL_OPTION";
var NM_CURRENT = "ZCAL_CURRENT";
var NM_CHECKED = "ZCAL_CHECKED";
var NM_ACTIVED = "ZCAL_ACTIVED";
function _tmpl(str){
    return _.template(str, {
        escape : /\{\{([\s\S]+?)\}\}/
    });
}
function options(ele, opt) {
    // 格式化配置
    if(opt){
        // 根据显示模式切换不同的模板
        if(_.isUndefined(opt.title)){
            // 多周或者范围模式
            if("range" == opt.mode || opt.byWeek>0){
                opt.title = function(c, dFrom, dTo){
                    var str;
                    // 跨年
                    if(c.from.yy != c.to.yy){
                        str = "{{from.Month}} {{from.d}}, {{from.yyyy}} - {{to.Month}} {{to.d}}, {{to.yyyy}}";
                    }
                    // 跨月
                    else if(c.from.M != c.to.M){
                        str = "{{from.Month}} {{from.d}} - {{to.Month}} {{to.d}}, {{from.yyyy}}";
                    }
                    // 同月
                    else{
                        str = "{{from.Month}} {{from.d}} - {{to.d}}, {{from.yyyy}}";
                    }

                    // 渲染
                    return _tmpl(str)(c);
                };
            }
            // 单月模式
            else{
                opt.title = "{{Month}} {{yyyy}}";
            }
        }
        // 指定格式化
        if(_.isString(opt.title)){
            opt.title = _tmpl(opt.title);
        }
        return $root(ele).data(NM_OPT, opt);
    }
    return $root(ele).data(NM_OPT);
}
function current(ele, d) {
    if(!d)
        return $root(ele).data(NM_CURRENT);
    $root(ele).data(NM_CURRENT, d);
}
function $root(ele) {
    var jq; 
    if (ele instanceof jQuery) {
        jq = ele;
    } else {
        jq = $(ele);
    }
    if(jq.hasClass("zcal"))
        return jq;
    var re = jq.parents(".zcal");
    if(re.size()>0)
        return re;
    re = jq.children(".zcal");
    if(re.size()>0)
        return re;
    throw "Can not found $root by : " + ele;
}
function dkey(d){
    return "" + d.getFullYear() 
           + "-" + $z.alignRight(d.getMonth()+1,2,'0')
           + "-" + $z.alignRight(d.getDate(),2,'0');
}
function genDateContext(d, opt){
    var c = {
        d   : d.getDate(),
        M   : d.getMonth() + 1,
        yyyy: ""+ d.getFullYear()
    };
    c.yy = c.yyyy.substring(2,4);
    c.MM = $z.alignRight(c.M, 2, '0');
    c.dd = $z.alignRight(c.d, 2, '0');
    c.Month = opt.i18n.month[c.M-1];
    return c;
}
//...........................................................
function cellD(jCell){
    var str;
    if(_.isString(jCell))
        str = jCell;
    else
        str = jCell.attr("key");
    return $z.parseDate(str+"T00:00:00");
}
//...........................................................
function get_range(ele, selector) {
    var jRoot = $root(ele);
    var jCells = jRoot.find(selector || ".zcal-cell-show");
    var from = $z.parseDate(jCells.first().attr("key")+"T00:00:00");
    var to   = $z.parseDate(jCells.last().attr("key")+"T23:59:59");
    return [from, to];
}
//...........................................................
function __format_array(list, mode) {
    if(!_.isArray(list) && list.length > 0)
        return list;

    if(mode && _.isString(mode)){
        for(var i=0; i<list.length; i++) {
            // 仅输出毫秒
            if("ms" == mode){
                list[i] = list[i].getTime();
            }
            // 输出日期
            else if("date" == mode){
                list[i] = list[i].format("yyyy-mm-dd");
            }
            // 自定义输出格式
            else if(_.isString(mode)){
                list[i] = list[i].format(mode);
            }
        }
    }

    // 输出日期对象
    return list;
}
//...........................................................
var commands = {
    blur : function(){
        var jRoot = $root(this);
        return do_blur(jRoot);
    },
    current : function(d){
        // 获取
        if(!d){
            return options(this).current;
        }
        // 设置
        update(this, options(this), d);
        return this;
    },
    viewport : function(){
        return get_range(this);
    },
    actived : function(){
        var ms = $root(this).data(NM_ACTIVED);
        if(ms){
            return $z.parseDate(ms);
        }
        return null;
    },
    active : function(d){
        if(d){
            var d2 = $z.parseDate(d);
            d2.setHours(0,0,0,0);
            do_active($root(this), d2);
        }
        return this;
    },
    range : function(mode){
        // 设置
        if(_.isArray(mode)){
            do_set_range(this, mode);
            return this;
        }

        // 获取
        var msChecked = $root(this).data(NM_CHECKED);
        var re = null;
        if(msChecked){
            re = [$z.parseDate(msChecked[0]), $z.parseDate(msChecked[1])];
        }
        // 没有范围，那么用激活的日期
        else{
            var ms = $root(this).data(NM_ACTIVED);
            if(ms){
                var dFrom = $z.parseDate(ms);
                dFrom.setHours(0,0,0,0);
                var dTo   = $z.parseDate(ms);
                dTo.setHours(23,59,59,999);
                re = [dFrom, dTo];
            }
        }

        // 返回
        return __format_array(re, mode);
    },
    multi : function(mode) {
        // 设置
        if(_.isArray(mode)){
            do_set_multi(this, mode);
            return this;
        }

        // 获取
        var msChecked = $root(this).data(NM_CHECKED);
        
        // 返回
        return __format_array(msChecked, mode);
    },
    resize : function(){
        do_resize($root(this));
        return this;
    }
};
//...........................................................
var _DOM = function(){/*
<div class="zcal">
    <div class="zcal-head">
        <div class="zcal-info">
            <div class="zcal-switcher">
                <div class="zcal-today" val="0"></div>
                <div class="zcal-prev"  val="-1"></div>
                <div class="zcal-next"  val="1"></div>
            </div>
            <div class="zcal-title"></div>
        </div>
    </div>
    <div class="zcal-viewport">
        <div class="zcal-wrapper"></div>
    </div>
</div>
*/};
//...........................................................
function on_click_swithcer(){
    var jRoot = $root(this);
    var opt   = options(jRoot);
    var val   = $(this).attr("val") * 1;
    var d;
    if(val != 0){
        // 如果是按周
        if(opt.byWeek > 0){
            d = cellD(jRoot.find(".zcal-cell-show").first());
            d.setDate(d.getDate() + (opt.byWeek*7*val));
        }
        // 那么是按月
        else{
            d = cellD(jRoot.find('.zcal-cell[in-month="yes"]').first());
            d.setMonth(d.getMonth() + val);
        }
        // 更新日历格子
        update(jRoot, options(jRoot), d);
    }
    // 用今天
    else{
        d = new Date();
        // 更新日历格子
        update(jRoot, options(jRoot), d);
        // 看看是不是要激活今天
        if(opt.activeWhenSwitchTody) {
            commands.active.call(jRoot, d);
        }
    }
    
}
//...........................................................
function on_click_cell(e){
    var jRoot = $root(this);
    var opt   = options(jRoot);
    var jCell = $(this);
    var d;
    // 如果是支持 toggle 取消
    if(opt.toggleBlur && jCell.hasClass("zcal-cell-actived")){
        d = do_blur(jRoot, opt);
    }
    // 否则激活
    else {
        d = do_active(jRoot, jCell, e.shiftKey);
    }
    $z.invoke(opt, "on_cell_click", [e, d], jCell);
}
//...........................................................
function on_click_title(e){
    var jRoot  = $root(this);
    var opt    = options(jRoot);
    var jTitle = jRoot.find(".zcal-title"); 
    var theD   = $z.parseDate(current(jRoot));
    $z.editIt(jTitle, {
        text : "" + theD.getFullYear(),
        after : function(newval, oldval){
            if(newval > 0){
                theD.setFullYear(newval);
                update(jRoot, opt, theD);
            }
        }
    });
}
//...........................................................
function bindEvents(jRoot, opt){
    jRoot.on("click", ".zcal-today", on_click_swithcer);
    jRoot.on("click", ".zcal-prev",  on_click_swithcer);
    jRoot.on("click", ".zcal-next",  on_click_swithcer);
    jRoot.on("click", ".zcal-cell-show", on_click_cell);
    jRoot.on("click", ".zcal-title", on_click_title);
}
//...........................................................
function redraw($ele, opt){
    // 得到 HTML 结构 
    var html = $z.getFuncBodyAsStr(_DOM.toString());
    html = html.substring(2, html.length - 2)

    var jRoot = $(html).appendTo($ele);
    // jRoot.addClass(opt.fitparent && opt.blockNumber==1
    //                 ? "zcal-fit-parent"
    //                 : "zcal-fixed");
    jRoot.attr({
            "mode" : opt.mode || "single"
        })
        .addClass(opt.simpleCell 
                    ? "zcal-simple" 
                    : "zcal-customized")
            .children(".zcal-viewport")
                .addClass(opt.showBorder 
                            ? "zcal-show-border" 
                            : "zcal-hide-border")
                    .children(".zcal-wrapper")
                        .addClass(opt.blockNumber>1 
                                    ? "zcal-multi-block" 
                                    : "zcal-single-block");

    // 保存配置
    options(jRoot, opt);

    if(opt.className)
        jRoot.addClass(opt.className);

    return jRoot;
}
//...........................................................
function update(jRoot, opt, d){
    opt.current = $z.parseDate(d || opt.current || new Date());
    d = opt.current;
    var ms = d.getTime()
    d = new Date();
    d.setTime(ms);
    // 日期时间归零，并存储当前时间
    d.setHours(0,0,0,0);
    current(jRoot, d);

    // 存储了当天日期的字符串
    var key = dkey(d);

    // 清除绘制区
    var jWrapper = jRoot.find(".zcal-wrapper").empty();

    // 开始绘制
    var d2 = d;
    var n = opt.blockNumber || 1;
    for(var i=0;i<n;i++){
        var re = draw_block(jWrapper, opt, d2);
        //console.log("re:", re[0], re[1]);
        d2 = $z.parseDate(re[1].getTime() + 86400000);
    }

    // 如果准备绘制标题
    var jHead  = jRoot.find(".zcal-head");
    if(opt.head !== false){
        jHead.show();
        update_head(jHead, opt, d);
    }else{
        jHead.hide();
    }

    // 标记今天
    var todayKey = dkey(new Date());
    jWrapper.find('.zcal-cell[key='+todayKey+']').attr("today","yes");

    // 固定计算单元格的比率
    var wCell = 100 / 7 + "%";
    // //var hCell = (100 / ((opt.byWeek||6) + 1)) + "%";
    jWrapper.find(".zcal-cell").css({
        "width"  : wCell,
        //"height" : hCell
    });

    // 重新规划尺寸
    do_resize(jRoot, opt);

    // 调用回调
    var dr = get_range(jRoot);
    $z.invoke(opt, "on_switch", [d, dr[0], dr[1]], jRoot)
}
//...........................................................
function update_head(jHead, opt, d){
    // 更新标题
    var jTitle = jHead.find(".zcal-title");
    var dr = get_range(jHead);
    var c;
    if("range"==opt.mode || opt.byWeek>0){
        c = {
            from : genDateContext(dr[0],opt),
            to   : genDateContext(dr[1],opt)
        };
    }else{
        c = genDateContext(d,opt);
    }
    var titleHtml = opt.title(c, dr[0], dr[1]);
    jTitle.html(titleHtml);

    // 转换按钮
    var jSwitcher = jHead.find('.zcal-switcher');
    if(opt.swticher){
        jSwitcher.show();
        var jToday = jSwitcher.find('.zcal-today');
        if(opt.swticher.today){
            jToday.show().html(opt.swticher.today);
        }else{
            jToday.hide();
        }
        jSwitcher.find('.zcal-prev').html(opt.swticher.prev);
        jSwitcher.find('.zcal-next').html(opt.swticher.next);
        if(opt.swticherAtRight){
            jSwitcher.addClass("zcal-switcher-right");
        }else{
            jSwitcher.removeClass("zcal-switcher-right");
        }
    }else{
        jSwitcher.hide();
    }

    // 菜单
}
//...........................................................
// 生成一个日期的表格，返回一个二元数组，表示绘制的起止日期
function draw_block(jWrapper, opt, d){
    //d.setDate(32);
    //d.setDate(0);
    //d = new Date("2015-12-21");
    var ssd = "" + d;
    console.log("zcal.draw_block:", ssd);
    if("Invalid Date" == ssd) {
        console.warn("hahahahahah");
    }
    // 开始计算前的准备工作
    var jRoot= $root(jWrapper);
    var ms   = d.getTime();
    var day  = d.getDay();
    var MM   = d.getMonth();
    var currentMonth = current(jWrapper).getMonth();
    //console.log("week day:", day)
    //console.log("   input date:", dkey(d));

    // 本周开始的日期，这里要看看是周日开始，还是周一开始
    if(opt.firstDayIsMonday)
        day = day == 0 ? 6 : day-1;
    var weekBeginMs   = ms - (day)*86400000;
    var weekBeginDate = $z.parseDate(weekBeginMs);
    //console.log("weekBeginDate:", dkey(weekBeginDate));

    // 绘制前计算
    var row_n;      // 一共要绘制几行    
    var from;       // 开始的日期，时间一定是 00:00:00
    var to;         // 结束的日期，时间一定是 23:59:59

    // 根据模式的不同，来决定是绘制整月的表格，还是绘制一个固定的周数
    if(opt.byWeek > 0){
        row_n = opt.byWeek;
        from  = $z.parseDate(weekBeginMs);
    }
    // 绘制整月
    else{
        // 首先得到整个块的第一天
        from = $z.parseDate(weekBeginMs);
        while(from.getMonth()==MM && from.getDate()>1){
            from.setTime(from.getTime() - 86400000*7);
        }
        from.setHours(0,0,0,0);


        // if("range" == opt.mode){
        //     // 计算每个块的最后一天
        //     to = $z.parseDate(weekBeginMs);
        //     while(to.getMonth()==MM){
        //         to.setDate(to.getDate()+7);
        //     }
        //     // 减去一天
        //     to.setDate(to.getDate()-1);
        //     to.setHours(23,59,59,999);

        //     row_n = Math.ceil((to.getTime() - from.getTime())/(7*86400000));
        // }
        // 靠，固定 6 个格子啦
        row_n = 6;
    }

    // 开始绘制
    var _d_cell = $z.parseDate(from.getTime());
    var jDiv   = $('<div class="zcal-block">').appendTo(jWrapper);
    var jTable = $('<table class="zcal-table">').appendTo(jDiv);
    // 绘制表头
    var jTHead = $('<thead class="zcal-table-head">').appendTo(jTable);
    var jTr    = $('<tr>').appendTo(jTHead);
    for(var i=0;i<7;i++){
        var jTh = $('<th class="zcal-th">').appendTo(jTr);
        var n;
        if(opt.firstDayIsMonday){
            n = i==6? 0 : i+1; 
        }else{
            n = i;
        }
        jTh.text(opt.i18n.week[n]);
    }

    // 得到之前的激活以及范围
    var msActived = jRoot.data(NM_ACTIVED);
    var msChecked = jRoot.data(NM_CHECKED);

    // 如果是范围模式，则生成一个日期的散列，以便快速判断
    var msMap = __gen_checked_map(jRoot, opt, msChecked);

    // 表体
    var jTBody = $('<tbody class="zcal-table-body">').appendTo(jTable);
    for(var i=0;i<row_n;i++){
        jTr = $('<tr>').appendTo(jTBody);
        for(var x=0;x<7;x++){
            var theKey   = dkey(_d_cell);
            var theMonth = _d_cell.getMonth();
            var theDate  = _d_cell.getDate();
            var jTd = $('<td class="zcal-cell">').appendTo(jTr);
            jTd.attr("key",   theKey)
               .attr("year",  _d_cell.getFullYear())
               .attr("month", theMonth)
               .attr("date",  theDate)
               .attr("day",   _d_cell.getDay());
            // 如果是按周显示，标记一下格子所在月份的奇偶
            if(opt.byWeek) {
                jTd.attr("in-month","yes")
                   .addClass(parseInt((theMonth - currentMonth)%2)==0
                            ?"zcal-cell-even"
                            :"zcal-cell-odd");
            }
            // 整月显示才标记一下，是否属于当月
            else if(theMonth != MM){
                jTd.attr("in-month","no");
            }
            // 否则算匹配当月
            else{
                jTd.attr("in-month","yes");
            }

            // 如果是范围选择，那么就不显示非本月日期了
            if(opt.blockNumber>1 && !opt.byWeek && MM != theMonth){
                jTd.attr("hide", "yes").html("&nbsp;");
            }
            // 绘制日期单元格的内容
            else{
                jTd.addClass("zcal-cell-show")
                var cellHtml = opt.cellHtml(_d_cell, jTd);
                jTd.html(cellHtml);

                // 恢复之前的选择
                var _cell_ms = _d_cell.getTime();
                if(msActived && msActived == _cell_ms){
                    jTd.addClass("zcal-cell-actived");
                }
                //console.log(_cell_ms, msChecked)
                // 多选模式
                if( "multi" == opt.mode && msMap && msMap[theKey]) {
                    jTd.addClass("zcal-cell-checked");
                }
                // 范围模式
                else if( "range" == opt.mode
                    && msChecked 
                    && _cell_ms>=msChecked[0] 
                    && _cell_ms<=msChecked[1]){
                    jTd.addClass("zcal-cell-checked");
                }
            }
            // 移动到下一天
            _d_cell.setDate(_d_cell.getDate()+1);
        }
    }

    // 返回最终绘制的日期范围
    to = _d_cell;
    to.setHours(23,59,59,999);
    return [from, to];
}
//...........................................................
function __gen_checked_map(jRoot, opt, msChecked){
    opt = opt || options(jRoot);
    msChecked = msChecked || jRoot.data(NM_CHECKED) || [];
    var msMap = {};
    for(var i=0; i<msChecked.length; i++){
        var d = $z.parseDate(msChecked[i]);
        msMap[d.format("yyyy-mm-dd")] = d;
    }
    return msMap;
}
//...........................................................
function do_blur(jRoot, opt, quit){
    var opt = opt || options(jRoot);
    var jLast = jRoot.find(".zcal-cell-actived");
    var dLast = commands.actived.call(jRoot);
    if(dLast){
        // 移除存储的激活日期
        jRoot.removeData(NM_ACTIVED);
        // 移除激活单元格的标记，并调用回调
        jLast.removeClass("zcal-cell-actived");

        // 调用回调
        if(!quit)
            $z.invoke(opt, "on_blur", [dLast], jLast);
    }
    return dLast;
}
//...........................................................
function __parse_date (obj, forceZero) {
    var d;
    if(_.isElement(obj) || $z.isjQuery(obj)){
        d = $z.parseDate($(obj).attr("key"));
    }
    // 其他的，试图解析一下
    else{
        d = $z.parseDate(obj);
    }
    if(forceZero)
        d.setHours(0,0,0,0);
    return d;
}
//...........................................................
function do_active(jRoot, obj, shiftOn){
    var opt = options(jRoot);
    // 不响应点击等默认事件
    if("none" == opt.mode)
        return;

    // 从传入的对象，获取日期
    var d = __parse_date(obj, true);

    // 找到上一个被激活的日期，并取消激活
    var dLast = do_blur(jRoot, opt, true);

    // 根据 key 找到现在应该被激活的日期，并激活
    var key = dkey(d);
    var jCell = jRoot.find(".zcal-cell-show[key="+key+"]");

    // 如果没找到，证明日历没有调整到这个日期，那么就跳转一下
    // 然后再激活
    if(jCell.size() == 0) {
        update(jRoot, opt, d);
        do_active(jRoot, d, shiftOn);
        return;
    }

    jCell.addClass("zcal-cell-actived");

    // 存储这个激活的日期
    jRoot.data(NM_ACTIVED, d.getTime());

    // 如果是范围模式
    if("range" == opt.mode){
        var dTo =  (opt.autoSelect || shiftOn) ? dLast : null;
        do_set_range(jRoot, d, dTo, opt);
    }
    // 如果是多选模式
    else if("multi" == opt.mode) {
        do_set_multi(jRoot, d, dLast, shiftOn, opt);
    }
    // 默认是单选模式，就主动调用一下回调
    else{
        var ds = [d, new Date(d)];
        ds[1].setHours(23,59,59,999);
        $z.invoke(opt, "on_range_change", ds, jRoot);
    }

    // 调用回调 
    $z.invoke(opt, "on_actived", [d, jCell], jCell);

    // 返回激活的日期
    return d;
}
//...........................................................
function do_set_multi(jRoot, d, dLast, shiftOn, opt) {
    opt = opt || options(jRoot);

    // 如果 d 直接是数组，则表示给定的激活范围
    var msChecked, msMap;
    if(_.isArray(d)){
        msChecked = [];
        for(var i=0;i<d.length;i++){
            msChecked.push($z.parseDate(d[i]));
        }
        msMap = __gen_checked_map(jRoot, opt, msChecked);
    }
    // 得到之前的激活以及范围，
    else {
        msChecked = jRoot.data(NM_CHECKED);

        // 如果是范围模式，则生成一个日期的散列，以便快速判断
        msMap = __gen_checked_map(jRoot, opt, msChecked);

        // 得到 yyyy-mm-dd
        var theKey = dkey(d);

        // 首先整理数据
        if(shiftOn) {
            // 确保有 dLast
            if(!dLast){
                dLast = $z.parseDate(jRoot.find(".zcal-cell-show").first().attr("key"));
            }
            // 确保顺序是对的
            var dFrom = d.getTime() > dLast.getTime() ? dLast : d;
            var dTo   = d.getTime() < dLast.getTime() ? dLast : d;
            // 增加到数据散列里
            for(var ams = dFrom.getTime(); ams<=dTo.getTime(); ams+=86400000){
                var theD = new Date();
                theD.setTime(ams);
                d.setHours(0,0,0,0);
                var theKey = dkey(theD);
                msMap[theKey] = theD;
            }
        }
        // 直接 Toggle
        else {
            msMap[theKey] = msMap[theKey] ? null : d;
        }

        // 排序并重新整理出 msChecked
        msChecked = [];
        for(var key in msMap) {
            var theD = msMap[key];
            if(theD)
                msChecked.push(theD);
        }
        msChecked.sort(function(a,b){
            var ams = a.getTime();
            var bms = b.getTime();
            if(ams == bms)
                return 0;
            if(ams < bms)
                return -1;
            return 1;
        });
    }

    // 保存
    jRoot.data(NM_CHECKED, msChecked);

    // 修正当前视图的显示
    jRoot.find(".zcal-cell-checked").removeClass("zcal-cell-checked");
    jRoot.find(".zcal-cell-show").each(function(){
        var jq = $(this);
        var theKey = jq.attr("key");
        if(msMap[theKey]) {
            jq.addClass("zcal-cell-checked");
        }
    });

    // 调用回调
    $z.invoke(opt, "on_multi_change", msChecked, jRoot);
}
//...........................................................
function do_set_range(jRoot, dFrom, dTo, opt){
    opt = opt || options(jRoot);
    var ds = [];
    if(_.isArray(dFrom)){
        if(dFrom.length==1){
            ds[0] = $z.parseDate(dFrom[0]);
            ds[1] = new Date(ds[0]);
        }else{
            ds[0] = $z.parseDate(dFrom[0]);
            ds[1] = $z.parseDate(dFrom[1]);
        }
    }else if(!dTo){
        ds[0] = $z.parseDate(dFrom);
        ds[1] = new Date(ds[0]);
    }else{
        ds[0] = $z.parseDate(dFrom);
        ds[1] = $z.parseDate(dTo);
    }
    // 确保 ds[0] 是小的那个
    if(ds[0].getTime() > ds[1].getTime()){
        var d = ds[0];
        ds[0] = ds[1];
        ds[1] = d;
    }
    // 确保时间正确
    ds[0].setHours(0,0,0,0);
    ds[1].setHours(23,59,59,999);

    // 获取毫秒数
    var ms_begin = ds[0].getTime();
    var ms_end   = ds[1].getTime();

    jRoot.find(".zcal-cell-checked").removeClass("zcal-cell-checked");
    jRoot.find(".zcal-cell-show").each(function(){
        var jq   = $(this);
        var theD = $z.parseDate(jq.attr("key")+"T00:00:00");
        var ms   = theD.getTime();
        if(ms>=ms_begin && ms<=ms_end){
            jq.addClass("zcal-cell-checked");
        }
    });

    // 存储这个日期范围
    jRoot.data(NM_CHECKED, [ms_begin, ms_end]);

    // 调用回调
    $z.invoke(opt, "on_range_change", ds, jRoot);

}
//...........................................................
function _count_size(sz, base, n){
    // 均分
    if("*" == sz){
        return parseInt(base/n);
    }
    // 否则根据基数计算 
    return $z.dimension(sz, base);
}
//...........................................................
function do_resize(jRoot, opt){
    opt = opt || options(jRoot);

    // 计算选区的宽高
    var jHead      = jRoot.children(".zcal-head");
    var jSelection = jRoot.parent();
    var jWrapper   = jRoot.find(".zcal-wrapper");
    var W = jSelection.width();
    var H = jSelection.height() - jHead.outerHeight(true);

    // 计算每个块的高度
    var bW = _count_size(opt.blockWidth,  W, opt.blockNumber);
    var bH = _count_size(opt.blockHeight, H, opt.blockNumber);

    // 设置
    jRoot.find(".zcal-block").css({
        "width"  : bW,
        "height" : bH
    });

    // 如果声明了resize的回调
    if(_.isFunction(opt.on_cell_resize)){
        jRoot.find(".zcal-cell-show").each(opt.on_cell_resize);
    }
}
//...........................................................
$.fn.extend({ "zcal" : function(opt, arg){
    // 命令
    if(_.isString(opt)){
        return commands[opt].call($root(this), arg);
    }
    // 默认配置必须为对象
    opt = _.extend({}, {
        weeks : 0,
        range       : "default",
        autoSelect  : false,
        simpleCell : true,
        blockNumber : 1,
        blockWidth  : "*",
        i18n: {
            month : ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
            week  : ["S","M","T","W","T","F","S"]
        },
        swticher : {
            today : "Today",
            prev  : '&lt;',
            next  : '&gt;',
        },
        drawWhenCreate : true,
        cellHtml  : function(d){
            var opt      = this;
            var theDate  = d.getDate();
            var theMonth = d.getMonth();
            if(opt.markMonthFirstDay && theDate==1 && theMonth!=opt.current.getMonth()){
                return '<div class="zcal-text"><div class="zcal-d1mark">' + opt.i18n.month[theMonth] + '</div></div>';
            }
            return '<div class="zcal-text">' + d.getDate() + '</div>';
        },
        markMonthFirstDay : true
    }, opt);

    // 默认，根据每块的行，觉得块的高度
    if(!opt.blockHeight) {
        opt.blockHeight = 36 * (opt.byWeek || 6);
    }

    // 重绘基础 dom 结构，同时这会保存配置信息
    var jRoot = redraw(this, opt);

    // 绘制 dom
    bindEvents(jRoot, opt);

    // 更新
    if(opt.drawWhenCreate)
        update(jRoot, opt);

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);




