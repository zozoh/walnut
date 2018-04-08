package org.nutz.walnut.ext.whoisx;

import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.mediax.util.Mxs;

public abstract class WhoisX {

    public static WhoInfo query(String host) {
        for (int i = 0; i < 3; i++) {
            try {
                WhoInfo info = queryByChinaz(host);
                if (info != null)
                    return info;
            }
            catch (Throwable e) {
                // 稍等,再查一查
                Lang.quiteSleep(300);
            }
        }
        return null;
    }

    protected static WhoInfo queryByChinaz(String host) {
        String url = "http://whois.chinaz.com/?Domain=" + host + "&isforceupdate=1";
        // String url = "http://whois.chinaz.com/" + host;
        Request req = Request.get(url, Mxs.genHeader("mac_chrome"));
        Response resp = Sender.create(req).setConnTimeout(3000).setTimeout(30000).send();
        if (resp.isOK()) {
            Document doc = Jsoup.parse(resp.getContent());
            Elements eles = doc.select("#sh_info");
            if (eles.isEmpty()) {
                return null;
            }
            Element sh_info = eles.first();
            eles = sh_info.select("li");
            WhoInfo info = new WhoInfo();
            info.setHost(host);
            NutMap map = new NutMap();
            for (Element li : eles) {
                Elements divs = li.select("div");
                if (divs.size() < 2)
                    continue;
                String key = divs.first().text();
                String value;
                Elements eles2 = divs.get(1).select("span");
                if ("域名".equals(key)) {
                    continue;
                } else if (eles2.isEmpty()) {
                    value = divs.get(1).text();
                } else {
                    value = eles2.first().text();
                }
                map.put(key.trim(), value.trim());
            }
            // 逐一设置
            if (map.has("注册商"))
                info.setRegistrar(map.getString("注册商"));
            if (map.has("联系人"))
                info.setRegistrant(map.getString("联系人"));
            if (map.has("联系邮箱"))
                info.setEmail(map.getString("联系邮箱"));
            if (map.has("DNS"))
                info.setDnsServers(Strings.splitIgnoreBlank(map.getString("DNS"), " "));
            if (map.has("状态"))
                info.setDomainStatus(map.getString("状态"));
            if (map.has("创建时间")) {
                info.setCreationDate(Times.parseq("yyyy年MM月dd日", map.getString("创建时间")));
            }
            if (map.has("过期时间")) {
                info.setExpirationDate(Times.parseq("yyyy年MM月dd日", map.getString("过期时间")));
            }
            return info;
        }
        return null;
    }

    // 这个版本会乱码,更权威,待解决
    protected static WhoInfo queryByNameBright(String host) throws UnsupportedEncodingException {
        String url = "https://www.namebright.com/Whois.aspx/getSecondLevel";
        Request req = Request.post(url, Mxs.genHeader("mac_chrome"));
        req.getHeader().clear();
        req.getHeader().asJsonContentType("UTF-8");
        req.setData("{\"dom\":\"" + host + "\",\"whoisServer\":\"\",\"registrar\":\"null\"}");
        Response resp = Sender.create(req).setConnTimeout(3000).setTimeout(10000).send();
        if (resp.isOK()) {
            NutMap re = Json.fromJson(NutMap.class, resp.getReader());
            // System.out.println(re);
            re = re.getAs("d", NutMap.class);
            String FirstLevel = re.getString("FirstLevel");
            for (String line : Strings.splitIgnoreBlank(FirstLevel, "\\n")) {
                System.out.println("1> " + new String(line.getBytes(Encoding.ISO_8859_1), "UTF-8"));
                System.out.println("1> " + line);
            }
            String SecondLevel = re.getString("SecondLevel");
            for (String line : Strings.splitIgnoreBlank(SecondLevel, "\\n")) {
                System.out.println("2> " + line);
            }
            System.out.println(re);
        }
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        // queryByNameBright("site0.cn");
        System.out.println(Json.toJson(queryByChinaz("nutz.cn")));
        System.out.println(Json.toJson(queryByChinaz("site0.cn")));
        System.out.println(Json.toJson(queryByChinaz("nutz.io")));
        System.out.println(Json.toJson(queryByChinaz("hurom.com.cn")));
        System.out.println(Json.toJson(queryByChinaz("hope.org.cn")));
        System.out.println(Json.toJson(queryByChinaz("chinaz.com")));
    }
}
