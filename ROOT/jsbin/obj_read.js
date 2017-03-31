/**
 * 文件读操作
 *
 * @param {string} ph - 文件路径或id
 * @param {bool} m - 读取meta，默认为false，直接将文件内容写入流
 */

var ph = ph || "";
var m = m == "true";

function _main(ph, m) {
    var obj = null;
    if (ph.indexOf("id:") == 0) {
        obj = sys.exec2("obj " + ph);
    } else if (ph.indexOf("/") > -1) {
        obj = sys.exec2("obj " + ph);
    } else {
        obj = sys.exec2("obj id:" + ph);
    }
    obj = eval('(' + obj + ')');
    if (m) {
        $wn.ajax_ok(obj);
    } else {
        sys.io().readAndClose(sys.io().get(obj.id), sys.out.getOutputStream());
    }
}
_main(ph, m);