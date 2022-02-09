package org.nutz.walnut.cheap.dom.docx.num;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.docx.Docxs;
import org.nutz.walnut.cheap.dom.docx.en.DocxInd;
import org.nutz.walnut.cheap.dom.docx.en.DocxRFonts;
import org.nutz.walnut.util.Wlang;

public class DocxAbstractNum {

    private int hanging;

    private DocxNumLvl[] lvls;

    public DocxAbstractNum() {
        this.hanging = 420;
        this.lvls = new DocxNumLvl[10];
        this.reset();
    }

    public CheapElement toElement(int numId) {
        CheapElement el = new CheapElement("w:abstractNum");
        el.attr("w:abstractNumId", numId);
        // el.attr("w15:restartNumberingAfterBreak", "0");
        CheapElement sub = Docxs.genElVal("w:multiLevelType", "hybridMultilevel");
        el.append(sub);

        for (DocxNumLvl lvl : lvls) {
            if (null != lvl) {
                sub = lvl.toElement();
                el.append(sub);
            }
        }

        return el;
    }

    public String toString() {
        CheapElement el = this.toElement(0);
        return el.toString();
    }

    public boolean equals(Object ta) {
        if (null == ta || this.forOl) {
            return false;
        }
        if (!(ta instanceof DocxAbstractNum)) {
            return false;
        }
        DocxAbstractNum num = (DocxAbstractNum) ta;
        int len = lvls.length;
        if (len != num.lvls.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            DocxNumLvl l0 = this.lvls[i];
            DocxNumLvl l1 = num.lvls[i];
            if (!Wlang.isEqual(l0, l1)) {
                return false;
            }
        }
        return true;
    }

    public DocxAbstractNum clone() {
        DocxAbstractNum re = new DocxAbstractNum();
        for (int i = 0; i < lvls.length; i++) {
            DocxNumLvl lvl = lvls[i];
            if (null != lvl) {
                re.lvls[i] = lvl.clone();
            } else {
                re.lvls[i] = null;
            }
        }
        return re;
    }

    public boolean isEmpty() {
        for (DocxNumLvl lvl : lvls) {
            if (null != lvl) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param lvlValue
     *            列表级别（0BASE）
     * @return 是否添加成功。如果不成功，表示这个级别已经有了一个不同的编号定义
     */
    public boolean tryPushLvlForOl(int lvlValue) {
        DocxNumLvl lvl = new DocxNumLvl();
        lvl.setValue(lvlValue);
        lvl.setStart(1);
        lvl.setNumFmt("decimal");
        lvl.setText(String.format("%%%d.", lvlValue + 1));
        lvl.setJc("left");
        lvl.setIndent(__gen_ind(lvlValue));
        return this.tryPushLvl(lvl);
    }

    /**
     * @param lvlValue
     *            列表级别（0BASE）
     * @return 是否添加成功。如果不成功，表示这个级别已经有了一个不同的编号定义
     */
    public boolean tryPushLvlForUl(int lvlValue) {
        DocxNumLvl lvl = new DocxNumLvl();
        lvl.setValue(lvlValue);
        lvl.setStart(1);
        lvl.setNumFmt("bullet");
        lvl.setText("");
        lvl.setJc("left");
        lvl.setIndent(__gen_ind(lvlValue));
        lvl.setFonts(DocxRFonts.genWingdings());
        return this.tryPushLvl(lvl);
    }

    public boolean tryPushLvl(DocxNumLvl lvl) {
        DocxNumLvl old = null;
        int index = lvl.getValue();
        if (index >= 0 && index < lvls.length) {
            old = this.lvls[index];
        } else {
            throw Er.create("e.docx.numLvl.outOfRange", index);
        }
        // 木有，那么加入
        if (null == old) {
            lvls[index] = lvl;
            return true;
        }
        // 如果有相同的，则也表示加入成功
        if (old.equals(lvl)) {
            return true;
        }
        // 拒绝加入
        return false;
    }

    private DocxInd __gen_ind(int lvlValue) {
        int left = this.hanging * (lvlValue + 1);
        DocxInd ind = new DocxInd(left, this.hanging);
        return ind;
    }

    public List<DocxNumLvl> getLvls() {
        List<DocxNumLvl> list = new ArrayList<>(lvls.length);
        for (DocxNumLvl lvl : lvls) {
            if (null != lvl) {
                list.add(lvl);
            }
        }
        return list;
    }

    public void reset() {
        Arrays.fill(lvls, null);
    }

}
