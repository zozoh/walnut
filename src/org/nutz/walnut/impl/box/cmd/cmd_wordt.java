package org.nutz.walnut.impl.box.cmd;

import java.io.OutputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_wordt extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "c");
        if (params.vals.length < 2) {
            throw Err.create("e.cmd.wordt.miss_destobj_or_miss_tmplobj");
        }
        // 模板, 目标文件
        String tmplPath = Wn.normalizeFullPath(params.vals[0], sys);
        String destPath = Wn.normalizeFullPath(params.vals[1], sys);
        WnObj tmplObj = sys.io.check(null, tmplPath);
        WnObj destObj = params.is("c") ? sys.io.createIfNoExists(null, destPath, WnRace.FILE)
                                       : sys.io.check(null, destPath);
        // 替换参数
        String vars = params.get("vars");
        if (Strings.isBlank(vars)) {
            vars = sys.in.readAll();
        }
        if (Strings.isBlank(vars)) {
            throw Err.create("e.cmd.wordt.miss_vars");
        }
        NutMap varMap = Lang.map(vars);

        // 修正换行符
        replaceRN(varMap);

        // 加载模板
        HWPFDocument word = new HWPFDocument(sys.io.getInputStream(tmplObj, 0));
        Range wordRange = word.getRange();
        int paraNum = wordRange.numParagraphs();
        for (int i = 0; i < paraNum; i++) {
            Paragraph paragraph = wordRange.getParagraph(i);
            String ptext = paragraph.text();
            if (ptext.indexOf("${") != -1) {
                Tmpl tmpl = Tmpl.parse(ptext);
                String ntext = tmpl.render(varMap);
                if (!ntext.equals(ptext)) {
                    paragraph.replaceText(ptext, ntext);
                    if (params.is("debug")) {
                        sys.out.printlnf("段落(%d)：\n%s", (i + 1), ptext);
                        sys.out.printlnf("替换文本：\n%s\n", ntext);
                    }
                }
            }
        }

        // 输出
        OutputStream outdoc = sys.io.getOutputStream(destObj, 0);
        word.write(outdoc);
        outdoc.flush();
        outdoc.close();
    }

    private String CHARACTER_NL = "\r\n";
    private String CHARACTER_NL_WORD = "\013";

    public void replaceRN(NutMap varMap) {
        for (String k : varMap.keySet()) {
            Object val = varMap.get(k);
            if (val instanceof String) {
                String valStr = (String) val;
                if (valStr.indexOf(CHARACTER_NL) > -1) {
                    valStr = valStr.replaceAll(CHARACTER_NL, CHARACTER_NL_WORD);
                    varMap.setv(k, valStr);
                }
            } else if (val instanceof NutMap) {
                replaceRN((NutMap) val);
            }
        }
    }

}
