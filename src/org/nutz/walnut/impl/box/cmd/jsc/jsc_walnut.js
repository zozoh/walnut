//-----------------------------------------
// 先来几个帮助方法
load("nashorn:mozilla_compat.js");
importPackage(java.util);
importPackage(org.nutz.lang.util);
importPackage(org.nutz.lang);
importPackage(org.nutz.walnut.api.io);

// 输出一段文本,可以是null
function print(msg) {
	if (msg == null)
		return;
	sys.out.print(msg.toString());
}

// 输出一个json文本
function printJson(obj) {
	print(sys.json(obj));
}

//AJAX 成功输出
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
