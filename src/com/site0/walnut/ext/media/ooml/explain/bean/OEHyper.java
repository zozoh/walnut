package com.site0.walnut.ext.media.ooml.explain.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class OEHyper extends OEVarItem {

    private WnMatch match;

    private String altText;

    private List<CheapElement> moreRuns;

    public OEHyper() {
        this.type = OENodeType.HYPER;
        this.moreRuns = new ArrayList<>();
    }

    private static final String R2 = "HYPERLINK\\s*"
                                     + "\"=(.*)\"" // n:\"[1,89]\"
                                     + "\\s*\\\\o\\s*" // \o
                                     + "\"(.*)\""; // "xxxx"
    private static final Pattern P2 = Pattern.compile(R2);

    /**
     * @param input
     *            参数格式类似：
     * 
     *            <pre>
     * HYPERLINK "=n:\"[1,89]\"" \o "some tip"
     *            </pre>
     * 
     * @return
     */
    public boolean setMatchAndAltText(String input) {
        String trim = Ws.trim(input);
        trim = trim.replaceAll("[“”]", "\"");
        // 0:[ 0, 35) `HYPERLINK "=abcde" \o "n:\"[1,89]\"`
        // 1:[ 12, 17) `abcde`
        // 2:[ 23, 34) `n:\"[1,89]\`
        Matcher m = P2.matcher(trim);
        if (!m.find()) {
            return false;
        }

        String alt = m.group(1);
        String s = m.group(2);
        s = Ws.unescape(s);
        this.setMatchInput(s);
        this.altText = Ws.unescape(alt);
        return true;
    }

    public WnMatch getMatch() {
        return match;
    }

    public void setMatchInput(String s) {
        s = s.replaceAll("[““]", "\"");
        Object maInput;
        if (Ws.isQuoteBy(s, '[', ']')) {
            maInput = Json.fromJson(s);
        } else {
            maInput = Wlang.map(s);
        }
        this.match = AutoMatch.parse(maInput);
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

    private CheapElement findTextOrSym(CheapElement r) {
        return r.findElement(child -> {
            return child.isTag("w:t") || child.isTag("w:sym");
        }, child -> {
            return !child.isTag("w:rPr");
        });
    }

    private void dropRunStyle(CheapElement r) {
        CheapElement rStyle = r.findElement(el -> el.isTagName("w:rStyle"));

        if (null != rStyle) {
            rStyle.remove();
        }
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        // 找到第一个文字节点，并克隆作为模板
        CheapElement r = null;
        for (CheapElement mr : this.moreRuns) {
            CheapElement tOrSym = findTextOrSym(mr);
            if (null != tOrSym) {
                r = mr;
            }
        }
        if (null == r) {
            return null;
        }
        // 复制到目标节点
        CheapElement el = r.clone();
        dropRunStyle(el);
        pEl.append(el);

        // 采用设置的文字内容
        if (null != match && match.match(vars)) {
            if (!Ws.isBlank(altText)) {
                CheapElement tOrSym = findTextOrSym(el);
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
                    CheapElement t = doc.createElement("w:t");
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
                    if (mr != r) {
                        CheapElement tOrSym = findTextOrSym(mr);
                        if (null != tOrSym) {
                            CheapElement c2 = mr.clone();
                            dropRunStyle(c2);
                            pEl.append(c2);
                        }
                    }
                }
            }
        }

        return el;
    }

}
