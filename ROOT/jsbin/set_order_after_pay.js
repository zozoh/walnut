/*
用法
jsc /mnt/site0/jsbin/set_order_after_pay.js -vars {
    id : 'xxxx',       // 「必」订单ID
    st : 'OK',         // 「必」支付状态 'OK|FAIL'
}
*/
//........................................
// 声明一些用到的变量
var id = (id || "").trim();
var st = (st || "FAIL").trim();
//........................................
function _main(params){
    if (!params.id) {
        sys.exec("ajaxre -qe site0.e.pay.submit.noOrderId");
        return;
    }
    if (params.st == "OK") {
        sys.exec("obj id:" + params.id + " -u 'pay_tp:\"OK\"'");
    }
}
//........................................
// 执行
_main({
    id : id,
    st : st
});
//........................................
