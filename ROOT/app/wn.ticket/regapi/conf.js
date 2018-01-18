var ustr = ustr || '';
var tp = tp || '';

function main(tp, ustr) {
    var re = sys.exec2("cat ~/.ticket/ticket.json");
    if (!/^e./.test(re)) {
        // 组装配置项
        var tjson = eval('(' + re + ')');
        $wn.ajax_re({
            ok: true,
            data: tjson
        });
    } else {
        $wn.ajax_error(null, re);
    }
}

main(tp, ustr);