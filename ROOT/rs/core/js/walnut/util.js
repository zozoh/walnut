define(function (require, exports, module) {
var Wn = {
    //=======================================================================
    // 获取当前的 app 的通用方法，不建议 UI 们直接获取 window._app
    // 因为以后这个对象可能会被改名或变到别的地方
    app : function(){
        return window._app;
    },
    //...................................................................
    // 生成标准缩略图的 DOM 结构
    gen_wnobj_thumbnail_html : function(nmTagName){
        nmTagName = nmTagName || 'a';
        var html = '<div class="wnobj"><div class="wnobj-wrapper">';
        html += '<div class="wnobj-seq"><span>0</span></div>'
        html += '<div class="wnobj-del"><i class="fa fa-close"></i></div>'
        html += '<div class="wnobj-thumbnail">';
        html += '<div class="img">';
        html += '<div class="wnobj-NW wnobj-icon-hide"></div>';
        html += '<div class="wnobj-NE wnobj-icon-hide"></div>';
        html += '<div class="wnobj-SW wnobj-icon-hide"></div>';
        html += '<div class="wnobj-SE wnobj-icon-hide"></div>';
        html += ' </div>';
        html += '</div>';
        html += '<div class="wnobj-nm-con"><'+nmTagName+' class="wnobj-nm"></'+nmTagName+'></div>';
        html += '</div></div>';
        return html;
    },
    //...................................................................
    // 生成一个缩略图的 jQuery 对象，但是没加入 DOM 树
    gen_wnobj_thumbnail : function(o, nmTagName, evalThumb, UI, nmMaxLen){
        var jq = $(this.gen_wnobj_thumbnail_html(nmTagName));
        this.update_wnobj_thumbnail(o, jq, evalThumb, UI, nmMaxLen);
        return jq;
    },
    //...................................................................
    // 根据对象，填充给定的一段 DOM 中的缩略图和名称
    update_wnobj_thumbnail : function(o, jq, evalThumb, UI, nmMaxLen){
        
        // 标记关键属性
        jq.attr("oid",o.id).attr("onm", o.nm);

        // 标记隐藏文件
        if(/^[.].+/.test(o.nm)){
            jq.addClass("wnobj-hide");
        }
        
        // 得到缩略图角标
        var jThumb = jq.find(".wnobj-thumbnail");
        if(_.isFunction(evalThumb)){
            var thumbnails = evalThumb(child);
            if(thumbnail){
                for(var thumbkey in thumbnails){
                    var jThumbIcon = jThumb.find(".wnobj-"+key);
                    jThumbIcon.html(thumbnails[thumbkey]).removeClass("wnobj-icon-hide");
                }
            }
        }
        // 设置缩略图地址
        var url = "url(/o/thumbnail/id:"+encodeURIComponent(o.id)+"?sh=64)";
        jThumb.find(".img").css("background-image", url);
        jThumb.attr("thumb", o.thumb);

        // 填充对象名称
        var jNm = jq.find(".wnobj-nm");
        var nmText = o.nm; 
        if(UI && _.isFunction(UI.text))
            nmText = UI.text(nmText);
        nmText = this.objDisplayName(nmText, nmMaxLen);
        jNm.prop("href","/a/open/wn.browser?ph=id:"+o.id).text(nmText);
    },
    //...................................................................
    objIconHtml : function(o){
        return o.icon || '<i class="oicon" otp="'+this.objTypeName(o)+'"></i>';;
    },
    //...................................................................
    objTypeName : function(o){
        return o.tp || ('DIR'==o.race ? 'folder' : 'unknown');
    },
    //...................................................................
    objDisplayName : function(nm, maxLen){
        var text = _.isString(nm) ? nm : nm.nm;   // TODO 以后考虑 _key_ 开头的名称
        if(_.isUndefined(maxLen)){
            maxLen = 20;
        }
        if(_.isNumber(maxLen))
            return $z.ellipsisCenter(text, 20);
        return text;
    },
    /*...................................................................
    提供一个通用的文件上传界面，任何 UI 可以通过
       this.listenModel("do:upload", this.on_do_upload); 
    来启用这个方法
    */
    uploadPanel: function (options) {
        var MaskUI    = require("ui/mask/mask");
        var UploadUI  = require("ui/upload/upload");

        var mask_options = _.extend({
            closer: true,
            escape: true,
            width: 460,
            height: 500,
            setup : {
                uiType : "ui/upload/upload",
                uiConf : _.extend({
                    parent: this,
                    gasketName: "main"
                }, options)
            }
        }, options);

        new MaskUI(mask_options).render();
    },
    /*................................................................
    执行一个命令，并且在一个弹出的日志窗口显示命令的返回情况
     - cmdText  : "xxxx"        # 命令文本
     - maskConf : {..}          # 可选，是弹出的遮罩层的配置信息
     - callback : {c}F(re)      # 可选，命令执行完毕后的回调，参数为命令的返回
    或者，你可以用更精细的方法来调用
    logpanel({
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
    logpanel : function(cmdText, maskConf, callback){
        var options;
        if(_.isString(cmdText)){
            options = {cmdText: cmdText};
            if(_.isFunction(maskConf)){
                options.maskConf = {};
                options.complete = maskConf;
            }else{
                options.maskConf = maskConf;
                options.complete = callback;
            }
        }else{
            options = cmdText;
        }
        // 显示遮罩
        var MaskUI = require('ui/mask/mask');
        new MaskUI(_.extend({
            width : "60%"
        }, options.maskConf)).render(function(){
            var jPre = $('<pre class="ui-log">').appendTo(this.$main);
            Wn.exec(options.cmdText, _.extend(options, {
                msgShow : function(str){
                    $('<div class="ui-log-info">')
                        .text(str)
                        .appendTo(jPre)[0].scrollIntoView({
                            block: "end", behavior: "smooth"
                        });
                },
                msgError : function(str){
                    $('<div class="ui-log-err">')
                        .text(str)
                        .appendTo(jPre)[0].scrollIntoView({
                            block: "end", behavior: "smooth"
                        });
                }
            }));
        });
    },
    /*................................................................
    # 执行命令的 opt 配置对象的内容
    {
        // 应用的名称命令执行时会自动添加环境变量 $WN_APP_HOME
        // 默认采用当前的 app 名称，如果设定的 app 找不到命令将拒绝执行
        appName     : "xxx"  
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
    }
    如果仅仅是一个函数，那么相当于
    {
        complate : function(content){..}
    }
    */
    exec : function (str, opt) {
        var app = window._app;
        var se = app.session;
        var re = undefined;

        // 没设置回调，则默认认为是同步调用
        if(_.isUndefined(opt)){
            opt = {
                async : false,
                complete : function(content){
                    re = content;
                }
            };
        }
        // 一个回调处理所有的情况
        else if (_.isFunction(opt)) {
            opt = {async:true, complete: opt};
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

        // 处理命令
        var mos = "%%wn.meta." + $z.randomString(10) + "%%";
        //var regex = new RegExp("^(\n"+mos+":BEGIN:)(\\w+)(.+)(\n"+mos+":END\n)$","m");
        var mosHead = "\n" + mos + ":BEGIN:";
        var mosTail = "\n" + mos + ":END\n";

        // 执行命令的地址
        var url = '/a/run/' + (opt.appName || app.name);

        // 准备发送的数据
        var sendData = "mos=" + encodeURIComponent(mos);
        sendData += "&PWD=" + encodeURIComponent(se.envs.PWD);
        sendData += "&cmd=" + encodeURIComponent(str);

        var oReq = new XMLHttpRequest();
        oReq._last = 0;
        oReq._content = "";
        oReq._moss = [];
        oReq._moss_tp = "";
        oReq._moss_str = "";

        oReq._show_msg = function () {
            var str = oReq.responseText.substring(oReq._last);
            oReq._last += str.length;
            var pos = str.indexOf(mosHead);
            var tailpos = str.indexOf(mosTail);
            // 发现完整的mos
            if (pos >= 0 && tailpos >= 0) {
                var from = pos + mosHead.length;
                var pl = str.indexOf("\n", from);
                var pr = str.indexOf(mosTail, pl);
                oReq._moss.push({
                    type: str.substring(from, pl),
                    content: str.substring(pl + 1, pr)
                });
                str = str.substring(0, pos);
            }
            // 发现开头
            else if (pos >= 0 && tailpos < 0) {
                var from = pos + mosHead.length;
                var pl = str.indexOf("\n", from);
                oReq._moss_tp = str.substring(from, pl);
                oReq._moss_str = str.substring(pl + 1);
                str = str.substring(0, pos);
            }
            // 发现结尾
            else if (pos < 0 && tailpos >= 0) {
                oReq._moss_str += str.substr(0, tailpos);
                oReq._moss.push({
                    type: oReq._moss_tp,
                    content: oReq._moss_str
                });
                oReq._moss_tp = "";
                oReq._moss_str = "";
                str = str.substring(tailpos + mosTail.length + 1);
            }
            // 累计 Content
            oReq._content += str;
            if (str) {
                // 正常显示
                if (oReq.status == 200) {
                    $z.invoke(opt, "msgShow", [str], context);
                }
                // 错误显示
                else {
                    $z.invoke(opt, "msgError", [str], context);
                }
            }
        };
        
        oReq.open("POST", url, opt.async);
        oReq.onreadystatechange = function () {
            //console.log("rs:" + oReq.readyState + " status:" + oReq.status + " :: \n" + oReq.responseText);
            // LOADING | DONE 只要有数据输入，显示一下信息
            if(oReq._show_msg)
                oReq._show_msg();
            // DONE: 请求结束了，调用回调
            if (oReq.readyState == 4) {
                // 处理请求的状态更新命令
                for (var i = 0; i < oReq._moss.length; i++) {
                    var mosc = oReq._moss[i];
                    // 修改环境变量
                    if ("envs" == mosc.type) {
                        app.session.envs = $z.fromJson(mosc.content);
                    }
                }
                // 最后确保通知了显示流结束
                $z.invoke(opt, "msgEnd", [str], context);
                
                var re = oReq._content;

                // 执行回调前数据处理
                if(opt.processData){
                    if("json" == opt.dataType){
                        re = $z.fromJson(re);
                        // 检查是不是 session 过期了，如果过期了，直接换地址
                        $z.checkSessionNoExists(re);
                    }
                }

                // 调用完成后的回调
                var funcName = oReq.status == 200 ? "done" : "fail";
                $z.invoke(opt, funcName,   [re], context);
                $z.invoke(opt, "complete", [re], context);
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
    // 获取对象的显示名称
    getObjDisplayName : function(o){
        return o.nm;
    },
    //..............................................
    /* 根据一个对象获取其应用配置信息
     opt - {
        forceTop: true,   // 是否为每个菜单项强制加上 "@"
        editor  : "xxx",  // 【选】采用指定的编辑器 
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
        // 开始从服务器获取数据
        var Wn = this;
        Wn.exec("appsetup id:"+o.id, function(json){
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
            var oedt = Wn.fetch("~/.ui/editors/"+edtnm+".js");
            var json = Wn.read(oedt);
            var ace  = $z.fromJson(json);
            asetup.currentEditor = ace;
                
            // 将编辑器的菜单项统统加入动作组
            if(_.isArray(ace.actions)){
                ace.actions.forEach(function(val, index, arr){
                    arr[index] = "e" + val;
                });
                asetup.actions = ace.actions.concat(asetup.actions);
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
        var ac_phs     = [];
        var menu_setup = [];
        var menu_items = [];
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
            menu_items.push(ac_phs.length);
            ac_phs.push("~/.ui/actions/"+ss[2]+".js");
            // 记录固定显示的项目下标
            if(forceTop || ss[0].indexOf("@")>=0)
                menu_setup.push(index);
        });
        // 逐次得到菜单的动作命令
        var alist = UI.batchRead(ac_phs);
        for(var i=0; i<menu_items.length; i++){
            var index = menu_items[i];
            if(_.isNumber(index)){
                var mi = eval('(' + alist[index] + ')');
                if(mi.type=="group" || _.isArray(mi.items)){
                    mi._items_array = mi.items;
                    mi.items = function(jq, mi, callback){
                        var items = this.extend_actions(mi._items_array, true);
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
    getChildren : function(o, filter, callback){
        var Wn = this;

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
            if(re){
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

        // 重新从服务器读取
        if(!o.children || o.children.length==0 ){
            // 有回调，异步
            if(_.isFunction(callback)){
                Wn.exec("obj id:"+o.id+"/* -l -sort 'nm:1'", function(re){
                    do_after_load(o, re);
                    var reList = do_filter(o, filter);
                    callback(reList);
                });
                return;
            }
            // 否则同步
            else{
                 var re = Wn.exec("obj id:"+o.id+"/* -l -sort 'nm:1'");
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
    read : function(o, callback, context){
        var Wn = this;
        var store = Wn._storeAPI;

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
            alert(re.errCode + " : " + re.msg);
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
    get : function(o, quiet){
        if(/^id:\w{6,}$/.test(o))
            return this.getById(o.substring(3), quiet);
        return this.fetch(o, quiet);
    },
    //..............................................
    getById : function(oid, quiet) {
        if(!oid)
            return null;

        var Wn = this;
        var store = Wn._storeAPI;
        // 首先获取缓存
        var key  = "oid:"+oid;
        var json = store.getItem(key);

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
        if(/^e.io.obj.noexists :/.test(re)){
            if(quiet){
                return null;
            }
            alert("fail to found obj: " + ph);
            throw "fail to found obj: " + ph;
        }

        var o2 = $z.fromJson(re);
        Wn.saveToCache(o2);
        return o2; 
    },
    //..............................................
    fetch : function(ph, quiet){
        var Wn = this;
        // 首先格式化 Path 到绝对路径
        var ss = ph.split(/\/+/);
        if(ss[0] == "~"){
            ss[0] = Wn.app().session.envs.HOME.substring(1);
        }
        var aph = "/" + ss.join("/");

        // 首先试图从缓存里获取 ID
        var oid = Wn._index.ph[aph];

        // 找到了 ID 就用 ID 读一下
        if(oid)
            return Wn.getById(oid);

        
        // 木有？ 那就重新根据路径加载
        var re  = Wn.exec("obj '"+ph+"' -PA");

        // 对于不存在的处理
        if(/^e.io.obj.noexists :/.test(re)){
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
    getHome : function(){
        return this.fetch("~");
    },
    //..............................................
    isHome : function(o){
        return o.id == this.getHome().id;
    },
    //..............................................
    // 根据 ID 列表批量获取对象的元数据
    batchGetById : function(ids, callback){
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
                Wn.saveToCache(list[i]);
            }

            // 更新未加载对象
            for(var i=0;i<objs.length;i++){
                var o = objs[i];
                if(_.isString(o)){
                    objs[i] = Wn.getById(o);
                }
            }

            // 调用回调
            if(_.isFunction(callback)){
                callback(objs);
            }
        };

        // 如果有需要读取的 ID，则发送请求
        if(loadIds.length>0){
            var cmdText = "obj id:" + loadIds.join(" id:") + " -lP -json";
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