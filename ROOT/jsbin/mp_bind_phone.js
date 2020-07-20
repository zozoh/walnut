/**
 * cat id:${id} | json -u 'ticket:"${http-qs-ticket}"'| json -put params | jsc /mnt/project/muting/jsbin/auth/mp_bind_phone.js -vars
 * 
 */
var _paramStr = sys.json(params) || "{}";
var _paramObj = JSON.parse(_paramStr);

function _main(params) {
    // 检查参数
    if (!params.iv) {
        return sys.exec("ajaxre -cqn", "e.cmd.auth.bind.miss_id")
    }
    if (!params.encryptedData) {
        return sys.exec("ajaxre -cqn", "e.cmd.auth.bind.miss_nm")
    }
    // 检查登录票据
    if (!params.ticket) {
        return sys.exec("ajaxre -cqn", "e.cmd.auth.bind.miss_ticket")
    }
    // 网站路径
    var www = params.www || "~/www";
    //sys.out.printlnf("www:%s", www);
    var _tmp;
    var reJSON = sys.exec2("www checkme -cqn '"+www+"' " + sys.safe(params.ticket))

    //sys.out.printlnf("\nreJSON:%s", reJSON);
    _tmp = JSON.parse(reJSON)
    if (!_tmp.me && !_tmp.ok) {
        return sys.out.print(reJSON)
    }
    var me = _tmp.me
    reJSON = sys.exec2("obj -cqn id:" + _tmp.id)
    var session_key = JSON.parse(reJSON).mp_session_key
    //sys.out.printlnf("\nsession_key:%s", session_key);
    reJSON = sys.exec2f("aes decode -cipher 'AES/CBC/PKCS5PADDING' -iv %s -aeskey %s %s | json -cqn", params.iv, session_key, params.encryptedData)

    //sys.out.printlnf("\naes decode:%s", reJSON);
    log.info("reJSON=" + reJSON)
    var data = JSON.parse(reJSON)
    if (data.phoneNumber) {
       sys.exec2f("obj -u '%s' id:%s", JSON.stringify({phone:data.phoneNumber}), me.id)
    }
    // 业务结束, 生成响应
    sys.exec("ajaxre -cqn", sys.exec2("www checkme -cqn '"+www+"' " + sys.safe(params.ticket)))
}
_main(_paramObj)