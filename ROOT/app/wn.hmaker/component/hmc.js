/**
 * 提供 hmaker 所有组件的帮助函数
 *
 * @author  zozoh(zozohtnt@gmail.com)
 * 2012-10 First created
 */
(function () {
//===================================================================
var hmc = {
    setProperty : function(key, val){
        var UI = this;

        var newProp = key;
        if(_.isString(key)){
            newProp = {};
            newProp[key] = val;
        }
        //console.log(UI.uiName, "change:", key, val)

        // 得到属性信息
        var info = UI.parent.getComponentInfo(UI.$el);

        // 更新属性信息
        for(var k in newProp){
            var v = newProp[k];
            // undefined 自然表示删除
            if(_.isUndefined(v)){
                delete info[k];
            }
            // 否则当做修改
            else {
                info[k] = v;
            }
        }

        // 保存到 DOM 节点
        info = UI.parent.setComponentInfo(UI.$el, info);

        // 将更新过的属性，重新设回到属性面板里
        UI.parent.gasket.prop.setData(info);

        // 更新自己的样式
        UI.updateStyle(info);
    },
    //...............................................................
    formatComponentInfo : function(info){
        return info;
    },
    //...............................................................
    _blank_img : function(){
        if(!this.__o_blank_img) {
            this.__o_blank_img = Wn.fetchBy("%wn.hmaker: obj $APP_HOME/component/hmc_image_blank.jpg");
        }
        return this.__o_blank_img;
    },
    //...............................................................
    imgSrc : function(o){
        o = o || this._blank_img();
        var id = _.isString(o) ? o : o.id || o.fid;
        return id ? "/o/read/id:" + encodeURIComponent(id)
                  : "";
    },
    //...............................................................
    objIdBySrc : function(src){
        var m = /\/o\/read\/id:([^)"]+)/.exec(src);
        return m ? decodeURIComponent(m[1]) : null;
    }
};

// TODO 支持 AMD | CMD 
//===============================================================
if (typeof define === "function") {
    // CMD
    if(define.cmd) {
        define(function (require, exports, module) {
            module.exports = hmc;
        });
    }
    // AMD
    else {
        define("zutil", [], function () {
            return hmc;
        });
    }
}
//===================================================================
})();