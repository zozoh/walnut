package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;

public class OEHyper extends OEVarItem {

    private WnMatch match;

    private String altText;

    private OECopyNode copy;

    public OEHyper() {
        this.type = OENodeType.HYPER;
    }

    public WnMatch getMatch() {
        return match;
    }

    public void setMatch(WnMatch match) {
        this.match = match;
    }

    private static final String REG = "^sym\\s*\\("
                                      + "\\s*(\\d+)"
                                      + "\\s*"
                                      + "(:\\s*(.+)\\s*)?"
                                      + "\\)$";
    private static final Pattern P = Pattern.compile(REG);

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        Object val = vars.get(varName);
        // 采用设置的文字内容
        if (null != match && match.match(val)) {
            if (!Ws.isBlank(altText)) {
                CheapElement el = refer.clone();
                CheapElement tOrSym = el.findElement(child -> {
                    return child.isTag("w:v") || child.isTag("w:sym");
                });
                CheapDocument doc = pEl.getOwnerDocument();
                // 分析 altText 看看是否需要输出符号
                // 0/31 Regin:0/31
                // 0:[ 0, 31) `sym ( 0052 : Wingdings 2 )`
                // 1:[ 7, 11) `0052`
                // 2:[ 13, 30) `: Wingdings 2 `
                // 3:[ 16, 30) `Wingdings 2 `
                Matcher m = P.matcher(altText);
                if (m.find()) {
                    String chav = m.group(1);
                    String font = Ws.trim(m.group(3));
                    font = Ws.sBlank(font, "Wingdings 2");
                    CheapElement sym = doc.createElement("w:sym");
                    sym.attr("w:font", font);
                    sym.attr("w:char", chav);
                    sym.setClosed(true);
                    tOrSym.insertNext(sym);
                }
                // 直接输出文本
                else {
                    CheapElement t = doc.createElement("t");
                    t.setText(altText);
                    tOrSym.insertNext(t);
                }
                // 最后移除原始占位文本
                tOrSym.remove();
            }
        }
        // 否则，复制
        else {
            copy.renderTo(pEl, vars);
        }
    }

}
