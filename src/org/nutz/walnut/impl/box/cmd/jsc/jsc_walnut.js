//-----------------------------------------
// 先来几个帮助方法
load("nashorn:mozilla_compat.js");
importPackage(java.util);
importPackage(org.nutz.lang.util);
importPackage(org.nutz.lang);
importPackage(org.nutz.walnut.api.io);

/**
 * 输出一段文本,可以是null
 */
function print(msg) {
	if (msg == null)
		return;
	sys.out.print(msg.toString());
}

/**
 * 输出一个json文本，一般 Java 的 Map 类用这个输出，否则输出不了
 */
function printJson(obj) {
	print(sys.json(obj));
}

/**
 * AJAX 输出的函数
 */
function ajax_re(obj) {
	// 处理返回对象
	if (typeof obj == "string") {
		obj = obj.trim();
		if (/^\{.+\}$/.test(obj)) {
			obj = eval('(' + obj + ')');
		}
	}
	// 响应体
	sys.out.println(JSON.stringify(obj, null, '    '));
}

/**
 * AJAX 成功信息打印便利函数
 */
function ajax_ok(data) {
	ajax_re({
		ok : true,
		data : data
	});
}

/**
 * AJAX 出错的错误信息打印便利函数
 */
function ajax_error(errCode, reason) {
	ajax_re({
		ok : false,
		errCode : errCode,
		data : reason
	});
}
