var ustr = ustr || '';
var tp = tp || '';
var rid = rid || '';
var content = content || '';

function main(tp, ustr, rid, content) {
    var _u_tp = " -u " + ustr + " -tp " + tp;
    // 新工单
    if (rid == '') {
        var re = sys.exec2("ticket record -new -c '" + content + "' " + _u_tp);
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
    // 现有工单
    else {
        var frecord = sys.exec2("ticket record -fetch '" + rid + "' " + _u_tp);
        if (!/^e./.test(frecord)) {
            // 开始提交回复
            var re = sys.exec2("ticket record -reply '" + rid + "' -c '" + content + "' " + _u_tp);
            if (!/^e./.test(re)) {
                var rejson = eval('(' + re + ')');
                $wn.ajax_re({
                    ok: true,
                    data: rejson
                });
            } else {
                $wn.ajax_error(null, re);
            }
        } else {
            $wn.ajax_error(null, frecord);
        }
    }
}

main(tp, ustr, rid, content);