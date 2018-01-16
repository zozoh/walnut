var ustr = ustr || '';
var tp = tp || '';
var query = query || '';
var skip = skip;
var limit = limit;
var sort = sort || "";

function main(tp, ustr, query, skip, limit) {
    var _u_tp = " -u " + ustr + " -tp " + tp;
    var _skip_limit = " -skip " + skip + " -limit " + limit  + " -sort " + sort;
    var re = sys.exec2("ticket record -query '" + query + "' " + _u_tp + _skip_limit);
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

main(tp, ustr, query, skip, limit);