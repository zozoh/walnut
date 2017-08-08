define(function (require, exports, module) {
// ....................................
// 依赖
var MaskUI    = require('ui/mask/mask');
var BrowserUI = require('ui/obrowser/obrowser');
// ....................................
// 方法表
module.exports = {
    //...............................................................
    // 弹出一个浏览器界面
    browser : function(title, opt, callback) {
        // 参数 popBrowser({..})
        // 参数 popBrowser({..}, F())
        if(_.isObject(title)) {
            callback = opt;
            opt      = title;
            title    = null;
        }
        // 参数 popBrowser(F())
        else if(_.isFunction(title)) {
            callback = title;
            opt      = null;
            title    = null;
        }
        // 参数 popBrowser("Title", F())
        else if(_.isFunction(opt)) {
            callback = opt;
            opt      = null;
        }
        // 参数 popBrowser("Title", {..})
        // 参数 popBrowser("Title", {..}, F())

        // 兼容旧模式
        title  = title || opt.title;

        // 确保配置非空
        opt = opt || {};

        // 设置默认值
        // 填充默认值
        $z.setUndefined(opt, "width", 800);
        $z.setUndefined(opt, "height", 600);
        $z.setUndefined(opt, "escape", true);
        $z.setUndefined(opt, "closer", true);
        $z.setUndefined(opt, "objTagName", 'SPAN');
        $z.setUndefined(opt, "defaultByCurrent", true);


        // 设置主体控件属性
        var uiConf = _.extend({
            sidebar : false
        }, opt, {
            gasketName: "body"
        });

        // 设置遮罩属性
        var mask_options = _.extend({
            dom  : 'ui/pop/pop.html',
            css  : "ui/pop/theme/pop-{{theme}}.css",
            closer: true,
            escape: true,
            width  : opt.width,
            height : opt.height,
            exec  : Wn.exec,
            app   : Wn.app(),
            setup : {
                uiType : "ui/obrowser/obrowser",
                uiConf : uiConf
            },
            events : {
                "click .pm-btn-ok" : function(){
                    var uiMask = this;
                    // 得到数据
                    var objs   = uiMask.body.getChecked();
                    // 支持当前目录作为默认
                    if(objs.length == 0 && opt.defaultByCurrent){
                        objs = [uiMask.body.getCurrentObj()];
                    }
                    // 回调的上下文
                    var context = opt.context || uiMask.body;
                    
                    // 调用回调
                    $z.invoke(opt, "on_ok", [objs], context);

                    // 关闭弹出框
                    uiMask.close();
                },
                "click .pm-btn-cancel" : function(){
                    var uiMask = this;

                    // 回调的上下文
                    var context = opt.context || uiMask.body;

                    // 调用回调
                    $z.invoke(uiMask.options, "on_cancel", [], context);

                    // 关闭弹出框
                    uiMask.close();
                }
            }
        }, opt);

        // 打开遮罩
        new MaskUI(mask_options).render(function(){
            // 设置标题
            if(title)
                this.arena.find(".pm-title").html(title);
            else
                this.arena.find(".pm-title").remove();

            // 设置数据
            this.body.setData(opt.base, callback);
        });
    },
    //...............................................................
    // 弹出一个 Quartz 编辑器
    quartz : function(opt) {
        // 确保配置非空
        opt = opt || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 480);
        $z.setUndefined(opt, "height", 540);
        $z.setUndefined(opt, "escape", true);
        $z.setUndefined(opt, "closer", true);
        $z.setUndefined(opt, "title", "i18n:quartz.title");

        // 设置遮罩属性
        var mask_options = _.extend({
            dom  : 'ui/pop/pop.html',
            css  : "ui/pop/theme/pop-{{theme}}.css",
            i18n : "ui/quartz/i18n/{{lang}}.js",
            closer: true,
            escape: true,
            arenaClass : opt.arenaClass,
            width  : opt.width,
            height : opt.height,
            exec  : Wn.exec,
            app   : Wn.app(),
            setup : {
                uiType : "ui/quartz/edit_quartz",
                uiConf : {}
            },
            events : {
                "click .pm-btn-ok" : function(){
                    var uiMask  = this;
                    var opt     = uiMask.options;
                    var qz      = uiMask.body.getData();
                    var context = opt.context || uiMask.body;
                    $z.invoke(opt, "on_ok", [qz], context);
                    uiMask.close();
                },
                "click .pm-btn-cancel" : function(){
                    var uiMask  = this;
                    var opt     = uiMask.options;
                    var context = opt.context || uiMask.body;
                    $z.invoke(opt, "on_cancel", [], context);
                    uiMask.close();
                }
            }
        }, opt);

        // 打开遮罩
        new MaskUI(mask_options).render(function(){
            // 设置标题
            if(opt.title)
                this.arena.find(".pm-title").html(this.text(opt.title));
            else
                this.arena.find(".pm-title").remove();

            // 设置数据
            if(opt.data) {
                this.body.setData(opt.data);
            }
        });
    },
    //...............................................................
    // 弹出一个 ZCron 编辑器
    zcron : function(cron, opt, referUI) {
        // opt 直接就是一个回调
        if(_.isFunction(opt)){
            opt = {ok : opt};
        }
        // 确保配置非空
        else {
            opt = opt || {};
        }

        // 默认标题
        $z.setUndefined(opt, "title", "i18n:edit");

        // 固定宽高
        opt.width  = 575;
        opt.height = 610;
        opt.arenaClass = "pop-zcron";

        // 初始化数据的回调
        opt.ready = function(uiCron){
            uiCron.setData(cron || "0 0 0 * * ?");
        };
        
        // 设置
        opt.setup = opt.setup || {};
        if(!opt.setup.uiType) {
            opt.setup = {
                uiType : 'ui/zcron/edt_zcron',
                uiConf : opt.setup
            }
        }

        // 打开
        this.openUIPanel(opt, referUI);
    },
    //...............................................................
    // 打开一个文本编辑器（弹出），接受的参数格式为:
    /*
    opt : {
        title       : "i18n:xxx"    // 弹出框标题
        arenaClass  : "xxx",        // 弹出框主题的类选择器
        width       : 900           // 弹出框宽度
        height      : "90%"         // 弹出框高度
        i18n        : i18n          // 弹出框控件组的 i18n 设定
        contentType : "text"        // 编辑内容类型
        data        : 要编辑的值
        callback    : 回调函数接受 callback(href)
        context     : MaskUI        // 回调的上下文，默认是 MaskUI
    }
    referUI : 为一个 UI 的引用，弹出框将复用它的 _msg_map | app | exec 设定
    */
    openEditTextPanel : function(opt, referUI){
        opt = opt || {};
        referUI  = referUI  || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 900);
        $z.setUndefined(opt, "height", "90%");
        $z.setUndefined(opt, "escape", true);
        $z.setUndefined(opt, "closer", true);
        $z.setUndefined(opt, "title", 'i18n:edit');
        $z.setUndefined(opt, "contentType", "text");

        // 打开编辑器
        new MaskUI({
            i18n : referUI._msg_map,
            exec : referUI.exec,
            app  : referUI.app,
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/theme/pop-{{theme}}.css',
            arenaClass : opt.arenaClass,
            width  : opt.width,
            height : opt.height,
            escape : opt.escape,
            closer : opt.closer,
            events : {
                "click .pm-btn-ok" : function(){
                    var context = opt.context || this;
                    var html = this.body.getData();
                    $z.invoke(opt, "callback", [html], context);
                    this.close();
                },
                "click .pm-btn-cancel" : function(){
                    this.close();
                }
            }, 
            setup : {
                uiType : 'ui/zeditor/zeditor',
                uiConf : {
                    contentType : opt.contentType
                }
            }
        }).render(function(){
            this.arena.find(".pm-title").html(this.text(opt.title));
            this.body.setData(opt.data);
        });
    },
    //...............................................................
    // 打开一个表单器（弹出），接受的参数格式为:
    /*
    {
        title       : "i18n:xxx"    // 弹出框标题
        arenaClass  : "xxx",        // 弹出框主题的类选择器
        width       : 900           // 弹出框宽度
        height      : "90%"         // 弹出框高度
        escape      : false         // 弹出框支持 Esc 关闭，默认 false
        closer      : false         // 弹出框显示关闭按钮
        i18n        : i18n          // 弹出框控件组的 i18n 设定
        form        : {..}          // 表单配置项
        data        : 要编辑的值
        after       : 回调函数, 设置完数据会调用 {uiForm}after(data)
        callback    : 回调函数, 按确认键 {uiForm}callback(data)
        context     : MaskUI    // 回调的上下文，默认是 FormUI
    }
    */
    openFormPanel : function(opt){
        opt = opt || {};
        opt.form = opt.form || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 640);
        $z.setUndefined(opt, "height", "80%");
        $z.setUndefined(opt, "escape", false);
        $z.setUndefined(opt, "closer", true);
        $z.setUndefined(opt, "title", 'i18n:edit');
        $z.setUndefined(opt.form, "uiWidth", 'all');

        // 打开编辑器
        new MaskUI({
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/theme/pop-{{theme}}.css',
            i18n : opt.i18n,
            arenaClass : opt.arenaClass,
            width  : opt.width,
            height : opt.height,
            escape : opt.escape,
            closer : opt.closer,
            events : {
                "click .pm-btn-ok" : function(){
                    var context = opt.context || this.body;
                    var data = this.body.getData();
                    $z.invoke(opt, "callback", [data], context);
                    this.close();
                },
                "click .pm-btn-cancel" : function(){
                    this.close();
                }
            }, 
            setup : {
                uiType : 'ui/form/form',
                uiConf : opt.form
            }
        }).render(function(){
            this.arena.find(".pm-title").html(this.text(opt.title));
            this.body.setData(opt.data || {});

            // 调用回调
            var context = opt.context || this.body;
            $z.invoke(opt, "after", [opt.data || {}], context);
        });
    },
    //...............................................................
    // 打开一个向导界面（弹出），它接受的参数格式为:
    /*
    {
        width       : 900           // 弹出框宽度
        height      : "90%"         // 弹出框高度
        i18n        : i18n          // 弹出框控件组的 i18n 设定
        ready       : 回调函数, body加载完会调用 {c}F(uiMask.body)
        context     : MaskUI    // 回调的上下文，默认是 uiMask.body
        // 下面是所有向导控件支持的属性
        title, headMode, data, startPoint, steps, on_done ...
    }
    */
    openWizard : function(opt, referUI) {
        opt = opt || {};
        referUI  = referUI  || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 640);
        $z.setUndefined(opt, "height", 480);

        // 准备向导界面配置项 
        var wzConf = $z.pick(opt, "!^(width|height|ready)$");
        var usr_on_done = null;
        if(_.isFunction(wzConf.on_done)){
            usr_on_done = wzConf.on_done; 
        }
        // 完成就关闭弹出框
        wzConf.on_done = function(data, uiWizard){
            uiWizard.parent.close();
            $z.doCallback(usr_on_done, [data, uiWizard], this);
        };

        // 打开编辑器
        new MaskUI({
            i18n : referUI._msg_map,
            exec : referUI.exec,
            app  : referUI.app,
            width  : opt.width,
            height : opt.height,
            escape : false,
            closer : true,
            setup : {
                uiType : 'ui/wizard/wizard',
                uiConf : wzConf
            }
        }).render(function(){
            var context = opt.context || this.body;
            $z.invoke(opt, "ready", [this.body], context);
        });
    },
    //...............................................................
    // 打开一个自定义界面（弹出），接受的参数格式为:
    /*
    opt : {
        title       : "i18n:xxx"    // 弹出框标题
        arenaClass  : "xxx",        // 弹出框主题的类选择器
        width       : 900           // 弹出框宽度
        height      : "90%"         // 弹出框高度
        escape      : true          // 弹出框支持 Esc 关闭，默认 true
        closer      : true          // 弹出框显示关闭按钮，默认 true
        i18n        : i18n          // 弹出框控件组的 i18n 设定
        setup       : {             // UI 配置项
            uiType : "xxxx"         // UI 的类型
            uiConf : {..}           // UI 的具体配置项目
        }          
        ready       : 回调函数, body加载完会调用 {c}F(uiMask.body)
        context     : MaskUI    // 回调的上下文，默认是 uiMask.body
        ok     : {c}F(uiMask.body):Boolean   // 返回 false 将阻止弹出框关闭
        cancel : {c}F(uiMask.body):Boolean   // 返回 false 将阻止弹出框关闭
        btnOk     : "i18n:ok"        // 默认 "i18n:ok", null 表示隐藏该按钮
        btnCancel : "i18n:cancel"    // 默认 "i18n:cancel", null 表示隐藏该按钮
    }
    referUI : 为一个 UI 的引用，弹出框将复用它的 _msg_map | app | exec 设定
    */
    openUIPanel : function(opt, referUI){
        opt = opt || {};
        referUI  = referUI  || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 640);
        $z.setUndefined(opt, "height", "80%");
        $z.setUndefined(opt, "escape", true);
        $z.setUndefined(opt, "closer", true);
        $z.setUndefined(opt, "btnOk", "i18n:ok");
        $z.setUndefined(opt, "btnCancel",  "i18n:cancel");
        $z.setUndefined(opt, "title", 'i18n:edit');

        // 打开编辑器
        new MaskUI({
            i18n : referUI._msg_map,
            exec : referUI.exec,
            app  : referUI.app,
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/theme/pop-{{theme}}.css',
            arenaClass : opt.arenaClass,
            width  : opt.width,
            height : opt.height,
            escape : opt.escape,
            closer : opt.closer,
            events : {
                "click .pm-btn-ok" : function(e){
                    var jBtn = $(e.currentTarget);
                    var context = opt.context || this.body;
                    if(false !== $z.invoke(opt, "ok", [this.body, jBtn], context)){
                        this.close();
                    }
                },
                "click .pm-btn-cancel" : function(e){
                    var jBtn = $(e.currentTarget);
                    var context = opt.context || this.body;
                    if(false !== $z.invoke(opt, "cancel", [this.body, jBtn], context)){
                        this.close();
                    }
                }
            }, 
            setup : opt.setup
        }).render(function(){
            // 设置标题
            this.arena.find(".pm-title").html(this.text(opt.title));
            
            // 设置按钮文字: OK
            if(opt.btnOk)
                this.$main.find(".pm-btn-ok").html(this.text(opt.btnOk));
            else
                this.$main.find(".pm-btn-ok").remove();

            // 设置按钮文字: Cancel
            if(opt.btnCancel)
                this.$main.find(".pm-btn-cancel").html(this.text(opt.btnCancel));
            else
                this.$main.find(".pm-btn-cancel").remove();

            // 调用回调
            var context = opt.context || this.body;
            $z.invoke(opt, "ready", [this.body], context);
        });
    },
    //...............................................................
}; // ~End methods
//=======================================================================
});