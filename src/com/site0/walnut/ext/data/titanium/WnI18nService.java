package com.site0.walnut.ext.data.titanium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wpath;
import com.site0.walnut.util.Ws;

public class WnI18nService {

    private WnIo io;

    private NutBean vars;

    private Map<String, NutMap> i18ns;

    public WnI18nService(WnSystem sys) {
        this(sys.io, sys.session);
    }

    public WnI18nService(WnIo io, WnSession session) {
        this(io, session.getEnv());
    }

    public WnI18nService(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
        this.i18ns = new HashMap<>();
    }

    public String getText(String lang, String text) {
        if (null == lang || null == text) {
            return null;
        }
        String langKey = Ws.kebabCase(lang);
        NutMap map = i18ns.get(langKey);
        if (null == map) {
            return null;
        }
        if (text.startsWith("i18n:")) {
            text = text.substring(5);
        }
        text = text.replace('.', '-');
        return map.getString(text);
    }

    /**
     * 加载路径格式为：
     * 
     * <pre>
     * - 目录：
     *   /rs/ti/i18n/ <- 自动寻找下面的语言目录
     *   |- zh-cn/
     *   |- en-us/
     *   |- ...
     * - 文件： /rs/ti/i18n/zh-cn.json
     * </pre>
     * 
     * 当前目录名，会被作为语言的键(kebabCase)
     * 
     * @param path
     *            加载路径
     */
    public void load(String path) {
        WnObj oDir = Wn.checkObj(io, vars, path);
        // 单个文件
        if (oDir.isFILE()) {
            String lang = Wpath.getMajorName(oDir.name());
            this.loadFile(lang, oDir);
        }
        // 目录
        else {
            List<WnObj> children = io.getChildren(oDir, null);
            for (WnObj oChild : children) {
                if (oChild.isFILE() || oChild.isHidden()) {
                    continue;
                }
                // 根据目录获取语言键
                String lang = oChild.name();
                // 读取所有的语言文件
                List<WnObj> oFiles = io.getChildren(oChild, null);
                for (WnObj oF : oFiles) {
                    if (!oF.isFILE() || oF.isHidden() || oF.isMime("application/json")) {
                        continue;
                    }
                    this.loadFile(lang, oF);
                }
            }
        }
    }

    public void load(String path, String lang) {
        WnObj oDir = Wn.checkObj(io, vars, path);
        WnObj oFile = io.fetch(oDir, lang);
        // 尝试加载 json 文件
        if (null == oFile) {
            oFile = io.fetch(oDir, lang + ".json");
        }
        if (null != oFile) {
            // 单个文件
            if (oFile.isFILE()) {
                this.loadFile(lang, oDir);
            }
            // 目录
            else {
                // 读取所有的语言文件
                List<WnObj> oFiles = io.getChildren(oFile, null);
                for (WnObj oF : oFiles) {
                    this.loadFile(lang, oF);
                }
            }
        }
    }

    public void loadFile(String lang, WnObj oFile) {
        String langKey = Ws.kebabCase(lang);
        NutMap map = i18ns.get(langKey);
        if (null == map) {
            map = new NutMap();
            i18ns.put(langKey, map);
        }
        String content = io.readText(oFile);
        NutMap msgs = Json.fromJson(NutMap.class, content);
        map.putAll(msgs);
    }

    public void clear() {
        i18ns.clear();
    }

    public void clearLang(String lang) {
        if (null != lang) {
            NutMap map = i18ns.get(lang);
            if (null != map) {
                map.clear();
            }
        }
    }
}
