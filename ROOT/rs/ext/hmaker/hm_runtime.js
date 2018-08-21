/**
提供运行时的帮助函数集合，IDE 也会用的到
*/
(function($, $z){
//===================================================================
function __set_layout_item_size (it, s) {
    var ls = $z.parseLayoutSize(s);
    it.w_desktop = ls.desktop;
    it.w_mobile  = ls.mobile;
    return it;
}
//===================================================================
// 选择器支持模板的写法
function __parse_selector(s) {
    var re = $z.parseLayoutSize(s, null);
    for(var key in  re) {
        re[key] = $z.tmpl(re[key]);
    }
    return re;
}
//===================================================================
window.HmRT = {
    //...............................................................
    __parse_layout_fld : function(line) {
        if(!line)
            return;

        //console.log(line)

        // 字段
        var m = /^([.>!])([0-9a-zA-Z_|]+)(<(.+)>)?(\[([+-])\])?(:([^=]+))?(=([^#]+))?(#(.+))?$/.exec(line);
        if(m) {
            // 分析正则表达式
            var m_pp  = m[1];       // `.>!` 表示字段的显示属性，是普通，标题，还是必显
            var m_key = m[2];
            var m_sel = m[4];
            var m_lnk = m[6];
            var m_sz  = m[8];
            var m_tp  = m[10];
            var m_tt  = m[12];      // 当字段作为表格时使用。譬如 th_table_obj 会用到

            // 得到键并创建字段对象
            var fkey = m_key.split("|");
            var fld = {
                type    : "field", 
                display : "String", 
                key     : fkey.length>1 ? fkey : fkey[0]
            };
            //console.log(fld)
            // 标识标题
            fld.isTitle = m_pp == ">";
            fld.show = m_pp=="!" ? "always" : "auto";
            // 选择器
            if(m_sel){
                fld.selector = __parse_selector(m_sel);
            }
            // 链接
            if(m_lnk){
                fld.linkTarget = "+"==m_lnk ? "_blank" : "_self";
            }
            // 尺寸
            if(m_sz){
                if(!__set_layout_item_size(fld, m_sz))
                    return;
            }
            // 标题
            if(m_tt) {
                fld.title = m_tt;
            }

            // 获取值的函数
            fld.getText = function(obj){
                var val;
                var ks = _.isArray(this.key) ? this.key : [this.key];
                for(var i=0; i<ks.length; i++){
                    val = $z.getValue(obj, ks[i]);
                    if(!_.isNull(val) && !_.isUndefined(val)){
                        break;
                    }
                }
                //console.log(val)
                // 空值
                if(_.isNull(val) || _.isUndefined(val))
                    return val;
                // .th_cate={A:"猫",B:"狗"}
                if("Mapping" == this.display && this.config) {
                    return this.config[val] || val;
                }
                // .th_birthday=Date(yyyy-mm-dd)
                if("Date" == this.display) {
                    var s = "N/A";
                    if(val) {
                        try{
                            var d = $z.parseDate(val);
                            s = d.format(this.config || "yyyy-mm-dd");
                        }
                        // 解析失败就用原来的值
                        catch(E) {
                            s = val;
                        }
                    }
                    return s;
                }
                // .len:100/?=Size(2)
                if("Size" == this.display) {
                    var s = "0Byte";
                    if(val) {
                        s = $z.sizeText(val, this.config);
                    }
                    return s;
                }
                // 默认直接返回咯
                return val;
            };

            // 类型
            if(m_tp) {
                var ts = m_tp;
                // Markdown
                if("Markdown" == ts){
                    fld.display = "Markdown";
                    return fld;
                }
                // Thumbnail
                if("Preview" == ts){
                    fld.display = "Preview";
                    return fld;
                }
                // Em
                if("Em" == ts){
                    fld.display = "Em";
                    return fld;
                }
                // Block
                if("Block" == ts){
                    fld.display = "Block";
                    return fld;
                }
                // 映射: .th_cate={A:"猫",B:"狗"}
                if(/^\{.+\}$/.test(ts)) {
                    fld.display = "Mapping";
                    fld.config = $z.fromJson(ts);
                    return fld;
                }
                // List
                var m2 = /^List(\(([^)]*)\))?(\[([^)]*)\])?$/.exec(ts);
                if(m2) {
                    fld.display = "List";
                    fld.config = {
                        sep    : m2[2] || /\r?\n/,
                        joinBy : m2[4] || ", ",
                    };
                    if(fld.config.sep) {
                        fld.config.sep = new RegExp(fld.config.sep);
                    }
                    return fld;
                }
                // 日期时间: .th_birthday=Date("yyyy-mm-dd")
                m2 = /^Date\(([^)]*)\)$/.exec(ts);
                if(m2){
                    fld.display = "Date";
                    fld.config = m2[1];
                    return fld;
                }
                // 日期范围: .th_section=DateRange(yyyy-mm-dd)[the_range,nm]
                m2 = /^DateRange\(([^|]+)[|]([^|]+)[|]([^\)]+)\)(\[([^,]+)(,(.+)?)?\])?("([^"]+)")?$/.exec(ts);
                if(m2){
                    fld.display = "DateRange";
                    fld.config = {
                        dfmt_yy  : m2[1] || "yyyy-mm-dd",
                        dfmt_mm  : m2[2] || "mm-dd",
                        dfmt_dd  : m2[3] || "dd",
                        rangeKey : m2[5],
                        textKey  : m2[7],
                        tmpl : $z.tmpl(m2[9]||"{{from}} -> {{to}}")
                    };
                    return fld;
                }
                // 尺寸: .len:100/?=Size(2)
                m2 = /^Size\(([0-9]*)\)$/.exec(ts);
                if(m2) {
                    fld.display = "Size";
                    fld.config  = m2[1]? parseInt(m2[1]) : 2;
                    return fld;
                }
                // 链接: .href=Link(Buy Now)
                // 按钮: .href=Button(Buy Now)
                m2 = /^(Link|Button)(\(([^)]*)\))?(->(.+))?$/.exec(ts);
                if(m2) {
                    fld.display = m2[1];
                    var text = $.trim(m2[3]);
                    var href = $.trim(m2[5]);
                    fld.config  = {
                        linkText : text ? $z.tmpl(text) : null,
                        linkHref : href ? $z.tmpl(href) : null
                    };
                    $z.setUndefined(fld, "linkTarget", "_self");
                    return fld;
                }
                // 列表: .lbls:100=UL(text)->.thumb
                m2 = /^UL(\(((!(media|attachment|img):)?([^)]*))\))?([-][>](.+))?$/.exec(ts);
                if(m2) {
                    fld.display = "UL";
                    fld.config = {
                        itemType : m2[4] || "text",
                        target   : m2[7],
                    };
                    var itKey = m2[5];
                    if(/^=/.test(itKey)) {
                        fld.config.itemTmpl = $z.tmpl(itKey.substring(1));
                    } else {
                        fld.config.itemKey = itKey;
                    }
                    return fld;
                }
            }
            // 普通字段咯
            return fld;
        }
        // 那么看看是否是一个 HR
        else if(/^[-]{3,}$/.test(line)) {
            return {type:"hr"};
        }
        // 默认就是静态文字咯
        return {type:"text", value:line};
    },
    //...............................................................
    // @see doc/ext/hmaker/hm_layout.md 《字段布局语法》
    parseLayout : function(str) {
        // 准备返回结果
        var layout = {data:[]};

        // 拆分行
        var lines = str.split(/\r?\n/g);
        
        // 逐行解析
        var grp, m, fld;
        for (var i = 0; i < lines.length; i++) {
            var line = $.trim(lines[i]);

            // 忽略空行
            if(!line)
                continue;
            //console.log(line)
            // 强制退出组
            if("~~~" == line) {
                if(grp) {
                    // 顶级组直接加入
                    if(!grp.__parent) {
                        layout.data.push(grp);
                    }
                    // 当前组指向父组，如果是顶级组，则自然是 undefined 咯
                    grp = grp.__parent;
                }
                // 嗯，继续下一行吧
                continue;
            }

            // 组
            m = /^@(\((.+)\))?(<(.+)>)?(\[([+-])\])?(.+)?/.exec(line)
            if(m) {
                // 分析正则表达式
                var m_gnm = m[2];
                var m_sel = m[4];
                var m_lnk = m[6];
                var m_sz  = m[7];

                // 建立新组
                var grp2 = __set_layout_item_size({
                        type : "group", 
                        name : $.trim(m_gnm) || null,
                        selector : __parse_selector(m_sel),
                        linkTarget : "+" == m_lnk 
                                        ? "_blank" 
                                        : (m_lnk ? "_self" : undefined),
                        items:[]
                    }, m_sz);

                // 加入当前组
                if(grp) {
                    grp2.__parent = grp;
                    grp.items.push(grp2);
                }
                grp = grp2;
                continue;
            }

            // 表格
            m = /^T(\((.+)\))?(<(.+)>)?(.+)?/.exec(line)
            if(m) {
                // 分析正则表达式
                var m_tnm = m[2];
                var m_sel = m[4];
                var m_sz  = m[5];

                // 建立表格
                var tbl = __set_layout_item_size({
                        type:"table", 
                        name:$.trim(m_tnm) || null,
                        selector : __parse_selector(m_sel),
                        rows:[]
                    }, m_sz);
                for (i++; i < lines.length; i++) {
                    line = $.trim(lines[i]);
                    m = /^-([^|]+)[|](.+)$/.exec(line);
                    if(m) {
                        var s2 = $.trim(m[2]);
                        fld = this.__parse_layout_fld(s2);
                        if(fld) {
                            tbl.rows.push({
                                title : $.trim(m[1]),
                                value : fld,
                            });
                        }
                    }
                    // 不是行了，退出
                    else {
                        i--;
                        break;
                    }
                }
                // 加入
                if(tbl.rows.length > 0) {
                    if(grp)
                        grp.items.push(tbl);
                    else
                        layout.data.push(tbl);
                }
                continue;
            }

            // HTML 片段
            m = /^(>)? *HTML(\((Block|Em)\))?(<(.+)>)?(\[([+-])\])?([^=]+)?(=(.+))$/.exec(line)
            if(m) {
                var fHTML = __set_layout_item_size({
                        type       : "HTML", 
                        isTitle    : m[1] == ">",
                        display    : m[3] || "String",
                        selector   : __parse_selector(m[5]), 
                        tmpl       : $z.tmpl(m[10]),
                        config     : {},
                    }, m[8]);
                // 链接
                if(m[7]){
                    fHTML.linkTarget = "+"==m[7] ? "_blank" : "_self";
                }

                for (i++; i < lines.length; i++) {
                    line = $.trim(lines[i]);
                    m = /^-([^:]+)[:](.+)$/.exec(line);
                    if(m) {
                        var sk = $.trim(m[1]);
                        var s2 = $.trim(m[2]);
                        fld = this.__parse_layout_fld(s2);
                        if(fld) {
                            fHTML.config[sk] = fld;
                        }
                    }
                    // 不是行了，退出
                    else {
                        i--;
                        break;
                    }
                }
                // 加入
                if(grp)
                    grp.items.push(fHTML);
                else
                    layout.data.push(fHTML);
                continue;
            }

            // 字段
            fld = this.__parse_layout_fld(line);
            if(fld) {
                if(grp)
                    grp.items.push(fld);
                else
                    layout.data.push(fld);
            }
            
        }

        // 推入最后一组所在的父组
        if(grp) {
            while(grp.__parent) {
                grp = grp.__parent;
            }
            layout.data.push(grp);
        }

        // console.log(layout)
        // 返回结果
        return layout;    
    },
    //...............................................................
    prepareHrefObj : function(href, obj) {
        if(href) {
            href = decodeURI(href);
            // hrefVMap = {};
            // var vmREG = /\{\{(.+)\}\}/g;
            // var m;
            // while(m=vmREG.exec(hrefs)){
            //     var key = m[1];
            //     hrefVMap[key] = obj[key] || "";
            // }
            // 得到 OBJ HREF
            var hrefTmpl = $z.tmpl(href);
            var result = obj ? href : null;
            if(obj)
                try{
                    result = hrefTmpl(obj);
                }catch(E){}
            return {
                tmpl   : hrefTmpl,
                obj    : obj,
                result : result,
            };
        }
        // 确保是假，且还不是undefined
        return "";
    },
    //...............................................................
    // 渲染字段内容
    renderLayoutFieldElementContent : function(fld, str, oHref, forceHTML) {
        var jFi;
        //console.log("abc")
        // 已经准备好了内容
        if($z.isjQuery(str)) {
            jFi = str;
        }
        // Em
        else if("Em" == fld.display) {
            jFi = $('<em>').text(str);
        }
        // Block
        else if("Block" == fld.display) {
            jFi = $('<div>').text(str);
            jFi = $('<blockquote>').append(jFi);
        }
        // .lbls=List(\r\n)[,]
        else if("List" == fld.display) {
            if(str && !_.isArray(str)){
                str = str.split(fld.config.sep);
            }
            jFi = $('<span>');
            for(var i=0; i<str.length; i++) {
                var s = str[i];
                if(i>0) {
                    $('<em>').text(fld.config.joinBy).appendTo(jFi);
                }
                $('<u>').text(s).appendTo(jFi);
            }
        }
        // 用 HTML
        else if(forceHTML){
            jFi = $('<span>').html(str);
        }
        // 普通文字咯
        else {
            jFi = $('<span>').text(str);
        }
        // 如果是链接的话，包裹一层
        if((fld.linkTarget && oHref)
            || 'Button' == fld.display
            || 'Link'   == fld.display) {
            var href = null;
            // 普通字符串
            if(oHref && _.isString(oHref)) {
                href = oHref;
            }
            // 是链接对象
            else if(oHref && _.isFunction(oHref.tmpl)) {
                href = oHref.tmpl(oHref.obj || {});
            }
            // 生成 Dom 节点
            jFi = $('<a>').attr({
                    "href"   : href,
                    "target" : "_blank" == fld.linkTarget && href 
                                    ? "_blank" : null,
                }).append(jFi);
        }
        // 如果是按钮的话，包裹一层
        if('Button' == fld.display) {
            jFi = $('<b>').append(jFi);
        }
        // 如果是标题的话，再包裹一层
        if(fld.isTitle) {
            return $('<h4>').append(jFi);
        }
        return jFi;
    },
    //...............................................................
    // 渲染字段元素
    renderLayoutFieldElement : function(obj, fld, str, oHref, forceHTML) {
        var jFld;
        //----------------------------------
        // 已经是弄好的东东了
        if($z.isjQuery(str)) {
            jFld = str;
        }
        // 否则当做文本搞一下
        else {
            jFld = this.renderLayoutFieldElementContent(fld, str, oHref, forceHTML);
        }
        // 增加类选择器
        if(fld.selector) {
            jFld.attr({
                "layout-desktop-selector" : fld.selector.desktop(obj) || "",
                "layout-mobile-selector"  : fld.selector.mobile(obj)  || ""
            });
        }
        // 增加一下属性
        return jFld.attr({
            "fld-type"             : fld.type || "text",
            "fld-display"          : fld.display,
            "fld-key"              : fld.key,
            "layout-desktop-width" : fld.w_desktop,
            "layout-mobile-width"  : fld.w_mobile,
        });
    },
    //----------------------------------
    // 渲染字段
    renderLayoutField : function(opt, jP, fld, obj, oHref) {
        //console.log(fld)
        var theHref = oHref ? oHref.result : null;
        // 普通文字
        if('text' == fld.type) {
            //return $('<em class="wn-obj-text">').text(fld.value).appendTo(jP);
            return this.renderLayoutFieldElement(obj, fld, fld.value).appendTo(jP);
        }
        // HR
        else if('hr' == fld.type) {
            return $('<div class="wn-obj-hr"><hr></div>').appendTo(jP);
        }

        // 表格
        if('table' == fld.type) {
            var jTable = $('<table>').attr("table-name", fld.name||null);
            for(var i=0; i<fld.rows.length; i++) {
                var row = fld.rows[i];
                if(row.value) {
                    var jTr = $('<tr>').appendTo(jTable);
                    $('<td class="tbl-col-name">')
                        .html(row.title)
                            .appendTo(jTr);
                    var jTd = $('<td class="tbl-col-value">')
                                .appendTo(jTr);
                    this.renderLayoutField(opt, jTd, row.value, obj, oHref);
                }
            }
            return this.renderLayoutFieldElement(obj, fld, jTable).appendTo(jP);
        }
        // HTML
        else if('HTML' == fld.type) {
            // 准备上下文
            var map = _.extend({}, obj);

            // 替换一下
            if(fld.config) {
                for(var key in fld.config) {
                    var f2 = fld.config[key];
                    map[key] = f2.getText(obj);
                }
            }

            // 渲染
            var html = fld.tmpl(map);

            // 加入DOM
            return this.renderLayoutFieldElement(obj, fld, html, oHref, true).appendTo(jP);
        }

        // 动态值，获取一下
        var val  = fld.getText(obj);

        // 这个比较优先，因为无论如何都要显示出预览区嘛
        // .thumb=Preview
        if("Preview" == fld.display) {
            //console.log(fld)
            var jThumb;
            // 缩略图
            if(val) {
                jThumb = $('<div class="wn-obj-preview">');
                var jPr;
                if(theHref && fld.linkTarget) {
                    jPr = $('<a>').appendTo(jThumb).attr({
                        "href": theHref,
                        "target" : "_blank" == fld.linkTarget ? "_blank" : null
                    });
                }
                else {
                    jPr = $('<span>').appendTo(jThumb);
                }
                $('<img>').attr({
                    "src" : opt.API + "/thumb?" + val
                }).appendTo(jPr);
            }
            // 仅仅显示一个空的产品图标
            else {
                if(theHref) {
                    jThumb = $('<div class="wn-obj-preview" empty="yes">')
                        .html('<a><i class="fas fa-image"></i></a>');
                    jThumb.find('a').attr("href", theHref);
                }
                // 木有链接
                else {
                    jThumb = $('<div class="wn-obj-preview" empty="yes">')
                        .html('<span><i class="fas fa-image"></i></span>');
                }
            }
            // 搞定返回
            return this.renderLayoutFieldElement(obj, fld, jThumb).appendTo(jP);
        }

        // 无效的值无视
        if("always" != fld.show)
            if(_.isNull(val) || _.isUndefined(val))
                return null;

        // 日期范围
        if('DateRange' == fld.display) {
            var jRange = $('<div class="wn-obj-daterange">');
            var jDrCon = $('<div class="wn-obj-drcon">').appendTo(jRange);
            if(_.isArray(val) && val.length > 0) {
                for(var i=0; i<val.length; i++) {
                    var v = val[i];
                    var dr_range, dr_text;
                    // 直接使用范围
                    if(_.isArray(v) && v.length == 2) {
                        dr_range = v;
                    }
                    // 从对象里获取
                    else if(fld.config.rangeKey) {
                        dr_range = v[fld.config.rangeKey];
                    }
                    // 得到显示文本
                    dr_text = v[fld.config.textKey];

                    // 计算日期范围
                    var d1 = $z.parseDate(dr_range[0]);
                    var d2 = $z.parseDate(dr_range[1]);
                    // 开始日期为完整日期
                    var ds1 = d1.format(fld.config.dfmt_yy);
                    
                    // 简约化结束日期
                    var ds2;
                    // 同年
                    if(d1.getYear() == d2.getYear()) {
                        // 同月
                        if(d1.getMonth() == d2.getMonth()) {
                            ds2 = d2.format(fld.config.dfmt_dd);
                        }
                        // 不同月
                        else {
                            ds2 = d2.format(fld.config.dfmt_mm);    
                        }
                    }
                    // 不同年
                    else {
                        ds2 = d2.format(fld.config.dfmt_yy);
                    }
                    

                    // 输出 DOM
                    var jUl = $('<ul>').appendTo(jDrCon);
                    if(dr_text){
                        $('<li class="dr-text">').text(dr_text).appendTo(jUl);
                    }
                    // 输出日期范围
                    $('<li class="dr-range">').html(fld.config.tmpl({
                            "from" : ds1,
                            "to"   : ds2
                        })).appendTo(jUl);
                }
            }
            // 搞定返回
            return this.renderLayoutFieldElement(obj, fld, jRange).appendTo(jP);
        }

        // .th_cate={A:"猫",B:"狗"}
        // .th_birthday=Date(yyyy-mm-dd)
        // .len:100/?=Size(2)
        if(/^(Mapping|Date|Size|List)$/.test(fld.display)) {
            return this.renderLayoutFieldElement(obj, fld, val, theHref).appendTo(jP);
        }
        
        // .lbls:100=UL(!image:src)->thumb
        if("UL" == fld.display) {
            // console.log("UL", fld);
            var liType = fld.config.itemType || "text";
            var jUl  = $('<ul>').attr({
                    "li-type"   : liType,
                    "li-img"    : /^(media|attachment|img)$/.test(liType)
                                    ? "yes" : null  
                });
            // 分析一下目标是不是指定的下载链接
            var m_down = /^([+]?)(media|attachment)$/.exec(fld.config.target);

            // 如果不是下载，那么就是连接咯
            if(fld.config.target && !m_down) {
                jUl.attr('li-target', fld.config.target);
            }

            if(!_.isNull(val) && !_.isUndefined(val)) {
                var list   = _.isArray(val)?val:[val];
                for(var i=0; i<list.length; i++) {
                    var li  = list[i];
                    var jLi = $('<li>').attr({
                                "current" : li.current ? "yes" : null
                            }).appendTo(jUl);
                    // 保存数据
                    jLi.data("@LI-DATA", li);

                    //--------------------
                    // 得到真实的值
                    var liv = li;
                    if(fld.config.itemTmpl) {
                        liv = fld.config.itemTmpl(li);
                    }
                    // 用 key
                    else if(fld.config.itemKey) {
                        liv = li[fld.config.itemKey];
                    }
                    //--------------------
                    // 无视无效的值
                    if(!liv)
                        continue;
                    //--------------------
                    // 计算链接
                    var liHref = null;
                    // 下载
                    if(m_down) {
                        var dirName = m_down[2];
                        liHref = opt.API + "/thing/" + dirName 
                                    + '?pid=' + obj.th_set
                                    + '&id='  + obj.id
                                    + '&fnm=' + li.nm
                                    + '&d=true';
                        fld.linkTarget = '+' == m_down[1] ? "_blank" : '_self';
                    }
                    // 全局链接
                    else if(oHref && fld.linkTarget) {
                        try{
                            liHref = oHref.tmpl(li);
                        }catch(E){}
                    }
                    //--------------------
                    // IMG
                    if("img" == liType) {
                        this.renderLayoutFieldElementContent(fld, $('<span>').css({
                                "background-image" : 'url("' + liv + '")',
                            }), liHref).appendTo(jLi);
                    }
                    //--------------------
                    // Media/Image
                    else if( /^(media|attachment)$/.test(liType)) {
                        // 首先得到图片的源
                        //console.log(li)
                        // 嗯，稍微记录一下，事件里面用的到
                        li.liType = liType;
                        li.th_set = obj.th_set;
                        li.th_id  = obj.id;
                        // 得到缩略图链接
                        var src = opt.API + "/thumb?" + li.thumb
                        // 然后输出这个图片
                        this.renderLayoutFieldElementContent(fld, $('<span>').css({
                                "background-image" : 'url("' + src + '")',
                            }), liHref).appendTo(jLi);
                    }
                    //--------------------
                    // 普通文字
                    else {
                        this.renderLayoutFieldElementContent(fld, liv, liHref)
                            .appendTo(jLi);
                    }
                }
            }
            return this.renderLayoutFieldElement(obj, fld, jUl).appendTo(jP);
        }
        // .content=Markdown
        if("Markdown" == fld.display) {
            // 解析媒体的回调
            var formatMedia = function(src){
                var obj = this;
                // 看看是否是媒体
                var m = /^(media|attachment)\/(.+)$/.exec(src);
                //console.log(m)
                if(m){
                    var th_set = obj.go_detail_th_set || obj.th_set;
                    var th_id  = obj.go_detail_th_id  || obj.id;
                    return opt.API + "/thing/"+m[1]
                            + "?pid=" + th_set
                            + "&id="  + th_id
                            + "&fnm=" + m[2];
                }
                // 原样返回
                return src;
            };
            // 转换 markdown 内容
            var jAr = $('<article class="md-content">')
                .html($z.markdownToHtml(val||"", {
                    media : formatMedia,
                    context : obj,
                }));
            // 标识标题
            jAr.find("h1,h2,h3,h4,h5,h6").addClass("md-header");

            // 解析一下海报
            jPoster = jAr.find('pre[code-type="poster"]');
            $z.explainPoster(jPoster, {
                media : formatMedia,
                context : obj,
            });

            // 处理一下视频
            // $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
            //     watchClick : $('html[hmaker-runtime]').size() > 0
            // });

            // 搞定返回
            return this.renderLayoutFieldElement(obj, fld, jAr).appendTo(jP);
        }
        // .href=Link[Buy Now]
        if("Link" == fld.display || "Button" == fld.display) {
            var s = fld.config || val || fld.display;
            var lnkText, lnkHref;
            // 就是 =Link()，那么采用全局链接模板
            if(!fld.config.linkHref) {
                lnkText = "Link";
                lnkHref = null;
                // 指定了全局模板
                if(_.isoHref) {

                }
            }
            // 指定了链接 =Link()->/path/to/target
            //...................................
            // 处理链接文字
            var lnkText = "Link";
            if(_.isFunction(fld.config.linkText)) {
                try {
                    lnkText = fld.config.linkText(val || {});
                }catch(E){}
            }
            //...................................
            // 处理链接目标
            var lnkHref = null;
            try {
                // 直接自定了一个固定链接
                if(_.isString(val)) {
                    lnkHref = val;
                }
                // 应该是自定义的动态链接
                else if(val && _.isFunction(fld.config.linkHref)) {
                    lnkHref = fld.config.linkHref(val);
                }
                // 用全局模板渲染的动态链接
                else if(val && oHref && _.isFunction(oHref.tmpl)) {
                    lnkHref = oHref.tmpl(val);
                }
            }
            // 容忍一下错误
            catch(E) {
                lnkHref = "!!!" + E;
            }
            
            // 渲染字段
            return this.renderLayoutFieldElement(obj, fld, lnkText, lnkHref).appendTo(jP);
        }
        // 默认就是文字咯
        return this.renderLayoutFieldElement(obj, fld, val, theHref).appendTo(jP);
    },
    //...............................................................
    renderLayoutDataItem : function(opt, jP, it, obj, oHref) {
        // 字段组
        if('group' == it.type) {
            var jGrp;
            if(oHref && it.linkTarget) {
                jGrp = $('<a>').attr({
                    "href"   : oHref.result,
                    "target" : it.linkTarget
                });
            }
            // 那么没有链接，就用一个块元素咯
            else {
                jGrp = $('<section>');
            }
            // 循环渲染字段
            for(var x=0; x<it.items.length; x++) {
                var fld  = it.items[x];
                this.renderLayoutDataItem(opt, jGrp, fld, obj, oHref);
            }
            // 设置属性，并加入 DOM
            jGrp.attr({
                "hm-layout-grp" : "yes",
                "group-name" : it.name,
                "layout-desktop-width" : it.w_desktop || "",
                "layout-mobile-width"  : it.w_mobile || "",
            });
            var sel = it.selector;
            if(sel) {
                jGrp.attr({
                    "layout-desktop-selector" : sel.desktop(obj) || "",
                    "layout-mobile-selector"  : sel.mobile(obj)  || ""
                }); 
            }

            
            jGrp.appendTo(jP);
        }
        // 字段
        else {
            this.renderLayoutField(opt, jP, it, obj, oHref)
        }
    },
    //...............................................................
    // 将 parseLayout 的结果宣传成 DOM 结构
    //  - jq     : 将结果渲染到的选区
    //  - layout : @see parseLayout 返回的结果
    //  - obj    : 需要渲染的对象
    //  - href   : 在需要的地方，放置的链接
    renderLayout : function(opt, jq, layout, obj, href) {
        // 首先解析一下链接
        var oHref = this.prepareHrefObj(href, obj);
        
        // 准备父元素
        var jLayout = $('<div class="wn-obj-layout">');
        
        // 开始生成
        for(var i=0; i<layout.data.length; i++) {
            var it = layout.data[i];
            this.renderLayoutDataItem(opt, jLayout, it, obj, oHref);
        }
        //----------------------------------
        // 保护一下所有的
        jLayout.find('a[href]').click(function(e){
            // 如果切换按钮在组内，且组还有一个 href，那就无穷循环了
            //   ：因为点击会触发 a 的行为，刷新页面后，又会自动触发第一个 li 的 click
            //   : 没完没了了
            // 因此这里，发现这种情况，禁止触发 a.href
            if($(e.target).closest('ul[li-target]').length > 0) {
                e.preventDefault();
            }
        });
        //----------------------------------
        // 返回
        return jLayout.appendTo(jq);
    },
    //...............................................................
    setupLayoutEvents : function(opt, win) {
        win = win || window;
        //console.log(opt)
        if(!win.__layout_event_binded) {
            $(win.document.body).on("click", ".wn-obj-layout ul[li-target] li",
                function(e){
                    e.stopPropagation();
                    var jLi = $(this);
                    var jUl = jLi.closest("ul");
                    var jTa = jUl.closest(".wn-obj-layout");
                    var jPr = jTa.find(".wn-obj-preview span");

                    // 分析数据准备媒体源
                    var li   = jLi.data("@LI-DATA");
                    //console.log(li)
                    var mime = li.mime;
                    var src  = opt.API
                                + "/thing/"+li.liType+"?pid=" + li.th_set
                                + "&id="  + li.th_id
                                + "&fnm=" + li.nm;

                    jPr.css("background-image", "").empty();

                    // 视频
                    if(/^video\//.test(mime)) {
                        //console.log("aaaa")
                        var jV = $('<video controls>').attr({
                            "src" : src,
                        }).appendTo(jPr);
                        // $z.wrapVideoSimplePlayCtrl(jV, {
                        //     watchClick : true
                        // });
                    }
                    // 图片
                    else {
                        $('<img>').attr({
                            "src" : src,
                        }).appendTo(jPr);
                    }

                    // 更新当前
                    jUl.find("li").removeAttr("current");
                    jLi.attr("current", "yes");
                });

            // 标识一下
            win.__layout_event_binded = true;
        }
        //----------------------------------------
        // 事件:确保运行时，仅仅允许一个视频播放
        $(win.document.body).find('video').each(function(){
            var jVideo = $(this);
            if(!jVideo.attr('hm-video-mutex')) {
                jVideo.on("play", function(){
                    // console.log("I am play", this);
                    var me = this;
                    $(this.ownerDocument.body).find("video").each(function(){
                        if(me !== this) {
                            this.pause();
                        }
                    });
                });
                // 标识
                jVideo.attr('hm-video-mutex', 'yes');
            }
        });
    },
    //...............................................................
    // 自动寻找一个合适的详情页面
    // - obj 表示要渲染的数据
    // - sitemap 表示站点索引数据，默认取 window.__SITEMAP
    explainAutoHref : function(obj, sitemap) {
        sitemap = sitemap || window.__SITEMAP;
        if(sitemap && obj.th_set){
            for(var rph in sitemap) {
                var oPage = sitemap[rph];
                // 如果符合目标对象所属的 th_set
                // API 返回的数据类型为 obj | goods 
                if(oPage.hm_pg_tsid == obj.th_set
                    && /^(obj|goods)$/.test(oPage.hm_api_return)) {
                    // 那么就是它了
                    return window.__ROOT_PATH + rph + ".html";
                }
            }
        }
    },
    //...............................................................
    // 通用的解析一个链接的方法，支持 @auto
    // - href 链接，@auto 的话则自动调用 explainAutoHref
    // - obj 表示要渲染的数据
    // - isIDE 表示在 IDE 里，那么就应该总是返回 href
    explainHref : function(href, obj, isIDE) {
        // 试图自动寻找链接
        if("@auto" == href && !isIDE) {
            return HmRT.explainAutoHref(obj);
        }
        return href;
    },
    //...............................................................
    // 判断一个模板的数据类型能否与 api 的返回值匹配
    isMatchDataType : function(apiReTypes, tmplDataType){
        if(!apiReTypes || !tmplDataType)
            return false;
        if(!_.isArray(apiReTypes)){
            apiReTypes = [apiReTypes];
        }

        for(var i=0;i<apiReTypes.length;i++) {
            var apiReType = apiReTypes[i];
            if(_.isArray(tmplDataType)){
                if(tmplDataType.indexOf(apiReType) < 0)
                    return false
            }
            else if(apiReType != tmplDataType){
                return false;
            }
        }

        return true;
    },
    //...............................................................
    // 将一个数据尽量转换成模板能支持的数据类型
    // 如果转换失败将抛错, null 将被原样返回
    convertDataForTmpl : function(data, tmplDataType){
        if(HmRT.isMatchDataType(["page", "list"], tmplDataType)){
            if(data.list && data.pager)
                return data.list;
            if(_.isArray(data))
                return data;
            throw "api_data_nomatch : API(" + $z.toJson(data) + ") != Template("+tmplDataType+")";
        }
        return data;
    },
    //...............................................................
    // 根据模板的数据类型，判断给定数据是否为空
    isDataEmptyForTmpl : function(data, tmplDataType) {
        if(!data)
            return true;
        if(HmRT.isMatchDataType(["page", "list"], tmplDataType)){
            if(!_.isArray(data) || data.length == 0)
                return true;
        }
        return false;
    },
    /*...............................................................
    解析动态设置的 setting 对象，
    函数接受 :
    - setting : {..}     // 符合动态设置规范的 JSON 对象
    - asMap : false      // 默认返回数组，本选项可以让返回值变成对象，键为 key

    函数返回（基本符合 form 控件的field定义）:
    [{
        type     : "thingset",  // 项目类型
        arg      : "xxx",       // 项目参数
        dft      : "xxx",       // 项目默认值
        mapping  : {..}         // 映射表（基本只有@com类型才会有用）
        required : true,        // 字段是否必须
        key      : "xxx",       // 字段名
        title    : "xxx",       // 字段显示名
        tip      : "xxx",       // 提示信息
    }, {
        // 下一个选项
    }]
    */
    parseSetting : function(setting, asMap) {
        var re = asMap ? {} : [];
        // 循环
        for(var key in setting) {
            var val = setting[key];
            // 默认的字段
            var fld = {
                type     : "input",
                arg      : undefined,
                dft      : undefined,
                key      : key,
                title    : undefined,
                required : false,
                mapping  : undefined,
                tip      : undefined,
            };

            // 字符串形式
            if(_.isString(val)) {
                // 分析一下
                var m = /^([*])?(\(([^\)]+)\))?@(input|text|json|TSS|thingset|site|com|link|toggle|switch|droplist|fields|layout)(=([^:#{]*))?(:([^#{]*))?(\{[^}]*\})?(#(.*))?$/.exec(val);
                // 指定了类型
                if(m) {
                    fld.required = m[1] ? true : false;
                    fld.title = m[3];
                    fld.type  = m[4];
                    fld.dft   = m[6];
                    fld.arg   = m[8];
                    fld.mapping = m[9] ? $z.fromJson(m[9]) : null;
                    fld.tip   = m[11];
                }
            }
            // 对象形式
            else if(_.isObject(val)){
                _.extend(fld, val, {
                    key : key
                });
            }
            // 靠什么鬼!
            else {
                throw "unsupport setting value type<" + (typeof val) + "> : " + val;
            }

            // 计入结果
            if(asMap) {
                re[key] = fld;
            }else{
                re.push(fld);
            }
        }

        //console.log(re);

        // 返回
        return re;
    },
    /*...............................................................
    解析参数表的值，无论是接口参数表还是模板选项都，通用
    函数接受 : 
     - result : {..}        // 动态设置的 result 对象
     - opt : {              // 配置信息
        // 替换参数值的上下文
        context : {..},

        // 动态设置的配置信息，必须为 Map 形式！！！不能是数组
        setting : {..},

        // 页面请求的参数表，动态值会从里面取
        // 如果取不到，则用默认值，默认值也木有，则记入 lackKeys
        // 当然，在 IDE 调用的时候，是木有这个参数的
        request : {..},

        // 获取控件值的函数
        getComValue: F(comId):Object,

        // 是否截取 result 值的空白，默认 true
        trimValue : true,
     }
    函数返回 : {
        dynamicKeys : ["c", "site"],  // 动态参数名
        lackKeys    : ["c"],          // 标识了 required 的字段，有哪些没值
        data : {..},                  // 重新填充完毕的数据，可以直接被提交
        
    }
    */
    evalResult : function(result, opt){
        // 准备返回值
        var re = {
            dynamicKeys : [],
            lackKeys    : [],
            data : {},
        };
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 处理默认的选项
        opt.trimValue = opt.trimValue === false ? false : true;
        opt.context = opt.context || {};
        opt.request = opt.request || {};
        opt.pg_args = opt.pg_args || {};
        opt.setting = opt.setting || {};
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 循环处理 ...
        for(var key in result) {
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 得到值
            var val = result[key];

            // 如果是一个名值对，那么直接融合了
            if(_.isObject(val)) {
                _.extend(re.data, val);
                continue;
            }

            if(opt.trimValue)
                val = $.trim(val);
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 进行标准占位符替换
            var v2 = $z.tmpl(val, {
                escape: /\$\{([\s\S]+?)\}/g
            })(opt.context);
            //console.log(key, val, v2);

            // 忽略空值
            if(!v2)
                continue;

            // 特殊类型的值
            // TODO Session 变量
            // TODO Cookie 的值
            var m = /^([@#%])(<(.+)>)?(.*)$/.exec(v2);
            //console.log(m);
            if(m && m[2]) {
                var p_tp  = m[1];
                var p_val = m[3];
                var p_arg = $.trim(m[4]);
                //console.log(m, p_tp, p_val, p_arg);
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 动态参数: 这里就直接取默认值了
                if("@" == p_tp) {
                    re.dynamicKeys.push(key);
                    p_val = opt.request[p_val] || p_arg;
                    if(p_val) {
                        re.data[key] = p_val;
                    }
                    // 继续吧
                    continue;
                }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 网页路径参数
                if("%" == p_tp) {
                    re.dynamicKeys.push(key);
                    p_val = opt.pg_args[p_val] || p_arg;
                    if(p_val) {
                        re.data[key] = p_val;
                    }
                    // 继续吧
                    continue;
                }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 来自控件
                if("#" == p_tp) {
                    // 得到控件的值，如果有值就填充到参数表
                    var comVal = $z.invoke(opt, "getComValue", [p_val]);
                    if(comVal) {
                        // 得到对应动态设置项
                        var conf = opt.setting[key] || {};
                        // 得到映射表
                        if(conf.mapping) {
                            // 融合到参数表同时进行映射
                            try {
                                for(var key in conf.mapping) {
                                    re.data[key] = comVal[conf.mapping[key]];
                                }
                            }
                            // 出错了
                            catch(E){
                                throw "e_p_mapping : " + p_arg; 
                            }
                        }
                        // 直接将控件返回值融合到参数表
                        else {
                            // 对象
                            if(_.isObject(comVal)) {
                                _.extend(re.data, comVal);
                            }
                            // 普通值，直接填充
                            else {
                                re.data[key] = comVal;
                            }
                        }
                    }
                    // 继续下一个参数
                    continue;
                }
            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 普通值
            re.data[key] = v2;
        } // ~ for(var key in result)
        
        // 随后搜索一轮缺失的值
        for(var key in opt.setting) {
            if(opt.setting[key] && opt.setting[key].required){
                var v = re.data[key];
                if(_.isUndefined(v) || _.isNull(v) || (_.isString(v) && v.length==0))
                    re.lackKeys.push(key);
            }
        }

        // 搞定，收工
        return re;
    },
    //...............................................................
    // 触发本 jQuery 插件对应的 dynamic 控件的重新加载行为
    invokeDynamicReload : function(jq, jumpToHead) {
        var jCom = jq.closest("[hm-dynamic-id]");
        var dyId = jCom.attr("hm-dynamic-id");
        if(dyId) {
            $("#"+dyId+" > .hmc-dynamic")
                .hmc_dynamic("reload", jumpToHead, function(){
                    // 重新调整皮肤尺寸
                    if(window.__wn_skin_context) {
                        var SC = window.__wn_skin_context;
                        $z.invoke(SC.skin, "ready", [], SC);
                        $z.invoke(SC.skin, "resize", [], SC);
                    }        
                });
        }
    },
    //...............................................................
    // 将一个 Thing 格式的对象的 markdown 内容转换成 html
    // - API : 一个 regapi 的前缀
    // - th  : Thing 对象，里面需要有 content, th_set, id 这几个字段
    thContentToHtml : function(API, th, key){
        return $z.markdownToHtml(th[key || "content"] || "", {
            media : function(src){
                // 看看是否是媒体
                var m = /^media\/(.+)$/.exec(src);
                if(m){
                    return API + "/thing/media"
                            + "?pid=" + th.th_set
                            + "&id="  + th.id
                            + "&fnm=" + m[1];
                }
                // 原样返回
                return src;
            }
        });
    },
    /*...............................................................
    解析分组逻辑
    type                         # 普通: type 字段值直接分组
    th_nm::S2                    # 截取: th_nm 字段值截取字符串后分组
    th_nm::S1::Last Name: %s     # 截取: th_nm 字段值截取字符串后分组，标题需映射
    th_cate::{0=A,1=B}X          # 映射: th_cate 字段分组，标题需要映射
    th_cate::{0=A,1=B}Y::Cate:%s # 映射: th_cate 字段映射后分组，标题需要替换模板
    lm::Date(yyyy)::Year:%s      # 日期: lm 字段做日期转换后分组，标题需要替换模板
    dead::[否,是]                # 布尔: dead 字段根据给定布尔值映射后分组
    price::R{奢侈>100;普通<20>100;便宜<20}普通 # 区间：price字段必须是数字，根据给定区间归纳
    */
    parseGroupBy: function(str) {
        str = $.trim(str);
        if(!str)
            return null;
        var ss = str.split("::");
        //-------------------------------------
        // 准备返回值
        var gb = {name : $.trim(ss[0])};
        //-------------------------------------
        // 标题
        if(ss.length >= 3)
            gb.title = ss[2];
        //-------------------------------------
        // 来吧，分析一下
        var s = ss.length>=2 ? $.trim(ss[1]) : null;

        //-------------------------------------
        // type # 普通: type 字段值直接分组
        if(!s) {
            gb.getGroupValue = function(obj){
                return obj[this.name];
            };
            return gb;
        }
        //-------------------------------------
        // th_nm::S2=Unknown  # 截取: th_nm 字段值截取字符串后分组
        var m = /^S([0-9]*)(=(.+))?$/.exec(s);
        if(m){
            gb.len = m[1] ? parseInt(m[1]) : 0;
            gb.dft = m[3];
            gb.getGroupValue = function(obj){
                var v = obj[this.name];
                if(_.isUndefined(v) || _.isNull(v))
                    return this.dft;
                return this.len > 0
                            ? v.substring(0, this.len)
                            : v;
            };
            return gb;
        }
        //-------------------------------------
        // th_cate::{0=A,1=B}X # 映射: th_cate 字段分组，标题需要映射
        m = /^\{([^}]+)\}(.+)$/.exec(s);
        if(m){
            var ms = m[1];
            gb.dft = m[2];
            gb.mapping = {};
            ss = ms.split(",");
            for(var i=0; i<ss.length; i++) {
                var m2 = /^([^=])=(.+)$/.exec(ss[i]);
                if(m2) {
                    var k = $.trim(m2[1]);
                    var v = $.trim(m2[2]);
                    gb.mapping[k] = $.trim(v);
                    continue;
                }
                // 错误，不忍！
                throw "Invalid mapping pair: [" + ss[i] + '] in "' + s + '"';
            }
            gb.getGroupValue = function(obj){
                return this.mapping[obj[this.name]+""] || this.dft;
            };
            return gb;
        }
        //-------------------------------------
        // lm::D(yyyy)None::Year:%s      # 日期: lm 字段做日期转换后分组，标题需要替换模板
        m = /^D(\(([^)]+)\))?(.+)?$/.exec(s);
        if(m){
            gb.fmt = m[2] || "yyyy-mm-dd";
            gb.dft = m[3];
            gb.getGroupValue = function(obj){
                var v = obj[this.name];
                if(!v)
                    return this.dft;
                return $z.parseDate(v).format(this.fmt);
            };
            return gb;
        }
        //-------------------------------------
        // dead::[否,是]                # 布尔: dead 字段根据给定布尔值映射后分组
        m = /^\[([^,\]]+),([^\]]+)\]?$/.exec(s);
        if(m){
            gb.mapping = [m[1],m[2]],
            gb.getGroupValue = function(obj){
                return this.mapping[obj[this.name] ? 1 : 0];
            };
            return gb;
        }
        //-------------------------------------
        /* # 区间：price字段必须是数字，根据给定区间归纳
        price::R{奢侈>100;普通<20>100;便宜<20} 
            mapping : {
                "奢侈" : [100],
                "普通" : [20, 100],
                "便宜" : 20
            },
        */
        m = /^R\{([^}]+)\}(.+)?$/.exec(s);
        if(m){
            var ms = m[1];
            gb.dft = m[2];
            gb.mapping = {};
            ss = ms.split(";");
            for(var i=0; i<ss.length; i++) {
                // 奢侈>100
                var m2 = /^(.+)>([0-9.-]+)$/.exec(ss[i]);
                if(m2) {
                    var k = $.trim(m2[1]);
                    gb.mapping[k] = [m2[2]*1];
                    continue;
                }
                // 便宜<20
                m2 = /^(.+)<([0-9.-]+)$/.exec(ss[i]);
                if(m2) {
                    var k = $.trim(m2[1]);
                    gb.mapping[k] = m2[2]*1;
                    continue;
                }
                // 普通<20>100
                m2 = /^(.+)<([0-9.-]+)>([0-9.-]+)$/.exec(ss[i]);
                if(m2) {
                    var k = $.trim(m2[1]);
                    gb.mapping[k] = [m2[2]*1, m2[3]*1];
                    continue;
                }
                // 错误，不忍！
                throw "Invalid range pair: [" + ss[i] + '] in "' + s + '"';
            }
            // 设置获取函数
            gb.getGroupValue = function(obj){
                var val = obj[this.name] * 1;
                if(!isNaN(val)) {
                    for(var key in this.ramappingnge) {
                        var r = this.mapping[key];
                        // 小于
                        if(_.isNumber(r)){
                            if(val < r)
                                return key;
                        }
                        // 大于
                        else if(_.isArray(r) && r.length == 1) {
                            if(val >= r[0])
                                return key;
                        }
                        // 区间 
                        else {
                            if(val >= r[0] && val < r[1])
                                return key;
                        }           
                    }
                }
                return this.dft;
            };
            // 返回区间
            return gb;
        }
        //-------------------------------------
        throw "Unknown groupBy:: " + str;
    },
    /*...............................................................
    // 将一个数组，按照 groupBy 进行分组。
    // 返回的数据结构为:
    [{
        title : "xxx"    // 组标题
        list  : [..]      // 本组的数据对象
    }, {
        // ... next group
    }, {
        // 如果没有标题的组，则表示总结不出来的数据 ...
        list : [..] 
    }]
    */
    groupData : function(list, gb) {
        // 木有合法数据或者木有合法分组
        if(!_.isArray(list) || list.length == 0 || !gb)
            return list;

        // 来吧开始解析分组
        var reMap = {};
        for(var i=0; i<list.length; i++) {
            var data  = list[i];
            var gval  = gb.getGroupValue(data) || "-no-title";
            var title = gb.title
                            ? gb.title.replace("%s", gval)
                            : gval;
            var glist = reMap[title];
            if(!glist) {
                reMap[title] = [data];
            } else {
                glist.push(data);
            }
        }

        // 生成返回数据
        var re = [];
        for(var title in reMap) {
            re.push({
                title : title,
                list  : reMap[title]
            });
        }

        // 对分组排序
        re.sort(function(a, b) {
            if(a.title > b.title)
                return 1;
            return -1;
        });

        // 修正每个分组的标题, 如果带着 `1.xxx` 去掉前面的数字
        for(var i=0; i<re.length; i++) {
            var g = re[i];
            console.log()
            var m = /^([0-9]+\.)(.+)$/.exec(g.title);
            if(m) {
                g.title = $.trim(m[2]);
            }
        }

        // 返回
        return re;
    }
    //...............................................................
};  // ~ window.HmRT =
})(window.jQuery, window.NutzUtil);