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

//........................................
function _main(params) {
    if (!params.id) {
        sys.exec("ajaxre -qe e.pay.callback.noPoId");
        return;
    }
    var re = sys.exec2('obj -cqn id:' + params.id);
    if (/^e./.test(re)) {
        sys.exec("ajaxre -qe e.pay.callback.poNoExists");
        return;
    }
    var po = JSON.parse(re);
	log.warn(re);
    if (po.st == "OK") {
		sys.exec("obj id:" + po.buy_for + " -u 'pay_st:\"OK\"'");
    }
}

//........................................
// 执行
_main({
    id: id
});
//........................................
