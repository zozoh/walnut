package org.nutz.walnut.ext.util.shorturl;

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

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        if (params.vals.length == 0) {
            sys.out.println(getManual());
            return;
        }
        boolean hiddenHttp = params.is("h", false);
        String apiurl = "http://api.t.sina.com.cn/short_url/shorten.json?source=1681459862&url_long="
                        + URLEncoder.encode(params.val_check(0), Encoding.UTF8);
        Response resp = Http.get(apiurl);
        if (resp.isOK()) {
            NutMap re = Json.fromJsonAsList(NutMap.class, resp.getContent()).get(0);
            String urls = re.getString("url_short");
            if (hiddenHttp) {
                urls = urls.substring(urls.indexOf(":") + 3);
            }
            sys.out.print(urls);
        } else {
            sys.err.println(resp.getContent());
        }
    }

}
