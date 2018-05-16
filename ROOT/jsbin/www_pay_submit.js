/*
用法
> httpparam -in id:${id} -map params | jsc /jsbin/www_pay_submit.js -vars 

需要如下参数:
{
	pid       : 'xxxzzz'      // 「必」thingSet id
    buyer     : 'xxxx',       // 「必」买家的 walnut 账号, 参数名是bu
    goods     : 'xxx',        // 「必」商品名称, 参数名是go
    payType   : 'zfb.qrcode'  // 「必」支付类型, 参数名是pt
    payTarget : 'woodpeax'    // 「必」支付目标商户,参数名是pa
	brief     : '夏令营ABC'    //  [必] 订单命名, 参数名是br
}
*/
//........................................
// 声明一些用到的变量
var bu = (bu || "").trim();
var go = (go || "").trim();
var pt = (pt || "").trim();
var ta = (ta || "").trim();
var ho = (ho || "").trim();
var la = (la || "").trim();
var co = (co || "").trim();
var sc = (sc || "").trim();
var br = (br || "").trim();
//........................................
function _main(params){
	var order = {};
	// 如果传了order的id, 直接找订单查出来
	if (params.id) {
		var reJson = sys.exec("obj id:" + params.id + " -cqn");
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
	    	var gd = JSON.parse(reJson);
	    	gd["count"] = count;
	    	// 商品必须有价格
	    	if(!goo.fee || goo.fee < 0 || count < 1) {
	    		sys.execf("ajaxre -qe 'site0.e.pay.submit.noFee : %s'", goo.nm);
	    		return;
	    	}
	    	fee += goo.fee * count;
	    	price += goo.price * count;
	    }
	    var cmdText = "thing %s create '%s' -fields '%s'";
        var reJson = sys.exec2f(cmdText, params.pid, params.brief
	    					JSON.stringify({
	    						uid : params.buyer,
	    						goods : _goods,
	    						fee : fee,
	    						cur : params.cur ? params.cur : 'rmb',
	    						price : price
	    					}));
		order = JSON.parse(reJson);
	}

    // 准备提交支付单
    var cmdText = "pay create -br '%s' -bu '%s' -fee %s -pt %s -ta %s -callback domain -meta '%s'";
    re = sys.exec2f(cmdText, 
                    order.th_nm, 
                    order.uid, 
                    order.fee,
                    //params.coupon ? params.coupon + " -scope traffic" : "",
                    params.payType, 
                    params.payTarget,
                    JSON.stringify({
                        order_id   : order.id,
                        buy_for    : order.th_nm,
                        client_ip  : params.clientIp,
                    }));
    
    // 输出成功内容
    sys.exec("ajaxre -q", re);
}
//........................................
// 执行
_main({
	pid       : pid,
    buyer     : bu,
    goods     : go,
    payType   : pt,
    payTarget : ta,
	brief     : br,
});
//........................................
