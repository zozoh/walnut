/**
 * 提供 hmaker 所有组件的帮助函数
 *
 * @author  zozoh(zozohtnt@gmail.com)
 * 2012-10 First created
 */
(function () {
//===================================================================
var hmc = {
    
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