package com.site0.walnut.ext.media.dom.hdl;

import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.cheap.dom.docx.CheapDocxRendering;
import com.site0.walnut.cheap.impl.WnCheapResourceLoader;
import com.site0.walnut.ext.media.dom.DomContext;
import com.site0.walnut.ext.media.dom.DomFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class dom_docx extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 分析参数：输入文档模板 ID
        WnObj oStyle = null;
        if (params.vals.length > 0) {
            oStyle = Wn.checkObj(sys, params.val(0));
        }

        // 得到变量
        String vars = params.getString("vars");
        NutBean varsData = null;
        if (!Ws.isBlank(vars)) {
            if (Ws.isQuoteBy(vars, '{', '}')) {
                varsData = Lang.map(vars);
            } else {
                WnObj oVars = Wn.checkObj(sys, vars);
                varsData = sys.io.readJson(oVars, NutMap.class);
            }
        }

        // 样式
        String json = params.getString("style");
        NutMap style = null;
        if (!Ws.isBlank(json)) {
            style = Wlang.map(json);
        }

        // 打开输出文件
        String outPath = params.getString("out");

        // 输出到标准输出
        OutputStream out = null;
        if (Strings.isBlank(outPath)) {
            out = sys.out.getOutputStream();
        }
        // 输出到指定文件
        else {
            String aph = Wn.normalizeFullPath(outPath, sys);
            WnObj oOut = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            out = sys.io.getOutputStream(oOut, 0);
        }

        // 开始渲染
        try {
            WordprocessingMLPackage wp;
            // 新建文档
            if (null == oStyle) {
                wp = WordprocessingMLPackage.createPackage();
            }
            // 读取模板文档
            else {
                InputStream ins = sys.io.getInputStream(oStyle, 0);
                wp = WordprocessingMLPackage.load(ins);
                wp.getMainDocumentPart().getContent().clear();
                Streams.safeClose(ins);
            }
            WnCheapResourceLoader loader = new WnCheapResourceLoader(sys);
            CheapDocxRendering ing = new CheapDocxRendering(fc.doc, wp, style, varsData, loader);
            ing.render();
            wp.save(out);

            fc.quiet = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(out);
        }

    }

}
