package org.nutz.walnut.ext.sys.dsync.bean;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ext.media.dom.util.CheapDomReplaceWnObjId;

public class WnRestoring {

    public WnDataSyncItem item;

    public WnObj obj;

    public Map<String, String> idPaths;

    public WnOutputable log;

    public WnExecutable run;

    public WnIo io;

    public NutBean vars;

    public List<WnRestoreAction> actions;

    public Map<String, WnObj> cachePathObj;

    public String getActionsTypeName() {
        if (!this.hasActions())
            return "{NO ACTIONS}";

        StringBuilder sb = new StringBuilder();
        for (WnRestoreAction a : actions) {
            sb.append('[');
            sb.append(a.getTypeName());
            sb.append(']');
        }
        return sb.toString();
    }

    public boolean hasActions() {
        return null != actions && !actions.isEmpty();
    }

    public void invoke() {
        if (null != actions)
            for (WnRestoreAction a : actions) {
                // 执行后续命令
                String cmdText = a.getRunCommand(obj);
                if (null != cmdText) {
                    // 打印日志
                    if (null != log) {
                        log.printlnf("     :> %s", cmdText);
                    }
                    // 执行命令
                    run.exec(cmdText);
                }
                // 特殊处理： 对于 DOM 重新修正其内的 ID 映射
                if (a.isReplaceDom()) {
                    // 读取 DOM
                    String html = io.readText(obj);
                    CheapDocument doc = new CheapDocument();
                    CheapXmlParsing ing = new CheapXmlParsing(doc);
                    doc = ing.parseDoc(html);

                    // 准备替换逻辑
                    CheapDomReplaceWnObjId rw = new CheapDomReplaceWnObjId();
                    rw.setDoc(doc);
                    rw.setIo(io);
                    rw.setVars(vars);
                    rw.setCachePathObj(cachePathObj);

                    // 执行替换逻辑成功后
                    // 输出新的 DOM
                    if (rw.doReaplace(idPaths)) {
                        html = rw.getDocMarkup();
                        io.writeText(obj, html);
                    }
                }
            }
    }

}
