//-----------------------------------------
// 先来几个帮助方法
load("nashorn:mozilla_compat.js");
importPackage(java.util);
importPackage(org.nutz.lang.util);
importPackage(org.nutz.lang);
importPackage(org.nutz.walnut.api.io);

/**
 * Walnut 的名称控件
 */
var $wn = {
	/**
	 * 输出一段文本,可以是null
	 */
	print : function(msg) {
		if (msg == null)
			return;
		this.sys.out.print(msg.toString());
	},

	/**
	 * 输出一个json文本，一般 Java 的 Map 类用这个输出，否则输出不了
	 */
	printJson : function(javaObj) {
		print(this.sys.json(javaObj));
	},

	_toObj : function(obj) {
		if (typeof obj == "string") {
			obj = obj.trim();
			if (/^\{.+\}$/.test(obj)) {
				obj = eval('(' + obj + ')');
			}
		}
		return obj;
	},

	/**
	 * AJAX 输出的函数
	 */
	ajax_re : function(obj) {
		// 响应体
		this.sys.out.println(this.toJsonStr(obj));
	},

	/**
	 * AJAX 成功信息打印便利函数
	 */
	ajax_ok : function(data) {
		this.ajax_re({
			ok : true,
			data : data
		});
	},

	/**
	 * 带着session信息
	 */
	ajax_se : function(obj, session) {
		session = this._toObj(session);
		// 响应头
		this.sys.out.println("HTTP/1.1 200 OK");
		this.sys.out.println("Content-Type: text/html;charset=UTF-8");
		this.sys.out.printlnf("SET-COOKIE:DSEID=%s; Path=/;", session.id);
		this.sys.out.println("Server: Walnut HTTPAPI");
		this.sys.out.println("");

		this.ajax_re(obj);
	},

	/**
	 * AJAX 出错的错误信息打印便利函数
	 */
	ajax_error : function(errCode, reason) {
		this.ajax_re({
			ok : false,
			errCode : errCode,
			data : reason
		});
	},

	/**
	 * js转换为json字符串
	 */
	toJsonStr : function(jsobj) {
		if (typeof jsobj == "string") {
			return jsobj;
		}
		return this.sys.json(jsobj);
	},
	
	getObjById: function(id) {
		var objStr = this.sys.exec2("obj id:" + id);
    	return eval('(' + objStr + ')');
	}
};
