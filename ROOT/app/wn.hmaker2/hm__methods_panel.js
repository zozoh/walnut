define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    },
    // 发出块属性修改通知，固定添加忽略面板更新的的标识
    notifyBlockChange : function(prop) {
        if(prop)
            this.fire("change:block", _.extend(prop, {
                __prop_ignore_update : true
            }));
    },
    // 获取背景属性编辑控件的关于 image 编辑的配置信息
    getBackgroundImageEditConf : function(){
        return {
            imageBy : {
                editAs : "link",
                uiConf : {
                    base : this.getHomeObj(),
                    setup : {
                        lastObjId : "hmaker_pick_media",
                        filter    : function(o) {
                            if('DIR' == o.race)
                                return true;
                            return /^image/.test(o.mime);
                        }
                    },
                    // 解析对象，如果是 url(/o/read/id:xxx) 那么就认为是对象
                    parseData : function(str){
                        // 看看是不是对象
                        var m = /^url\("?\/o\/read\/(id:\w+)"?\)$/i.exec(str);
                        if(m)
                            return m[1];
                        // 外部链接 
                        m = /^url\("?(https?:\/\/[^"\)]+)"?\)$/i.exec(str);
                        if(m)
                            return m[1];
                        return null;
                    },
                    // 把 link 搞出来的东西用 url() 包裹
                    formatData : function(link){
                        // 内部对象
                        if(/^id:.+/.test(link)) {
                            return 'url("/o/read/' + link + '")';
                        }
                        // 外部链接
                        if(/^https?:\/\/.+/i.test(link)) {
                            return 'url("' + link + '")';
                        }
                        // 其他
                        return null;
                    }
                }
            }
        };
    }
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/hm__methods");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});