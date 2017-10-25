var ustr = ustr || '';
var tp = tp || '';

function main(tp, ustr) {
    var re = sys.exec2("ticket people -add " + ustr + " -tp " + tp);
    log.infof("ticket rapi[reg]: %s", re);
    if (re == null || re == '') {
        $wn.ajax_ok();
    } else {
        $wn.ajax_error(null, re.trim());
    }
}

main(tp, ustr);