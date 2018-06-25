/*
用法
jsc /mnt/site0/jsbin/order_sms.js -vars {
    id : 'xxxx',       // 「必」订单ID
    tmpl : 'payok',         // 「必」短信模板的别名
}
*/
//........................................
// 声明一些用到的变量
var id = (id || "").trim();
var tmpl = (tmpl || "").trim();
//........................................
function _main(params) {
    if (!params.id) {
        sys.exec("ajaxre -qe e.pay.callback.noOrderId");
        return;
    }
    if (!params.tmpl) {
        sys.exec("ajaxre -qe e.pay.callback.noSmsTmplName");
        return;
    }
    var re = sys.exec2('obj -cqn id:' + params.id);
    if (/^e./.test(re)) {
        sys.exec("ajaxre -qe e.pay.callback.orderNoExists");
        return;
    }
    var order = JSON.parse(re);
    var phone = order.phone || order.od_phone;
    if (!phone) {
        sys.exec("ajaxre -qe e.pay.callback.noPhone");
        return;
    }
    sys.exec2f("sms -r '%s' -t 'i18n:%s' '%s' &", order.phone, params.tmpl, JSON.stringify(order));
    sys.exec2f("obj -u 'sms_%s:true' id:%s", params.tmpl, order.id);
}

//........................................
// 执行
_main({
    id: id,
    tmpl : tmpl
});
//........................................
