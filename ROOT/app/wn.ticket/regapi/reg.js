var ustr = ustr || '';
var tp = tp || '';

function main(tp, ustr) {
    var re = sys.exec2("ticket people -add " + ustr + " -tp " + tp);
    log.infof("ticket regapi-reg: %s", re);
    if (re == null || re == '') {
        // 组装配置项
        var tjson = sys.exec2("cat ~/.ticket/ticket.json");
        tjson = eval('(' + tjson + ')');
        // $wn.ajax_ok(tjson);
        $wn.ajax_re({
            ok: true,
            data: tjson
        });
    } else {
        $wn.ajax_error(null, re.trim());
    }
}

main(tp, ustr);