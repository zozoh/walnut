package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class OEHyper extends OEVarItem {

    private WnMatch match;

    private String altText;

    private List<CheapElement> moreRuns;

    public OEHyper() {
        this.type = OENodeType.HYPER;
        this.moreRuns = new LinkedList<>();
    }

    private static final String R2 = "HYPERLINK"
                                     + "\\s*\""
                                     + "=([^\":]+)"
                                     + ":"
                                     + "([^\"]+)"
                                     + "\"\\s*\\\\o\\s*"
                                     + "\"([^\"]+)\"";
    private static final Pattern P2 = Pattern.compile(R2);

    public boolean setMatchAndAltText(String input) {
        String trim = Ws.trim(input);

        // 2/36 Regin:0/36
        // 0:[ 2, 36) `HYPERLINK "=num:189" \o "hahahaha"`
        // 1:[ 14, 17) `num`
        // 2:[ 18, 21) `189`
        // 3:[ 27, 35) `hahahaha`
        Matcher m = P2.matcher(trim);
        if (!m.find()) {
            return false;
        }

        String maInput = m.group(2);
        this.setVarName(m.group(1));
        this.match = AutoMatch.parse(maInput);
        this.altText = m.group(3);
        return true;
    }

    public WnMatch getMatch() {
        return match;
    }

    public void setMatch(WnMatch match) {
        this.match = match;
    }

    public void addMoreRun(CheapElement r) {
        moreRuns.add(r);
    }

    public boolean hasMoreRuns() {
        return null != moreRuns && !moreRuns.isEmpty();
    }

    public List<CheapElement> getMoreRuns() {
        return moreRuns;
    }

    private static final String R1 = "^sym\\s*\\("
                                     + "\\s*(\\d+)"
                                     + "\\s*"
                                     + "(:\\s*(.+)\\s*)?"
                                     + "\\)$";
    private static final Pattern P1 = Pattern.compile(R1);

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        // 复制到目标节点
        CheapElement el = refer.clone();
        pEl.append(el);

        Object val = vars.get(varName);
        // 采用设置的文字内容
        if (null != match && match.match(val)) {
            if (!Ws.isBlank(altText)) {
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
                Matcher m = P1.matcher(altText);
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
        // 采用默认文字设定
        else {
            if (null != this.moreRuns) {
                for (CheapElement mr : moreRuns) {
                    pEl.append(mr.clone());
                }
            }
        }

    }

}
