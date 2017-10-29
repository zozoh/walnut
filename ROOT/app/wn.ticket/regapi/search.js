var ustr = ustr || '';
var tp = tp || '';
var search = search || '';
var skip = skip;
var limit = limit;

function main(tp, ustr, search, skip, limit) {
    var _u_tp = " -u " + ustr + " -tp " + tp;
    var _skip_limit = " -skip " + skip + " -limit " + limit;
    var re = sys.exec2("ticket record -search '" + search + "' " + _u_tp + _skip_limit);
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

main(tp, ustr, search, skip, limit);