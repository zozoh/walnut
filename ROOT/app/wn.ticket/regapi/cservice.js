var ustr = ustr || '';
var tp = tp || '';

function main(tp, ustr) {
    // 不做校验了
    var re = sys.exec2("ticket people -query -tp cservice");
    if (!/^e./.test(re)) {
        var rejson = eval('(' + re + ')');
        $wn.ajax_re({
            ok: true,
            data: rejson
        });
    } else {
        $wn.ajax_error(null, re);
    }
}

main(tp, ustr);