define(function (require, exports, module) {
// 链入的 cSS
seajs.use("ui/support/theme/walnut-" + window.$zui_theme + ".css");
// 声明帮助方法
var Wn = {
    //=======================================================================
    // 获取当前的 app 的通用方法，不建议 UI 们直接获取 window._app
    // 因为以后这个对象可能会被改名或变到别的地方
    app : function(){
        return window._app;
    },
    env : function(key, dft) {
        return this.app().session.envs[key] || dft;
    },
    //...................................................................
    // 生成标准缩略图的 DOM 结构
    gen_wnobj_thumbnail_html : function(nmTagName, innerOnly){
        nmTagName = nmTagName || 'a';
        var html = innerOnly ? '' : '<div class="wnobj">';
        html += '<div class="wnobj-wrapper">';
        //-------------------------------------------
        html += '<div class="wnobj-seq"><span>0</span></div>'
        html += '<div class="wnobj-del"><i class="fa fa-close"></i></div>'
        html += '<div class="wnobj-thumbnail">';
        html += '<div class="img">';
        html += '<div class="wnobj-NW wnobj-icon-hide"></div>';
        html += '<div class="wnobj-NE wnobj-icon-hide"></div>';
        html += '<div class="wnobj-SW wnobj-icon-hide"></div>';
        html += '<div class="wnobj-SE wnobj-icon-hide"></div>';
        html += '</div>';
        html += '</div>';
        // 名称
        if(_.isString(nmTagName))
            html += '<div class="wnobj-nm-con"><'+nmTagName+' class="wnobj-nm"></'+nmTagName+'></div>';
        // 进度
        html += '<div class="wnobj-ing"><div class="wnobj-ing-W">';
        html += '<div class="wnobj-ing-nb">0%</div>';
        html += '<div class="wnobj-ing-bar"><span></span></div>';
        html += '</div></div>';
        //-------------------------------------------
        html += '</div>';
        html += innerOnly ? '' : '</div>';
        
        // 返回
        return html;
    },
    //...................................................................
    // 生成一个缩略图的 jQuery 对象，但是没加入 DOM 树
    gen_wnobj_thumbnail : function(UI, o, nmTagName, evalThumb, nmMaxLen){
        var jq = $(Wn.gen_wnobj_thumbnail_html(nmTagName));
        Wn.update_wnobj_thumbnail(UI, o, jq, evalThumb, nmMaxLen);
        return jq;
    },
    //...................................................................
    // 根据对象，填充给定的一段 DOM 中的缩略图和名称
    update_wnobj_thumbnail : function(UI, o, jq, evalThumb, nmMaxLen){
        var jThumb = jq.find(".wnobj-thumbnail");
        
        // 对象不存在，那么显示删除
        if(!o) {
            jThumb.attr("noexists","yes");
            return;
        }
        
        // 标记关键属性
        jq.attr("oid",o.id).attr("onm", o.nm).attr("otp", o.tp || "");

        // 标记隐藏文件
        if(/^[.].+/.test(o.nm)){
            jq.addClass("wnobj-hide");
        }
        
        // 得到缩略图角标
        if(_.isFunction(evalThumb)){
            var thumbIcons = evalThumb(o);
            if(thumbIcons){
                // 统一设定 className
                if(thumbIcons.className){
                    jThumb.addClass(thumbIcons.className);
                }
                // 依次设置各个角标
                for(var tiKey in thumbIcons){
                    var jThumbIcon = jThumb.find(".wnobj-"+tiKey);
                    jThumbIcon.html(thumbIcons[tiKey]).removeClass("wnobj-icon-hide");
                }
            }
        }
        // 设置缩略图地址
        var url = o.id ? "url(/o/thumbnail/id:"+encodeURIComponent(o.id)+"?sh=64)"
                       : "url(/o/thumbnail/type:"+encodeURIComponent(o.tp)+"?sh=64)";
        jThumb.find(".img").css("background-image", url);
        jThumb.attr("thumb", o.thumb);

        // 填充对象名称
        var jNm = jq.find(".wnobj-nm");
        if(jNm.length > 0) {
            var nmText = o.nm; 
            if(UI && _.isFunction(UI.text))
                nmText = UI.text(nmText);
            nmText = this.objDisplayName(UI, nmText, nmMaxLen);
            
            jNm.text(nmText)
            // 标记链接
            if(jNm[0].tagName == 'A' && o.id){
                jNm.prop("target", "_blank");
                jNm.prop("href","/a/open/"+(window.wn_browser_appName||"wn.browser")+"?ph=id:"+o.id);
            }
        }
    },
    //...................................................................
    objIconHtml : function(o){
        // 有了指定的 icon
        if(o.icon)
            return o.icon;
        // 主目录特殊显示
        if(this.app().session.envs.HOME == o.ph){
            return '<i class="fa fa-home" style="font-size:1.2em;"></i>';
        }
        // 采用自身的文件类型
        return o.icon 
            || '<i class="oicon '
                + ('DIR'== o.race ? 'oicon-folder' : '')
                + '" otp="'
                + this.objTypeName(o)
                + '"></i>';;
    },
    //...................................................................
    objTypeName : function(o){
        return o.tp || ('DIR'==o.race ? 'folder' : 'unknown');
    },
    //...................................................................
    // 本地化字符串 fnm.xxx 声明的名称，不可编辑，否则本地化失败
    isObjNameEditable : function(UI, o) {
        var nm  = _.isString(o) ? o : o.nm;
        var nms = UI.msg("fnm") || {};
        return nms[nm] ? false : true;
    },
    //...................................................................
    objDisplayName : function(UI, nm, maxLen){
        var text = _.isString(nm) ? nm : nm.nm;
        if(!text)
            return "??";
        // 默认大小
        if(!_.isNumber(maxLen)){
            maxLen = 20;
        }
        // 翻译多国语言
        if(/^i18n:.+$/g.test(text)){
            text = UI.msg(text.substring(5));
        }
        // _key_xxx 格式
        else {
            var nms = UI.msg("fnm") || {};
            text = nms[text] || text;
        }

        // 加入省略号
        if(maxLen > 0)
            return $z.ellipsisCenter(text, maxLen);
        return text;
    },
    //...................................................................
    // offset : >0 从正面算，<0 从后面算
    // 如果是个函数，必须返回数组 [beginIndex, lastIndex]
    // 其中 lastIndex 与 offset 等意，如果返回不是二元数组，继续迭代
    // wrapper : F(nms, index):HTML 一个包裹函数，默认用 <b> 包裹，并对显示名称 i18n
    //           如果返回空，则忽略输出
    // sep : 一段 HTML，用来分隔个个路径，默认为 <em>/</em>
    objDisplayPath : function(UI, ph, offset, wrapper, sep){
        var Wn  = this;
        var nms = ph;
        if(!_.isArray(ph)){
            nms = _.without(ph.split("/"), "");
        }
        
        // 默认包裹函数
        wrapper = wrapper || function(nms, index){
            return '<b>' + Wn.objDisplayName(UI, nms[index], 0) + '</b>';
        };
        // 开始的下标
        var beginIndex = 0;
        // 判断 offset
        if(_.isFunction(offset)){
            for(var index = 0; index<nms.length; index++){
                var ns = offset(nms, index);
                if(_.isArray(ns) && ns.length == 2){
                    beginIndex = ns[0];
                    offset = ns[1];
                    break;
                }
            }
        }
        // 那么 offset 被假设为一个数字，小于 0 从后面来
        if(offset < 0) {
            offset = Math.max(beginIndex, nms.length + offset);
        }

        var thePath = offset > 0 ? "/" + nms.slice(0, offset).join("/") : "";
        var ary = [];
        for(var i=(offset||0); i<nms.length; i++){
            // 得到对象的 title
            thePath += "/" + nms[i];
            var o = Wn.fetch(thePath, true);
            if(o && o.title) {
                nms[i] = o.title;
            }

            // 得到显示的 HTML
            var html = wrapper(nms, i);
            if(html)
                ary.push(html);
        }
        return ary.join(sep || '<em>/</em>');
    },
    /*...................................................................
    提供一个通用的创建界面，可以在给定的目录对象下面创建一个对象
    app clist 命令会提供数据方面的帮助
    */
    createPanel: function(o, callback, clist, opt){
        var context = this;
        // 打开遮罩
        var MaskUI    = require("ui/mask/mask");
        new MaskUI({
            width : 640,
            height: 480,
            setup : {
                uiType : "ui/o_create/o_create",
                uiConf : _.extend({
                    on_ok : function(o){
                        $z.doCallback(callback, [o], context);
                        this.parent.close();
                    },
                    on_cancel : function(){
                        this.parent.close();
                    }
                }, opt)
            }
        }).render(function(){
            this.body.update(o, clist);
        });
    },
    /*...................................................................
    提供一个通用的文件上传界面
    */
    uploadPanel: function (opt) {
        var MaskUI    = require("ui/mask/mask");
        var UploadUI  = require("ui/upload/upload");

        var mask_options = _.extend({
            closer: true,
            escape: true,
            width: 460,
            height: 500,
            setup : {
                uiType : "ui/upload/upload",
                uiConf : _.extend({}, opt)
            }
        }, opt);

        var maskUI = new MaskUI(mask_options);
        maskUI.render();

        return maskUI;
    },
    /*................................................................
    提供一个文件对象选择（上传）框的快捷方法，参数为
    {
        icon  : '<xxxx>', // 对话框标题的图标
        title : "xxx",    // 对话框标题，不支持i18n格式 
        mask  : {.. @see ui/mask/mask 的配置规范 ..},
        body  : {.. @see ui/support/select_file 的配置规范 ..}
        on_ok     : F(objs);
        on_cancel : F();
    }
    */
    selectFilePanel : function(opt) {
        opt = opt || {}
        var MaskUI = require("ui/mask/mask");

        new MaskUI(_.extend({
            dom    : "ui/pop/pop.html",
            css    : "ui/pop/theme/pop-{{theme}}.css",
            exec   : Wn.exec,
            app    : Wn.app(),
            closer : true,
            escape : true,
            width  : "60%",
            height : "80%",
            setup  : {
                uiType : "ui/support/select_file",
                uiConf : _.extend({}, opt.body)
            },
            events : {
                "click .pm-btn-ok" : function(){
                    var data = this.body.getData();
                    this.close();
                    $z.invoke(opt, "on_ok", [data]);
                },
                "click .pm-btn-cancel" : function(){
                    this.close();
                    $z.invoke(opt, "on_cancel", []);
                }
            },
        }, opt.mask)).render(function(){
            this.$main.find(".pm-title")
                .append($(opt.icon))
                .append($('<b>').text(opt.title || this.msg("selectFile")));
        });
    },
    /*................................................................
    在列表中显示一个对象的上传进度
    - f : 要上传的文件对象 (File)
    - opt : {
        overwrite : true,    // 重名的的是否覆盖

        // 上传完毕后的回调。给你的 callback 函数必须要调用
        // 它是用来清理上传进度标志的
        done : F(newObj, callback)

        // 新对象是否要滚动到显示
        scrollIntoView : true
    }
    - index : 第几个要上传的文件（0Base）。不传默认认为的 0 （第一个）
              本函数遇到重名的对象，会弹出覆盖确认框。确认框有「不再提示」选项
              勾选了以后，所有 index > 0 的项目将不再提问
    */
    uploadToList : function(f, opt, index) {
        opt = opt || {};
    },
    /*................................................................
    格式化命令执行面板的参数
     - cmdText  : "xxxx"        # 命令文本
     - maskConf : {..}          # 可选，是弹出的遮罩层的配置信息
     - callback : {c}F(re)      # 可选，命令执行完毕后的回调，参数为命令的返回
    或者，你可以用更精细的方法来调用
    xxxpanel({
       cmdText  : "xxxxx",
       maskConf : {...},
       // .. 下面的参数字段与 exec 相同
       async
       complete
       done
       fail
       context
       // msgShow,msgError,msgEnd 会被本函数覆盖，你设置了也木用
    });
    */
    __fmt_cmd_panel_opt : function(cmdText, maskConf, callback, referUI) {
        var opt;
        // 给入命令文本 
        if(_.isString(cmdText)){
            opt = {cmdText: cmdText};
            // 第二个参数是回调
            if(_.isFunction(maskConf)){
                opt.maskConf = {};
                opt.complete = maskConf;
                opt.referUI = callback;
            }
            // 第二个参数就是预先显示的信息
            else if(_.isString(maskConf)) {
                opt.maskConf = {
                    welcome : maskConf
                };
                opt.complete = callback;
                opt.referUI  = referUI;
            }
            // 第二个参数是弹出层配置
            else{
                opt.maskConf = maskConf || {};
                opt.complete = callback;
                opt.referUI  = referUI;
            }
        }
        // 直接就是配置项
        else{
            opt = cmdText;
            $z.setUndefined(opt, "referUI", maskConf);
        }
        // 返回
        return opt;
    },
    //................................................................
    // 一个假名，用来兼容
    logpanel : function(cmdText, maskConf, callback){
        //this.logPanel(cmdText, maskConf, callback);
        // 格式化参数
        // var opt = Wn.__fmt_cmd_panel_opt(cmdText, maskConf, callback);
        // // 显示遮罩
        // var MaskUI = require('ui/mask/mask');
        // new MaskUI(_.extend({
        //     width : "60%"
        // }, opt.maskConf)).render(function(){
        //     // 得到遮罩实例
        //     var uiMask = this;

        //     // 准备输出 DOM
        //     var jPre = $('<pre class="wn-log-panel">').appendTo(this.$main);
            
        //     // 预先显示信息
        //     if(opt.maskConf.welcome) {
        //         $('<div>').html(opt.maskConf.welcome).appendTo(jPre);
        //     }
        //     // 执行命令
        //     Wn.exec(opt.cmdText, {
        //         msgShow : function(str){
        //             $('<div class="msg-info">')
        //                 .text(str)
        //                 .appendTo(jPre)[0].scrollIntoView({
        //                     block: "end", behavior: "smooth"
        //                 });
        //         },
        //         msgError : function(str){
        //             $('<div class="msg-err">')
        //                 .text(str)
        //                 .appendTo(jPre)[0].scrollIntoView({
        //                     block: "end", behavior: "smooth"
        //                 });
        //         },
        //         done : function(re){
        //             $z.invoke(opt, "done", [re], uiMask);
        //         },
        //         fail : function(re){
        //             $z.invoke(opt, "fail", [re], uiMask);
        //         },
        //         complete : function(re){
        //             $z.invoke(opt, "complete", [re], uiMask);
        //         }
        //     });
        // });
        this.loggingPanel(cmdText, maskConf, callback);
    },
    //................................................................
    // 执行一个命令，并且在一个弹出的日志窗口显示命令的返回情况
    // 参数 See #__fmt_cmd_panel_opt
    loggingPanel : function(cmdText, maskConf, callback, referUI){
        //this.logPanel(cmdText, maskConf, callback);
        // 格式化参数
        var opt = Wn.__fmt_cmd_panel_opt(cmdText, maskConf, callback);
        referUI = referUI || {};
        // 显示遮罩
        var MaskUI = require('ui/mask/mask');
        new MaskUI(_.extend({
            width : "60%"
        }, opt.maskConf, {
            i18n  : referUI._msg_map,
            exec  : referUI.exec,
            app   : referUI.app,
            setup : {
                uiType : 'ui/support/cmd_log',
                uiConf : {
                    welcome  : opt.maskConf.welcome,
                    cmdText  : opt.cmdText,
                    done     : opt.done,
                    fail     : opt.fail,
                    complete : opt.complete,
                }
            }
        })).render();
    },
    //................................................................
    // 执行一个命令，并且在一个弹出的日志窗口显示命令的进度情况
    // 参数 See #__fmt_cmd_panel_opt
    // 所谓进度情况，会根据命令的输出，查看有没有 %[23/78] 这种格式的字符串
    // 如果有，则表示进度更新
    // 本面板不同于 logpanel，它显示仅仅是滚动显示单行日志
    processPanel : function(cmdText, maskConf, callback){
        // 格式化参数
        var opt = Wn.__fmt_cmd_panel_opt(cmdText, maskConf, callback);
        // 显示遮罩
        var MaskUI = require('ui/mask/mask');
        new MaskUI(_.extend({
            width : "60%",
            events : {
                'click .wn-process-panel footer' : function(e){
                    var jP = $(e.currentTarget).closest(".wn-process-panel");
                    $z.toggleAttr(jP, "show-log");
                }
            }
        }, opt.maskConf)).render(function(){
            var uiMask = this;
            // 准备 DOM
            var jP = $('<div class="wn-process-panel" st="ing">'
                +'<header>'
                +'<h4>Processing ... </h4>'
                +'<div st="ing"><i class="fa fa-spinner fa-pulse fa-fw"></i></div>'
                +'<div st="ok"><i class="zmdi zmdi-check-circle"></i></div>'
                +'<div st="fail"><i class="zmdi zmdi-alert-triangle"></i></div>'
                +'</header>'
                +'<section>'
                +'<div class="wpp-bar"><em>0%</em><b><i style="width:0%;"></i></b></div>'
                +'<div class="wpp-msg"></div>'
                +'</section>'
                +'<pre></pre>'
                +'<footer>'
                +'<i class="zmdi zmdi-alert-circle"></i>'
                +'<i class="zmdi zmdi-format-subject"></i>'
                +'</footer>'
            +'</div>').appendTo(this.$main);

            // 得到关键的 DOM 节点
            var jStatus = jP.find("header");
            var jSec    = jP.find("section");
            var jMsg    = jP.find(".wpp-msg");
            var jBar    = jP.find(".wpp-bar");
            var jBarEm  = jP.find(".wpp-bar > em");
            var jBarIn  = jP.find(".wpp-bar > b > i");
            var jPre    = jP.find("pre");

            // 收集 100% 后面的输出
            var res = [];

            // 预先显示信息
            if(opt.maskConf.welcome) {
                jStatus.find("h4").html(opt.maskConf.welcome);
            }

            // 执行命令
            Wn.exec(opt.cmdText, {
                msgShow : function(str){
                    var lines = str.split(/[\r\n]+/g);
                    if(lines && lines.length > 0) {
                        for(var line of lines) {
                            // 分析有没进度
                            var regex = /%\[[ ]*(-?\d+)\/(-?\d+)\]/g;
                            var m = regex.exec(line);
                            var msg = line;
                            if(m) {
                                var nb   = m[1] * 1;
                                var sum  = m[2] * 1;
                                // 更新进度
                                if(sum > 0 && nb >= 0) {
                                    var per  = parseInt(100 * nb / sum);
                                    var perS = per + "%";
                                    jBarEm.text(perS);
                                    jBarIn.css("width", perS);
                                    msg = $.trim(line.substring(regex.lastIndex+1));
                                }
                                // 收集后续输出
                                else {
                                    msg = $.trim(line.substring(regex.lastIndex+1));
                                    res.push(msg);
                                }
                            }
                            // 显示消息
                            if(msg){
                                jMsg.text(msg);
                            }
                        }
                    }

                    // 计入
                    $('<div class="msg-info">')
                        .text(str).appendTo(jPre);
                },
                msgError : function(str){
                    $('<div class="msg-err">')
                        .text(str).appendTo(jPre);
                },
                done : function(){
                    jStatus.find("h4").html(
                        opt.maskConf.titleDone || "Done"
                    );
                    jP.attr("st", "ok");
                },
                fail : function(){
                    jStatus.find("h4").html(
                        opt.maskConf.titleDone || "Fail"
                    );
                    jP.attr("st", "fail");
                },
                complete : function(re){
                    jBarIn.css("width", "100%");
                    $z.invoke(opt, "complete", [res, jMsg, re], uiMask);
                }
            });
        });
    },
    //................................................................
    // 执行明后后，直接转换为json格式
    execJ : function(str, input, opt) {
        var result = this.exec(str, input, opt);
        try {
            return $z.fromJson(result);
        } catch (e) {
            throw e;
        }
    },
    //................................................................
    // 采用模板方式执行命令
    execf : function(tmpl, input, context, opt) {
        if(!_.isString(input)) {
            opt = context;
            context = input;

            var str = $z.tmpl(tmpl)(context);
            return this.exec(str, opt);
        }
        // 带 input 的模式
        var str = $z.tmpl(tmpl)(context);
        return  this.exec(str, input, opt);
    },
    /*................................................................
    # 执行命令的 opt 配置对象的内容
    {
        // 应用的名称命令执行时会自动添加环境变量 $WN_APP_HOME
        // 默认采用当前的 app 名称，如果设定的 app 找不到命令将拒绝执行
        appName     : "xxx" 
        
        // 传入输入流内容
        input : "xxxx"
        //..................................... 回调设定
        processData : false  // 调用结束回调的时候，是否先解析数据
        dataType : "json"    // 如果 processData 为 true 时的数据类型，默认JSON
        async    : true      // 指明同步异步，默认 true
        // 当得到返回的回调
        msgShow  : {c}F(line){..}      // 显示一行输出
        msgError : {c}F(line){..}      // 显示一行错误输出
        msgEnd   : {c}F(){..}          // 表示不会再有输出了
        // 请求完毕的回调
        complete : {c}F(content){..}   // 全部执行完
        done     : {c}F(content){..}   // 执行成功
        fail     : {c}F(Content){..}   // 执行失败
        // 所有回调的 this 对象，默认用本函数的 this
        context  : {..}
        // 传入 UI 对象，以便提供 alert 等操作
        // 如果不传，则会静默
        UI : null
    }
    // 其他模式
    exec(cmdText, input, complete);
    exec(cmdText, complete);
    // 如果 
    */
    exec : function (str, input, opt) {
        var app = window._app;
        var se = app.session;
        var re = undefined;

        // 没设置回调，则默认认为是同步调用
        if(_.isUndefined(input)){
            opt = {
                async : false,
                complete : function(content){
                    re = content;
                }
            };
        }
        // 一个回调处理所有的情况
        else if (_.isFunction(input)) {
            opt = {async:true, complete: input};
        }
        // 给入输入 
        else if(_.isString(input)){
            // 同步
            if(_.isUndefined(opt)){
                opt = {
                    async : false,
                    input : input,
                    complete : function(content){
                        re = content;
                    }
                }
            }
            // 异步
            else if(_.isFunction(opt)){
                opt = {async:true, complete: opt, input:input};
            }
            // 补充对象
            else if(_.isObject(opt)){
                opt.input = input;
            }
        }
        // 如果是直接是配置对象
        else if(input && _.isObject(input)) {
            opt = input;
        }

        // 有 opt 的情况，默认是异步
        $z.setUndefined(opt, "async", true);
        
        // 固定上下文
        var context = opt.context || this;

        // 没内容，那么就表执行了，直接回调吧
        str = (str || "").trim();
        if (!str) {
            $z.invoke(opt, "complete", [], context);
            return;
        }

        // 分析字符串，如果 %xxxx: 开头，表示模拟一个 APP
        // 那么后面的才是命令内容
        var m = /^%([^ :]+):(.+)$/.exec(str);
        var appName, cmdText;
        if(m){
            appName = m[1];
            cmdText = $.trim(m[2]);
        }else{
            cmdText = str;
        }

        // 处理命令 mos = (Macro Object Separator)
        var mos = "%%wn.meta." + $z.randomString(10) + "%%";
        //var regex = new RegExp("^(\n"+mos+":BEGIN:)(\\w+)(.+)(\n"+mos+":END\n)$","m");
        var mosHead = "\n" + mos + ":MACRO:";

        // 执行命令的地址
        var url = '/a/run/' + (appName || opt.appName || app.name);

        // 准备发送的数据
        var sendData = "mos=" + encodeURIComponent(mos);
        sendData += "&PWD=" + encodeURIComponent(se.envs.PWD);
        sendData += "&cmd=" + encodeURIComponent(cmdText);
        sendData += "&in=" + encodeURIComponent(opt.input||"");
        if(opt.foreFlushBuffer) {
            sendData += "&ffb=true"
        }

        var oReq = new XMLHttpRequest();
        oReq._last = 0;
        oReq._macro_begin_pos = -1;

        oReq._show_msg = function () {
            // 本次要处理的内容（已经读到的所有有效内容）
            var content = oReq.responseText;

            // 还没内容
            if(content.length <= oReq._last)
                return;

            //.........................................
            // 如果之前的 _show_msg 找到过宏，则什么都不做，直到最后执行宏的时候才解析
            if(oReq._macro_begin_pos>=0) {
                return;
            }

            // 看看内容是否包含宏
            oReq._macro_begin_pos = content.indexOf(mosHead, oReq._last > 128 ? (oReq._last - 128) : oReq._last);

            // 定义本次要处理的字符串
            var str;

            //.........................................
            // 如果找到了宏，宏之前算是可处理部分
            if(oReq._macro_begin_pos >= 0) {
                content = content.substring(oReq._last, oReq._macro_begin_pos);
                str = content;
                nextPos = oReq._macro_begin_pos;
            }
            //.........................................
            // 那么没找到过宏，就找到最后一个整行，作为本次的消息
            else {
                // 找到最后一个整行，以便保证是整行拼接的
                var lastEOL = oReq.responseText.lastIndexOf('\n');
                var nextPos = lastEOL + 1;
                str = oReq.responseText.substring(oReq._last, nextPos);
            }

            // 本次得到内容为空
            if(!str)
                return;

            // 正常显示
            if (oReq.status == 200) {
                $z.invoke(opt, "msgShow", [str], context);
            }
            // 错误显示
            else {
                $z.invoke(opt, "msgError", [str], context);
            }

            //console.log("_show_msg@" + (new Date()) + ":\n" + lastEOL + "/" + txtLen);
            // 标记下一次要处理的起始位置
            oReq._last = nextPos;
        };
        
        oReq.open("POST", url, opt.async);
        oReq.onreadystatechange = function () {
            //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
            // LOADING | DONE 只要有数据输入，显示一下信息
            if(oReq._show_msg)
                oReq._show_msg();
            // DONE: 请求结束了，调用回调
            if (oReq.readyState == 4) {
                //...............................................
                // 请求都处理完毕了，得到总体的文本
                var content    = oReq.responseText;
                var macro_str  = null;
                var macroArray = [];
                if(oReq._macro_begin_pos >= 0) {
                    content   = oReq.responseText.substring(0, oReq._macro_begin_pos);
                    macro_str = oReq.responseText.substring(oReq._macro_begin_pos);
                }
                //...............................................
                // 得到宏的内容，并进行解析
                if(macro_str) {
                    var pos_l = mosHead.length;
                    while(pos_l > 0) {
                        var macro = {};
                        // 找到行尾，作为宏类型
                        var pos_begin = macro_str.indexOf('\n', pos_l);
                        macro.type = $.trim(macro_str.substring(pos_l, pos_begin));
                        
                        // 找到下一个宏的开始作为本宏的内容
                        pos_l = macro_str.indexOf(mosHead, ++pos_begin);

                        // 如果没有宏了，剩下的内容作为宏的内容
                        if(pos_l <= pos_begin) {
                            macro.content = $.trim(macro_str.substring(pos_begin));
                        }
                        // 否则截取内容，并移动下标到下一个宏的开始
                        else {
                            macro.content = $.trim(macro_str.substring(pos_begin, pos_l));
                            pos_l += mosHead.length;
                        }

                        // 记录宏到数组，以便后续处理
                        macroArray.push(macro);
                    }
                }
                //...............................................
                // 处理请求的状态更新命令
                for (var macro of macroArray) {
                    // 修改环境变量
                    if ("update_envs" == macro.type) {
                        app.session.envs = $z.fromJson(macro.content);
                    }
                    // 修改当前会话
                    else if("change_session" == macro.type) {
                        var data = $z.fromJson(macro.content);
                        // 有新回话，试图切换一下
                        if(data && data.seid){
                            $.ajax({
                                type : "GET",
                                async:false, 
                                url  : "/u/ajax/chse",
                                data : data,
                                success : function(re) {
                                    var reo = $z.fromJson(re);
                                    if(reo.ok)
                                        app.session = reo.data;
                                    else
                                        $z.invoke(opt, "msgError", [re], context);
                                }
                            });
                        }
                        // 否则重定向
                        else {
                            window.location = "/";
                        }
                    }
                }
                //...............................................
                // 最后确保通知了显示流结束
                $z.invoke(opt, "msgEnd", [], context);
                //...............................................
                // 准备调用结束的回调 done/fail/complete
                var re = content;
                //...............................................
                // 准备函数
                var funcName = oReq.status == 200 ? "done" : "fail";
                //...............................................
                // 执行回调前数据处理
                if(opt.processData){
                    // 处理错误
                    if(/^e[.]/.test(re)) {
                        funcName = "fail";
                        // 主动报错
                        if(opt.UI) {
                            opt.UI.alert(re, "warn");
                        }
                    }
                    // 处理 json
                    else if("json" == opt.dataType){
                        re = $z.fromJson(re);
                        // 检查是不是 session 过期了，如果过期了，直接换地址
                        $z.checkSessionNoExists(re);
                    }
                }
                //...............................................
                // 调用完成后的回调
                $z.invoke(opt, funcName,   [re], context);
                $z.invoke(opt, "complete", [re], context);
                //...............................................
            }
        };
        oReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        // oReq.send("cmd=" + cmdText);
        //oReq.setRequestHeader("Content-length", sendData.length);
        oReq.send(sendData);
        //oReq.send("mos=haha&pwd=/home/zozoh&cmd=cd")

        // 返回返回值，如同是同步的时候，会被设置的
        return re;
    },
    //..............................................
    /* 根据一个对象获取其应用配置信息
     opt - {
        forceTop: true    // 是否为每个菜单项强制加上 "@"
        editor  : "xxx"   // 【选】采用指定的编辑器 
        tp      : "xxx"   // 【选】采用指定对象类型的菜单
        context : this    // 回调的上下文
        inEditor: false   // 是否指定在编辑器，如果是未定义
                          // 则 asetup.editors 数组有内容就表示在编辑器内
     }
    */
    loadAppSetup : function(o, opt, callback){
        // 格式化选项
        if(_.isFunction(opt)){
            callback = opt;
            opt = {forceTop : false};
        }
        else if(_.isBoolean(opt)){
            opt = {forceTop : opt};
        }
        else if(!opt){
            opt = {forceTop : false};
        }

        // 分析一下，如果 theEditor 格式类似  type:xxxx 那么就不是编辑器
        if(opt.editor){
            var m = /^type:(.+)$/.exec(opt.editor);
            if(m) {
                opt.tp = m[1];
                opt.editor = null;
            }
        }

        // 开始从服务器获取数据
        var Wn = this;
        var cmdText = "app setup id:"+o.id;
        if(opt.tp)
            cmdText += " -tp " + opt.tp;
        Wn.exec(cmdText, function(json){
            var asetup = $z.fromJson(json);
            
            // 强制使用指定的编辑器
            if(opt.editor){
                asetup.editors = [opt.editor];
            }

            // 展开
            Wn.extendAppSetup(asetup, opt.forceTop, opt.inEditor);

            // 调用回调
            if(_.isFunction(callback)){
                callback.call(opt.context || Wn, o, asetup);
            }
        });
    },
    extendAppSetup : function(asetup, forceTop, isInEditor){
        var Wn = this;
        asetup.editors = asetup.editors || [];
        asetup.actions = asetup.actions || [];
        isInEditor = _.isUndefined(isInEditor)
                            ? asetup.editors.length > 0
                            : isInEditor;
        // 首先读取编辑器
        if(isInEditor){
            // 得到默认编辑器
            var edtnm = asetup.editors[0];
            var json = Wn.exec('app editor -cq ' + edtnm);

            // 出错了
            if(/^e./.test(json)) {
                alert(json);
                throw json;
            }

            // 分析
            var ace  = $z.fromJson(json);
            asetup.currentEditor = ace;
                
            // 如果编辑器声明了菜单，将其替换
            if(_.isArray(ace.actions)){
                asetup.actions = ace.actions;
            }
        }

        // 展开动作组
        asetup.menu = Wn.extendActions(asetup.actions, forceTop, isInEditor);
    },
    extendActions : function(actions, forceTop, isInEditor){
        var UI = this;
        // 分析菜单项，生成各个命令的读取路径
        // actions 的格式类似 ["@:r:new", ":w:tree", "::search", "~", "::properties"]
        // @ 开头的项表示固定显示
        var ac_nms     = [];  // 收集动作名称
        var menu_setup = [];  // 顶级菜单的下标
        var menu_items = [];  // 折叠的项目的下标
        actions.forEach(function(str, index){
            // 分隔符，特殊处理
            if(str == "~"){
                menu_items.push({type:'separator'});
                if(forceTop){
                    menu_setup.push(index);
                }
                return;
            }
            var ss = str.split(":");
            // 在编辑器中，忽略 E
            if(isInEditor){
                if(ss[0].indexOf("E")>=0)
                    return;
            }
            // 不在编辑器中，忽略 e
            else{
                if(ss[0].indexOf("e")>=0)
                    return;
            }

            // 生成命令的读取路径
            menu_items.push(ac_nms.length);
            ac_nms.push(ss[2]);
            // 记录固定显示的项目下标
            if(forceTop || ss[0].indexOf("@")>=0)
                menu_setup.push(index);
        });
        // 逐次得到菜单的动作命令
        var re = Wn.exec("app actions " + ac_nms.join(" "));
        var alist = eval('(' + re + ')');
        for(var i=0; i<menu_items.length; i++){
            var index = menu_items[i];
            if(_.isNumber(index)){
                var mi = eval('(' + alist[index] + ')');
                if(mi.type=="group" || _.isArray(mi.items)){
                    mi._items_array = mi.items;
                    mi.items = function(jq, mi, callback){
                        var items = this.browser().extend_actions(mi._items_array, true);
                        callback(items);
                    };
                }
                mi.context = UI;
                menu_items[i] = mi;
            }
        }
        // 将有固定显示的项目移动到顶级
        for(var i=0; i<menu_setup.length; i++){
            var index = menu_setup[i];
            menu_setup[i] = menu_items[index];
            menu_items[index] = null;
        }

        // 消除被移除的项目
        menu_items = _.without(menu_items, null);

        if(menu_items.length>0){
            // 和折叠按钮有分隔符
            if(menu_setup.length>0){
                menu_setup.push({type:"separator"});
            }

            // 最后创建一个固定扳手按钮，以便展示菜单
            // ? 折叠按钮用 <i class="fa fa-ellipsis-v"></i> 如何 ?
            // ? 折叠按钮用 <i class="fa fa-bars"></i> 如何 ?
            menu_setup.push({
                type  : 'group',
                icon  : '<i class="fa fa-ellipsis-v"></i>',
                items : menu_items
            });
        }

        // 返回配置好的菜单命令
        return menu_setup;
    },
    //..............................................
    _index : {
        ph : {}
    },
    _storeAPI : sessionStorage,
    //..............................................
    // filter 表示对子节点的一些过滤
    //  - undefined    : 全部显示
    //  - "DIR"        : 仅目录
    //  - "FILE"       : 仅文件
    //  - {..}         : 一个搜索条件，比如 {tp:'jpeg', len:0}
    //  - F(o):boolean : 高级过滤方法 
    // callback : F(os) 可以有，也可以木有，木有的话就同步读，否则异步读
    // forceReload : 强制刷新，否则会尽量利用缓存，默认 false
    getChildren : function(o, filter, callback, forceReload){
        var Wn = this;

        // 支持同步方式
        if(_.isBoolean(callback)) {
            forceReload = callback;
            callback = undefined;
        }

        // 确保有路径
        if(!o.ph){
            o = Wn.getById(o.id);
        }

        // 过滤方法
        var do_filter = function(o, filter){
            var reList  = [];
            // 组合的过滤条件，预先编译
            if($z.isPlainObj(filter)){
                filter = _.matcher(filter);
            }
            // 字符串的话，不是 DIR|FILE 统统认为是正则表达式
            else if(_.isString(filter) && !/^(DIR|FILE)$/.test(filter)){
                filter = new RegExp(filter);
            }

            // 循环找一下
            for(var i=0;i<o.children.length;i++){
                var childId = o.children[i];
                var child = Wn.getById(childId);

                // 按照 race 过滤
                if(_.isString(filter) && filter != child.race){
                    continue;
                }
                // 正则表达式表示类型过滤类型
                else if(_.isRegExp(filter) && !filter.test(Wn.objTypeName(child))){
                    continue;
                }
                // 自定义过滤
                else if(_.isFunction(filter) && !filter(child)){
                    continue;
                }

                reList.push(child);
            }
            // 返回
            return reList;
        };

        // 定制回调
        var do_after_load = function(o, re){
            o.children = [];
            if(re && !/^e./.test(re)){
                var list  = $z.fromJson(re);
                for(var i=0;i<list.length;i++){
                    var child = list[i];
                    // 手工补充子的全路径
                    if(!child.ph){
                        child.ph = o.ph + "/" + child.nm;
                    }
                    Wn.saveToCache(child);
                    o.children.push(child.id);
                }
            }
            // 更新到缓存
            Wn.saveToCache(o);
        };

        // 无对象
        if(!o) {
            if(_.isFunction(callback)){
                callback([]);
            }
            return [];
        }

        // 重新从服务器读取
        if(forceReload || !o.children || o.children.length==0 ){
            // 有回调，异步
            if(_.isFunction(callback)){
                Wn.exec("obj id:"+o.id+"/* -l -limit 1000 -sort 'nm:1'", function(re){
                    do_after_load(o, re);
                    var reList = do_filter(o, filter);
                    callback(reList);
                });
                return;
            }
            // 否则同步
            else{
                 var re = Wn.exec("obj id:"+o.id+"/* -l -limit 1000 -sort 'nm:1'");
                 do_after_load(o, re);
                 var reList = do_filter(o, filter);
                 return reList;
            }
        }
        // 从缓存里读
        var reList = do_filter(o, filter);
        // 回调
        if(_.isFunction(callback)){
            callback(reList);
        }
        return reList;
    },
    //..............................................
    getAncestors : function(o, includeSelf){
        var Wn  = this;
        var list = [];
        // 不是根，还有
        if(o.ph.lastIndexOf("/")>0){
            var oP  = this.getById(o.pid);
            var ans = this.getAncestors(oP, true);
            list = list.concat(ans);
        }

        if(includeSelf){
            list.push(o);
        }

        // 否则，没父了，返回空数组吧
        return list;
    },
    //..............................................
    getAncestorPath : function(o, includeSelf){
        var ans  = this.getAncestors(o, includeSelf);
        var list = [];
        for(var i=0;i<ans.length;i++){
            list.push(ans[i].nm);
        }
        return "/" + list.join("/");
    },
    //..............................................
    batchRead : function(objList, callback, context){
        var Wn = this;
        // 建立一个等长的数组，用来存放返回内容
        var list = [];
        // 循环处理对象
        for(var i=0;i<objList.length;i++){
            var o = objList[i];
            // 如果是字符串，可能是表示 ID 或者路径
            if(_.isString(o)){
                objList[i] = Wn.get(o);
            }
            // 返回数组记录初始值
            list[i] = null;
        }

        // 如果是异步调用
        if(_.isFunction(callback)){
            objList.forEach(function(o, index){
                Wn.read(o, function(re){
                    list[index] = re;
                    // 如果全都完事了，才调用回调
                    for(var i=0;i<list.length;i++){
                        if(!list[i])
                            return;
                    }
                    callback.call(context || Wn, list);
                });
            });

            // 返回不重要，关键是要有
            return;
        }
        // 如果是同步调用 ...
        for(var i=0;i<objList.length;i++){
            list[i] = Wn.read(objList[i]);
        }
        return list;
    },
    //..............................................
    read : function(o, callback, context, forceReload){
        var Wn = this;
        var store = Wn._storeAPI;

        // 支持同步
        if(_.isBoolean(callback)){
            forceReload = callback;
            callback = undefined;
            context  = undefined;
        } 
        // 支持匿名 context 模式
        else if(_.isBoolean(context)) {
            forceReload = context;
            context = undefined;
        }

        // 如果是字符串，变对象
        if(_.isString(o)){
            o = Wn.get(o);
        }

        // 如果对象是挂载对象，不能给出 SHA1，就用 ID 作为 KEY
        var sha1Key = o.sha1 ? "SHA1:"+o.sha1 : "SHA1:!id:"+o.id;

        // 首选准备一个 ajax读取对象
        var ajaxConf = {
            url : "/o/read/id:"+encodeURIComponent(o.id),
            method : "GET",
            data : {
                sha1 : o.sha1
            },
            dataType : "text",
            error : function(){
                alert("Fail to load obj: "+o.id+":> "+o.ph);
            }
        };

        // 强制刷新，清除缓存，发送的请求也不带 sha1
        if(forceReload) {
            store.removeItem(sha1Key);
            ajaxConf.data = {}
        }

        // 异步的方式 ...........................................
        if(_.isFunction(callback)){
            // 没值，就直接返回空串吧
            if(o.len == 0){
                callback.call(context || Wn, "");
                return;
            }
            // 从本地存储里读取 sha1
            var reText = store.getItem(sha1Key);
            if(reText){
                callback.call(context || Wn, reText);
                return;
            }

            // 发送请求
            $.ajax(_.extend({
                async : false,
                success : function(re){
                    store.setItem(sha1Key, re);
                    callback.call(context || Wn, re);
                }
            }, ajaxConf));

            // 结束吧
            return;
        }

        // 同步的方式 ...........................................
        // 没值，就直接返回空串吧
        if(o.len == 0)
            return "";

        // 从本地存储里读取 sha1
        var reText = store.getItem(sha1Key);

        // 从服务器读取
        if(!reText){
            $.ajax(_.extend({
                async : false,
                success : function(re){
                    store.setItem(sha1Key, re);
                    reText = re;
                }
            }, ajaxConf));    
        }
        
        // 返回结果
        return reText;
    },
    //..............................................
    write : function(o, content, callback, context) {
        var Wn = this;
        var store = Wn._storeAPI;

        // 如果是字符串，变对象
        if(_.isString(o)){
            o = Wn.get(o);
        }

        // 对于挂载对象，不能保存
        if(o.mnt) {
            alert("save mount obj is forbidden");
            return;
        }

        // 首选准备一个 ajax读取对象
        var ajaxConf = {
            type: "POST",
            url: "/o/write/id:" + o.id,
            data: content || ""
        };

        var _save_to_cache = function(re){
            var reo = $z.fromJson(re);
            if(reo.ok){
                var newObj = reo.data;
                Wn.saveToCache(newObj);
                if(newObj.sha1){
                    var sha1Key = "SHA1:" + newObj.sha1;
                    store.setItem(sha1Key, content);
                }
                return newObj;
            }
            alert(reo.errCode + " : " + reo.msg);
            return null;
        };

        // 异步的方式 ...........................................
        if(_.isFunction(callback)){
            // 发送请求
            $.ajax(_.extend({
                async : false,
                success : function(re){
                    var newObj = _save_to_cache(re);
                    callback.call(context || Wn, newObj);
                }
            }, ajaxConf));

            // 结束吧
            return;
        }

        // 同步的方式 ...........................................
        // 没值，就直接返回空串吧
        if(o.len == 0)
            return "";

        // 从服务器读取
        var reObj;
        $.ajax(_.extend({
            async : false,
            success : function(re){
                reObj = _save_to_cache(re);;
            }
        }, ajaxConf));
        
        // 返回结果
        return reObj;
    },
    //..............................................
    get : function(o, quiet, force){
        if(/^id:\w{6,}$/.test(o) && o.indexOf("/")<0)
            return this.getById(o.substring(3), quiet, force);
        return this.fetch(o, quiet, force);
    },
    //..............................................
    getById : function(oid, quiet, force) {
        if(!oid)
            return null;

        var Wn = this;
        var store = Wn._storeAPI;
        // 首先获取缓存
        var key  = "oid:"+oid;
        var json = force ? null : store.getItem(key);

        // 有内容的话，看看是否过期了
        if(json){
            var o = $z.fromJson(json);
            var nowms = (new Date()).getTime();
            var du = 600000;  // 默认缓存 10 分钟
            // 不过期就返回
            if(((o.__local_cache||0) + du) > nowms )
                return o;
        }

        // 重新从服务器读取
        var re = Wn.exec("obj id:"+oid+" -P");

        // 对于不存在的处理
        if(/^e.io/.test(re)){
            if(quiet){
                return null;
            }
            alert("fail to found obj: " + oid);
            throw "fail to found obj: " + oid;
        }

        var o2 = $z.fromJson(re);
        Wn.saveToCache(o2);
        return o2; 
    },
    //..............................................
    // 根据一条命令的输出，获取 WnObj 对象
    fetchBy : function(cmdText, callback, quiet){
        var objs;
        // 处理参数 
        if(_.isBoolean(callback)){
            quiet = callback;  callback = null;
        }

        // 定义对象的处理，只有符号标准的对象，才会存到缓存里
        // var _cache_obj = function(obj){
        //     if(obj.id && obj.nm && obj.race && obj.ct && obj.lm && obj.c && obj.g){
        //         Wn.saveToCache(obj);
        //     }
        // };

        // 定义处理函数
        var handler = function(json){
            if(json){
                try{
                    objs = $z.fromJson(json);
                    // for issue #224 这种随意查询的对象，先不要缓存吧，否则会爆
                    // // 数组的话，循环处理对象
                    // if(_.isArray(objs)){
                    //     objs.forEach(function(obj){
                    //         _cache_obj(obj);
                    //     });
                    // }
                    // // 单个对象
                    // else{
                    //     _cache_obj(objs);
                    // }
                }
                // 处理错误 
                catch(E){
                    var eMsg = "cmd: " + cmdText + "\nreturn no json:\n" + json;
                    // 明确的抛错错误
                    if(!quiet) {
                        throw eMsg;
                        alert(eMsg);
                    }
                    // 就是警告一下
                    else if(console && _.isFunction(console.warn)){
                        console.warn(eMsg);
                    }
                }
            }
            // 最后调用回调
            $z.doCallback(callback, [objs]);
        };

        // 异步
        if(_.isFunction(callback)){
            Wn.exec(cmdText, function(re){
                handler(re);
            });
        }
        // 同步
        else {
            handler(Wn.exec(cmdText));
        }

        // 最后怎么都返回一下
        return objs;
    },
    //..............................................
    // 将一堆字符串合并成一个路径
    // @deprecated 请使用 $z.appendPath
    appendPath : function() {
        var paths = Array.from(arguments);
        if (paths && paths.length > 0) {
            var str = paths.join("/").toString();
            var ss  = str.split(/\/+/);
            return ss.join("/");
        }
        return null;
    },
    //..............................................
    // 得到绝对路径
    absPath : function(ph) {
        // 本来就是绝对路径
        if(/^\//.test(ph))
            return ph;

        // ~/xxxx 的路径
        if(/^~/.test(ph)){
            return this.appendPath(Wn.app().session.envs.HOME, ph.substring(1));
        }

        // 相对路径
        return this.appendPath(Wn.app().session.envs.PWD, ph);
    },
    //..............................................
    fetch : function(ph, quiet, force){
        var Wn = this;
        // 首先格式化 Path 到绝对路径
        // var ss = ph.split(/\/+/);
        // if(ss[0] == "~"){
        //     ss[0] = Wn.app().session.envs.HOME.substring(1);
        // }
        // var aph = "/" + ss.join("/");
        var aph = this.absPath(ph);

        // 首先试图从缓存里获取 ID
        var oid = Wn._index.ph[aph];

        // 找到了 ID 就用 ID 读一下
        if(oid)
            return Wn.getById(oid, quiet, force);

        
        // 木有？ 那就重新根据路径加载
        var re  = Wn.exec("obj '"+ph+"' -PA");

        // 对于不存在的处理
        if(/^e.io/.test(re)){
            if(quiet){
                return null;
            }
            alert("fail to found obj: " + ph);
            throw "fail to found obj: " + ph;
        }

        // 解析保存到缓冲
        var o2  = $z.fromJson(re);
        Wn.saveToCache(o2);
        return o2;
    },
    //..............................................
    // 返回一个对象的路径，如果是目录，一定会以 "/" 结尾
    getRegularPath : function(o){
        if('DIR' == o.race)
            return o.ph + "/";
        return o.ph;
    },
    //..............................................
    // 本函数主要用在打开浏览对象的对话框，
    // 总是要让弹出对话框路径是对象的父路径才好将对象高亮。
    // 同时也要考虑到 localKey 的问题
    //  - o  : 目标对象，如果为空，则会尝试用 localKey 来取得
    //  - UI : 相关的 UI，主要使用 local 接口
    //  - localKey : 如果给定对象为空，那么尝试读取 localKey 作为返回
    getBaseDirPath : function(o, UI, localKey) {
        // 如果是数组，那么取第一个
        if(_.isArray(o)){
            if(o.length<=0)
                return;
            o = o[0];
        }
        
        // 支持字符串形式 
        if(_.isString(o))
            o = this.get(o, true);

        // 试图从 local 获取上一次的记录
        if(!o && localKey) {
            return UI.local(localKey);
        }

        // 根据对象重新定义 base
        if(o) {
            // 主目录
            if("./" == Wn.getRelativePathToHome(o)) {
                return o.ph;
            }
            // 其他目录显示上一级
            return "id:"+o.pid;
        }
        // 默认返回 null
        return null;
    },
    //..............................................
    isInDir : function(oDir, o) {
        if(_.isString(oDir))
            oDir = this.get(oDir, true);
        if(_.isString(o))
            o = this.get(o, true);
        if(!oDir || !o)
            return false;
        var ph_dir = oDir.ph;
        var ph_obj = o.ph;
        return $z.isInPath(ph_dir, ph_obj);
    },
    //..............................................
    getRelativePath : function(oBase, o) {
        var ph_base = Wn.getRegularPath(oBase);
        var ph_obj  = Wn.getRegularPath(o);   
        return $z.getRelativePath(ph_base, ph_obj);
    },
    //..............................................
    getRelativePathToHome : function(o) {
        var oHome = Wn.getHome();
        var ph_home = Wn.getRegularPath(oHome);
        var ph_obj  = Wn.getRegularPath(o);   
        return $z.getRelativePath(ph_home, ph_obj);
    },
    //..............................................
    getHome : function(){
        return this.fetch("~");
    },
    //..............................................
    isHome : function(o){
        if(!o)
            return false;
        return o.id == this.getHome().id;
    },
    //..............................................
    // 根据 ID 列表批量获取对象的元数据
    batchGetById : function(ids, callback, noExistsAsNull){
        var Wn = this;
        var store = Wn._storeAPI;

        // 收集所有未加入缓存的项目
        var objs = [];
        var loadIds = [];
        for(var i=0; i<ids.length; i++){
            var oid  = ids[i];
            var key  = "oid:" + oid;
            var json = store.getItem(key);
            if(json){
                objs.push($z.fromJson(json));
            }else{
                objs.push(oid);
                loadIds.push(oid);
            }
        }

        // 声明处理函数
        var fill_unload_objs_and_invoke_callback = function(re){
            // 存入缓存
            var list = $z.fromJson(re);
            for(var i=0;i<list.length;i++){
                if(list[i])
                    Wn.saveToCache(list[i]);
            }

            // 更新未加载对象
            for(var i=0;i<objs.length;i++){
                var o = objs[i];
                if(_.isString(o)){
                    objs[i] = Wn.getById(o, true);
                }
            }

            // 调用回调
            if(_.isFunction(callback)){
                callback(objs);
            }
        };

        // 如果有需要读取的 ID，则发送请求
        if(loadIds.length>0){
            var cmdText = "obj id:" + loadIds.join(" id:") + " -lP -json -noexists null";
            // 异步调用
            if(_.isFunction(callback)){
                Wn.exec(cmdText, fill_unload_objs_and_invoke_callback);
            }
            // 同步调用
            else {
                var re = Wn.exec(cmdText);
                fill_unload_objs_and_invoke_callback(re);
            }
        }
        // 否则如果有回调就调用
        else if(_.isFunction(callback)){
            callback(objs);
        }

        // 最后返回
        return objs;
    },
    //..............................................
    getObjFromCache : function(oid){
        var key  = "oid:" + oid;
        var json = store.getItem(key);
        if(json)
            return $z.fromJson(json);
        return null;
    },
    //..............................................
    saveToCache : function(o, cleanSubs){
        var Wn = this;
        var store = Wn._storeAPI;

        // 必须要有路径
        if(!o.ph){
            if("@WnRoot" == o.pid){
                o.ph = "/";
            }else{
                var oP = Wn.getById(o.pid);
                if(!oP){
                    throw "fail to saveToCache: nopid: " + o.pid;
                }
                o.ph = oP.ph + "/" + o.nm;
            }
        }

        // 开始执行保存逻辑
        var key  = "oid:"+o.id;
        var oldJson = store.getItem(key);
        if(oldJson){
            var old = $z.fromJson(oldJson);
            // 清除子孙
            if(cleanSubs){
                o.children = undefined;
                if(_.isArray(old.children)){
                    for(var i=0;i<old.children.length;i++){
                        var childId = old.children[i];
                        Wn.cleanCache("oid:"+childId);
                    }
                }
            }
            // 否则尽量复用
            else if(!_.isArray(o.children) && _.isArray(old.children)){
                o.children = old.children;
            }
        }
        o.__local_cache = (new Date()).getTime();
        // 存储祖先，当然本地保存的时候，就没必要保存这个字段了
        if(o.ancestors){
            if(o.ancestors.length > 0){
                for(var i=0;i<o.ancestors.length;i++){
                    var an = o.ancestors[i];
                    Wn.saveToCache(an);
                }
            }
            delete o.ancestors;
        }
        var json = $z.toJson(o);
        store.setItem(key, json);
        Wn._index.ph[o.ph] = o.id;
    },
    //..............................................
    removeFromCache : function(o) {
        var key = "oid:"+o.id;
        Wn.cleanCache(key);
    },
    //..............................................
    cleanCache : function(key){
        var Wn = this;
        var store = Wn._storeAPI;
        // 全部清除
        if(!key){
            Wn._index.ph = {};
            store.clear();
            return;
        }
        // 指定清理函数
        var _the_cleaner = function(Wn, store, key, val){
            if(!val)
                return;
            // 对象的话，要递归清理
            if(/^oid:\w{6,}/.test(key)) {
                var o = $z.fromJson(val);
                if(_.isArray(o.children)){
                    for(var i=0;i<o.children.length;i++){
                        Wn.cleanCache("oid:" + o.children[i]);
                    }
                    // 删除路径索引和自身
                    if(o.ph && Wn._index.ph[o.ph])
                        delete Wn._index.ph[o.ph];
                }
            }
            // 移除自身
            store.removeItem(key);
        };

        // 指定一个正则表达式，则，清理一批
        if(_.isRegExp(key)){
            var regex = key;
            for(var i=0;i<store.length;i++){
                var key = store.key(i);
                if(regex.test(key)){
                    var val = store.getItem(val);
                    _the_cleaner(Wn, store, key, val);
                }
            }
        }
        // 清除指定的对象
        else if(_.isString(key)){
            var val  = store.getItem(key);
            _the_cleaner(Wn, store, key, val);
        }
    },
    readObj: function (UI, obj, readS) {
        obj = obj || UI.app.obj;
        // 解析参数标志位
        var href = $z.parseHref(window.location.href)
        var params = href.params || {}
        // 读元数据
        if (obj.__obj_meta_rw || obj.race=="DIR" || params.m) {
            Wn.exec("obj id:" + obj.id, function (re) {
                obj = $z.fromJson(re);
                if (readS) {
                    readS.call(UI, re);
                }
            });
        }
        // 读内容
        else {
            Wn.exec("cat id:" + obj.id, function (re) {
                if (readS) {
                    readS.call(UI, re);
                }
            });
        }
    },
    writeObj: function (UI, obj, content, readB, readS, readF) {
        obj = obj || UI.app.obj;
        if (readB) {
            readB.call(UI);
        }
        // 解析参数标志位
        var href = $z.parseHref(window.location.href)
        var params = href.params || {}
        // 写入元数据
        if (obj.__obj_meta_rw || obj.race=="DIR" || params.m) {
            var o2 = $z.fromJson(content) || {};
            o2.id = obj.id;
            _.extend(obj, o2);
            $.ajax({
                type: "POST",
                url: "/o/set/id:" + obj.id,
                contentType: "application/jsonrequest",
                data: $z.toJson(o2)
            }).done(function (re) {
                if (readS) {
                    readS.call(UI);
                }
            }).fail(function (re) {
                if (readF) {
                    readF.call(UI);
                }
                throw "fail to save!";
            });
        }
        // 写入内容
        else {
            $.ajax({
                type: "POST",
                url: "/o/write/id:" + obj.id,
                data: content
            }).done(function (re) {
                if (readS) {
                    readS.call(UI);
                }
            }).fail(function (re) {
                if (readF) {
                    readF.call(UI);
                }
                throw "fail to save!";
            });
        }
    },
    // 得到系统的配置信息
    getSysConf : function(forceReload){
        var Wn = this;
        var store = Wn._storeAPI;

        // 看看缓存里有木有
        var json = forceReload ? null : store.getItem(key);
        
        // 从服务器读取
        if(!json)
            json = Wn.exec("sys");
        
        // 存入缓存
        if(json)
            store.setItem("_WN_SYS_CONF", json);

        // 返回
        return $z.fromJson(json);
    }
}; // ~End wn
//====================================================================
// 初始化内存索引
var store = sessionStorage;
// 固定清除缓存
store.clear();
// 从本地缓存读取所有的对象，并用 path 进行索引
var nowms = (new Date()).getTime();
var du = 600000;  // 默认缓存 10 分钟
for(var i=0;i<store.length;i++){
    var key = store.key(i);
    if(/^oid:\w{6,}$/.test(key)){
        var json = store.getItem(key);
        var o = $z.fromJson(json);
        // 如果这个本地存储对象有效，用路径索引它的ID
        if(o.ph && ((o.__local_cache||0) + du) > nowms ){
            Wn._index.ph[o.ph] = o.id;
        }
        // 没路径，或者过期了就删掉吧
        else{
            store.removeItem(key);
        }
    }
}
//====================================================================
// 输出
_.extend(exports, Wn);
window.Wn = Wn;
//=======================================================================
});