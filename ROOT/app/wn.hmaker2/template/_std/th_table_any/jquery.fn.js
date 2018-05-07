(function($, $z){
//..........................................................
$.fn.extend({ "_std_th_table_any" : function(list, opt){
    var jData = this;

    // 判断当前环境是否是 IDE
    var isIDE = this.closest('html[hmaker-ide]').length > 0;

    // 默认字段
    $z.setUndefined(opt, "flds", "th_nm|名称|未设置");

    // 解析字段
    var flds = $z.parseLine(opt.flds, "|", 
        ['key', 'text', 'dft', 'display'], 
        function(fld){
            // 显示隐藏字段等
            if(fld.key && /^-/.test(fld.key)) {
                fld.hideWhenMobile = true;
                fld.key = $.trim(fld.key.substring(1));
            }
            // 高级的 display
            if(fld.display) {
                var ts = fld.display;
                // 日期时间: Date("yyyy-MM-dd")
                var m = /^Date\(([^)]*)\)$/.exec(ts);
                if(m){
                    fld.config = m[1];
                    fld.display = function(obj){
                        var v = obj[this.key];
                        var d = $z.parseDate(v);
                        return d.format(this.config || "yyyy-mm-dd");
                    };
                    return;
                }
                // 尺寸: 100/?=Size(2)
                var m = /^Size\(([0-9]*)\)$/.exec(ts);
                if(m){
                    fld.config  = m[1]? parseInt(m[1]) : 2;
                    fld.display = function(obj){
                        var v = obj[this.key] * 1;
                        if(!v)
                            return "0Byte";
                        return $z.sizeText(val, this.config);
                    };
                    return;
                }
                // 映射: {A:"猫",B:"狗"}
                if(/^\{.+\}$/.test(ts)) {
                    fld.config = $z.fromJson(ts);
                    fld.display = function(obj){
                        var v = obj[this.key];
                        return this.config[v] || v;
                    };
                    return;
                }
                // 默认就是一个模板咯
                fld.display = $z.tmpl(fld.display);
            }
            // 默认就是显示字段咯
            else {
                fld.display = function(obj) {
                    return obj[this.key] || this.dft || "--";
                };
            }
    });
    
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
            $('<th>').attr({
                "m"        : "fld",
                "fld-key"  : fld.key,
                "hide-when-mobile" : fld.hideWhenMobile ? "yes" : null,
            }).text(fld.text || fld.key).appendTo(jTr);
        }
    }

    // 准备循环变量
    var target = opt.newtab ? "_blank" : null;
    
    // 循环数据
    var jTbody = $('<tbody>').appendTo(jTable);
    for(var i=0; i<list.length; i++) {
        jTr = $('<tr>').appendTo(jTbody);
        var obj = list[i];

        // 编号行
        if("nb" == opt.rowNumber) {
            $('<td m="nb">').text(i+1).appendTo(jTr);
        }
        
        // 显示每个数据字段
        for(var x=0; x<flds.length; x++) {
            var fld = flds[x];
            var str = $z.escapeText(fld.display(obj), true);
            var o_href = "";
            
            // 第一行才可能加链接
            if(x == 0) {
                var href = HmRT.explainHref(opt.href, obj, isIDE);
                if(href){
                    var taId = obj.id;
                    var panm = opt.paramName || "id";
                    o_href = href + "?" + panm + "=" + taId;
                }
            }

            // 生成文字对象
            var jTx = o_href 
                        ? $('<a>').attr({target:target, href: o_href})
                        : $('<span>');
            jTx.html(str || fld.dft || "&nbsp;");

            // 加入 DOM
            $('<td>').attr({
                "m"        : "fld",
                "fld-key"  : fld.key,
                "hide-when-mobile" : fld.hideWhenMobile ? "yes" : null,
            }).append(jTx).appendTo(jTr);
        }
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);