var appid = args[0];
var reurl = args[1];

// https%3A%2F%2Fcherry.danoolive.com%2Fapi%2Froot%2Fwxlogin%2Fcallback
//reurl = encodeURIComponent(reurl);

//sys.out.println("HTTP/1.1 302 Moved Temporarily");
//sys.out.println("Location: https://open.weixin.qq.com/connect/qrconnect?appid="+appid+"&redirect_uri="+reurl+"&response_type=code&scope=snsapi_login#wechat_redirect")
//sys.out.println("Location: https://open.weixin.qq.com/connect/qrconnect?appid="+appid+"&redirect_uri="+reurl+"&response_type=code&scope=snsapi_login#wechat_redirect")
//sys.out.println();

sys.exec("httpout -status 302 -headers \"Location: '`weixin "+appid+" oauth2 -wxopen "+reurl+"`'\"");