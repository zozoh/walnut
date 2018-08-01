//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if (!params.js_code) {
        sys.out.print("{'ok':false, 'msg':'miss js_code'}");
        return;
    }
    var homeUserName = sys.se.name();
    var re = sys.exec2f("weixin %s jscode2session %s", homeUserName, params.js_code);
    var wxlogin = JSON.parse(re);
    
    var query = {};
    query["wx_" + homeUserName] = wxlogin.openid;
    var reJson = sys.exec2f("thing ~/thing/我的用户 query -obj -match '%s' -cqn", JSON.stringify(query));
    var user = {};
    if (reJson) {
        user = JSON.parse(reJson);
    } else {
        reJson = sys.exec2f("thing ~/thing/我的用户 create -fields '%s' -cqn", JSON.stringify(query));
        user = JSON.parse(reJson);
    }
    var re = {};
    re["sekey_" + homeUserName] = wxlogin.session_key
    sys.exec2f("obj -u '%s' id:%s", JSON.stringify(re), user.id)
    sys.out.print(JSON.stringify({openid:wxlogin.openid, id:user.id}));
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................