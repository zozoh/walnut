var ustr = ustr || '';
var tp = tp || '';
var rid = fetch || '';

function main(tp, ustr, rid) {
    var _u_tp = " -u " + ustr + " -tp " + tp;
    var re = sys.exec2("ticket record -fetch '" + rid + "' " + _u_tp);
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

main(tp, ustr, rid);