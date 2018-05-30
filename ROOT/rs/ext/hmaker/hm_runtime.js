/**
提供运行时的帮助函数集合，IDE 也会用的到
*/
(function($, $z){
//===================================================================
function ___layout_size (sz, dft) {
    if("?" == sz)
        return undefined;
    if("-" == sz)
        return "hidden";
    if(!sz)
        return dft;
    return parseInt(sz);
}
function __set_layout_item_size (it, s) {
    s = $.trim(s);
    if(!s)
        return it;
    m = /^([0-9?-]+)(\/([0-9?-]+))?$/.exec($.trim(s));
    if(!m) {
        console.warn("invalid layout sizing:", s);
        return it;
    }
    it.w_desktop = ___layout_size(m[1], '?');
    it.w_mobile  = ___layout_size(m[3], it.w_desktop);
    return it;
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
            var fkey = m[2].split("|");
            var fld = {
                type    : "field", 
                display : "String", 
                key     : fkey.length>1 ? fkey : fkey[0]
            };
            //console.log(fld)
            // 标识标题
            fld.isTitle = m[1] == ">";
            fld.show = m[1]=="!" ? "always" : "auto";
            // 选择器
            if(m[4]){
                fld.selector = $.trim(m[4]);
            }
            // 链接
            if(m[6]){
                fld.linkTarget = "+"==m[6] ? "_blank" : "_self";
            }
            // 尺寸
            if(m[8]){
                if(!__set_layout_item_size(fld, m[8]))
                    return;
            }
            // 标题
            if(m[12]) {
                fld.title = m[12];
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
                        var d = $z.parseDate(val);
                        s = d.format(this.config || "yyyy-mm-dd");
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
            if(m[10]) {
                var ts = m[10];
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
                // 日期时间: .th_birthday=Date("yyyy-MM-dd")
                m2 = /^Date\(([^)]*)\)$/.exec(ts);
                if(m2){
                    fld.display = "Date";
                    fld.config = m2[1];
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
                m2 = /^Link\(([^)]*)\)$/.exec(ts);
                if(m2) {
                    fld.display = "Link";
                    fld.config  = m2[1];
                    $z.setUndefined(fld, "linkTarget", "_blank");
                    return fld;
                }
                // 链接: .href=Button(Buy Now)
                m2 = /^Button\(([^)]*)\)$/.exec(ts);
                if(m2) {
                    fld.display = "Button";
                    fld.config  = m2[1];
                    $z.setUndefined(fld, "linkTarget", "_blank");
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

            // 强制退出组
            if("~~~" == line) {
                if(grp && grp.items.length > 0)
                    layout.data.push(grp);
                grp = undefined;
                continue;
            }

            // 组
            m = /^@(\((.+)\))?(.+)?/.exec(line)
            if(m) {
                // 推入前组
                if(grp && grp.items.length > 0)
                    layout.data.push(grp);
                // 建立新组
                grp = __set_layout_item_size({
                        type:"group", name:$.trim(m[2]) ,items:[]
                    }, m[3]);
                continue;
            }

            // 表格
            m = /^T(\((.+)\))?(.+)?/.exec(line)
            if(m) {
                var tbl = __set_layout_item_size({
                        type:"table", name:$.trim(m[2]) ,rows:[]
                    }, m[3]);
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
                        selector   : $.trim(m[5]), 
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

        // 推入最后一组
        if(grp && grp.items.length > 0)
            layout.data.push(grp);

        //console.log(layout)
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
            return {
                tmpl   : hrefTmpl,
                obj    : obj,
                result : obj ? hrefTmpl(obj) : null,
            };
        }
        // 确保是假，且还不是undefined
        return "";
    },
    //...............................................................
    // 渲染字段内容
    renderLayoutFieldElementContent : function(fld, str, oHref, forceHTML) {
        var jFi;
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
        if(fld.linkTarget && oHref) {
            var hs = _.isString(oHref) ? oHref : oHref.tmpl(oHref.obj);
            jFi = $('<a>').attr({
                    "href"   : hs,
                    "target" : "_blank" == fld.linkTarget && oHref 
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
    renderLayoutFieldElement : function(fld, str, oHref, forceHTML) {
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
        if(fld.selector)
            jFld.addClass(fld.selector);
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
        var theHref = oHref ? oHref.result : null;
        // 普通文字
        if('text' == fld.type) {
            return $('<em class="wn-obj-text">').text(fld.value).appendTo(jP);
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
            return this.renderLayoutFieldElement(fld, jTable).appendTo(jP);
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
            return this.renderLayoutFieldElement(fld, html, oHref, true).appendTo(jP);
        }

        // 动态值，获取一下
        var val  = fld.getText(obj);

        // 这个比较优先，因为无论如何都要显示出预览区嘛
        // .thumb=Preview
        if("Preview" == fld.display) {
            var jThumb;
            // 缩略图
            if(val) {
                jThumb = $('<div class="wn-obj-preview">');
                $('<span>').css({
                    "background-image" : 'url("' + opt.API + "/thumb?" + val + '")'
                }).appendTo(jThumb);
            }
            // 仅仅显示一个空的产品图标
            else {
                jThumb = $('<div class="wn-obj-preview" empty="yes">')
                    .html('<span><i class="fas fa-image"></i></span>');
            }
            // 搞定返回
            return this.renderLayoutFieldElement(fld, jThumb).appendTo(jP);
        }

        // 无效的值无视
        if("always" != fld.show)
            if(_.isNull(val) || _.isUndefined(val))
                return null;

        // .th_cate={A:"猫",B:"狗"}
        // .th_birthday=Date(yyyy-mm-dd)
        // .len:100/?=Size(2)
        if(/^(Mapping|Date|Size|List)$/.test(fld.display)) {
            return this.renderLayoutFieldElement(fld, val, theHref).appendTo(jP);
        }
        
        // .lbls:100=UL(!image:src)->thumb
        if("UL" == fld.display) {
            //console.log(fld)
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
            return this.renderLayoutFieldElement(fld, jUl).appendTo(jP);
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
            $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
                watchClick : $('html[hmaker-runtime]').size() > 0
            });
            // 搞定返回
            return this.renderLayoutFieldElement(fld, jAr).appendTo(jP);
        }
        // .href=Link[Buy Now]
        if("Link" == fld.display || "Button" == fld.display) {
            var s = fld.config || val || fld.display;
            return this.renderLayoutFieldElement(fld, s, val||"N/A").appendTo(jP);
        }
        // 默认就是文字咯
        return this.renderLayoutFieldElement(fld, val, theHref).appendTo(jP);
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
            // 字段组
            if('group' == it.type) {
                var jGrp = $('<section>');
                // 循环渲染字段
                for(var x=0; x<it.items.length; x++) {
                    var fld  = it.items[x];
                    this.renderLayoutField(opt, jGrp, fld, obj, oHref)
                }
                // 设置属性，并加入 DOM
                jGrp.attr({
                    "group-name" : it.name,
                    "layout-desktop-width" : it.w_desktop,
                    "layout-mobile-width"  : it.w_mobile,
                });
                jGrp.appendTo(jLayout);
            }
            // 字段
            else {
                this.renderLayoutField(opt, jLayout, it, obj, oHref)
            }
        }
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
            throw "api_data_nomatch : API(" + data + ") != Template("+tmplDataType+")";
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
            var m = /^([@#])(<(.+)>)?(.*)$/.exec(v2);
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
    //...............................................................
};  // ~ window.HmRT =
})(window.jQuery, window.NutzUtil);