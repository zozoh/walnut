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
	//log.warn(re);
    if (po.st == "OK") {
		sys.exec("obj id:" + po.buy_for + " -u 'pay_st:\"OK\"'");
		var reJson = sys.exec2("obj -cqn id:" + po.buy_for);
		if (/^e./.test(reJson)) {
		}
		else {
			var order = JSON.parse(reJson);
			if (order && order.od_phone) {
				sys.exec2f("sms -r '%s' -t 'i18n:payok' '%s' &", order.phone, JSON.stringify(order));
				sys.exec2f("obj -u 'sms_payok:true' id:" + order.id);
			}
		}
    }
}

//........................................
// 执行
_main({
    id: id
});
//........................................
