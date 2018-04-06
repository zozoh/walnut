define(function (require, exports, module) {
// 依赖
require("/gu/rs/ext/hmaker/hm_runtime.js");
var Wn   = require('wn/util');
var POP  = require('ui/pop/pop');
var CssP = require("ui/support/cssp");
// ....................................
// 块的 CSS 属性基础对象
var CSS_BASE = {
    position:"",top:"",left:"",width:"",height:"",right:"",bottom:"",
    margin:"",padding:"",border:"",borderRadius:"",
    color:"",background:"",
    overflow:"",boxShadow:"",
    textShadow:"",textAlign:"",
    font:"", fontFamily:"", fontSize:"", fontWeight:"", 
    letterSpacing:"", lineHeight:"", textAlign:"",
};
// ....................................
// 方法表
var methods = {
    //=========================================================
    hmaker : function(){
        var reUI = this;
        //console.log(reUI.uiName, reUI.__hmaker__);
        while(!reUI.__hmaker__) {
            reUI = reUI.parent;
            if(!reUI)
                return;
        }
        return reUI;
    },
    // 得到 HmPageUI，如果不是，则抛错
    pageUI : function(quiet) {
        var UI = this;
        var uiHMaker = this.hmaker();
        var re = uiHMaker ? uiHMaker.gasket.main : null;
        // 严格模式
        if(!quiet){
            if(!re){
                throw 'PageUI Not Loadded!';
            }
            if(re.uiName != "app.wn.hmaker_page"){
                throw 'Not A PageUI: ' + re.uiName;
            }
        }
        if(re && re.uiName != "app.wn.hmaker_page")
            return null;
        return re;
    },
    // 获取资源列表的 UI 实例
    resourceUI : function() {
        return this.hmaker().gasket.resource;
    },
    // 获取 prop UI 下面的子 UI，如果类型未定义，则返回 prop UI 本身
    propUI : function(uiPath) {
        var uiHMaker = this.hmaker();
        var uiProp   = uiHMaker.gasket.prop;
        if(uiPath)
            return uiProp.subUI(uiPath);
        return uiProp;
    },
    //=========================================================
    // 得到站点主目录
    getHomeObj : function() {
        var homeId = this.getHomeObjId();
        return Wn.getById(homeId);
    },
    // 得到站点主目录 ID
    getHomeObjId : function() {
        return this.hmaker().__home_id;
    },
    // 得到站点主目录名称
    getHomeObjName : function() {
        return this.getHomeObj().nm;
    },
    getFileObj : function(path, quiet) {
        var oHome = this.getHomeObj();
        var aph = Wn.appendPath(oHome.ph, path);
        return Wn.fetch(aph, quiet);
    },
    //=========================================================
    moveTo : function(oTa, objs, callback) {
        var UI = this;
        var resUI = UI.resourceUI();

        // 无需移动
        if(!oTa || !objs)
            return;

        if(!_.isArray(objs))
            objs = [objs];

        if(objs.length == 0)
            return;

        // 准备命令吧
        var cmds = [];
        for(var i=0; i<objs.length; i++) {
            var obj = objs[i];
            cmds.push('mv id:' + obj.id + ' id:' + oTa.id);
            cmds.push('echo "%[' + (i+1) + '/' + objs.length
                        +'] move ' + obj.nm + ' => ' + oTa.nm + '"');
        }
        cmds.push('echo "%[-1/0] ' + objs.length + ' objs done!"');
        // 执行命令
        var cmdText = cmds.join(";\n");
        //console.log(cmdText)
        Wn.processPanel(cmdText, function(res, jMsg, re){
            // 发现错误了
            if(/^e.cmd.mv.dupname :/.test(re)) {
                UI.alert("有重名文件，请看日志获取详情");
            }
            // 关闭对话框
            else {
                Wn.cleanCache();  // 还是要清一下，否则缓存的pid可能导致诡异
                this.close();
            }

            // 调用回调
            $z.doCallback(callback);
        });
    },
    //=========================================================
    // 得到站点的皮肤设定， null 表示没有设定皮肤
    getSkinInfo : function() {
        var UI = this;
        var oHome = UI.getHomeObj();

        var skinName = oHome.hm_site_skin;
        if(!skinName)
            return {};
        
        // 读取皮肤信息
        var oInfo = Wn.fetch("~/.hmaker/skin/" + skinName + "/skin.info.json", true);
        if(!oInfo)
            return null;

        var json = Wn.read(oInfo);
        if(json)
            return $z.fromJson(json);

        return null;
    },
    // 根据一个控件类型，获取其皮肤的可用样式表单，返回的一律是
    // {text,selector} 格式的对象
    // 返回 null 表示站点没设置皮肤
    // 返回 [] 表示皮肤中没有定义控件的类选择器列表
    getSkinListForCom : function(comType) {
        var skinInfo = this.getSkinInfo();
        if(skinInfo && skinInfo.com)
            return skinInfo.com[comType] || [];
        return null;
    },
    // 返回皮肤支持的分栏列表
    // {text,selector} 格式的对象
    // 返回 null 表示站点没设置皮肤
    // 返回 [] 表示皮肤中没有规定分栏的样式
    getSkinListForArea : function() {
        var skinInfo = this.getSkinInfo();
        if(skinInfo)
            return skinInfo.area || [];
        return null;
    },
    // 返回皮肤支持的菜单项列表
    // {text,selector} 格式的对象
    // 返回 null 表示站点没设置皮肤
    // 返回 [] 表示皮肤中没有规定分栏的样式
    getSkinListForMenuItem : function() {
        var skinInfo = this.getSkinInfo();
        if(skinInfo)
            return skinInfo.menuItem || [];
        return null;
    },
    // 针对一个组件，根据选择器获取其皮肤的配置对象，没找到返回 null
    getSkinItemForCom : function(comType, selector) {
        if(selector){
            var sList = this.getSkinListForCom(comType);
            if(_.isArray(sList))
                for(var si of sList){
                    if(si.selector == selector)
                        return si;
                }
        }
        return null;
    },
    // 针对一个组件，根据选择器获取其样式名
    getSkinTextForCom : function(comType, selector) {
        var si = this.getSkinItemForCom(comType, selector);
        return si ? (si.text || si.selector) : null;
    },
    // 针对一个分栏区域，根据选择器获取其样式名
    getSkinTextForArea : function(selector) {
        var sList = this.getSkinListForArea();
        if(_.isArray(sList))
            for(var si of sList){
                if(si.selector == selector)
                    return si.text || si.selector;
            }
        return null;
    },
    // 针对一个菜单项目，根据选择器获取其样式名
    getSkinTextForMenuItem : function(selector) {
        var sList = this.getSkinListForMenuItem();
        if(_.isArray(sList))
            for(var si of sList){
                if(si.selector == selector)
                    return si.text || si.selector;
            }
        return null;
    },
    // 得到一个模板对应的类选择器名称
    getSkinForTemplate : function(templateName) {
        var skinInfo = this.getSkinInfo();

        if(skinInfo && skinInfo.template)
            return skinInfo.template[templateName];

        return null;
    },
    // 得到当前站点的皮肤变量 less 文件内容
    reloadSkinVarSet : function(callback){
        var UI = this;
        var oHome = UI.getHomeObj();

        // 准备 API 的 URL
        var url = $z.tmpl('/api/{{d1}}/hmaker/load/{{siteId}}/_skin_var.less')({
                d1     : oHome.d1, 
                siteId : oHome.id,
            });

        $.get(url, function(re){
            $z.doCallback(callback, [re]);
        });
    },
    // 将 _skin_var.less 转换成 form fields 格式
    parseSkinVar : function(str) {
        var UI   = this;
        var data = {};
        var form = {fields:[]};
        if(!str)
            return null;
        // 开始的字段组
        var fldgrp = form;
        // 逐行解析
        var lines = str.split(/\r?\n/g);
        var REG = /^@([0-9a-zA-Z_]+) *: *([^;]+) *; *\/\/ *([0-9a-zA-Z_]+) *: *(.+)$/;
        for (var i = 0; i < lines.length; i++) {
            var line = $.trim(lines[i]);
            // 无视空行
            if(!line)
                continue;
            //....................................
            // 开启一个新的字段组
            var m = /^\/\/ *#(.+)$/.exec(line);
            if(m) {
                // 将之前的字段组推入
                if(fldgrp != form && fldgrp.fields.length > 0) {
                    form.fields.push(fldgrp);
                }
                // 开启一个新的字段组
                fldgrp = {
                    title  : m[1],
                    fields : []
                };
                continue;
            }
            //....................................
            // 加入当前字段组
            m = REG.exec(line);
            if(m) {
                // 先解析出来
                var a_key = m[1];
                var a_val = m[2];
                var a_tp  = m[3];
                var a_txt = m[4];

                // 记录数据
                data[a_key] = a_val;

                // 计算 UI 控件
                var fld = UI.getCssFieldConf(a_tp, null, a_txt, null, a_key);
                fld.__vtype = a_tp;
                
                // 计入
                fldgrp.fields.push(fld);
            }
            // 显示一个警告吧
            else {
                console.warn("invalid line in _skin_var.less:", line);
            }
        }
        // 处理最后一行
        if(fldgrp != form && fldgrp.fields.length > 0) {
            form.fields.push(fldgrp);
        }
        // 嗯，返回
        return {
            form : form,
            data : data
        };
    },
    // 将 parseSkinVar 的结果，转换为  _skin_var.less 的文本格式
    renderSkinVar : function(skinVar) {
        var str = "";
        var grps = skinVar.form.fields;
        var data = skinVar.data;
        for(var i=0; i<grps.length; i++) {
            var grp = grps[i];

            // 输出标题
            if(grp.title) {
                str += "//# " + grp.title + "\n";
            }
            // 输出字段
            for(var x=0; x<grp.fields.length; x++) {
                var fld = grp.fields[x];
                var val = data[fld.key];
                var vtp = fld.__vtype;
                // 如果为空，那么就设置成 unset
                if(!val) {
                    val = "unset";
                }
                // 如果为对象
                else if(!_.isString(val)) {
                    // 背景
                    if("background" == vtp) {
                        val = CssP.strBackground(val);
                    }
                    // 其他的暂不支持
                    else {
                        console.warn("unsupport object css", vtp, val);
                        val = "unset";
                    }
                }

                str += "@" + fld.key;
                str += ": " + val + ";";
                str += " //" + fld.__vtype + ": ";
                str += fld.title;
                str += "\n";
            }

            // 加个空行
            str += "\n";
        }
        return str;
    },
    //=========================================================
    // 得到站点 api 的前缀
    //  - path 为 api 的路径
    getHttpApiUrl : function(path) {
        var oHome = this.getHomeObj();
        return "/api/" + oHome.d1 + (path || "");
    },
    // 得到站点所有可用的 api
    getHttpApiList : function(filter) {
        var UI = this;
        var uiHMaker = UI.hmaker();

        var list = uiHMaker.__API_LIST;

        // 重新读取
        if(!_.isArray(list)) {
            var re = Wn.exec("obj -mine -match \"api_return:'^([a-z]+)$'\""
                             + " -json -l -sort 'pid:1,nm:1'");
            list = $z.fromJson(re);
        }

        // 准备返回过滤结果
        if(_.isFunction(filter)) {
            var list2 = [];
            for(var i=0; i<list.length; i++){
                var oApi = list[i];
                if(filter(oApi))
                    list2.push(oApi);
            }
            return list2;
        }
        // 直接返回
        return list;
    },
    //=========================================================
    // 站点的模板
    // 让模板的JS生效，并返回模板的信息对象
    // ignoreJS : 是否不 eval 模板的 JS 文件（即不让 jQuery 插件生效）
    // forceReload : 是否强制从服务器读取
    evalTemplate : function(templateName, ignoreJS, forceReload) {
        var oHome = this.getHomeObj();
        var phTmplHome = "/home/" + oHome.d1 + "/.hmaker/template/" + templateName;

        // 加载 jQuery 控件
        if(!ignoreJS) {
            var jsContent = Wn.read(phTmplHome + "/jquery.fn.js", forceReload);
            eval(jsContent);
        }

        // 返回模板信息
        var jsonInfo = Wn.read(phTmplHome + "/template.info.json", forceReload);
        return $z.fromJson(jsonInfo);
    },
    // //=========================================================
    // // 得到本站点可用的模板列表
    // // apiReType : 字符串，表示只显示支持此种数据类型的模板
    // //             可能的值为 obj|list|page
    // //             null 表示全部模板
    // // forceReload : 是否强制从服务器读取
    // getTemplateList : function(apiReType, forceReload) {
    //     var UI = this;
    //     var oHome = this.getHomeObj();
    //     // 得到模板列表
    //     var oTmplHome = Wn.fetch("/home/" + oHome.d1 + "/.hmaker/template");
    //     var oTmplList = Wn.getChildren(oTmplHome, forceReload);
    //     //console.log(oTmplList)

    //     // 依次解析 ..
    //     var list = [];
    //     for(var i=0; i<oTmplList.length; i++) {
    //         var oTmpl = oTmplList[i];
    //         var tmpl  = UI.evalTemplate(oTmpl.nm, true, forceReload);
    //         if(!apiReType || HmRT.isMatchDataType(apiReType, tmpl.dataType)) {
    //             tmpl.value = oTmpl.nm;
    //             list.push(tmpl);
    //         }
    //     }

    //     // 返回
    //     return list;
    // },
    //=========================================================
    get_com_display_text: function(ctype, comId, skin, showId){
        var UI = this;
        var re = skin ? UI.getSkinTextForCom(ctype, skin)
                      : UI.msg("hmaker.com._.dft")
                        + UI.msg("hmaker.com." + ctype + ".name");
        if(showId)
            return re + "#" + comId;
        return re;
    },
    //=========================================================
    // 监听消息
    listenBus : function(event, handler){
        var uiHMaker = this.hmaker();
        this.listenUI(uiHMaker, event, handler);
    },
    // 发送消息
    fire : function() {
        var args = Array.from(arguments);
        var uiHMaker = this.hmaker();
        // console.log("fire", args)
        if(uiHMaker)
            uiHMaker.trigger.apply(uiHMaker, args);
    },
    //=========================================================
    // 得到某个对象相对于 HOME 的路径
    getRelativePath : function(o) {
        var oHome = this.getHomeObj();
        return Wn.getRelativePath(oHome, o);
    },
    // 得到一个对象在 HMaker 里表示的 Text
    getObjText : function(o) {
        // 特殊的目录: lib
        if(o.pid == this.getHomeObjId()) {
            if('lib' == o.nm) {
                return this.msg("hmaker.lib.title");
            }
            if('resource' == o.nm) {
                return this.msg("hmaker.res.resource");
            }
        }

        // 其他
        return o.nm;
    },
    // 得到一个对象在 HMaker 里表示的 Icon HTML
    getObjIcon : function(o, onlyOne) {
        // 有了自定义
        if(o.icon)
            return o.icon;

        // 特殊的目录: lib
        if(o.pid == this.getHomeObjId()) {
            if('lib' == o.nm) {
                return this.msg('hmaker.lib.icon');
            }
            if('image' == o.nm){
                return '<i class="zmdi zmdi-collection-image-o"></i>';
            }
            if('resource' == o.nm){
                return '<i class="zmdi zmdi-collection-item"></i>';
            }
            if('css' == o.nm) {
                return '<i class="zmdi zmdi-language-css3"></i>';
            }
            if('js' == o.nm) {
                return '<i class="fa fa-flash"></i>';
            }
        }

        // 库组件
        if('hm_lib' == o.tp) {
            return this.msg('hmaker.lib.icon_item');
        }

        // CSS 文件
        // JS
        // if(/^(css|js)$/.test(o.tp)) {
        //     //return '<i class="fa fa-file-text-o"></i>';
        //     return Wn.objIconHtml(o);
        // }
        
        // 文件夹
        if('DIR' == o.race) {
            var re = '<i class="far fa-folder"></i>';
            if(!onlyOne)
                return re + '<i class="far fa-folder-open"></i>';
            return re;
        }
        
        // 网页 / XML
        if(/^text\/(xml|html)$/.test(o.mime)) {
            // 无后缀的用编辑器编辑
            if(!$z.getSuffixName(o.nm)) {
                return  '<i class="fa fa-file"></i>';
            }
            // 其他的用文本编辑
            //return  '<i class="fa fa-file-code-o"></i>';
        }

        // 默认图标
        return Wn.objIconHtml(o);

        /*
        // 文本
        if(/^text\//.test(o.mime))
            return  '<i class="fa fa-file-text"></i>';

        // 图片
        if(/^image\//.test(o.mime))
            return  '<i class="fa fa-file-image-o"></i>';

        // 视频
        if(/^video\//.test(o.mime))
            return  '<i class="fa fa-file-video-o"></i>';

        // 音频
        if(/^audio\//.test(o.mime))
            return  '<i class="fa fa-file-audio-o"></i>';

        // pdf
        if("pdf" == o.tp)
            return  '<i class="fa fa-file-pdf-o"></i>';

        // excel
        if(/^xlsx?$/.test(o.tp))
            return  '<i class="fa fa-file-excel-o"></i>';

        // word
        if(/^docx?$/.test(o.tp))
            return  '<i class="fa fa-file-word-o"></i>';

        // 其他
        return  '<i class="fa fa-file-o"></i>';*/
    },
    //=========================================================
    // 根据控件的块定位模式，从 css 集合里提取出相关的熟悉
    // 模式字符串为 "TLBRWH" 分别代表顶点和宽高，所有的模式字符串都遵守这个顺序
    //  @mode : 可能的值为:
    //            - WH   : 宽高
    //            - TLWH : 左上顶点定位
    //            - TRWH : 右上顶点定位
    //            - LBWH : 左下顶点定位
    //            - BRWH : 右下顶点定位
    //            - TLBR : 四角顶点定位
    //            - TLBW : 左边定位
    //            - TBRW : 右边定位
    //            - TLRH : 顶边定位
    //            - LBRH : 底边定位
    pickCssForMode : function(css, mode) {
        var keys = this.comBlockModeToKeys(mode);
        return $z.pick(css, keys);
    },
    // 根据控件的块定位模式，转换成 css 通常的位置属性写法
    //  - mode : 为字符串，可能是 "TLBRWH" 任意组合
    //  - asString : true 表示返回半角逗号分隔字符串，否则是数组
    comBlockModeToKeys : function(mode, asString) {
        var re = [];
        var cs = mode.toUpperCase();
        for(var i=0; i<cs.length; i++) {
            var key = ({
                T:"top", L:"left", B:"bottom", R:"right", W:"width", H:"height"
            })[cs[i]];
            if(!key)
                throw "unsupport mode: '" + mode + "' -> [" + cs[i] + "]";
            re.push(key);
        }
        return asString ? re.join(",") : re ;
    },
    // 将 CSS 对象与 base 合并，并将内部所有的 undefined 和 null 都变成空串
    formatCss : function(css, mergeWithBase) {
        // 传入了 base 对象
        if(_.isObject(mergeWithBase)){
            css = _.extend({}, mergeWithBase, css);
        }
        // 与默认 base 对象合并
        else if(mergeWithBase) {
            css = _.extend({}, CSS_BASE, css);   
        }

        // 将所有的 undefined 和 null 都变成空串，表示去掉
        // 如果 key 以 _ 开头，则会被删除掉
        var re = {};
        for(var key in  css) {
            if(/^_/.test(key))
                continue;
            var val = css[key];
            if(_.isUndefined(val) || _.isNull(val))
                re[key] = "";
            else
                re[key] = val;
        }

        // 返回新创建的对象 
        return re;
    },
    // 返回 base_css 的一个新实例
    getBaseCss : function() {
        return _.extend({}, CSS_BASE);
    },
    // 生成一个新的 css 集合，所有未给定的 css 会被表示空属性而删除
    // 参见 CSS_BASE
    // 本函数假定传入的 css 键值都是驼峰命名的
    normalizeCss : function(css) {
        return _.extend({}, CSS_BASE, css);
    },
    //=========================================================
    // 获取资源文件的 picker 配置信息
    getObjPickerEditConf : function(lastBaseKey, mimeRegex) {
        var oHome = this.getHomeObj();
        return {
            base : oHome,
            lastBaseKey : lastBaseKey,
            mustInBase : true,
            setup : {
                defaultByCurrent : false,
                multi : false,
                filter    : function(o) {
                    if('DIR' == o.race)
                        return true;
                    return mimeRegex ? mimeRegex.test(o.mime) : true;
                }
            },
            parseData : function(str){
                //console.log("parseData", str);
                if(!str)
                    return null;
                // 指定了 ID
                var m = /id:([\w\d]+)/.exec(str);
                if(m)
                    return Wn.getById(m[1]);

                // 指定了相对站点的路径
                if(/^\//.test(str)){
                    return Wn.fetch(Wn.appendPath(oHome.ph, str));
                }

                // 默认指定了相对页面的路径
                var oPage = UI.pageUI().getCurrentEditObj();
                var pph = oPage.ph;
                var pos = pph.lastIndexOf("/");
                var aph = Wn.appendPath(pph.substring(0,pos), str);
                return Wn.fetch(aph);
            },
            formatData : function(o){
                if(!o)
                    return null;
                //console.log("formatData:", o)
                return "/" + Wn.getRelativePath(oHome, o);
            }
        };
    },
    //=========================================================
    // 获取背景属性编辑控件的关于 image 编辑的配置信息
    getBackgroundImageEditConf : function(){
        var UI    = this;
        var oHome = UI.getHomeObj();
        return {
            imageBy : {
                uiType : "ui/picker/opicker",
                uiConf : {
                    //base : UI.getFileObj("image", true) || oHome,
                    base : oHome,
                    lastBaseKey : "hmaker_pick_image",
                    mustInBase : true,
                    setup : {
                        defaultByCurrent : false,
                        multi  : false,
                        filter : function(o) {
                            if('DIR' == o.race)
                                return true;
                            return /^image/.test(o.mime);
                        }
                    },
                    // 解析对象，如果是 url(/o/read/id:xxx) 那么就认为是对象
                    parseData : function(str){
                        //console.log(str)
                        // 看看是不是对象
                        var m = /^url\("?\/o\/read\/id:\w+\/([^"']+)"?\)$/i.exec(str);
                        if(m) {
                            var rph   = m[1];
                            return Wn.fetch(Wn.appendPath(oHome.ph, rph));
                        }
                        return null;
                    },
                    // 把 link 搞出来的东西用 url() 包裹
                    formatData : function(o){
                        if(!o)
                            return null;
                        // 得到相对路径
                        var rph   = Wn.getRelativePath(oHome, o);
                        return 'url("/o/read/id:' + oHome.id + '/' + rph + '")';
                    }
                }
            }
        };
    }, // ~ getBackgroundImageEditConf()
    //=========================================================
    // 打开超链接编辑界面，接受的参数格式为:
    /*
    {
        href      : "xxx"    // 要编辑的值
        callback  : F(href)  // 回调函数接受
    }
    */
    openEditLinkPanel : function(opt) {
        var UI = this;
        var homeObj = UI.getHomeObj();
        var pageUI  = UI.pageUI();
        var pageObj = pageUI.getCurrentEditObj();
        POP.openUIPanel({
            title  : 'i18n:hmaker.link.edit',
            width  : 480,
            height : 520,
            setup  : {
                uiType : 'app/wn.hmaker2/support/edit_link',
                uiConf : {
                    emptyItem  : opt.emptyItem,
                    fixItems   : opt.fixItems,
                    homeObj    : homeObj,
                    pageObj    : pageObj,
                    pageUI     : pageUI,
                    pagePath   : "/" + Wn.getRelativePath(homeObj, pageObj),
                    anchorText : function(o) {
                        return UI.get_com_display_text(o.ctype, o.id, o.skin);
                    }
                }
            },
            ready : function(){
                this.setData(opt.href);
            },
            ok : function(){
                var href = (this.getData()||"").replace(
                        /[\r\n]/g, "");
                $z.invoke(opt, "callback", [href]);
            }
        }, UI);
    },
    //...............................................................
    /* 显示一个分栏选择的列表，主要用于 navmenu 的区域显示
    opt : {
        comId - 表示要选择哪个布局控件的分栏列表
        uiCom - 指定一个组件，这个组件所在的 Area 给予忽略，通常为 navmenu 控件
        areaId - 表示指定的 areaId,
        callback - F(newAreaId)
    }
    */
    openPickAreaPanel : function(opt) {
        var UI = this;

        // 确保有分栏ID
        if(!opt.comId) {
            alert("no comId");
            throw "no comId";
        }

        // 获取分栏下的区域列表
        var areaiList = UI.pageUI().getLayoutAreaList(opt.comId);

        // 得到自己所在的分栏，看看是否是所选分栏
        var myLayouts = [];
        if(opt.uiCom) {
            opt.uiCom.$el.parents(".hm-area").each(function(){
                var jMyArea = $(this);
                myLayouts.push({
                    comId  : jMyArea.closest(".hm-layout").attr("id"),
                    areaId : jMyArea.attr("area-id")
                });
            });
        }

        // 看看是否选择了自己所在的布局链
        var myAreaId = null;
        for(var i=0; i<myLayouts.length; i++) {
            var myl = myLayouts[i];
            if(myl.comId == opt.comId) {
                myAreaId = myl.areaId;
                break;
            }
        }

        // 得到自己已经选择的区域列表
        var usedAreaMap = $z.invoke(opt.uiCom, "joinToggleAreaMap") || {};

        // 最后得到自己应该显示的下拉列表项目
        var items = [{
            areaId : null
        }];

        // 加入所有的区域
        for(var ao of areaiList) {
            // 自己所在的区域不可选
            if(ao.areaId == myAreaId) 
                continue;

            // 已经使用过的区域标识一下
            if(usedAreaMap[ao.areaId] && ao.areaId != opt.areaId){
                ao.__used = true;
            }

            items.push(ao);
        }

        // 准备显示的列表
        POP.openUIPanel({
            title : "i18n:hmaker.edit.pick_area",
            width : 300,
            height : 400,
            setup : {
                uiType : 'ui/list/list',
                uiConf : {
                    arenaClass : "edit-pick-area",
                    escapeHtml : false,
                    icon  : '<i class="zmdi zmdi-view-dashboard"></i>',
                    idKey : "areaId",
                    display : function(ao) {
                        if(!ao.areaId) {
                            return '<em>'+UI.msg("hmaker.edit.none_area")+'</em>';
                        }
                        return '<i class="zmdi zmdi-view-dashboard"></i>'
                                + '<span>' + ao.areaId + '</span>';
                    },
                    on_draw_item : function(jItem, ao) {
                        if(ao.__used) {
                            jItem.attr("ao-used", "yes");
                        }
                    }
                }
            },
            ready : function(){
                this.setData(items);
                console.log(opt.areaId)
                this.setActived(opt.areaId);
            },
            ok : function() {
                var areaId = this.getActivedId();
                $z.invoke(opt, "callback", [areaId]);
            }
        }, UI);

    },
    //...............................................................
    // 将 SRC 变成 /o/read/xxx 的模式
    // 可能返回三个种值:
    //  - "http:/xxx"   : 字符串表示外链
    //  - null          : 表示文件不存在
    //  - {...}         : 对应的 Obj
    explain_src : function(src, oHome, oPage) {
        var UI = this;
        // 指定了绝对路径
        if(/^https?:\/\//i.test(src)) {
            return src;
        }
        // 指定了文件对象
        if(/^id:[\w\d]+/.test(src)) {
            return Wn.get(src, true);
        }
        // 指定了相对站点的路径
        else if(/^\//.test(src)){
            oHome = oHome || UI.getHomeObj();
            src = "id:" + oHome.id + src;
            // 试图检查一下这个对象是否存在
            var re = Wn.exec('obj ' + src);
            // 不存在
            if(/^e./.test(re)) {
                return null;
            }
            // 返回对象
            var o = $z.fromJson(re);
            Wn.saveToCache(o);
            return o;
        }
        // 默认是指定了相对页面的路径
        oPage = oPage || UI.pageUI().getCurrentEditObj();
        src = "id:" + oPage.pid + "/" + src;
        // 试图检查一下这个对象是否存在
        var re = Wn.exec('obj ' + src);
        // 不存在
        if(/^e./.test(re)) {
            return null;
        }
        // 返回对象
        var o = $z.fromJson(re);
        Wn.saveToCache(o);
        return o;
    },
    //...............................................................
    tidy_src : function(src, oHome, oPage) {
        // 已经被整理过了
        if(/^\/o\/read\/id:/.test(src))
            return src;

        // 找到对应的媒体文件
        console.log(src);
        var oMedia = this.explain_src(src, oHome, oPage);
        if(oMedia){
            return "/o/read/id:" + oMedia.id;
        }
        return src;
    },
    //...............................................................
    __form_fld_pick_folder : function(fld) {
        var UI = this;
        return {
            key    : fld.key,
            title  : UI.text(fld.title),
            type   : "string",
            dft    : null,
            uiType : "ui/picker/opicker",
            uiConf : {
                setup : {
                    lastObjId : fld.lastObjId,
                    filter    : function(o) {
                        return 'DIR' == o.race;
                    },
                    objTagName : 'SPAN',
                },
                parseData : function(str){
                    var m = /id:(\w+)/.exec(str);
                    if(m)
                        return Wn.getById(m[1], true);
                    if(str)
                        return Wn.fetch(str, true);
                    return null;
                },
                formatData : function(o){
                    return o ? "~/" + Wn.getRelativePathToHome(o) : null;
                }
            }
        };
    },
    //...............................................................
    // 这里的 CSS 属性名，需要用驼峰
    getCssFieldConf : function(key, dft, title, tip, fldKey) {
        var UI = this;

        // 显示简单输入框的
        if(/^(width|height|borderRadius|boxShadow|textShadow|letterSpacing|fontSize|lineHeight|maxWidth|maxHeight|minWidth|minHeight)$/.test(key)
            || /^(border(Top|Bottom|Left|Right)?(Width|Style)?)$/.test(key)
            || /^(padding(Top|Bottom|Left|Right)?)$/.test(key)
            || /^(margin(Top|Bottom|Left|Right)?)$/.test(key)) {
            return {
                key    : fldKey || key,
                title  : title || "i18n:hmaker.prop." + key,
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "input"
            };
        }
        // 字体选择
        if("fontFamily" == key) {
            return {
                key    : fldKey || "fontFamily",
                title  : title || "i18n:hmaker.prop.fontFamily",
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "droplist",
                uiConf : {
                    value : function(it){return it;},
                    emptyItem : {},
                    items:[
                        "Verdana",
                        "Arial",
                        "Consolas",
                        "Times New Roman",
                        "Comic Sans MS",
                        "Courier New",
                    ]}
            };
        }
        // 字体风格
        if("_font" == key) {
            return {
                key    : fldKey || "_font",
                title  : title || "i18n:hmaker.prop._font",
                tip    : tip,
                dft    : dft || "",
                type   : "object",
                simpleKey : true,
                editAs : "switch",
                uiConf : {
                    multi : true,
                    singleKeepOne : false,
                    items:[
                        {icon:'<i class="fa fa-bold"></i>',value:"bold"},
                        {icon:'<i class="fa fa-italic"></i>',value:"italic"},
                        {icon:'<i class="fa fa-underline"></i>',value:"underline"},
                    ]}
            };
        }
        // 不带 justify 的 textAlign
        if("_align" == key) {
            return {
                key    : fldKey || "textAlign",
                title  : title || "i18n:hmaker.prop._align",
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "switch",
                uiConf : {
                    singleKeepOne : false,
                    items:[
                        {tip:"i18n:hmaker.prop.ta_left",    value:"left",    icon:'<i class="zmdi zmdi-format-align-left"></i>'},
                        {tip:"i18n:hmaker.prop.ta_center",  value:"center",  icon:'<i class="zmdi zmdi-format-align-center"></i>'},
                        {tip:"i18n:hmaker.prop.ta_right",   value:"right",   icon:'<i class="zmdi zmdi-format-align-right"></i>'},
                    ]}
            };
        }
        // 全本 textAlign
        if("textAlign" == key) {
            return {
                key    : fldKey || "textAlign",
                title  : title || "i18n:hmaker.prop.textAlign",
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "switch",
                uiConf : {
                    singleKeepOne : false,
                    items:[
                        {tip:"i18n:hmaker.prop.ta_left",    value:"left",    icon:'<i class="zmdi zmdi-format-align-left"></i>'},
                        {tip:"i18n:hmaker.prop.ta_center",  value:"center",  icon:'<i class="zmdi zmdi-format-align-center"></i>'},
                        {tip:"i18n:hmaker.prop.ta_right",   value:"right",   icon:'<i class="zmdi zmdi-format-align-right"></i>'},
                        {tip:"i18n:hmaker.prop.ta_justify", value:"justify", icon:'<i class="zmdi zmdi-format-align-justify"></i>'},
                    ]}
            };
        }
        if(/^((color)|(border(Left|Right|Bottom|Top)?Color))$/.test(key)) {
            return {
                key    : fldKey || "color",
                title  : title || "i18n:hmaker.prop.color",
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "color",
            };
        }
        if("background" == key) {
            return {
                key    : fldKey || "background",
                title  : title || "i18n:hmaker.prop.background",
                tip    : tip,
                dft    : dft || null,
                type   : "object",
                simpleKey : true,
                nullAsUndefined : true,
                editAs : "background",
                uiConf : UI.getBackgroundImageEditConf()
            };
        }
        if("overflow" == key) {
            return {
                key    : fldKey || "overflow",
                title  : title || "i18n:hmaker.prop.overflow",
                tip    : tip,
                dft    : dft || "",
                type   : "string",
                simpleKey : true,
                editAs : "switch", 
                uiConf : {
                    singleKeepOne : false,
                    items : [{
                        text : 'i18n:hmaker.prop.overflow_visible',
                        val  : 'visible',
                    }, {
                        text : 'i18n:hmaker.prop.overflow_auto',
                        val  : 'auto',
                    }, {
                        text : 'i18n:hmaker.prop.overflow_hidden',
                        val  : 'hidden',
                    }]
                }
            };
        } // end overflow
    },
    //=========================================================
}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});

