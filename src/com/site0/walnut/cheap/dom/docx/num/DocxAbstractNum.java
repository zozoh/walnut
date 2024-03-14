package com.site0.walnut.cheap.dom.docx.num;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.docx.Docxs;
import com.site0.walnut.cheap.dom.docx.en.DocxInd;
import com.site0.walnut.cheap.dom.docx.en.DocxRFonts;
import com.site0.walnut.util.Wlang;

public class DocxAbstractNum {

    static enum Mode {
        OL, UL
    }

    private Mode mode;

    private int hanging;

    private DocxNumLvl[] lvls;

    public DocxAbstractNum() {
        this.hanging = 420;
        this.lvls = new DocxNumLvl[9];
        this.reset();
    }

    public DocxAbstractNum asForUL() {
        mode = Mode.UL;
        return this;
    }

    public DocxAbstractNum asForOL() {
        mode = Mode.OL;
        return this;
    }

    public boolean isForUL() {
        return Mode.UL == this.mode;
    }

    public boolean isForOL() {
        return Mode.OL == this.mode;
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
        if (!(ta instanceof DocxAbstractNum)) {
            return false;
        }
        DocxAbstractNum num = (DocxAbstractNum) ta;

        if (this.hanging != num.hanging) {
            return false;
        }

        if (this.mode != num.mode) {
            return false;
        }

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
        re.hanging = this.hanging;
        re.mode = this.mode;
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

    private DocxInd __gen_ind(int lvlValue) {
        int left = this.hanging * (lvlValue + 1);
        DocxInd ind = new DocxInd(left, this.hanging);
        return ind;
    }

    private DocxNumLvl __gen_lvl_OL(int lvlValue) {
        DocxNumLvl lvl = new DocxNumLvl();
        lvl.setValue(lvlValue);
        lvl.setStart(1);
        lvl.setNumFmt("decimal");
        lvl.setText(String.format("%%%d.", lvlValue + 1));
        lvl.setJc("left");
        lvl.setIndent(__gen_ind(lvlValue));
        return lvl;
    }

    private DocxNumLvl __gen_lvl_UL(int lvlValue) {
        DocxNumLvl lvl = new DocxNumLvl();
        lvl.setValue(lvlValue);
        lvl.setStart(1);
        lvl.setNumFmt("bullet");
        lvl.setText("");
        lvl.setJc("left");
        lvl.setIndent(__gen_ind(lvlValue));
        lvl.setFonts(DocxRFonts.genWingdings());
        return lvl;
    }

    private DocxNumLvl __gen_lvl(Mode mode, int lvlValue) {
        if (Mode.OL == mode) {
            return __gen_lvl_OL(lvlValue);
        }
        if (Mode.UL == mode) {
            return __gen_lvl_UL(lvlValue);
        }
        throw Wlang.impossible();
    }

    /**
     * @param lvlValue
     *            列表级别（0BASE）
     * @return 是否添加成功。如果不成功，表示这个级别已经有了一个不同的编号定义
     */
    public boolean tryPushLvlForOl(int lvlValue) {
        return this.__try_push_lvl(Mode.OL, lvlValue);
    }

    /**
     * @param lvlValue
     *            列表级别（0BASE）
     * @return 是否添加成功。如果不成功，表示这个级别已经有了一个不同的编号定义
     */
    public boolean tryPushLvlForUl(int lvlValue) {
        return this.__try_push_lvl(Mode.UL, lvlValue);
    }

    private boolean __try_push_lvl(Mode tryMode, int lvlValue) {
        DocxNumLvl old = null;
        int index = lvlValue;
        if (index >= 0 && index < lvls.length) {
            old = this.lvls[index];
        } else {
            throw Er.create("e.docx.numLvl.outOfRange", index);
        }
        // 如果是 OL 那么只要同级别有项目，肯定就是不成功
        // 这样才能重新开始一个新编号
        if (null != old && this.isForOL() && Mode.OL == tryMode) {
            return false;
        }
        // 创建 LVL 对象
        DocxNumLvl lvl = __gen_lvl(tryMode, lvlValue);

        // 木有，那么加入
        if (null == old) {
            lvls[index] = lvl;
            // 自动补完前序
            for (int i = 0; i < index; i++) {
                DocxNumLvl l2 = lvls[i];
                if (null == l2) {
                    lvls[i] = __gen_lvl(mode, i);
                }
            }
            return true;
        }
        // 如果有相同的，则也表示加入成功
        if (old.equals(lvl)) {
            return true;
        }
        // 拒绝加入
        return false;
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

    public void autoFillNilLvls() {
        for (int i = 0; i < lvls.length; i++) {
            DocxNumLvl lvl = lvls[i];
            if (null == lvl) {
                lvl = __gen_lvl(mode, i);
                lvls[i] = lvl;
            }
        }
    }

    public void reset() {
        Arrays.fill(lvls, null);
    }

}
