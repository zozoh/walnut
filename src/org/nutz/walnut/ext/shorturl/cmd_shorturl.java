package org.nutz.walnut.ext.shorturl;

import java.net.URLEncoder;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_shorturl extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        if (params.vals.length == 0) {
            sys.out.println(getManual());
            return;
        }
        String apiurl = "http://urlc.cn/api.php?turl=" + URLEncoder.encode(params.val_check(0), Encoding.UTF8);
        Response resp = Http.get(apiurl);
        if (resp.isOK()) {
            NutMap re = Json.fromJson(NutMap.class, resp.getReader());
            sys.out.print(re.getString("value"));
        } else {
            sys.err.println(resp.getContent());
        }
    }

}
