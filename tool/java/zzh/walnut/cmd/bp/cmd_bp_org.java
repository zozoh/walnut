package zzh.walnut.cmd.bp;

import java.io.File;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_bp_org extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        File f = Files.findFile("~/workspace/site0/BP/a_list/org_list.html");
        String html = Files.read(f);
        Document doc = Jsoup.parse(html);

        int limit = 0;
        if (args.length > 0)
            limit = Integer.parseInt(args[0]);

        WnObj oTsOrg = Wn.checkObj(sys, "~/thing/投资机构");
        WnThingService tsOrg = new WnThingService(sys.io, oTsOrg);

        JsonFormat jfmt = JsonFormat.compact();

        Elements trs = doc.getElementsByTag("tr");
        int i = 0;
        for (Element tr : trs) {
            NutMap map = new NutMap();
            // 名称/href
            Element el = tr.select(".company-name a").first();
            if (null == el)
                continue;
            String nm = Strings.trim(el.text());
            map.put("kr36_href", "https://rong.36kr.com" + el.attr("href"));

            // 头像 URL
            el = tr.child(0).child(0);
            String style = el.attr("style");
            int p_l = style.indexOf('(') + 1;
            int p_r = style.lastIndexOf(')');
            String avatar = style.substring(p_l, p_r);
            if (avatar.startsWith("//"))
                avatar = null;
            if (!Strings.isBlank(avatar))
                map.put("kr36_avatar", avatar);

            // 简介
            el = tr.select(".project-intro").first();
            map.put("brief", el.text());
            // 总投资数
            el = tr.child(2);
            map.put("nb_inv", Integer.parseInt(el.text()));
            // 成员数
            el = tr.child(3);
            map.put("nb_peo", Integer.parseInt(el.text()));

            // 看看数据库有木有这个机构
            WnObj oT = tsOrg.fetchThing(nm, false);

            // 计数
            i++;

            // 没有的话，增加
            if (null == oT) {
                map.put("th_nm", nm);
                sys.out.printlnf(" %04d) + create: %s : %s", i, nm, Json.toJson(map, jfmt));
                tsOrg.createThing(map);
            }
            // 有的话，更新一下它的信息
            else {
                sys.out.printlnf(" %04d) = update: %s : %s", i, nm, Json.toJson(map, jfmt));
                tsOrg.updateThing(oT.id(), map, sys);
            }

            // 很好，那么最后试图弄一下头像
            if (!Strings.isBlank(avatar) && !oT.has("thumb")) {
                sys.out.printlnf("    >>> add avatar : %s", avatar);
                WnObj oAvatar = sys.io.createIfNoExists(oTsOrg,
                                                        "data/" + oT.id() + "/thumb.png",
                                                        WnRace.FILE);
                oT.setv("thumb", "id:" + oAvatar.id());
                sys.io.set(oT, "^thumb$");

                Response resp = Http.get(avatar);
                InputStream ins = resp.getStream();
                // File fa =
                // Files.createFileIfNoExists("~/workspace/site0/BP/a_list/" +
                // nm + ".png");
                // Files.write(fa, ins);
                sys.io.writeAndClose(oAvatar, ins);

            }

            if (limit > 0 && i >= limit)
                break;

        }
        // 搞定
        sys.out.printlnf("All %d items done", i);

    }
}
