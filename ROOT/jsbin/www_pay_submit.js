/*
用法
> httpparam -in id:${id} -map params | jsc /jsbin/www_pay_submit.js -vars 

需要如下参数:
{
    id        : 'dadfsfdsf'   //  [可] 订单id,可选
    pid       : 'xxxzzz'      // 「可」thingSet id
    bu        : 'xxxx',       // 「必」买家识别号
    unm       : '卖家昵称'    //  [可] 买家的昵称,默认是买家识别号
    go        : 'xxx',        // 「必」商品名称
    pt        : 'zfb.qrcode'  // 「必」支付类型
    pa        : 'woodpeax'    // 「必」支付目标商户
    br        : '夏令营ABC'   //  [必] 订单命名
    ca        : '回调'        //  [必] 回调名称, 对应 ~/.payment/${callback}
    cm        : '备注'        //  [可] 订单备注
}
*/
//........................................
// 声明一些用到的变量
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
paramObj = {
    orderId   : paramObj["id"],
    pid       : paramObj["pid"],
    buyer     : paramObj["bu"],
    goods     : paramObj["go"],
    payType   : paramObj["pt"],
    payTarget : paramObj["pa"],
    brief     : paramObj["br"],
    callback  : paramObj["ca"],
    unm       : paramObj["unm"],
    comment   : paramObj["cm"] || ""
};
//........................................
function _main(params){
    var order = {};
    // 如果传了order的id, 直接找订单查出来
    if (params.orderId) {
        var reJson = sys.exec("obj id:" + params.orderId + " -cqn");
        order = JSON.parse(reJson);
    }
    else {
        // thingSet pid为空
        if(!params.pid){
            sys.exec("ajaxre -qe site0.e.pay.submit.noThingSet");
            return;
        }
        // 买家为空
        if(!params.buyer){
            sys.exec("ajaxre -qe site0.e.pay.submit.noBuyer");
            return;
        }
        // 商品为空
        if(!params.goods){
            sys.exec("ajaxre -qe site0.e.pay.submit.noGoods");
            return;
        }
        // 支付方式为空
        if(!params.payType){
            sys.exec("ajaxre -qe site0.e.pay.submit.noPayType");
            return;
        }
        // 支付方式为空
        if(!params.brief){
            sys.exec("ajaxre -qe site0.e.pay.submit.noBrief");
            return;
        }
        // 支付方式为空
        if(!params.callback){
            sys.exec("ajaxre -qe site0.e.pay.submit.noCallback");
            return;
        }
        if(!params.unm) {
            params.unm = params.buyer;
        }
        
        // 查询商品以及其价格
        var tmp = params.goods.split(",");
        var _goods = [];
        var fee = 0;
        var price = 0;
        for (var i = 0; i < tmp.length; i++) {
            var tmp2 = tmp[i].split(":");
            var id = tmp2[0];
            var count = parseInt(tmp2[1]);
            var cmdText = 'obj id:' + id + ' -cqn';// 准备生成命令
            var reJson = sys.exec2(cmdText);
            var goo = JSON.parse(reJson);
            goo["count"] = count;
            // 商品必须有价格
            if(!goo.fee || goo.fee < 0 || count < 1) {
                sys.execf("ajaxre -qe 'site0.e.pay.submit.noFee : %s'", goo.nm);
                return;
            }
            fee += goo.fee * count;
            price += goo.price * count;
            _goods.push(goo);
        }
        var cmdText = "thing %s create '%s' -fields '%s' -cqn";
        var reJson = sys.exec2f(cmdText, params.pid, params.brief,
                            JSON.stringify({
                                uid : params.buyer,
                                unm : params.unm,
                                goods : _goods,
                                fee : fee,
                                cur : params.cur ? params.cur : 'RMB',
                                price : price,
                                comment : params.comment
                            }));
        order = JSON.parse(reJson);
        log.warn("order=" + reJson);
    }

    // 准备提交支付单
    var cmdText = "pay create -br '%s' -bu '%s' -fee %s -pt %s -ta %s -callback %s -meta '%s' -cqn";
    re = sys.exec2f(cmdText, 
                    order.th_nm, 
                    "%"+order.uid, 
                    order.fee,
                    //params.coupon ? params.coupon + " -scope traffic" : "",
                    params.payType, 
                    params.payTarget,
                    params.callback,
                    JSON.stringify({
                        buy_for    : order.id,
                        client_ip  : params.clientIp,
                    }));
    var payment = JSON.parse(re);
    //log.warn(re);
    payment["order_id"] = order.id;
    re = JSON.stringify(payment);
    // 输出成功内容
    sys.exec("ajaxre -q", re);
}
//........................................
// 执行
_main(paramObj);
//........................................
