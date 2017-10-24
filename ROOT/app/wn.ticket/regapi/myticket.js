var ustr = ustr || '';

function main(ustr) {
    var re = sys.exec2("ticket people -add " + ustr + " -tp " + tp);
    log.infof("ticket rapi[reg]: %s", re);
    if (re == null || re == '') {
        $wn.ajax_ok();
    } else {
        $wn.ajax_error(null, re);
    }
}

main(ustr);