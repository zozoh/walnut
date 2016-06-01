package org.nutz.walnut.ext.app;

import java.util.Map;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.app.bean.AppApiItem;
import org.nutz.walnut.ext.app.bean.AppDataItem;
import org.nutz.walnut.ext.app.bean.AppInfo;
import org.nutz.walnut.ext.app.bean.AppWxhookItem;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxMsgHandler;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

class DoAppInit {

    WnSystem sys;

    WnObj taHome;

    AppInfo ai;

    Context c;

    private void _Ln(String str) {
        sys.out.println(str);
    }

    private void _Lnf(String fmt, Object... args) {
        sys.out.printlnf(fmt, args);
    }

    private void _Lf(String fmt, Object... args) {
        sys.out.printf(fmt, args);
    }

    private void _set_metas(WnObj o, NutMap metas) {
        NutMap map = new NutMap();

        for (Map.Entry<String, Object> en : metas.entrySet()) {
            String key = en.getKey();
            if (key.startsWith("?")) {
                key = key.substring(1);
                if (!o.has(key)) {
                    map.put(key, en.getValue());
                }
            } else {
                map.put(key, en.getValue());
            }
        }

        sys.io.appendMeta(o, map);
    }

    void doIt() {

        Stopwatch sw = Stopwatch.begin();

        // 处理数据目录
        if (ai.dataItems.size() > 0) {
            _do_data();
        }

        // 处理 httpapi
        if (ai.apiItems.size() > 0) {
            _do_httpapi(c);
        }

        // 处理微信
        String pnb = c.getString("pnb");
        if (!Strings.isBlank(pnb) && ai.wxhookItems.size() > 0) {
            _do_wxhooks(pnb);
        }

        sw.stop();
        _Ln(sw.toString());
    }

    private void _do_wxhooks(String pnb) {
        _Lnf("init weixin hooks: %s", pnb);

        String ph = Wn.normalizeFullPath("~/.weixin/" + pnb, sys);
        WnObj oWxHome = sys.io.createIfNoExists(null, ph, WnRace.DIR);
        WnObj oWxConf = sys.io.createIfNoExists(oWxHome, "wxconf", WnRace.FILE);
        WxConf wxConf = sys.io.readJson(oWxConf, WxConf.class);
        if (null == wxConf) {
            wxConf = new WxConf();
        }
        if (null == wxConf.handlers) {
            wxConf.handlers = new WxMsgHandler[0];
        }

        // 安装配置依次查找 ...
        for (AppWxhookItem item : ai.wxhookItems) {

            // 找找以前有木有抽奖的设定
            WxMsgHandler wmh = null;
            for (WxMsgHandler h : wxConf.handlers) {
                if (item.id.equals(h.id)) {
                    wmh = h;
                    break;
                }
            }

            // 如果没有，创建一个
            if (null == wmh) {
                wmh = new WxMsgHandler();
                wmh.id = item.id;
                wxConf.handlers = Lang.arrayFirst(wmh, wxConf.handlers);

                _Lf(" ++ %-38s", wmh.id);
            }
            // 否则修改它
            else {
                _Lf(" == %-38s", wmh.id);
            }

            // 更新配置
            wmh.match = item.match;
            wmh.context = item.context;
            wmh.command = item.command;

            _Ln("OK");

        }

        // 最后保存
        sys.io.writeJson(oWxConf, wxConf, JsonFormat.forLook().setIgnoreNull(true));
        _Ln(Strings.dup('-', 44));
    }

    private void _do_httpapi(Context c) {
        _Ln("init httpapi :");

        String phApiHome = Wn.normalizeFullPath("~/.regapi/api", sys);
        WnObj apiHome = sys.io.createIfNoExists(null, phApiHome, WnRace.DIR);
        for (AppApiItem item : ai.apiItems) {
            
            if (!Strings.isBlank(item.when)) {
                if (item.when.startsWith("user:") && !item.when.equals("user:"+c.getString("usr"))) {
                    continue;
                }
            }
            
            _Lf("  - %-38s", item.path);

            WnObj oApi = sys.io.createIfNoExists(apiHome, item.path, WnRace.FILE);

            NutMap map = new NutMap();
            // 处理所有的 header
            for (Map.Entry<String, String> en : item.headers.entrySet()) {
                map.put("http-header-" + en.getKey(), en.getValue());
            }

            // 处理元数据
            map.putAll(item.metas);

            // 保存
            _set_metas(oApi, map);

            // 写入内容
            String cmdText = Lang.concat("\n", item.commands).toString();
            sys.io.writeText(oApi, cmdText);

            _Ln("OK");
        }
        _Ln(Strings.dup('-', 44));
    }

    private void _do_data() {
        _Ln("init data :");
        for (AppDataItem item : ai.dataItems) {
            _Lf("  - %-38s", item.path);
            WnObj o = ".".equals(item.path) ? taHome
                                            : sys.io.createIfNoExists(taHome, item.path, item.race);
            _set_metas(o, item.metas);

            // 空文件，将用默认内容写入
            if (o.isFILE() && !Strings.isBlank(item.content) && o.len() == 0) {
                sys.io.writeText(o, item.content);
            }

            _Ln("OK");
        }
        _Ln(Strings.dup('-', 44));
    }
}
