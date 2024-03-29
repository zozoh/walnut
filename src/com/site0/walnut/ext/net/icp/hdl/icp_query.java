package com.site0.walnut.ext.net.icp.hdl;

import java.net.InetAddress;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class icp_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String hostname = hc.params.val_check(0);
        if (hostname.startsWith("www"))
            hostname = hostname.substring(4);
        Elements eles = Jsoup.parse(new URL("http://" + hostname + ".xin"), 10000).select(".company .detail");
        NutMap map = new NutMap();
        if (eles.size() < 1) {
            map.put("error", "icp_not_found");
            sys.out.writeJson(map, JsonFormat.full());
            return;
        }
        for (Element ele : eles.first().children()) {
            if ("name".equals(ele.attr("class"))) {
                map.put("ltdname", ele.text());
            } else if ("info".equals(ele.attr("class"))) {
                String[] tmp = ele.text().split("：");
                if (tmp.length != 2)
                    continue;
                switch (tmp[0]) {
                case "网站名称":
                    map.put("ipcname", tmp[1].trim());
                    break;
                case "网站域名":
                    map.put("ipchost", tmp[1].trim());
                    break;
                case "备案信息":
                    map.put("ipcno", tmp[1].trim());
                    break;
                case "数据更新日期":
                    map.put("ipcdate", tmp[1].trim());
                    break;
                }

            }
        }
        try {
            map.put("ip", InetAddress.getByName(hostname).getHostAddress());
        } catch (Throwable e) {
        }
        sys.out.writeJson(map, JsonFormat.full());
    }
}
