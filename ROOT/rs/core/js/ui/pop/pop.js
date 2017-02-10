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
        title    = title    || opt.title;

        // 确保配置非空
        opt = opt || {};

        // 设置默认值
        $z.setUndefined(opt, "defaultByCurrent", true);

        // 设置主体控件属性
        var uiConf = _.extend({
            sidebar : false
        }, opt, {
            gasketName: "body"
        });

        // 设置遮罩属性
        var mask_options = _.extend({
            css  : "theme/ui/pop/pop.css",
            dom  : "ui/pop/pop.html",
            closer: true,
            escape: true,
            width : 800,
            height: 600,
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

                    // 关闭对话框
                    uiMask.close();
                },
                "click .pm-btn-cancel" : function(){
                    var uiMask = this;

                    // 回调的上下文
                    var context = opt.context || uiMask.body;

                    // 调用回调
                    $z.invoke(uiMask.options, "on_cancel", [], context);

                    // 关闭对话框
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
    // 弹出一个浏览器界面
    // 打开一个文本编辑器（弹出），接受的参数格式为:
    /*
    {
        title       : "i18n:xxx"    // 对话框标题
        width       : 900           // 对话框宽度
        height      : "90%"         // 对话框高度
        i18n        : i18n          // 对话框控件组的 i18n 设定
        contentType : "text"        // 编辑内容类型
        data        : 要编辑的值
        callback    : 回调函数接受 callback(href)
        context     : MaskUI        // 回调的上下文，默认是 MaskUI
    }
    */
    openEditTextPanel : function(opt){
        opt = opt || {};

        // 填充默认值
        $z.setUndefined(opt, "width", 900);
        $z.setUndefined(opt, "height", "90%");
        $z.setUndefined(opt, "title", 'i18n:edit');
        $z.setUndefined(opt, "contentType", "text");

        // 打开编辑器
        new MaskUI({
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/pop.css',
            i18n : opt.i18n,
            width  : opt.width,
            height : opt.height,
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
}; // ~End methods
//=======================================================================
});
