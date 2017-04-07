var wxlogin_conf_name = args[0];
var wxlogin_code = args[1];
var resp = sys.exec2map("weixin " + wxlogin_conf_name  + " user -code " + wxlogin_code + " -infol others"); // TODO 若infol other失败,应该回到基础信息,而不是null
if (resp && resp.openid) {
    //sys.out.println("登录成功" + sys.json(resp));
    // 先查询一下是否已经存在对应的用户, 用unionid属性
    var user_login_name = "oauth:wxlogin:"+resp.unionid;
    var user = sys.exec2map("adduser -exists -json -q " + user_login_name);
    // 检查一下结果
    if (user && user.id) {
        sys.exec2("setup -u id:" + user.id + " usr/create");
        var session = sys.exec2map("session -create " + user_login_name);
        //var headers = '\'{Location:"/",';
        //headers += 'Set-Cookie:"seid='+session.id+'"}\'';
        //sys.out.print(sys.exec("httpout -status 302 -headers " + headers));
        sys.out.println("HTTP/1.1 302 Found");
        sys.out.println("Set-Cookie: SEID=" + session.id + "; Path=/;");
        sys.out.println("Location: /");
        sys.out.println("");
        //sys.out.println(msg);
        //sys.out.println(headers);
    } else {
        var msg = "System Error";
        sys.out.println("HTTP/1.1 500 Internal Server Error");
        sys.out.println("Content-Length: " + msg.length)
        sys.out.println("");
        sys.out.println(msg);
    }
    
} else {
    sys.out.println("登录失败" + sys.json(resp));
}