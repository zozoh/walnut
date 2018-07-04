(function($, $z){
//..........................................................
$.fn.extend({ "_std_th_table_obj" : function(list, opt){
    var jData = this;

    // 判断当前环境是否是 IDE
    var isIDE = this.closest('html[hmaker-ide]').length > 0;

    // 默认字段
    $z.setUndefined(opt, "flds", ".th_nm");

    // 解析字段
    var layout = HmRT.parseLayout(opt.flds);
    //console.log(layout)
    var flds = layout.data;
    
    // 生成表格
    var jTable = $('<table>').appendTo(jData);
    var jTr;

    // 生成表格标题
    if(opt.caption) {
        $('<caption>').text(opt.caption).appendTo(jTable);
    }

    // 生成表头
    if(opt.showTHead) {
        var jThead = $('<thead>').appendTo(jTable);
        jTr = $('<tr>').appendTo(jThead);
        // 编号行
        if("nb" == opt.rowNumber) {
            $('<th>').html("&nbsp;").appendTo(jTr);
        }
        // 逐个输出表头
        for(var i=0; i<flds.length; i++) {
            var fld = flds[i];
            var jTh = $('<th>').attr({
                "m"        : "fld",
                "fld-key"  : fld.key,
                "hide-when-mobile" : fld.w_mobile=="hidden" ? "yes" : null,
            }).text(fld.title || fld.key).appendTo(jTr);
        }
    }

    // 准备循环变量
    var target = opt.newtab ? "_blank" : null;
    
    // 循环数据
    var jTbody = $('<tbody>').appendTo(jTable);
    for(var i=0; i<list.length; i++) {
        jTr = $('<tr>').appendTo(jTbody);
        var obj = list[i];

        // 指定链接（包括封面图片和标题）
        var href = HmRT.explainHref(opt.href, obj);

        // 首先解析一下链接
        var oHref = HmRT.prepareHrefObj(href, obj);

        // 编号行
        if("nb" == opt.rowNumber) {
            $('<td m="nb">').text(i+1).appendTo(jTr);
        }
        
        // 显示每个数据字段
        for(var x=0; x<flds.length; x++) {
            var fld = flds[x];
                        
            // 准备单元格
            var jTd = $('<td>').attr({
                "m"        : "fld",
                "fld-key"  : fld.key,
                "hide-when-mobile" : fld.w_mobile=="hidden" ? "yes" : null,
            });

            if(fld.selector){
                jTd.addClass(fld.selector);
            }

            // 渲染单元格内容
            HmRT.renderLayoutField(opt, jTd, fld, obj, oHref);

            // 加入行
            jTd.appendTo(jTr);
        }
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);