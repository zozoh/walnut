//-----------------------------------------
// 先来几个帮助方法
load("nashorn:mozilla_compat.js");
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
