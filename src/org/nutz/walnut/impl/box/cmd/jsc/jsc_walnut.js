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


function _toObj(obj) {
	if (typeof obj == "string") {
		obj = obj.trim();
		if (/^\{.+\}$/.test(obj)) {
			obj = eval('(' + obj + ')');
		}
	}
	return obj;
}

/**
 * AJAX 输出的函数
 */
function ajax_re(obj) {
	// 处理返回对象
	obj = _toObj(obj);
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
 * 带着session信息
 */
function ajax_se(obj, session) {
	session = _toObj(session);
	// 响应头
    sys.out.println("HTTP/1.1 200 OK");
    sys.out.println("Content-Type: text/html;charset=UTF-8");
    sys.out.printlnf("SET-COOKIE:DSEID=%s; Path=/;", session.id);
    sys.out.println("Server: Walnut HTTPAPI");
    sys.out.println("");

    ajax_re(obj);
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

// js转换为json字符串
function toJsonStr(jsobj) {
    if (typeof jsobj == "string") {
        return jsobj;
    }
    return JSON.stringify(jsobj, null, '');
}
