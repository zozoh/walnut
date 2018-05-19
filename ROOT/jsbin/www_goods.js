/*
提供了商品的列表信息

本函数假设有下面格式参数输入

params: {
    // 下面2个字段必须选一个
    go  : "14eulqbbkgh6vq9t5ldpipjvs0:12,14eulqbbkgh6vq9t5ldpipjvs0:1",  // 商品id:数量,商品id:数量
    id  : "wtqwrqwerwereqwreqwreqwrqe", // 订单id, 可选        
}

用法
> httpparam -in id:${id} -map params | jsc /jsbin/www_goods.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
	if (params.id) {
		var cmdText = 'obj id:' + params.id + ' -cqn';// 准备生成命令
		var reJson = sys.exec2(cmdText);
		var order = JSON.parse(reJson);
		sys.out.println(JSON.stringify(order["goods"]));
	}
	else if (params.go) {
		var tmp = params.go.split(",");
		var goods = [];
		for (var i = 0; i < tmp.length; i++) {
			var tmp2 = tmp[i].split(":");
			var id = tmp2[0];
			var cmdText = 'obj id:' + id + ' -cqn';// 准备生成命令
			var reJson = sys.exec2(cmdText);
			var gd = JSON.parse(reJson);
			//log.warn(reJson);
			if (gd) {
				if (tmp2.length > 1) {
					gd["count"] = tmp2[1];
				}
				//log.warn(sys.json(gd[0]));
				goods.push(gd);
			}
		}
		sys.out.println(JSON.stringify(goods));
	}
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
