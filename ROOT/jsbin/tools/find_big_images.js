/*
jsc /jsbin/tools/find_big_images.js | sheet -flds "thingset,th_nm,nm" -sep ","
*/

// 处理请求参数
var params = params || {}
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);

var _width = paramObj.w || 1920;
var _height = paramObj.h || 1080;

var q = {
	width  : {"$gt":_width},
	height : {"$gt":_height},
	d0 : "home",
	d1 : sys.se.me()
}
if (paramObj.mb) {
	q["len"] = {"$gt":paramObj.mb*1024*1024}
}
if (paramObj.kb) {
	q["len"] = {"$gt":paramObj.mb*1024}
}

var reJson = sys.exec2f("obj -match '%s' -cqn", JSON.stringify(q).replace(/\$/g, "\\\$"))
var re = JSON.parse(reJson);
var result = [];
var homePath = "/home/" + sys.se.me();
var thingPath = homePath + "/thing";
var sitePath = homePath + "/sites";
for (var i in re) {
	var wobj = re[i]
	var tmp = wobj.ph.split("/");
	if (wobj.ph.startsWith(thingPath)) {
		// Thing里面的图片/视频
		// 分解路径,得到thingSet的名字和th_nm的值
		//sys.out.println(JSON.stringify(tmp))
		var thingSetName = tmp[4]
		var thingDataId = tmp[6]
		var thingData = JSON.parse(sys.exec2("obj -cqn id:" + thingDataId))
		wobj["retype"] = "thingset";
		wobj["thingset"] = thingSetName;
		wobj["thing_data_id"] = thingDataId;
		wobj["th_nm"] = thingData.th_nm;
		result.push(wobj);
	}
	else if (wobj.ph.startsWith(sitePath)) {
		var siteName = tmp[4];
		wobj["retype"] = "site";
		wobj["site_name"] = siteName;
		wobj["site_path"] = wobj.ph.substring((sitePath + "/" + siteName + "/").length)
	}
}
sys.out.println(JSON.stringify(result))